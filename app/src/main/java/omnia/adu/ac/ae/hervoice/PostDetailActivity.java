package omnia.adu.ac.ae.hervoice;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PostDetailActivity extends AppCompatActivity {

    TextView titleText, descriptionText, cityText, areaText, dateText, timeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Linking views
        titleText = findViewById(R.id.postTitle);
        descriptionText = findViewById(R.id.postDescription);
        cityText = findViewById(R.id.postCity);
        areaText = findViewById(R.id.postArea);
        dateText = findViewById(R.id.postDate);
        timeText = findViewById(R.id.postTime);

        // Retrieving data from intent
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String city = getIntent().getStringExtra("city");
        String area = getIntent().getStringExtra("area");
        String date = getIntent().getStringExtra("date");
        String time = getIntent().getStringExtra("time");

        // Populating the views
        titleText.setText(title);
        descriptionText.setText(description);
        cityText.setText(city);
        areaText.setText(area);
        dateText.setText(date);
        timeText.setText(time);
    }

    // Go back button handler
    public void goBack(View view) {
        finish(); // Closes this activity and returns to HomePageActivity
    }
}