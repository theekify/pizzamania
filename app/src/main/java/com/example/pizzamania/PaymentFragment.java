package com.example.pizzamania;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

public class PaymentFragment extends Fragment {
    private double total;
    private List<FoodItem> items;
    private SessionManager session;
    private DatabaseHelper dbHelper;
    private Branch nearestBranch;
    private TextView tvBranchInfo;
    private Button btnSelectLocation;

    public PaymentFragment(double total, List<FoodItem> items) {
        this.total = total;
        this.items = items;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, ViewGroup container, Bundle b) {
        View v = inf.inflate(R.layout.fragment_payment, container, false);
        session = new SessionManager(requireContext());
        dbHelper = new DatabaseHelper(requireContext());

        TextView tvTotal = v.findViewById(R.id.tvPaymentTotal);
        tvBranchInfo = v.findViewById(R.id.tvBranchInfo);
        RadioGroup rg = v.findViewById(R.id.paymentOptions);
        Button btnConfirmPayment = v.findViewById(R.id.btnConfirmPayment);
        btnSelectLocation = v.findViewById(R.id.btnSelectLocation);

        tvTotal.setText("Pay Rs. " + String.format("%.2f", total));

        btnSelectLocation.setOnClickListener(view -> {
            LocationSelectionFragment locationFragment = new LocationSelectionFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, locationFragment)
                    .addToBackStack("location_selection")
                    .commit();
        });

        findAndDisplayNearestBranch();

        btnConfirmPayment.setOnClickListener(view -> {
            String method = (rg.getCheckedRadioButtonId() == R.id.radioCash) ? "Cash" : "Card";
            try {
                JSONArray itemsArray = new JSONArray();
                for (FoodItem item : items) {
                    JSONObject obj = new JSONObject();
                    obj.put("name", item.getName());
                    obj.put("price", item.getPrice());
                    obj.put("quantity", item.getQuantity());
                    obj.put("image", item.getImageUrl() != null ? item.getImageUrl() : "");
                    itemsArray.put(obj);
                }

                JSONObject order = new JSONObject();
                order.put("userEmail", session.getUserEmail());
                order.put("items", itemsArray);
                order.put("total", total);
                order.put("status", "Out for Delivery"); // ✅ default so tracking works
                order.put("method", method);

                if (nearestBranch != null) {
                    order.put("branchId", nearestBranch.getId());
                    order.put("branchName", nearestBranch.getName());
                    order.put("branchAddress", nearestBranch.getAddress());
                    order.put("branchPhone", nearestBranch.getPhone());
                    order.put("branchLatitude", nearestBranch.getLatitude());
                    order.put("branchLongitude", nearestBranch.getLongitude());
                }

                if (LocationPreference.hasSavedLocation(requireContext())) {
                    double[] location = LocationPreference.getSavedLocation(requireContext());
                    order.put("customerLatitude", location[0]);
                    order.put("customerLongitude", location[1]);
                }

                JsonObjectRequest req = new JsonObjectRequest(
                        Request.Method.POST,
                        ApiRoutes.ORDERS,
                        order,
                        response -> {
                            Toast.makeText(getContext(), "Payment successful! Order will be delivered from " +
                                            (nearestBranch != null ? nearestBranch.getName() : "nearest branch"),
                                    Toast.LENGTH_LONG).show();
                            CartManager.getInstance().clearCart();
                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_container, new OrdersFragment())
                                    .commit();
                        },
                        error -> {
                            Toast.makeText(getContext(), "Failed to place order", Toast.LENGTH_SHORT).show();
                        }
                );

                VolleySingleton.getInstance(requireContext()).add(req);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error creating order", Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }

    private void findAndDisplayNearestBranch() {
        try {
            if (LocationPreference.hasSavedLocation(requireContext())) {
                double[] location = LocationPreference.getSavedLocation(requireContext());
                nearestBranch = LocationUtils.findNearestBranch(requireContext(), dbHelper, location[0], location[1]);
            } else {
                nearestBranch = LocationUtils.findNearestBranch(requireContext(), dbHelper);
            }

            if (nearestBranch != null) {
                String branchInfo = "🛵 Delivery from: " + nearestBranch.getName() +
                        "\n📍 " + nearestBranch.getAddress() +
                        "\n📞 " + nearestBranch.getPhone();

                if (LocationPreference.hasSavedLocation(requireContext())) {
                    branchInfo += "\n🎯 Using your selected location";
                } else {
                    branchInfo += "\n📍 Using your current location";
                }

                tvBranchInfo.setText(branchInfo);
                tvBranchInfo.setVisibility(View.VISIBLE);
            } else {
                tvBranchInfo.setText("Could not determine nearest branch. Using default branch.");
                tvBranchInfo.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            tvBranchInfo.setText("Error finding nearest branch");
            tvBranchInfo.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        findAndDisplayNearestBranch();
    }
}
