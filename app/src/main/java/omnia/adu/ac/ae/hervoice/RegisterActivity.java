package omnia.adu.ac.ae.hervoice;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    TextView registerEmailValidation, registerPasswordValidation, confirmPasswordValidation;
    EditText registerEmailET, registerPasswordET, confirmPasswordET;
    Button registerButton;

    boolean isEmailValid = false;
    boolean isPasswordValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        registerEmailValidation = findViewById(R.id.registerEmailValidation);
        registerPasswordValidation = findViewById(R.id.registerPasswordValidation);
        confirmPasswordValidation = findViewById(R.id.confirmPasswordValidation);
        registerEmailET = findViewById(R.id.registerEmailET);
        registerPasswordET = findViewById(R.id.registerPasswordET);
        confirmPasswordET = findViewById(R.id.confirmPasswordET);
        registerButton = findViewById(R.id.registerButton);

        //Text watchers
        registerEmailET.addTextChangedListener(new RegisterActivity.EmailWatcher());
        registerPasswordET.addTextChangedListener(new RegisterActivity.PasswordWatcher());


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = registerEmailET.getText().toString().trim();
                String password = registerPasswordET.getText().toString().trim();

                //Ensuring neither email nor password have '
                if (email.contains("'") || password.contains("'"))
                {
                    Toast.makeText(RegisterActivity.this, "Invalid character: ' not allowed", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseManager db = DatabaseManager.getInstance(RegisterActivity.this);
                boolean success = db.registerUser(email, password);

                if (success)
                {
                    Toast.makeText(RegisterActivity.this, "Account created successful!", Toast.LENGTH_SHORT).show();

                    Intent i = new Intent(RegisterActivity.this, Otp.class);
                    startActivity(i);

                    finish();

                } else
                {
                    registerPasswordValidation.setText("Incorrect email or password :(");
                    registerPasswordValidation.setTextColor(getResources().getColor(R.color.deep_red));
                }
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
                registerEmailValidation.setText("Invalid email.");
                isEmailValid = false;
            } else {
                registerEmailValidation.setText("");
                isEmailValid = true;
            }

            registerButton.setEnabled(isEmailValid && isPasswordValid);
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
                registerPasswordValidation.setText("Invalid password.");
                isPasswordValid = false;
            } else {
                registerPasswordValidation.setText("");
                isPasswordValid = true;
            }

            registerButton.setEnabled(isEmailValid && isPasswordValid);
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