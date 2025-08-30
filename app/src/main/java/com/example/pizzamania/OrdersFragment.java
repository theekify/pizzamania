package com.example.pizzamania;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private OrdersAdapter adapter;
    private List<Order> ordersList = new ArrayList<>();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private String currentUserEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_orders, container, false);

        recyclerView = v.findViewById(R.id.recyclerOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get current user email from SessionManager
        SessionManager session = new SessionManager(requireContext());
        currentUserEmail = session.getUserEmail();

        adapter = new OrdersAdapter(ordersList, this, currentUserEmail);
        recyclerView.setAdapter(adapter);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            Toast.makeText(requireContext(), "Please login to view orders", Toast.LENGTH_SHORT).show();
            return;
        }

        fetchOrders();
    }

    public void requestLocationPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(requireContext(), "Location permission is recommended for better tracking.", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Location permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Location permission denied. Tracking will still work.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void fetchOrders() {
        try {
            JsonArrayRequest request = new JsonArrayRequest(
                    Request.Method.GET,
                    ApiRoutes.ORDERS,
                    null,
                    response -> {
                        try {
                            Log.d("OrdersFragment", "API Response received");
                            parseOrders(response);
                        } catch (Exception e) {
                            Log.e("OrdersFragment", "Error in response handling", e);
                            Toast.makeText(requireContext(), "Error loading orders", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Log.e("OrdersFragment", "Network error: " + error.getMessage());
                        Toast.makeText(requireContext(), "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
            );
            VolleySingleton.getInstance(requireContext()).add(request);
        } catch (Exception e) {
            Log.e("OrdersFragment", "Error creating request", e);
            Toast.makeText(requireContext(), "Error fetching orders", Toast.LENGTH_SHORT).show();
        }
    }

    private void parseOrders(JSONArray jsonArray) {
        try {
            Log.d("OrdersFragment", "Parsing orders array length: " + jsonArray.length());

            List<Order> newOrdersList = new ArrayList<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    // Filter orders by current user
                    String orderUserEmail = jsonObject.optString("userEmail", "");
                    if (!orderUserEmail.equals(currentUserEmail)) {
                        continue; // Skip orders that don't belong to the current user
                    }

                    Order order = new Order();

                    order.setId(jsonObject.optString("id", "N/A"));
                    order.setUserEmail(orderUserEmail);
                    order.setTotal(jsonObject.optDouble("total", 0.0));
                    // ✅ default fallback changed to "Out for Delivery"
                    order.setStatus(jsonObject.optString("status", "Out for Delivery"));

                    if (jsonObject.has("customerLatitude") && jsonObject.has("customerLongitude")) {
                        order.setCustomerLatitude(jsonObject.optDouble("customerLatitude", 6.9147));
                        order.setCustomerLongitude(jsonObject.optDouble("customerLongitude", 79.9738));
                    } else {
                        order.setCustomerLatitude(6.9147);
                        order.setCustomerLongitude(79.9738);
                    }

                    if (jsonObject.has("branchLatitude") && jsonObject.has("branchLongitude")) {
                        order.setBranchLatitude(jsonObject.optDouble("branchLatitude", 6.9271));
                        order.setBranchLongitude(jsonObject.optDouble("branchLongitude", 79.8612));
                        order.setBranchName(jsonObject.optString("branchName", "Pizza Mania Branch"));
                    } else {
                        order.setBranchLatitude(6.9271);
                        order.setBranchLongitude(79.8612);
                        order.setBranchName("Pizza Mania Branch");
                    }

                    // Parse createdAt date if available
                    String createdAtStr = jsonObject.optString("createdAt", "");
                    if (!createdAtStr.isEmpty()) {
                        try {
                            Date createdAt = dateFormat.parse(createdAtStr);
                            order.setCreatedAt(createdAt);
                        } catch (ParseException e) {
                            Log.e("OrdersFragment", "Error parsing date: " + createdAtStr, e);
                            order.setCreatedAt(new Date()); // Fallback to current date
                        }
                    } else {
                        order.setCreatedAt(new Date()); // Fallback to current date
                    }

                    List<FoodItem> items = new ArrayList<>();
                    JSONArray itemsArray = jsonObject.optJSONArray("items");

                    if (itemsArray != null) {
                        for (int j = 0; j < itemsArray.length(); j++) {
                            try {
                                JSONObject itemObject = itemsArray.getJSONObject(j);
                                FoodItem item = parseFoodItem(itemObject);
                                items.add(item);
                            } catch (Exception e) {
                                Log.e("OrdersFragment", "Error parsing item " + j, e);
                            }
                        }
                    }

                    order.setItems(items);
                    newOrdersList.add(order);

                } catch (Exception e) {
                    Log.e("OrdersFragment", "Error parsing order " + i, e);
                }
            }

            requireActivity().runOnUiThread(() -> {
                ordersList.clear();
                ordersList.addAll(newOrdersList);
                adapter.notifyDataSetChanged();

                if (ordersList.isEmpty()) {
                    Toast.makeText(requireContext(), "No orders found for your account", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Loaded " + ordersList.size() + " orders", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e("OrdersFragment", "Error parsing orders", e);
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Error parsing orders", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private FoodItem parseFoodItem(JSONObject itemObj) {
        FoodItem item = new FoodItem();
        try {
            item.setName(itemObj.optString("name", "Unknown Item"));
            item.setPrice(itemObj.optString("price", "0.00"));
            item.setQuantity(itemObj.optInt("quantity", 1));
            item.setImage(itemObj.optString("image", ""));
        } catch (Exception e) {
            Log.e("OrdersFragment", "Error parsing food item", e);
        }
        return item;
    }
}