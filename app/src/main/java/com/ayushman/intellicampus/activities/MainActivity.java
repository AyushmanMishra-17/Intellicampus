package com.ayushman.intellicampus.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
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

        // Handle system bars and the keyboard explicitly. This is important
        // for targetSdk 35+ where edge-to-edge is enforced on newer Android.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupKeyboardInsets();

        auth = FirebaseAuth.getInstance();

        setupBottomNavigation();

        if (savedInstanceState == null) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        }

        logCurrentUser();
    }

    /**
     * Keeps the content area above the keyboard and hides the bottom
     * navigation while the AI Tutor keyboard is open, giving the message
     * composer the available screen space.
     */
    private void setupKeyboardInsets() {
        final View root = binding.getRoot();

        final int initialLeft = root.getPaddingLeft();
        final int initialTop = root.getPaddingTop();
        final int initialRight = root.getPaddingRight();
        final int initialBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );
            Insets imeInsets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.ime()
            );

            boolean keyboardVisible = windowInsets.isVisible(
                    WindowInsetsCompat.Type.ime()
            );

            // Use the keyboard bottom inset when open; otherwise use the
            // normal navigation-bar inset.
            int bottomInset = keyboardVisible
                    ? imeInsets.bottom
                    : systemBars.bottom;

            view.setPadding(
                    initialLeft + systemBars.left,
                    initialTop + systemBars.top,
                    initialRight + systemBars.right,
                    initialBottom + bottomInset
            );

            Fragment currentFragment = getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);

            boolean isAiTutor = currentFragment instanceof AITutorFragment;

            if (isAiTutor && keyboardVisible) {
                binding.bottomNavigation.setVisibility(View.GONE);
            } else {
                binding.bottomNavigation.setVisibility(View.VISIBLE);
            }

            return windowInsets;
        });

        ViewCompat.requestApplyInsets(root);
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

        // Re-evaluate which bottom navigation state should be used after
        // the fragment transaction has been applied.
        binding.getRoot().post(() -> ViewCompat.requestApplyInsets(binding.getRoot()));
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
