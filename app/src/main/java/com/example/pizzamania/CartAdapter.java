package com.example.pizzamania;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    // ✅ Callback interface
    public interface OnCartChangeListener {
        void onCartUpdated();
    }

    private Context context;
    private List<FoodItem> cartList;
    private OnCartChangeListener listener;

    public CartAdapter(Context context, List<FoodItem> cartList, OnCartChangeListener listener) {
        this.context = context;
        this.cartList = cartList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        FoodItem item = cartList.get(position);

        holder.name.setText(item.getName());
        holder.price.setText(item.getPrice());
        holder.quantity.setText("Qty: " + item.getQuantity());

        // ✅ Handle image
        if (item.getImageResId() != 0) {
            holder.image.setImageResource(item.getImageResId());
        }

        holder.btnRemove.setOnClickListener(v -> {
            CartManager.getInstance().removeFromCart(item);
            notifyItemRemoved(position);
            if (listener != null) listener.onCartUpdated(); // notify CartFragment
            Toast.makeText(context, item.getName() + " removed!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView name, price, quantity;
        ImageView image;
        Button btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.cartName);
            price = itemView.findViewById(R.id.cartPrice);
            quantity = itemView.findViewById(R.id.cartQuantity);
            image = itemView.findViewById(R.id.cartImage);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
