package com.example.pizzamania;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private List<Order> ordersList;
    private OrdersFragment fragment;
    private String currentUserEmail;

    public OrdersAdapter(List<Order> ordersList, OrdersFragment fragment, String currentUserEmail) {
        this.ordersList = ordersList != null ? ordersList : java.util.Collections.emptyList();
        this.fragment = fragment;
        this.currentUserEmail = currentUserEmail;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        try {
            if (ordersList == null || position >= ordersList.size()) {
                return;
            }

            Order order = ordersList.get(position);

            // Check if order should be marked as delivered (older than 1 minute)
            if (order.isOlderThanOneMinute() && !"Delivered".equals(order.getStatus())) {
                order.setStatus("Delivered");
                // In a real app, you would update this on the server too
            }

            holder.bind(order);

            // Check if order is delivered
            boolean isDelivered = "Delivered".equals(order.getStatus());

            // Disable track button for delivered orders
            holder.btnTrack.setEnabled(!isDelivered);
            holder.btnTrack.setAlpha(isDelivered ? 0.5f : 1.0f);

            holder.btnTrack.setOnClickListener(v -> {
                try {
                    if (isDelivered) {
                        Toast.makeText(v.getContext(), "This order has been delivered", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Context context = v.getContext();
                    Log.d("TrackingDebug", "Track button clicked");

                    // Permissions / services checks
                    boolean hasPerms = LocationUtils.hasLocationPermissions(context);
                    boolean servicesEnabled = LocationUtils.areLocationServicesEnabled(context);

                    Log.d("TrackingDebug", "Has permissions: " + hasPerms);
                    Log.d("TrackingDebug", "Services enabled: " + servicesEnabled);

                    if (!hasPerms) {
                        fragment.requestLocationPermissions();
                        Toast.makeText(context, "Please grant location permissions to track your order", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (!servicesEnabled) {
                        LocationUtils.openLocationSettings(context);
                        Toast.makeText(context, "Please enable location services to track your order", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Log.d("TrackingDebug", "Opening tracking fragment for order: " + order.getId());

                    // ✅ Use the fragment’s FragmentManager; don’t rely on Activity type
                    View containerView = fragment.requireActivity().findViewById(R.id.fragment_container);
                    if (containerView == null) {
                        Toast.makeText(fragment.requireContext(), "Layout error", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    OrderTrackingFragment orderTrackingFragment = new OrderTrackingFragment();

                    // Pass order data with location information
                    Bundle args = new Bundle();
                    args.putString("orderId", order.getId());
                    args.putString("orderStatus", order.getStatus());
                    args.putDouble("orderTotal", order.getTotal());

                    // Location data
                    args.putDouble("customerLatitude", order.getCustomerLatitude());
                    args.putDouble("customerLongitude", order.getCustomerLongitude());
                    args.putDouble("branchLatitude", order.getBranchLatitude());
                    args.putDouble("branchLongitude", order.getBranchLongitude());
                    args.putString("branchName", order.getBranchName());

                    orderTrackingFragment.setArguments(args);

                    fragment.requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, orderTrackingFragment)
                            .addToBackStack("order_tracking")
                            .commit();

                } catch (Exception e) {
                    Log.e("OrdersAdapter", "Error opening tracking: " + e.getMessage(), e);
                    Toast.makeText(v.getContext(), "Error opening tracking", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("OrdersAdapter", "Error binding order", e);
        }
    }

    @Override
    public int getItemCount() {
        return ordersList != null ? ordersList.size() : 0;
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvOrderId, tvStatus, tvItems, tvTotal;
        Button btnTrack;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvItems = itemView.findViewById(R.id.tvItems);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            btnTrack = itemView.findViewById(R.id.btnTrack);
        }

        public void bind(Order order) {
            try {
                String orderId = order.getId();
                if (orderId != null && orderId.length() >= 6 && !orderId.equals("N/A")) {
                    tvOrderId.setText("Order #" + orderId.substring(orderId.length() - 6));
                } else {
                    tvOrderId.setText("Order #" + (orderId != null ? orderId : "Unknown"));
                }

                String status = order.getStatus();
                tvStatus.setText("Status: " + (status != null ? status : "Unknown"));

                StringBuilder itemsText = new StringBuilder();
                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    for (int i = 0; i < order.getItems().size(); i++) {
                        FoodItem item = order.getItems().get(i);
                        if (item != null) {
                            itemsText.append(item.getName() != null ? item.getName() : "Unknown Item")
                                    .append(" x")
                                    .append(item.getQuantity());
                            if (i < order.getItems().size() - 1) {
                                itemsText.append(", ");
                            }
                        }
                    }
                    tvItems.setText("Items: " + itemsText.toString());
                } else {
                    tvItems.setText("Items: None");
                }

                tvTotal.setText("Total: Rs " + String.format("%.2f", order.getTotal()));

            } catch (Exception e) {
                Log.e("OrderViewHolder", "Error binding order data", e);
                tvOrderId.setText("Order #Error");
                tvStatus.setText("Status: Error");
                tvItems.setText("Items: Error loading");
                tvTotal.setText("Total: Rs 0.00");
            }
        }
    }
}