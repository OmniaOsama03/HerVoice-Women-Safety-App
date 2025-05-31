package omnia.adu.ac.ae.hervoice;

import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Collections;
import java.util.Map;

import omnia.adu.ac.ae.hervoice.databinding.ActivityAnalyticsBinding;

public class AnalyticsActivity extends AppCompatActivity {

    private ActivityAnalyticsBinding binding;

    LinearLayout cityBarChart, timeBarChart;
    TextView cityInsightText,timeInsightText, safetyTipText;
    Button nextTipButton;

    String[] safetyTips =
    {
            "Trust your instincts and remove yourself from uncomfortable situations.",
            "Always let someone know your whereabouts.",
            "Avoid poorly lit or isolated areas when possible.",
            "If you feel followed, enter a public space like a shop or café.",
            "Use apps to share your location with a trusted person."
    };

    int currentTipIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAnalyticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Link views
        cityBarChart = findViewById(R.id.cityBarChart);
        timeBarChart = findViewById(R.id.timeBarChart);
        cityInsightText = findViewById(R.id.cityInsightText);
        timeInsightText = findViewById(R.id.timeInsightText);
        safetyTipText = findViewById(R.id.safetyTipText);
        nextTipButton = findViewById(R.id.nextTipButton);

        //Display initial tip
        safetyTipText.setText(safetyTips[currentTipIndex]);

        nextTipButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                currentTipIndex = (currentTipIndex + 1) % safetyTips.length;

                safetyTipText.setText(safetyTips[currentTipIndex]);
            }
        });

        displayCityPostGraph();
        displayTimePostGraph();

    }

    private void displayCityPostGraph() {
        DatabaseManager db = DatabaseManager.getInstance(this);

        //Getting the number of posts r city
        Map<String, Integer> cityPostCounts = db.getPostCountGroupedByCity();

        //Counting the total number of posts we have
        int totalPosts = 0;
        for (int count : cityPostCounts.values())
        {
            totalPosts += count;
        }

        //Finding the maximum value in my hashmap
        int maxCount = Collections.max(cityPostCounts.values());

        //If there were any bars, removing them
        cityBarChart.removeAllViews();

        for (Map.Entry<String, Integer> entry : cityPostCounts.entrySet()) //Looping through each set in my hashmap
        {
            String city = entry.getKey();
            int count = entry.getValue();

            TextView label = new TextView(this);
            label.setText(city + " - " + count );
            label.setTextSize(16);
            label.setTextColor(Color.parseColor("#d9d9d9"));

            //Creating the bar for that city
            View bar = new View(this);
            bar.setBackgroundColor(Color.parseColor("#efe9b9")); // Bar color
            int barWidth = (int) (((float) count / maxCount) * 600); // scale width

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(barWidth, 70);

            params.setMargins(0, 10, 0, 25);
            bar.setLayoutParams(params);

            //Adding the city to the linear layout for city bars
            cityBarChart.addView(label);
            cityBarChart.addView(bar);
        }


        String topCity = "";
        int topCount = 0;

        //Finding the city with the highest count of posts
        for (Map.Entry<String, Integer> entry : cityPostCounts.entrySet())
        {
            if (entry.getValue() > topCount)
            {
                topCity = entry.getKey();
                topCount = entry.getValue();
            }
        }

            int percentage = (int) (((double) topCount / totalPosts) * 100);

            cityInsightText.setText(percentage + "% of reports came from " + topCity + ".");

    }

    private void displayTimePostGraph()
    {
        DatabaseManager db = DatabaseManager.getInstance(this);

        Map<String, Integer> timeCounts = db.getPostCountByTimeRange();

        //Calculating the total count of posts
        int totalPosts = 0;
        for (int count : timeCounts.values())
        {
            totalPosts += count;
        }

        //Finding the max count of grouped timings
        int maxCount = Collections.max(timeCounts.values());

        timeBarChart.removeAllViews();

        for (Map.Entry<String, Integer> entry : timeCounts.entrySet())
        {
            String labelStr = entry.getKey() + " -" + entry.getValue();

            //Adding the timing label
            TextView label = new TextView(this);
            label.setText(labelStr);
            label.setTextSize(16);
            label.setTextColor(Color.parseColor("#d9d9d9"));


            int barWidth = (int) (((float) entry.getValue() / maxCount) * 600);

            //Bar
            View bar = new View(this);
            bar.setBackgroundColor(Color.parseColor("#efe9b9"));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(barWidth, 70);
            params.setMargins(0, 8, 0, 24);
            bar.setLayoutParams(params);

            timeBarChart.addView(label);
            timeBarChart.addView(bar);

        }

        //Time insight
        String peakTime = "";
        int peakCount = 0;


        for (Map.Entry<String, Integer> entry : timeCounts.entrySet())
        {
            if (entry.getValue() > peakCount) {
                peakTime = entry.getKey();
                peakCount = entry.getValue();
            }
        }


            int percentage = (int) (((double) peakCount / totalPosts) * 100);
            timeInsightText.setText("Most reports occurred in the " + peakTime + " (" + percentage + "%).");

    }


}