package omnia.adu.ac.ae.hervoice;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavFragment extends Fragment {

    public BottomNavFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_nav, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BottomNavigationView nav = view.findViewById(R.id.bottomNav);

        //Set the current item based on the activity
        if (getActivity() instanceof AnalyticsActivity)
        {
            nav.setSelectedItemId(R.id.nav_reports);

        } else if (getActivity() instanceof AccountActivity)
        {
            nav.setSelectedItemId(R.id.nav_profile);

        } else if (getActivity() instanceof HomePageActivity)
        {
            nav.setSelectedItemId(R.id.nav_home);

        }else if(getActivity() instanceof PostActivity)
        {
            nav.setSelectedItemId(R.id.nav_add);
        }

        //Setting the action based on which item from the nav bar is selected
        nav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getActivity(), HomePageActivity.class));
                return true;
            } else if (itemId == R.id.nav_reports) {
                startActivity(new Intent(getActivity(), AnalyticsActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(getActivity(), AccountActivity.class));
                return true;
            } else if (itemId == R.id.nav_add) {
                startActivity(new Intent(getActivity(), PostActivity.class));
                return true;
            }

            return false;
        });
    }
}


