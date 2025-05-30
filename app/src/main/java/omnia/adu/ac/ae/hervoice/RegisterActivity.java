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

public class RegisterActivity extends AppCompatActivity {

    TextView registerAgeValidation, registerEmailValidation, registerPasswordValidation, confirmPasswordValidation;
    EditText registerFirstNameET, registerLastNameET, registerAgeET, registerEmailET, registerPasswordET, confirmPasswordET;
    RadioButton abudhabiRB, dubaiRB, sharjahRB, alainRB;
    Button registerButton;
    String email;
    String password;
    String confirmPassword;
    boolean isEmailValid = false;
    boolean isPasswordValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        registerAgeValidation = findViewById(R.id.registerAgeValidation);
        registerEmailValidation = findViewById(R.id.registerEmailValidation);
        registerPasswordValidation = findViewById(R.id.registerPasswordValidation);
        confirmPasswordValidation = findViewById(R.id.confirmPasswordValidation);

        registerFirstNameET = findViewById(R.id.registerFisrtNameET);
        registerLastNameET = findViewById(R.id.registerLastNameET);
        registerEmailET = findViewById(R.id.registerEmailET);
        registerAgeET = findViewById(R.id.registerAgeET);

        abudhabiRB = findViewById(R.id.abudhabiRB);
        dubaiRB = findViewById(R.id.dubaiRB);
        sharjahRB = findViewById(R.id.sharjahRB);
        alainRB = findViewById(R.id.alainRB);

        registerPasswordET = findViewById(R.id.registerPasswordET);
        confirmPasswordET = findViewById(R.id.confirmPasswordET);
        registerButton = findViewById(R.id.registerButton);

        //Text watchers
        registerAgeET.addTextChangedListener(new AgeWatcher());
        registerEmailET.addTextChangedListener(new EmailWatcher());
        registerPasswordET.addTextChangedListener(new PasswordWatcher());
        confirmPasswordET.addTextChangedListener(new PasswordMatchWatcher());


        registerButton.setOnClickListener(new View.OnClickListener() {                              //Creates a Member object NOT an Admin. Admin accounts creation will be hardcoded. And later they can login. However, the registerUser method (insert to User Table) in DatabaseManager class is for Users
            @Override
            public void onClick(View v) {

                // Get values from input fields
                String firstName = registerFirstNameET.getText().toString().trim();
                String lastName = registerLastNameET.getText().toString().trim();
                int age = Integer.parseInt(registerAgeET.getText().toString().trim());
                String email = registerEmailET.getText().toString().trim();
                String password = registerPasswordET.getText().toString().trim();

                //Radiobuttons for the city
                String city = "Abu Dhabi";
                if(dubaiRB.isChecked())
                    city = "Dubai";
                else if(sharjahRB.isChecked())
                    city="Sharjah";
                else if(alainRB.isChecked())
                    city = "Al Ain";


                //By default a Member would be created, NOT Admin. (Admins' accounts are created through hardcoding in the system)
                Member member=null;
                try {                                                                               //TRY-CATCH surrounding the member creation. Because if user leaves blank for age, then cannot parseInt blank to integer hence error and app crashes
                    member = new Member(
                            0,                 // Database will auto-generate the ID
                            firstName,
                            lastName,
                            age,
                            email,
                            password,
                            false,     // permission = false (regular member i.e not Admin)
                            city);
                }
                catch(Exception e) {
                    Toast.makeText(RegisterActivity.this, "Error creating a User", Toast.LENGTH_LONG).show();
                }

                if(member != null) {

                    DatabaseManager db = DatabaseManager.getInstance(RegisterActivity.this);
                    boolean success = db.registerUser(member);

                    if (success)
                    {
                        Toast.makeText(RegisterActivity.this, "Account created successful!", Toast.LENGTH_SHORT).show();

                        Intent i = new Intent(RegisterActivity.this, Otp.class);
                        startActivity(i);

                        finish();

                    }
                    else
                        Toast.makeText(RegisterActivity.this, "Oops! Something went wrong", Toast.LENGTH_LONG).show();

                }

            }
        });

    }



    // Age validation watcher
    private class AgeWatcher implements TextWatcher
    {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s)
        {
            int age = Integer.parseInt(registerAgeET.getText().toString());

            if (age<=18)
            {
                registerAgeValidation.setText("Must be above 18 to create an account with us. Seek parental advice and register using parents' account!");
                registerEmailET.setEnabled(false);

            } else {
                registerAgeValidation.setText("");
                registerEmailET.setEnabled(true);
            }

        }
    }



    // Email validation watcher
    private class EmailWatcher implements TextWatcher
    {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s)
        {
            email = registerEmailET.getText().toString().trim();

            if (!email.contains("@") || email.contains("'"))
            {
                registerEmailValidation.setText("Invalid email.");
                registerPasswordET.setEnabled(false);
                isEmailValid = false;
            } else {
                registerEmailValidation.setText("");
                registerPasswordET.setEnabled(true);
                isEmailValid = true;
            }

        }
    }


    // Password validation watcher
    private class PasswordWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s)
        {
            password = registerPasswordET.getText().toString().trim();

            if (!isPasswordValid(password))
            {
                registerPasswordValidation.setText("Invalid password.");
                registerPasswordValidation.setTextColor(getResources().getColor(R.color.deep_red));
                confirmPasswordET.setEnabled(false);
                isPasswordValid = false;
            } else {
                registerPasswordValidation.setText("");
                confirmPasswordET.setEnabled(true);
                isPasswordValid = true;
            }

        }
    }


    //password validation confirm watcher
    class PasswordMatchWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String localPass1 = registerPasswordET.getText().toString();
            confirmPassword = confirmPasswordET.getText().toString().trim();

            if (localPass1.equals(confirmPassword)) {
                registerButton.setEnabled(true);
                confirmPasswordValidation.setText("");
            } else {
                confirmPasswordValidation.setText("PASSWORDS MUST MATCH");
                confirmPasswordValidation.setTextColor(getResources().getColor(R.color.deep_red));
                registerButton.setEnabled(false);
            }
        }
    }



    //Password rules
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

}