package omnia.adu.ac.ae.hervoice;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EditProfileActivity extends AppCompatActivity {

    Button backButton, saveBTN;
    EditText firstNameET, lastNameET, emailET, passwordET, ageET;
    int currentId;
    RadioButton abudhabiRB, dubaiRB, sharjahRB, alainRB;

    TextView firstNameValidationTV, lastNameValidationTV, emailValidationTV,
            passwordValidationTV, ageValidationTV, cityValidationTV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        backButton = findViewById(R.id.backButton);
        saveBTN = findViewById(R.id.saveBTN);

        firstNameET = findViewById(R.id.firstNameET);
        lastNameET = findViewById(R.id.lastNameET);
        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        ageET = findViewById(R.id.ageET);

        firstNameValidationTV = findViewById(R.id.firstNameValidationTV);
        lastNameValidationTV = findViewById(R.id.lastNameValidationTV);
        emailValidationTV = findViewById(R.id.emailValidationTV);
        passwordValidationTV = findViewById(R.id.passwordValidationTV);
        ageValidationTV = findViewById(R.id.ageValidationTV);
        cityValidationTV = findViewById(R.id.cityValidationTV);

        abudhabiRB = findViewById(R.id.abudhabiRB);
        dubaiRB = findViewById(R.id.dubaiRB);
        sharjahRB = findViewById(R.id.sharjahRB);
        alainRB = findViewById(R.id.alainRB);

        firstNameET.setText(getIntent().getStringExtra("firstName"));
        lastNameET.setText(getIntent().getStringExtra("lastName"));
        emailET.setText(getIntent().getStringExtra("email"));
        passwordET.setHint("********");
        ageET.setText(String.valueOf(getIntent().getStringExtra("age")));


        // Preselect based on existing city
        String currentCity = getIntent().getStringExtra("city");
        if (currentCity != null)
        {
            switch (currentCity.toLowerCase())
            {
                case "abu dhabi":
                    abudhabiRB.setChecked(true);
                    break;

                case "dubai":
                    dubaiRB.setChecked(true);
                    break;

                case "sharjah":
                    sharjahRB.setChecked(true);
                    break;

                case "al ain":
                    alainRB.setChecked(true);
                    break;
            }
        }


        //Adding text watchers to make sure none of the fields have ' in them
        TextWatcher watcher = new ValidationWatcher();
        firstNameET.addTextChangedListener(watcher);
        lastNameET.addTextChangedListener(watcher);
        emailET.addTextChangedListener(watcher);
        passwordET.addTextChangedListener(watcher);
        ageET.addTextChangedListener(watcher);

        //Allowing the form to be validated for city options too
        abudhabiRB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateForm();
            }
        });

        dubaiRB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateForm();
            }
        });

        sharjahRB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateForm();
            }
        });

        alainRB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateForm();
            }
        });
    }

    private class ValidationWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            validateForm();
        }
    }

    private void validateForm() {
        boolean isValid = true;

        // Fetch text
        String fname = firstNameET.getText().toString().trim();
        String lname = lastNameET.getText().toString().trim();
        String email = emailET.getText().toString().trim();
        String pass = passwordET.getText().toString().trim();
        String ageStr = ageET.getText().toString().trim();
        String city = null;

        if (abudhabiRB.isChecked()) city = "Abu Dhabi";
        else if (dubaiRB.isChecked()) city = "Dubai";
        else if (sharjahRB.isChecked()) city = "Sharjah";
        else if (alainRB.isChecked()) city = "Al Ain";

        if (city == null)
        {
            cityValidationTV.setText("Please select a city.");
            isValid = false;

        } else {
            cityValidationTV.setText("");
        }

        // First name
        if (fname.isEmpty() || fname.contains("'")) {
            firstNameValidationTV.setText("Invalid first name.");
            isValid = false;
        } else {
            firstNameValidationTV.setText("");
        }

        // Last name
        if (lname.isEmpty() || lname.contains("'")) {
            lastNameValidationTV.setText("Invalid last name.");
            isValid = false;
        } else {
            lastNameValidationTV.setText("");
        }

        // Email
        if (email.isEmpty() || email.contains("'") || !email.contains("@")) {
            emailValidationTV.setText("Invalid email address.");
            isValid = false;
        } else {
            emailValidationTV.setText("");
        }

        // Password (if not empty, validate format)
        if (!pass.isEmpty())
        {
            if (pass.contains("'") || !isPasswordValid(pass))
            {
                passwordValidationTV.setText("Weak or invalid password.");
                isValid = false;
            } else {
                passwordValidationTV.setText("");
            }
        } else {
            passwordValidationTV.setText("");
        }

        // Age
        if (ageStr.isEmpty() || ageStr.contains("'"))
        {
            ageValidationTV.setText("Age is required.");
            isValid = false;

        } else {
            try {
                int age = Integer.parseInt(ageStr);
                if (age <= 0 || age > 150) {
                    ageValidationTV.setText("Enter a valid age.");
                    isValid = false;
                } else {
                    ageValidationTV.setText("");
                }
            } catch (NumberFormatException e) {
                ageValidationTV.setText("Age must be a number.");
                isValid = false;
            }
        }


        saveBTN.setEnabled(isValid);
    }

    private boolean isPasswordValid(String password) {
        if (password.length() < 8)
            return false;

        if (!password.matches("[A-Z0-9*%#&a-z]+"))
            return false;

        if (!password.matches(".*\\d.*\\d.*"))
            return false;

        if (!password.matches(".*[A-Z].*"))
            return false;

        if (!password.matches(".*[a-z].*"))
            return false;

        return true;
    }


    public void goBack(View v)
    {
        finish();
    }


    public void saveData(View v) {
        String firstname = firstNameET.getText().toString().trim();
        if (firstname.isEmpty()) {
            firstname = getIntent().getStringExtra("firstName");
        }

        String lastname = lastNameET.getText().toString().trim();
        if (lastname.isEmpty()) {
            lastname = getIntent().getStringExtra("lastName");
        }

        String email = emailET.getText().toString().trim();
        if (email.isEmpty()) {
            email = getIntent().getStringExtra("email");
        }

        String password = passwordET.getText().toString().trim();
        if (password.isEmpty()) {
            password = null; // means: do not update
        }

        String ageStr = ageET.getText().toString().trim();
        if (ageStr.isEmpty()) {
            ageStr = getIntent().getStringExtra("age");
        }
        int age = Integer.parseInt(ageStr);

        String city = null;

        if (abudhabiRB.isChecked()) city = "Abu Dhabi";
        else if (dubaiRB.isChecked()) city = "Dubai";
        else if (sharjahRB.isChecked()) city = "Sharjah";
        else if (alainRB.isChecked()) city = "Al Ain";

        if (city == null) {
            Toast.makeText(this, "Please select a city.", Toast.LENGTH_SHORT).show();
            return;
        }


        currentId = SessionManager.getInstance().getCurrentUserId();
        DatabaseManager db = DatabaseManager.getInstance(EditProfileActivity.this);

        Boolean success = db.updateById(currentId, firstname, lastname, age, email, password, city);

        if (success)
            Toast.makeText(EditProfileActivity.this, "Successfully Updated!", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(EditProfileActivity.this, "Error updating profile", Toast.LENGTH_LONG).show();
    }

}
