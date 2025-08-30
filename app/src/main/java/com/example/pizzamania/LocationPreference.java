package com.example.pizzamania;

import android.content.Context;
import android.content.SharedPreferences;

public class LocationPreference {
    private static final String PREF_NAME = "LocationPrefs";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";

    public static void saveLocation(Context context, double latitude, double longitude) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(KEY_LATITUDE, (float) latitude);
        editor.putFloat(KEY_LONGITUDE, (float) longitude);
        editor.apply();
    }

    public static double[] getSavedLocation(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        float lat = prefs.getFloat(KEY_LATITUDE, 0f);
        float lng = prefs.getFloat(KEY_LONGITUDE, 0f);
        return new double[]{lat, lng};
    }

    public static boolean hasSavedLocation(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.contains(KEY_LATITUDE) && prefs.contains(KEY_LONGITUDE);
    }
}