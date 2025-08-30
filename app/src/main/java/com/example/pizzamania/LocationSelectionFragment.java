package com.example.pizzamania;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

public class LocationSelectionFragment extends Fragment {

    private MapView map;
    private Marker locationMarker;
    private GeoPoint selectedLocation;
    private TextView tvSelectedLocation;
    private Button btnConfirmLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_selection, container, false);

        // Initialize osmdroid
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        tvSelectedLocation = view.findViewById(R.id.tvSelectedLocation);
        btnConfirmLocation = view.findViewById(R.id.btnConfirmLocation);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Setup map
        map = view.findViewById(R.id.map);
        setupMap();

        btnConfirmLocation.setOnClickListener(v -> confirmLocation());

        view.findViewById(R.id.btnUseCurrentLocation).setOnClickListener(v -> getCurrentLocation());

        return view;
    }

    private void setupMap() {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        // Set default location (Colombo)
        GeoPoint defaultLocation = new GeoPoint(6.9271, 79.8612);
        map.getController().setCenter(defaultLocation);
        map.getController().setZoom(15.0);

        // Add map click listener
        MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                selectedLocation = p;
                updateLocationMarker();
                updateLocationText();
                btnConfirmLocation.setEnabled(true);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(mapEventsReceiver);
        map.getOverlays().add(0, mapEventsOverlay);
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                selectedLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                map.getController().setCenter(selectedLocation);
                updateLocationMarker();
                updateLocationText();
                btnConfirmLocation.setEnabled(true);
            } else {
                Toast.makeText(requireContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLocationMarker() {
        if (locationMarker != null) {
            map.getOverlays().remove(locationMarker);
        }
        locationMarker = new Marker(map);
        locationMarker.setPosition(selectedLocation);
        locationMarker.setTitle("Delivery Location");
        locationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(locationMarker);
        map.invalidate();
    }

    private void updateLocationText() {
        if (selectedLocation != null) {
            String locationText = String.format("Selected: %.4f, %.4f",
                    selectedLocation.getLatitude(), selectedLocation.getLongitude());
            tvSelectedLocation.setText(locationText);
        }
    }

    private void confirmLocation() {
        if (selectedLocation != null) {
            // Save location to shared preferences
            LocationPreference.saveLocation(requireContext(),
                    selectedLocation.getLatitude(), selectedLocation.getLongitude());

            // Find nearest branch
            DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
            Branch nearestBranch = LocationUtils.findNearestBranch(requireContext(), dbHelper,
                    selectedLocation.getLatitude(), selectedLocation.getLongitude());

            if (nearestBranch != null) {
                Toast.makeText(requireContext(),
                        "Nearest branch: " + nearestBranch.getName(), Toast.LENGTH_LONG).show();
            }

            // Go back to payment
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) {
            map.onPause();
        }
    }
}