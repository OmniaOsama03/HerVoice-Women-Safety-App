package omnia.adu.ac.ae.hervoice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EditProfileActivity extends AppCompatActivity {

    Button backButton, saveBTN;
    EditText firstNameET, lastNameET, emailET, passwordET, ageET, cityET;
    int currentId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        backButton.findViewById(R.id.backButton);
        saveBTN.findViewById(R.id.saveBTN);

        firstNameET.findViewById(R.id.firstNameET);
        lastNameET.findViewById(R.id.lastNameET);
        emailET.findViewById(R.id.emailET);
        passwordET.findViewById(R.id.passwordET);
        ageET.findViewById(R.id.ageET);
        cityET.findViewById(R.id.cityET);


        firstNameET.setHint(getIntent().getStringExtra("firstName"));
        lastNameET.setHint(getIntent().getStringExtra("lastName"));
        emailET.setHint(getIntent().getStringExtra("email"));
        passwordET.setHint("********");
        ageET.setHint(getIntent().getStringExtra("age"));
        cityET.setHint(getIntent().getStringExtra("city"));
    }


    public void goBack(View v)
    {
        finish();
    }


    public void saveData(View v) {

        String firstname = firstNameET.getText().toString();
        String lastname = lastNameET.getText().toString();
        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();
        int age = Integer.parseInt(ageET.getText().toString());
        String city = cityET.getText().toString();

        currentId = SessionManager.getInstance().getCurrentUserId();

        DatabaseManager db = DatabaseManager.getInstance(EditProfileActivity.this);

        Boolean success = db.updateById( currentId, firstname, lastname,  age,  email,  password, city );

        if(success)
            Toast.makeText(EditProfileActivity.this, "Successfully Updated!", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(EditProfileActivity.this, "Error updating profile", Toast.LENGTH_LONG).show();

    }

}
