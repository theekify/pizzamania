package com.example.pizzamania;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;
import android.text.TextUtils;
import android.util.Patterns;
import android.content.Intent;
import android.util.Log;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    private DatabaseHelper db;
    private SessionManager session;
    private EditText nameInput, emailInput, passwordInput;
    private Button signupBtn;
    private TextView loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        session = new SessionManager(this);
        db = new DatabaseHelper(this);

        // If already logged in, skip signup
        if (session.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        // UI
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        signupBtn = findViewById(R.id.signupBtn);
        loginLink = findViewById(R.id.loginLink);

        signupBtn.setOnClickListener(v -> attemptSignup());
        loginLink.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
    }

    private void attemptSignup() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (TextUtils.isEmpty(name)) {
            showToast("Please enter your name");
            return;
        }
        if (!isValidEmail(email)) {
            showToast("Please enter a valid email");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 4) {
            showToast("Password must be at least 4 characters");
            return;
        }
        if (db.userExists(email)) {
            showToast("Email already registered");
            return;
        }

        try {
            boolean success = db.insertUser(name, email, password,false);
            if (success) {
                // FIXED: Use setLogin() instead of setLoggedIn() and saveUser()
                session.setLogin(name, email, false);

                showToast("Account created successfully!");
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                showToast("Failed to create account");
            }
        } catch (Exception e) {
            Log.e(TAG, "Signup error", e);
            showToast("Account creation failed. Try again.");
        }
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}