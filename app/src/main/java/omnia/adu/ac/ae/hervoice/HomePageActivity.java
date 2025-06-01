package omnia.adu.ac.ae.hervoice;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;

import omnia.adu.ac.ae.hervoice.databinding.ActivityHomePageBinding;

public class HomePageActivity extends AppCompatActivity {
    private ActivityHomePageBinding binding;

    TextView welcomeText, postCountText;
    Button viewPostsBtn;

    EditText titleSearchInput, areaInput;
    RadioGroup cityRadioGroup;
    TextView noResultsText;
    Button applyFilterBtn, clearFilterBtn;
    LinearLayout postsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = ActivityHomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        welcomeText = findViewById(R.id.welcomeText);
        postCountText = findViewById(R.id.postCountText);

        titleSearchInput = findViewById(R.id.titleSearchInput);
        areaInput = findViewById(R.id.areaInput);
        cityRadioGroup = findViewById(R.id.cityRadioGroup);
        noResultsText = findViewById(R.id.noResultsText);
        applyFilterBtn = findViewById(R.id.applyFilterBtn);
        clearFilterBtn = findViewById(R.id.clearFilterBtn);
        postsContainer = findViewById(R.id.postsContainer);
        viewPostsBtn = findViewById(R.id.viewPostsBtn);

        int userId = SessionManager.getInstance().getCurrentUserId();
        DatabaseManager db = DatabaseManager.getInstance(this);


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

    public void applyFilter(View v)
    {
        String title = titleSearchInput.getText().toString().trim();
        String area = areaInput.getText().toString().trim();

        int selectedCityId = cityRadioGroup.getCheckedRadioButtonId();
        String city = null;

        if (selectedCityId != -1)
        {
            RadioButton selectedCity = findViewById(selectedCityId);
            city = selectedCity.getText().toString();
        }

        ArrayList<Post> filteredPosts = DatabaseManager.getInstance(this).getFilteredPosts(title, city, area);

        displayPosts(filteredPosts);
    }

    public void clearFilter(View v)
    {
        titleSearchInput.setText("");
        areaInput.setText("");
        cityRadioGroup.clearCheck();

        displayAllPosts();
    }

    public void displayPosts(ArrayList<Post> posts) {
        postsContainer.removeAllViews();

        if (posts.isEmpty())
        {
            noResultsText.setText("No incidents match your search/filter :(");
            return;
        }else
        {
            noResultsText.setText("    ");
        }

        for (Post post : posts)
        {
            LinearLayout card = new LinearLayout(this);

            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(50, 100, 50, 30);
            card.setBackground(getDrawable( R.drawable.postcard));

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    600
            );

            layoutParams.setMargins(0, 0, 0, 32);
            card.setLayoutParams(layoutParams);

            // Title
            TextView titleView = new TextView(this);
            titleView.setText(post.getTitle());
            titleView.setTextSize(18);
            titleView.setTextColor(Color.WHITE);

            // Short Description
            TextView descView = new TextView(this);
            String fullDesc = post.getDescription();
            String shortDesc = fullDesc.length() > 80 ? fullDesc.substring(0, 80) + "..." : fullDesc;
            descView.setText(shortDesc);
            descView.setTextColor(Color.WHITE);

            LinearLayout.LayoutParams descLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            descLayoutParams.setMargins(0, 50, 0, 50); // left, top, right, bottom
            descView.setLayoutParams(descLayoutParams);


            // City + Date
            TextView infoView = new TextView(this);
            infoView.setText(post.getCity() + "  |  " + post.getDate());
            infoView.setTextColor(Color.WHITE);

            // Read More button
            Button readMoreBtn = new Button(this);
            readMoreBtn.setText("Read More");
            readMoreBtn.setBackgroundColor(Color.parseColor("#232142"));
            readMoreBtn.setTextColor(Color.WHITE);
            readMoreBtn.callOnClick();

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


            //horizontal row for infoView and readMoreBtn
            LinearLayout infoRow = new LinearLayout(this);

            infoRow.setOrientation(LinearLayout.HORIZONTAL);

            infoRow.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            infoRow.setPadding(0, 20, 0, 20);
            infoRow.setWeightSum(1f);

            //layout params for infoView
            LinearLayout.LayoutParams infoViewParams = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    0.6f
            );
            infoView.setLayoutParams(infoViewParams);

            // Set layout params for readMoreBtn
            LinearLayout.LayoutParams readMoreBtnParams = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    0.4f
            );
            readMoreBtn.setLayoutParams(readMoreBtnParams);

            // Add both views to the row
            infoRow.addView(infoView);
            infoRow.addView(readMoreBtn);

            // Add views to card
            card.addView(titleView);
            card.addView(descView);
            card.addView(infoRow); // add the horizontal row instead of them individually

            // Add card to container
            postsContainer.addView(card);
        }
    }


    public void displayAllPosts()
    {
        ArrayList<Post> posts = DatabaseManager.getInstance(this).getAllPosts();

        displayPosts(posts);
    }

}