package omnia.adu.ac.ae.hervoice;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import omnia.adu.ac.ae.hervoice.databinding.ActivityPostBinding;

public class PostActivity extends AppCompatActivity {

    private EditText titleET, descriptionET, areaET, dayET, monthET, yearET, timeHourET, timeMinET;
    private RadioButton abudhabiRB, dubaiRB, sharjahRB, alainRB, amRB, pmRB;
    private Button postButton;
    boolean isDateValid = false;
    boolean isTimeValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        titleET = findViewById(R.id.titleET);
        descriptionET = findViewById(R.id.descriptionET);
        areaET = findViewById(R.id.areaET);

        dayET = findViewById(R.id.dayET);
        monthET = findViewById(R.id.monthET);
        yearET = findViewById(R.id.yearET);

        timeHourET = findViewById(R.id.timeHourET);
        timeMinET = findViewById(R.id.timeMinET);

        abudhabiRB = findViewById(R.id.abudhabiRB);
        dubaiRB = findViewById(R.id.dubaiRB);
        sharjahRB = findViewById(R.id.sharjahRB);
        alainRB = findViewById(R.id.alainRB);

        amRB = findViewById(R.id.amRB);
        pmRB = findViewById(R.id.pmRB);

        postButton = findViewById(R.id.postButton);


        //Text watchers
        dayET.addTextChangedListener(new DateWatcher());                           //
        monthET.addTextChangedListener(new DateWatcher());
        yearET.addTextChangedListener(new DateWatcher());
        timeHourET.addTextChangedListener(new TimeWatcher());
        timeMinET.addTextChangedListener(new TimeWatcher());



        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //logic for value of city RB
                String city = "Abu Dhabi";
                if(dubaiRB.isChecked())
                    city = "Dubai";
                else if(sharjahRB.isChecked())
                    city="Sharjah";
                else if(alainRB.isChecked())
                    city = "Al Ain";

                //logic for value of meridiem RB
                String meridiem = "AM";
                if(pmRB.isChecked())
                    meridiem = "PM";

                // Get values from input fields
                String title = titleET.getText().toString().trim();
                String description = descriptionET.getText().toString().trim();
                String area = areaET.getText().toString().trim();
                String date = dayET.getText().toString().trim() + "/" + monthET.getText().toString().trim() + "/" + yearET.getText().toString().trim();    //
                String time = timeHourET.getText().toString().trim() + ":" + timeMinET.getText().toString().trim() +meridiem;                               //
                int userID = SessionManager.getInstance().getCurrentUserId();                                                                              //SESSIIONNNN

                Post post = null;
                try {
                    post = new Post(
                            0,                 // Database will auto-generate the ID
                            title,
                            description,
                            city,
                            area,
                            date,
                            time,
                            userID);
                }
                catch(Exception e) {
                    Toast.makeText(PostActivity.this, "Invalid input fields. Try again", Toast.LENGTH_LONG).show();
                }

                if(post != null) {

                    DatabaseManager db = DatabaseManager.getInstance(PostActivity.this);
                    boolean success = db.createPost(post);

                    if (success)
                    {
                        Toast.makeText(PostActivity.this, "Your post is online now!", Toast.LENGTH_LONG).show();

                        Intent i = new Intent(PostActivity.this, HomePageActivity.class);
                        startActivity(i);

                        finish();
                    }
                    else
                        Toast.makeText(PostActivity.this, "Oops! Something went wrong", Toast.LENGTH_LONG).show();

                }

            }
        });

    }


    // Date watcher
    private class DateWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String dayStr = dayET.getText().toString();
            String monthStr = monthET.getText().toString();
            String yearStr = yearET.getText().toString();

            if (dayStr.isEmpty() || monthStr.isEmpty() || yearStr.isEmpty()) { //Checking if any part of the date is empty
                isDateValid = false;
                postButton.setEnabled(false);
                return;
            }

            try {
                int day = Integer.parseInt(dayStr);
                int month = Integer.parseInt(monthStr);
                int year = Integer.parseInt(yearStr);

                if ((day < 1 || day > 31) || (month < 1 || month > 12) || (year < 0 || year > 99)) {
                    Toast.makeText(PostActivity.this, "Invalid date", Toast.LENGTH_SHORT).show();
                    isDateValid = false;
                } else {
                    isDateValid = true;
                }
            } catch (NumberFormatException e) {
                isDateValid = false;
            }

            postButton.setEnabled(isDateValid && isTimeValid);
        }
    }

        // Time watcher
        private class TimeWatcher implements TextWatcher {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String hourStr = timeHourET.getText().toString();
                String minStr = timeMinET.getText().toString();

                if (hourStr.isEmpty() || minStr.isEmpty()) {
                    isTimeValid = false;
                    postButton.setEnabled(false);
                    return;
                }

                try {
                    int hour = Integer.parseInt(hourStr);
                    int min = Integer.parseInt(minStr);

                    if ((hour < 1 || hour > 12) || (min < 0 || min > 59)) {
                        Toast.makeText(PostActivity.this, "Invalid time", Toast.LENGTH_SHORT).show();
                        isTimeValid = false;
                    } else {
                        isTimeValid = true;
                    }
                } catch (NumberFormatException e) {
                    isTimeValid = false;
                }

                postButton.setEnabled(isDateValid && isTimeValid);

            }
        }


    }
