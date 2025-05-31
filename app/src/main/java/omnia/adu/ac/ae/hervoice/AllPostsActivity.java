package omnia.adu.ac.ae.hervoice;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class AllPostsActivity extends AppCompatActivity {

    LinearLayout postsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_posts);

        postsContainer = findViewById(R.id.postsContainer);

        displayMyPosts();

    }

    public void displayMyPosts() {
        int userId = SessionManager.getInstance().getCurrentUserId();

        ArrayList<Post> posts = DatabaseManager.getInstance(this).getPostsByUserId(userId);

        postsContainer.removeAllViews();

        if (posts.isEmpty())
        {
            TextView noPosts = new TextView(this);

            noPosts.setText("You haven’t reported anything yet.");
            noPosts.setTextColor(Color.WHITE);

            postsContainer.addView(noPosts);
            return;
        }

        for (Post post : posts)
        {
            LinearLayout card = new LinearLayout(this);

            card.setOrientation(LinearLayout.VERTICAL);

            card.setPadding(50, 100, 50, 30);
            card.setBackground(getDrawable(R.drawable.mypostcard));

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, 600);

            layoutParams.setMargins(0, 0, 0, 32);
            card.setLayoutParams(layoutParams);

            // Title
            TextView titleView = new TextView(this);
            titleView.setText(post.getTitle());
            titleView.setTextSize(18);
            titleView.setTextColor(Color.WHITE);

            // Description
            TextView descView = new TextView(this);
            String shortDesc = post.getDescription().length() > 80 ? post.getDescription().substring(0, 80) + "..." : post.getDescription();
            descView.setText(shortDesc);
            descView.setTextColor(Color.WHITE);

            LinearLayout.LayoutParams descLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            descLayoutParams.setMargins(0, 50, 0, 50);
            descView.setLayoutParams(descLayoutParams);

            // City + Date
            TextView infoView = new TextView(this);
            infoView.setText(post.getCity() + "  |  " + post.getDate());
            infoView.setTextColor(Color.WHITE);

            // Read More Button
            Button readMoreBtn = new Button(this);

            readMoreBtn.setText("Read More");
            readMoreBtn.setBackgroundColor(Color.parseColor("#232142"));
            readMoreBtn.setTextColor(Color.WHITE);

            readMoreBtn.setOnClickListener(v ->
            {
                Intent i = new Intent(AllPostsActivity.this, PostDetailActivity.class);
                i.putExtra("title", post.getTitle());
                i.putExtra("description", post.getDescription());
                i.putExtra("city", post.getCity());
                i.putExtra("area", post.getArea());
                i.putExtra("date", post.getDate());
                i.putExtra("time", post.getTime());
                startActivity(i);
            });

            //Delete Button
            Button deleteBtn = new Button(this);
            deleteBtn.setText("Delete");
            deleteBtn.setBackgroundColor(Color.parseColor("#8B0000"));
            deleteBtn.setTextColor(Color.WHITE);

            deleteBtn.setOnClickListener(v ->
            {
                DatabaseManager.getInstance(this).deletePost(post.getId());
                displayMyPosts(); // Refresh list

            });

            // Horizontal layout for buttons + info
            LinearLayout infoRow = new LinearLayout(this);
            infoRow.setOrientation(LinearLayout.HORIZONTAL);
            infoRow.setWeightSum(1f);
            infoRow.setPadding(0, 20, 0, 20);

            infoView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.4f));
            readMoreBtn.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.3f));
            deleteBtn.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.3f));

            infoRow.addView(infoView);
            infoRow.addView(readMoreBtn);
            infoRow.addView(deleteBtn);

            // Assemble card
            card.addView(titleView);
            card.addView(descView);
            card.addView(infoRow);

            postsContainer.addView(card);
        }
    }

}