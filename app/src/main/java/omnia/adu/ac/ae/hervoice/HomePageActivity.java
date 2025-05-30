package omnia.adu.ac.ae.hervoice;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;

import omnia.adu.ac.ae.hervoice.databinding.ActivityHomePageBinding;

public class HomePageActivity extends AppCompatActivity {
    private ActivityHomePageBinding binding;

    TextView welcomeText, postCountText;
    Button viewPostsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = ActivityHomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        welcomeText = findViewById(R.id.welcomeText);
        postCountText = findViewById(R.id.postCountText);

        int userId = SessionManager.getInstance().getCurrentUserId();
        DatabaseManager db = DatabaseManager.getInstance(this);
        viewPostsBtn = findViewById(R.id.viewPostsBtn);

        String fullName = db.getUserFullNameById(userId);
        int postCount = db.getPostCountByUserId(userId);

        welcomeText.setText("Hello, " + fullName);
        postCountText.setText("You have " + postCount + " posts");

        displayAllPosts();

    }

    public void viewAllPosts(View v)
    {
        Intent intent = new Intent(HomePageActivity.this, AllPostsActivity.class);
        startActivity(intent);
    }

    public void displayAllPosts()
    {
        DatabaseManager db = DatabaseManager.getInstance(this);

        ArrayList<Post> posts = db.getAllPosts();
        Log.e("HOMEPAGE", "Number of posts found: " + posts.size());

        LinearLayout postsContainer = findViewById(R.id.postsContainer);

        for (Post post : posts) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(32, 32, 32, 32);
            card.setBackgroundColor(Color.parseColor("#6C74B9"));

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0, 0, 0, 32);
            card.setLayoutParams(layoutParams);

            // Title
            TextView titleView = new TextView(this);
            titleView.setText(post.getTitle());
            titleView.setTextSize(18);
            titleView.setTypeface(null, Typeface.BOLD);
            titleView.setTextColor(Color.WHITE);

            // Short Description
            TextView descView = new TextView(this);
            String fullDesc = post.getDescription();
            String shortDesc = fullDesc.length() > 100 ? fullDesc.substring(0, 100) + "..." : fullDesc;
            descView.setText(shortDesc);
            descView.setTextColor(Color.WHITE);

            // City + Date
            TextView infoView = new TextView(this);
            infoView.setText(post.getCity() + "  |  " + post.getDate());
            infoView.setTextColor(Color.WHITE);
            infoView.setPadding(0, 8, 0, 8);

            // Read More button
            Button readMoreBtn = new Button(this);
            readMoreBtn.setText("Read More");
            readMoreBtn.setBackgroundColor(Color.parseColor("#4B4C8C"));
            readMoreBtn.setTextColor(Color.WHITE);

            // Pass data to PostDetailActivity
            readMoreBtn.setOnClickListener(v ->
            {
                Intent i = new Intent(HomePageActivity.this, PostDetailActivity.class);

                i.putExtra("title", post.getTitle());
                i.putExtra("description", post.getDescription());
                i.putExtra("city", post.getCity());
                i.putExtra("area", post.getArea());
                i.putExtra("date", post.getDate());
                i.putExtra("time", post.getTime());

                startActivity(i);
            });

            // Add views to card
            card.addView(titleView);
            card.addView(descView);
            card.addView(infoView);
            card.addView(readMoreBtn);

            // Add card to container
            postsContainer.addView(card);
        }

    }


}