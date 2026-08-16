package com.ayushman.intellicampus.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.ayushman.intellicampus.R;
import com.ayushman.intellicampus.databinding.ActivityMainBinding;
import com.ayushman.intellicampus.fragments.AITutorFragment;
import com.ayushman.intellicampus.fragments.AcademicsFragment;
import com.ayushman.intellicampus.fragments.HomeFragment;
import com.ayushman.intellicampus.fragments.ProfileFragment;
import com.ayushman.intellicampus.fragments.ScheduleFragment;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        setupBottomNavigation();

        if (savedInstanceState == null) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        }

        logCurrentUser();
    }

    private void setupBottomNavigation() {

        binding.bottomNavigation.setOnItemSelectedListener(item -> {

            Fragment fragment = null;

            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.nav_schedule) {
                fragment = new ScheduleFragment();
            } else if (itemId == R.id.nav_ai) {
                fragment = new AITutorFragment();
            } else if (itemId == R.id.nav_academics) {
                fragment = new AcademicsFragment();
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }

            return false;
        });
    }

    private void loadFragment(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void logCurrentUser() {

        if (auth.getCurrentUser() == null) {
            Log.d("MainActivity", "No user logged in");
        } else {
            Log.d("MainActivity",
                    "User: " + auth.getCurrentUser().getEmail());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}