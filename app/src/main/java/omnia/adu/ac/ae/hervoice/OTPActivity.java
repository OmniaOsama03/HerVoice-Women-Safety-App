package omnia.adu.ac.ae.hervoice;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class OTPActivity extends AppCompatActivity {

    EditText otpInput;
    Button submitOtpButton, resendOtpButton;
    Button backButton;

    String userEmail; //Passed from RegisterActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpactivity);

        otpInput = findViewById(R.id.otpInput);
        submitOtpButton = findViewById(R.id.submitOtpButton);
        resendOtpButton = findViewById(R.id.resendOtpButton);
        backButton = findViewById(R.id.backButton);

        userEmail = getIntent().getStringExtra("email");

        submitOtpButton.setOnClickListener(v -> {
            String otp = otpInput.getText().toString().trim();
            if (otp.length() == 6) {
                verifyOtp(userEmail, otp);
            } else {
                Toast.makeText(this, "Enter a valid 6-digit OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        sendOtp(userEmail); //Automatically send OTP when activity starts or resumes
    }

    public void goBack(View v)
    {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void resendOTP(View v)
    {
        sendOtp(userEmail);
    }

    public void submitOTP(View v)
    {
        String otp = otpInput.getText().toString().trim();

        if (otp.length() == 6)
        {
            verifyOtp(userEmail, otp);

        } else
        {
            Toast.makeText(this, "Enter a valid 6-digit OTP", Toast.LENGTH_SHORT).show();
        }

    }
    private void sendOtp(String email) {
        new Thread(() -> //Creating and starting a thread
        {
            try
            {
                URL url = new URL("http://10.40.33.180:3000/send-otp");

                //Opening a connection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                //Creating a POST request
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                //JSON body
                String jsonBody = "{\"email\":\"" + email + "\"}";

                //Writing the request into the output stream
                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.getBytes());

                //empty and close stream
                os.flush();
                os.close();

                if (conn.getResponseCode() == 200) //Meaning successful
                {
                    showToast("OTP sent to your email");

                } else //Unsuccessful- either server isn't up or user didn't enter valid email
                {

                    showToast("Failed to send OTP. Enter a valid email");
                }

                conn.disconnect();

            } catch (Exception e) //Exception Handling
            {
                Log.e("OTP Activity", e.getMessage());
                showToast("Error sending OTP");
            }

        }).start();
    }

    private void verifyOtp(String email, String otp) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.40.33.180:3000/verify-otp");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String jsonBody = "{\"email\":\"" + email + "\", \"otp\":\"" + otp + "\"}";

                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.getBytes());

                os.flush();
                os.close();

                if (conn.getResponseCode() == 200)
                {
                    showToast("OTP verified");
                    Intent intent = new Intent(OTPActivity.this, HomePageActivity.class);
                    startActivity(intent);

                } else
                {
                    showToast("Incorrect OTP");
                }

                conn.disconnect();

            } catch (Exception e) {
                Log.e("OTP Activity", e.getMessage());
                showToast("Verification failed");
            }
        }).start();
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(OTPActivity.this, message, Toast.LENGTH_SHORT).show()); //Because it can't run on the same thread as used above- clash
    }

}
