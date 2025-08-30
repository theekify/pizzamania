package com.example.pizzamania;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BranchFragment extends Fragment {

    private EditText branchNameInput, branchAddressInput, branchPhoneInput;
    private Button btnAddBranch;
    private RecyclerView recyclerBranches;
    private DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_branch, container, false);

        branchNameInput = v.findViewById(R.id.branchNameInput);
        branchAddressInput = v.findViewById(R.id.branchAddressInput);
        branchPhoneInput = v.findViewById(R.id.branchPhoneInput);
        btnAddBranch = v.findViewById(R.id.btnAddBranch);
        recyclerBranches = v.findViewById(R.id.recyclerBranches);

        db = new DatabaseHelper(getContext());

        recyclerBranches.setLayoutManager(new LinearLayoutManager(getContext()));

        btnAddBranch.setOnClickListener(view -> addBranch());

        loadBranches();

        return v;
    }

    private void addBranch() {
        String name = branchNameInput.getText().toString().trim();
        String address = branchAddressInput.getText().toString().trim();
        String phone = branchPhoneInput.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean inserted = db.insertBranch(name, address, phone, 0.0, 0.0);
        if (inserted) {
            Toast.makeText(getContext(), "Branch added!", Toast.LENGTH_SHORT).show();
            branchNameInput.setText("");
            branchAddressInput.setText("");
            branchPhoneInput.setText("");
            loadBranches();
        } else {
            Toast.makeText(getContext(), "Failed to add branch", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBranches() {
        List<Branch> branches = db.getBranches();
        BranchAdapter adapter = new BranchAdapter(getContext(), branches, db, this::loadBranches);
        recyclerBranches.setAdapter(adapter);
    }
}
