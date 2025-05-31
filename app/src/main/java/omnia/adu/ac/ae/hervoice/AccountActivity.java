package omnia.adu.ac.ae.hervoice;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import omnia.adu.ac.ae.hervoice.databinding.ActivityAccountBinding;

public class AccountActivity extends AppCompatActivity {

    TextView profileName;
    TextView firstNameTV;
    TextView lastNameTV;
    TextView emailTV;
    TextView ageTV;
    TextView cityTV;
    Button editProfileBTN;
    int currentId;
    Member currentMember;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        profileName = findViewById(R.id.profileName);
        firstNameTV = findViewById(R.id.firstNameTV);
        lastNameTV = findViewById(R.id.lastNameTV);
        emailTV = findViewById(R.id.emailTV);
        ageTV = findViewById(R.id.ageTV);
        cityTV = findViewById(R.id.cityTV);
        editProfileBTN = findViewById(R.id.editProfileBTN);

        currentId = SessionManager.getInstance().getCurrentUserId();

        DatabaseManager db = DatabaseManager.getInstance(AccountActivity.this);

        currentMember = db.selectById(currentId);

        profileName.setText(currentMember.getFirstName() + " " + currentMember.getLastName());
        firstNameTV.setText(currentMember.getFirstName());
        lastNameTV.setText(currentMember.getLastName());
        emailTV.setText(currentMember.getEmail());
        ageTV.setText(currentMember.getAge());
        cityTV.setText(currentMember.getCity());
    }


    public void editProfileMethod(View v) {
        Intent i = new Intent(AccountActivity.this, EditProfileActivity.class);

        i.putExtra("firstName", currentMember.getFirstName());
        i.putExtra("lastName", currentMember.getLastName());
        i.putExtra("email", currentMember.getEmail());
        i.putExtra("age", currentMember.getAge());
        i.putExtra("city", currentMember.getCity());
        startActivity(i);
    }

}