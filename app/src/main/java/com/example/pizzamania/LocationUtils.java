package com.example.pizzamania;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.List;

public class LocationUtils {

    public static Branch findNearestBranch(Context context, DatabaseHelper dbHelper,
                                           double userLat, double userLng) {
        try {
            List<Branch> branches = dbHelper.getBranches();
            if (branches == null || branches.isEmpty()) {
                return null;
            }

            Branch nearestBranch = branches.get(0);
            double minDistance = calculateDistance(userLat, userLng,
                    nearestBranch.getLatitude(), nearestBranch.getLongitude());

            for (int i = 1; i < branches.size(); i++) {
                Branch branch = branches.get(i);
                double distance = calculateDistance(userLat, userLng,
                        branch.getLatitude(), branch.getLongitude());

                if (distance < minDistance) {
                    minDistance = distance;
                    nearestBranch = branch;
                }
            }

            return nearestBranch;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // New overloaded method that uses device location
    public static Branch findNearestBranch(Context context, DatabaseHelper dbHelper) {
        try {
            // Get device's current location
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // Check for location permissions
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // If no permission, return the first branch as default
                return getDefaultBranch(dbHelper);
            }

            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                return findNearestBranch(context, dbHelper, location.getLatitude(), location.getLongitude());
            } else {
                // Fallback to default branch if location not available
                return getDefaultBranch(dbHelper);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return getDefaultBranch(dbHelper);
        }
    }

    // New method to check if tracking can be opened
    public static boolean canOpenTracking(Context context) {
        // Check if location services are enabled
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        // Check location permissions
        boolean hasFineLocationPermission = ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean hasCoarseLocationPermission = ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        return (isGpsEnabled || isNetworkEnabled) && (hasFineLocationPermission || hasCoarseLocationPermission);
    }

    // New method to request location permissions
    public static void requestLocationPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                1001);
    }

    // New method to open location settings
    public static void openLocationSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(intent);
    }

    private static Branch getDefaultBranch(DatabaseHelper dbHelper) {
        try {
            List<Branch> branches = dbHelper.getBranches();
            if (branches != null && !branches.isEmpty()) {
                return branches.get(0); // Return first branch as default
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula
        final int R = 6371; // Earth radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    public static boolean areLocationServicesEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return isGpsEnabled || isNetworkEnabled;
    }

    public static boolean hasLocationPermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    // Add this method to your LocationUtils.java
    public static boolean shouldShowPermissionRationale(Activity activity) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION);
    }


}