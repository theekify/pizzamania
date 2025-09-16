package com.example.pizzamania;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private static final String TAG = "AdminActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        Log.d(TAG, "AdminActivity created");

        sessionManager = new SessionManager(this);

        BottomNavigationView bottomNav = findViewById(R.id.adminBottomNav);

        // Set initial selection first
        bottomNav.setSelectedItemId(R.id.nav_branch);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d(TAG, "Item selected: " + itemId);

            if (itemId == R.id.nav_branch) {
                Log.d(TAG, "Loading BranchFragment");
                loadFragment(new BranchFragment());
                return true;
            } else if (itemId == R.id.nav_manage_menu) {
                Log.d(TAG, "Loading AdminPizzaFragment");
                loadFragment(new AdminPizzaFragment());
                return true;
            } else if (itemId == R.id.nav_logout) {
                Log.d(TAG, "Logout selected");
                logout();
                return true; // Important: return true to consume the event
            }

            Log.d(TAG, "No fragment found for item: " + itemId);
            return false;
        });

        // Load default fragment
        if (savedInstanceState == null) {
            Log.d(TAG, "Loading default fragment");
            loadFragment(new BranchFragment());
        }
    }

    private void loadFragment(Fragment fragment) {
        try {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.adminFragmentContainer, fragment)
                    .commit();
            Log.d(TAG, "Fragment loaded successfully: " + fragment.getClass().getSimpleName());
        } catch (Exception e) {
            Log.e(TAG, "Error loading fragment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void logout() {
        Log.d(TAG, "Logout initiated");
        try {
            sessionManager.logout();
            Log.d(TAG, "Session cleared");

            Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Log.d(TAG, "Login activity started");

            finish();
            Log.d(TAG, "AdminActivity finished");
        } catch (Exception e) {
            Log.e(TAG, "Error during logout: " + e.getMessage());
            e.printStackTrace();
            // Emergency fallback
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "AdminActivity destroyed");
    }
}