package omnia.adu.ac.ae.hervoice;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import android.view.View;


public class MainActivity extends AppCompatActivity {
    EditText emailInput, passwordInput;
    TextView emailValidation, passwordValidation;
    Button loginButton, registerButton;

    boolean isEmailValid = false;
    boolean isPasswordValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Link views
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        emailValidation = findViewById(R.id.emailValidation);
        passwordValidation = findViewById(R.id.passwordValidation);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        //Text watchers
        emailInput.addTextChangedListener(new EmailWatcher());
        passwordInput.addTextChangedListener(new PasswordWatcher());

        //Login button click
        loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                //Ensuring neither email nor password have '
                if (email.contains("'") || password.contains("'"))
                {
                    Toast.makeText(MainActivity.this, "Invalid character: ' not allowed", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseManager db = DatabaseManager.getInstance(MainActivity.this);
                boolean success = db.loginUser(email, password);

                if (success)
                {
                    Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                    Intent i = new Intent(MainActivity.this, HomePageActivity.class);
                    startActivity(i);

                    finish();

                } else
                {
                    passwordValidation.setText("Incorrect email or password :(");
                    passwordValidation.setTextColor(getResources().getColor(R.color.deep_red));
                }
            }
        });

        // Register button click
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });


    }

    // Email validation watcher
    private class EmailWatcher implements TextWatcher
    {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s)
        {
            String email = s.toString().trim();

            if (!email.contains("@") || email.contains("'"))
            {
                emailValidation.setText("Invalid email.");
                isEmailValid = false;
            } else {
                emailValidation.setText("");
                isEmailValid = true;
            }

            loginButton.setEnabled(isEmailValid && isPasswordValid);
        }
    }

    // Password validation watcher
    private class PasswordWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s)
        {
            String password = s.toString();

            if (!isPasswordValid(password))
            {
                passwordValidation.setText("Invalid password.");
                isPasswordValid = false;
            } else {
                passwordValidation.setText("");
                isPasswordValid = true;
            }

            loginButton.setEnabled(isEmailValid && isPasswordValid);
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