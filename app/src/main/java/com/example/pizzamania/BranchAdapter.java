package com.example.pizzamania;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class BranchAdapter extends RecyclerView.Adapter<BranchAdapter.BranchViewHolder> {

    private final Context context;
    private final List<Branch> branchList;
    private final DatabaseHelper db;
    private final Runnable refreshCallback; // if you still want to reload from DB after edits

    public BranchAdapter(Context context, List<Branch> branchList, DatabaseHelper db, Runnable refreshCallback) {
        this.context = context;
        this.branchList = branchList;
        this.db = db;
        this.refreshCallback = refreshCallback;
    }

    @NonNull
    @Override
    public BranchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_branch, parent, false);
        return new BranchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BranchViewHolder holder, int position) {
        Branch branch = branchList.get(position);

        holder.branchName.setText(branch.getName());
        holder.branchAddress.setText(branch.getAddress());
        holder.branchPhone.setText(branch.getPhone());

        holder.btnDelete.setOnClickListener(v -> confirmDelete(branch, position));

        holder.btnEdit.setOnClickListener(v -> showEditDialog(branch, position));
    }

    @Override
    public int getItemCount() {
        return branchList.size();
    }

    private void confirmDelete(Branch branch, int adapterPosition) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Branch")
                .setMessage("Are you sure you want to delete \"" + branch.getName() + "\"?")
                .setPositiveButton("Delete", (d, w) -> {
                    boolean deleted = db.deleteBranch(branch.getId());
                    if (deleted) {
                        branchList.remove(adapterPosition);
                        notifyItemRemoved(adapterPosition);
                        Toast.makeText(context, "Branch deleted", Toast.LENGTH_SHORT).show();
                        if (refreshCallback != null) refreshCallback.run();
                    } else {
                        Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(Branch branch, int adapterPosition) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_branch, null, false);
        TextInputEditText etName = dialogView.findViewById(R.id.etBranchName);
        TextInputEditText etAddress = dialogView.findViewById(R.id.etBranchAddress);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etBranchPhone);

        // Prefill
        etName.setText(branch.getName());
        etAddress.setText(branch.getAddress());
        etPhone.setText(branch.getPhone());

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Edit Branch")
                .setView(dialogView)
                .setPositiveButton("Save", null) // we override later to prevent auto-dismiss
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String newName = safeText(etName);
                String newAddress = safeText(etAddress);
                String newPhone = safeText(etPhone);

                if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newAddress) || TextUtils.isEmpty(newPhone)) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return; // keep dialog open
                }
                // Simple phone check (optional)
                if (newPhone.length() < 7) {
                    Toast.makeText(context, "Enter a valid phone", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean updated = db.updateBranch(branch.getId(), newName, newAddress, newPhone, branch.getLatitude(), branch.getLongitude());
                if (updated) {
                    // Update in-memory model and refresh the single item
                    branch.setName(newName);
                    branch.setAddress(newAddress);
                    branch.setPhone(newPhone);
                    notifyItemChanged(adapterPosition);
                    Toast.makeText(context, "Branch updated", Toast.LENGTH_SHORT).show();

                    // If you also want to re-query DB:
                    if (refreshCallback != null) refreshCallback.run();

                    dialog.dismiss();
                } else {
                    Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private String safeText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    public static class BranchViewHolder extends RecyclerView.ViewHolder {
        TextView branchName, branchAddress, branchPhone;
        MaterialButton btnEdit, btnDelete;

        public BranchViewHolder(@NonNull View itemView) {
            super(itemView);
            branchName = itemView.findViewById(R.id.branchName);
            branchAddress = itemView.findViewById(R.id.branchAddress);
            branchPhone = itemView.findViewById(R.id.branchPhone);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
