package com.example.pizzamania;

import android.os.*;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

public class OrderTrackingFragment extends Fragment {
    private MapView map;
    private Marker driverMarker;
    private GeoPoint fromPoint;
    private GeoPoint toPoint;
    private TextView statusText;
    private int animationStep = 0;
    private Handler animationHandler;
    private Polyline routeLine;
    private Button btnBack;
    private String branchName;

    // ✅ animation tuning
    private static final int TOTAL_DURATION_MS = 60000; // ~60s total
    private static final int TICK_MS = 200;             // update every 200ms
    private int totalSteps = TOTAL_DURATION_MS / TICK_MS;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

            View view = inflater.inflate(R.layout.fragment_order_track, container, false);

            map = view.findViewById(R.id.map);
            statusText = view.findViewById(R.id.statusText);
            btnBack = view.findViewById(R.id.btnBack);

            btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

            // Args
            Bundle args = getArguments();
            if (args != null) {
                double customerLat = args.getDouble("customerLatitude", 6.9147);
                double customerLng = args.getDouble("customerLongitude", 79.9738);
                double branchLat = args.getDouble("branchLatitude", 6.9271);
                double branchLng = args.getDouble("branchLongitude", 79.8612);
                branchName = args.getString("branchName", "Pizza Mania Branch");

                fromPoint = new GeoPoint(branchLat, branchLng);
                toPoint = new GeoPoint(customerLat, customerLng);
            } else {
                fromPoint = new GeoPoint(6.9271, 79.8612);
                toPoint = new GeoPoint(6.9147, 79.9738);
                branchName = "Pizza Mania Branch";
            }

            // ✅ do NOT block tracking if device location is off
            if (!LocationUtils.canOpenTracking(requireContext())) {
                statusText.setText("Tracking without device location. Enable location for best accuracy.");
                // proceed anyway
            }

            setupMap();
            return view;

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error initializing tracking", Toast.LENGTH_SHORT).show();
            return inflater.inflate(R.layout.fragment_order_track, container, false);
        }
    }

    private void setupMap() {
        try {
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setMultiTouchControls(true);

            GeoPoint centerPoint = new GeoPoint(
                    (fromPoint.getLatitude() + toPoint.getLatitude()) / 2,
                    (fromPoint.getLongitude() + toPoint.getLongitude()) / 2
            );

            map.getController().setCenter(centerPoint);
            map.getController().setZoom(14.0);

            Marker branchMarker = new Marker(map);
            branchMarker.setPosition(fromPoint);
            branchMarker.setTitle("Pizza Mania Branch");
            branchMarker.setSnippet(branchName);
            map.getOverlays().add(branchMarker);

            Marker customerMarker = new Marker(map);
            customerMarker.setPosition(toPoint);
            customerMarker.setTitle("Delivery Location");
            customerMarker.setSnippet("Your destination");
            map.getOverlays().add(customerMarker);

            driverMarker = new Marker(map);
            driverMarker.setPosition(fromPoint);
            driverMarker.setTitle("Delivery Driver");
            driverMarker.setSnippet("On the way");
            map.getOverlays().add(driverMarker);

            drawRoute();
            startDeliveryAnimation();

        } catch (Exception e) {
            statusText.setText("Error setting up map");
            Toast.makeText(requireContext(), "Map setup error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void drawRoute() {
        try {
            routeLine = new Polyline();
            routeLine.addPoint(fromPoint);
            routeLine.addPoint(toPoint);
            routeLine.setColor(0xFF2196F3);
            routeLine.setWidth(8.0f);
            map.getOverlays().add(routeLine);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error drawing route", Toast.LENGTH_SHORT).show();
        }
    }

    private void startDeliveryAnimation() {
        try {
            animationHandler = new Handler(Looper.getMainLooper());
            animationStep = 0;

            animationHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (animationStep <= totalSteps) {
                        updateDriverPosition();
                        animationStep++;
                        animationHandler.postDelayed(this, TICK_MS);
                    } else {
                        deliveryComplete();
                    }
                }
            }, TICK_MS);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error starting animation", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDriverPosition() {
        try {
            double progress = animationStep / (double) totalSteps;
            double easedProgress = easeInOutCubic(progress);

            double lat = fromPoint.getLatitude() + (toPoint.getLatitude() - fromPoint.getLatitude()) * easedProgress;
            double lon = fromPoint.getLongitude() + (toPoint.getLongitude() - fromPoint.getLongitude()) * easedProgress;

            GeoPoint newPosition = new GeoPoint(lat, lon);
            driverMarker.setPosition(newPosition);

            if (progress < 0.3) {
                statusText.setText("🚗 Out for Delivery from " + branchName);
            } else if (progress < 0.6) {
                statusText.setText("🚗 Driver is on the way... (" + (int)(progress * 100) + "%)");
            } else if (progress < 0.9) {
                statusText.setText("📦 Almost there! (" + (int)(progress * 100) + "%)");
            } else {
                statusText.setText("📍 Arriving soon... (" + (int)(progress * 100) + "%)");
            }

            map.getController().animateTo(newPosition);
            map.invalidate();

        } catch (Exception e) {
            statusText.setText("Error updating position");
        }
    }

    private double easeInOutCubic(double p) {
        return p < 0.5 ? 4 * p * p * p : 1 - Math.pow(-2 * p + 2, 3) / 2;
    }

    private void deliveryComplete() {
        try {
            driverMarker.setPosition(toPoint);
            driverMarker.setSnippet("Arrived at destination");
            statusText.setText("✅ Delivery Complete! Your pizza has arrived!");
            Toast.makeText(requireContext(), "🎉 Order delivered successfully!", Toast.LENGTH_LONG).show();
            map.invalidate();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error completing delivery", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try { if (map != null) map.onResume(); } catch (Exception ignored) {}
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (map != null) map.onPause();
            if (animationHandler != null) animationHandler.removeCallbacksAndMessages(null);
        } catch (Exception ignored) {}
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try { if (animationHandler != null) animationHandler.removeCallbacksAndMessages(null); } catch (Exception ignored) {}
    }
}
