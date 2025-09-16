package com.example.pizzamania;

import android.app.AlertDialog;
import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class PizzaAdminAdapter extends RecyclerView.Adapter<PizzaAdminAdapter.ViewHolder> {
    Context context;
    List<FoodItem> list;
    OnDeleteListener deleteListener;
    OnUpdateListener updateListener;

    public PizzaAdminAdapter(Context ctx, List<FoodItem> list, OnDeleteListener deleteListener, OnUpdateListener updateListener) {
        this.context = ctx;
        this.list = list;
        this.deleteListener = deleteListener;
        this.updateListener = updateListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_admin_pizza, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem item = list.get(position);
        holder.name.setText(item.getName());
        holder.price.setText("Rs " + item.getPrice());

        // Load image using Glide
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(item.getId());
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            showEditDialog(item);
        });
    }

    private void showEditDialog(FoodItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Pizza");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_pizza, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etPrice = dialogView.findViewById(R.id.etPrice);
        EditText etImageUrl = dialogView.findViewById(R.id.etImageUrl);

        etName.setText(item.getName());
        etPrice.setText(item.getPrice());
        etImageUrl.setText(item.getImageUrl());

        builder.setView(dialogView);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = etName.getText().toString().trim();
            String newPrice = etPrice.getText().toString().trim();
            String newImageUrl = etImageUrl.getText().toString().trim();

            if (!newName.isEmpty() && !newPrice.isEmpty()) {
                if (updateListener != null) {
                    updateListener.onUpdate(item.getId(), newName, newPrice, newImageUrl);
                }
            } else {
                Toast.makeText(context, "Please fill name and price fields", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, price;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View v) {
            super(v);
            image = v.findViewById(R.id.pizzaImage);
            name = v.findViewById(R.id.pizzaName);
            price = v.findViewById(R.id.pizzaPrice);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }

    public interface OnDeleteListener {
        void onDelete(String id);
    }

    public interface OnUpdateListener {
        void onUpdate(String id, String newName, String newPrice, String newImageUrl);
    }
}