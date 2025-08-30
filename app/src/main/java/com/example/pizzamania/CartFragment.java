package com.example.pizzamania;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CartFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView totalText;
    private Button btnPlaceOrder;
    private CartAdapter adapter;
    private List<FoodItem> cartItems;
    private SessionManager session;
    private double total = 0.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = v.findViewById(R.id.recyclerCart);
        totalText = v.findViewById(R.id.tvTotal);
        btnPlaceOrder = v.findViewById(R.id.btnPlaceOrder);

        session = new SessionManager(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        refreshCartData();

        btnPlaceOrder.setOnClickListener(view -> placeOrder());

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCartData();
    }

    private void refreshCartData() {
        cartItems = CartManager.getInstance().getCartItems();
        adapter = new CartAdapter(getContext(), cartItems, this::updateTotal);
        recyclerView.setAdapter(adapter);
        updateTotal();
    }

    private void updateTotal() {
        total = 0.0;
        for (FoodItem item : cartItems) {
            try {
                String priceStr = item.getPrice().replaceAll("[^0-9.]", "");
                double price = Double.parseDouble(priceStr);
                total += price * item.getQuantity();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        totalText.setText("Total: Rs. " + String.format("%.2f", total));
    }

    private void placeOrder() {
        if (cartItems.isEmpty()) {
            Toast.makeText(getContext(), "Cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 Redirect to payment screen
        Fragment payment = new PaymentFragment(total, cartItems);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, payment)
                .addToBackStack(null)
                .commit();
    }
}
