package com.example.pizzamania;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "PizzaManiaSession";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_IS_ADMIN = "is_admin";
    private static final String KEY_PROFILE_IMAGE = "profile_image";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences sharedPref;

    public SessionManager(Context context) {
        sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Save login details + role
    public void setLogin(String name, String email, boolean isAdmin) {
        sharedPref.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .putBoolean(KEY_IS_ADMIN, isAdmin)
                .apply();
    }

    // Save login details with ID
    public void setLoginWithId(int id, String name, String email, boolean isAdmin) {
        sharedPref.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putInt(KEY_USER_ID, id)
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .putBoolean(KEY_IS_ADMIN, isAdmin)
                .apply();
    }

    // Check session
    public boolean isLoggedIn() {
        return sharedPref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public boolean isAdmin() {
        return sharedPref.getBoolean(KEY_IS_ADMIN, false);
    }

    public String getUserName() {
        return sharedPref.getString(KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return sharedPref.getString(KEY_USER_EMAIL, "");
    }

    public int getUserId() {
        return sharedPref.getInt(KEY_USER_ID, -1);
    }

    public String getProfileImage() {
        return sharedPref.getString(KEY_PROFILE_IMAGE, "");
    }

    public void setProfileImage(String imagePath) {
        sharedPref.edit().putString(KEY_PROFILE_IMAGE, imagePath).apply();
    }

    // Clear session
    public void logout() {
        sharedPref.edit().clear().apply();
    }
}