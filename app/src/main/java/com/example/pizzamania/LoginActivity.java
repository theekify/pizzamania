package com.example.pizzamania;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;
import android.text.TextUtils;
import android.util.Patterns;
import android.content.Intent;
import android.util.Log;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private Toast currentToast;

    private DatabaseHelper db;
    private SessionManager session;
    private EditText emailInput, passwordInput;
    private Button loginBtn;
    private TextView signupLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new SessionManager(this);
        db = new DatabaseHelper(this);

        // Ensure admin accounts exist in database
        db.ensureAdminAccountsExist();

        // Debug: List all users in database
        db.debugListAllUsers();

        // If already logged in, skip login
        if (session.isLoggedIn()) {
            Log.d(TAG, "Already logged in, redirecting...");
            if (session.isAdmin()) {
                startActivity(new Intent(this, AdminActivity.class));
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }
            finish();
            return;
        }

        // Initialize UI components
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        signupLink = findViewById(R.id.signupLink);

        loginBtn.setOnClickListener(v -> attemptLogin());
        signupLink.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));
    }

    private void attemptLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        Log.d(TAG, "Attempting login for email: '" + email + "'");

        // Validate input
        if (!isValidEmail(email)) {
            showToast("Please enter a valid email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            showToast("Please enter your password");
            return;
        }

        try {
            // Check if login credentials are valid
            if (db.checkLogin(email, password)) {
                String name = db.getUserNameByEmail(email);
                boolean isAdmin = db.isUserAdmin(email);
                int userId = db.getUserIdByEmail(email);
                String profileImage = db.getUserProfileImage(email);

                Log.d(TAG, "Login successful for: " + email);
                Log.d(TAG, "User name: " + name);
                Log.d(TAG, "Is admin: " + isAdmin);
                Log.d(TAG, "User ID: " + userId);
                Log.d(TAG, "Profile image: " + profileImage);

                // Set session with all user data including profile image
                session.setLoginWithId(userId, name, email, isAdmin);
                session.setProfileImage(profileImage);

                // Debug session after setting
                Log.d(TAG, "Session admin status after setting: " + session.isAdmin());
                Log.d(TAG, "Session logged in status: " + session.isLoggedIn());
                Log.d(TAG, "Session user ID: " + session.getUserId());
                Log.d(TAG, "Session profile image: " + session.getProfileImage());

                cancelCurrentToast();

                if (isAdmin) {
                    showToast("Welcome, Admin " + name + "!");
                    Log.d(TAG, "Redirecting to AdminActivity");
                    Intent intent = new Intent(this, AdminActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    showToast("Welcome back, " + name + "!");
                    Log.d(TAG, "Redirecting to MainActivity");
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                finish();

            } else {
                Log.d(TAG, "Login failed - invalid credentials");
                cancelCurrentToast();
                showToast("Invalid email or password");
            }

        } catch (Exception e) {
            Log.e(TAG, "Login error", e);
            cancelCurrentToast();
            showToast("Login failed. Please try again.");
        }
    }

    private void cancelCurrentToast() {
        if (currentToast != null) {
            currentToast.cancel();
        }
    }

    private void showToast(String message) {
        cancelCurrentToast();
        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCurrentToast();
    }
}
