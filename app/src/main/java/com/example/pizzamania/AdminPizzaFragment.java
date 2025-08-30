package com.example.pizzamania;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class AdminPizzaFragment extends Fragment {
    RecyclerView recyclerView;
    PizzaAdminAdapter adapter;
    List<FoodItem> pizzaList;
    EditText nameInput, priceInput, imageUrlInput;
    Button addBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_pizza, container, false);

        recyclerView = v.findViewById(R.id.recyclerPizzas);
        nameInput = v.findViewById(R.id.pizzaNameInput);
        priceInput = v.findViewById(R.id.pizzaPriceInput);
        imageUrlInput = v.findViewById(R.id.pizzaImageInput);
        addBtn = v.findViewById(R.id.btnAddPizza);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        pizzaList = new ArrayList<>();
        adapter = new PizzaAdminAdapter(getContext(), pizzaList, this::deletePizza, this::updatePizza);
        recyclerView.setAdapter(adapter);

        addBtn.setOnClickListener(v1 -> addPizza());

        fetchPizzas();
        return v;
    }

    private void fetchPizzas() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, ApiRoutes.PIZZAS, null,
                this::parsePizzas,
                error -> {
                    Toast.makeText(getContext(), "Failed to fetch pizzas", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
        );
        VolleySingleton.getInstance(requireContext()).add(request);
    }

    private void parsePizzas(JSONArray response) {
        try {
            pizzaList.clear();
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                FoodItem item = new FoodItem(
                        obj.getString("id"),
                        obj.getString("name"),
                        obj.getString("price"),
                        obj.getString("imageUrl")
                );
                pizzaList.add(item);
            }
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error parsing pizza data", Toast.LENGTH_SHORT).show();
        }
    }

    private void addPizza() {
        String name = nameInput.getText().toString().trim();
        String price = priceInput.getText().toString().trim();
        String imageUrl = imageUrlInput.getText().toString().trim();

        if (name.isEmpty() || price.isEmpty()) {
            Toast.makeText(getContext(), "Please fill name and price", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use default placeholder if no image URL provided
        if (imageUrl.isEmpty()) {
            imageUrl = "https://via.placeholder.com/150/FF6B6B/FFFFFF?text=" + name.replace(" ", "+");
        }

        try {
            JSONObject pizzaData = new JSONObject();
            pizzaData.put("name", name);
            pizzaData.put("price", price);
            pizzaData.put("imageUrl", imageUrl);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    ApiRoutes.PIZZAS,
                    pizzaData,
                    response -> {
                        Toast.makeText(getContext(), "Pizza added successfully!", Toast.LENGTH_SHORT).show();
                        // Clear inputs
                        nameInput.setText("");
                        priceInput.setText("");
                        imageUrlInput.setText("");
                        fetchPizzas(); // Refresh the list
                    },
                    error -> {
                        Toast.makeText(getContext(), "Failed to add pizza", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
            );
            VolleySingleton.getInstance(requireContext()).add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deletePizza(String id) {
        String deleteUrl = ApiRoutes.PIZZAS + "/" + id;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                deleteUrl,
                null,
                response -> {
                    Toast.makeText(getContext(), "Pizza deleted!", Toast.LENGTH_SHORT).show();
                    fetchPizzas(); // Refresh the list
                },
                error -> {
                    Toast.makeText(getContext(), "Failed to delete pizza", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
        );
        VolleySingleton.getInstance(requireContext()).add(request);
    }

    private void updatePizza(String id, String newName, String newPrice) {
        String updateUrl = ApiRoutes.PIZZAS + "/" + id;

        try {
            JSONObject updateData = new JSONObject();
            updateData.put("name", newName);
            updateData.put("price", newPrice);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    updateUrl,
                    updateData,
                    response -> {
                        Toast.makeText(getContext(), "Pizza updated!", Toast.LENGTH_SHORT).show();
                        fetchPizzas(); // Refresh the list
                    },
                    error -> {
                        Toast.makeText(getContext(), "Failed to update pizza", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
            );
            VolleySingleton.getInstance(requireContext()).add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}