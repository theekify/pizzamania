package com.example.pizzamania;

import android.os.Bundle;
import android.util.Log;
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
import org.json.JSONException;
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

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        pizzaList = new ArrayList<>();
        adapter = new PizzaAdminAdapter(requireContext(), pizzaList, this::deletePizza, this::updatePizza);
        recyclerView.setAdapter(adapter);

        addBtn.setOnClickListener(v1 -> addPizza());

        fetchPizzas();
        return v;
    }

    private void fetchPizzas() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, ApiRoutes.PIZZAS, null,
                this::parsePizzas,
                error -> {
                    Toast.makeText(requireContext(), "Failed to fetch pizzas: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
        );
        VolleySingleton.getInstance(requireContext()).add(request);
    }

    private void parsePizzas(JSONArray response) {
        try {
            pizzaList.clear();
            Log.d("API_DEBUG", "Response received: " + response.toString());

            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                Log.d("API_DEBUG", "Pizza " + i + ": " + obj.toString());

                // Handle missing ID field - MockAPI.io might use different field names
                String id;
                if (obj.has("id")) {
                    id = obj.getString("id");
                } else {
                    // Generate a temporary ID if not provided by API
                    id = "temp_" + System.currentTimeMillis() + "_" + i;
                }

                FoodItem item = new FoodItem(
                        id,
                        obj.getString("name"),
                        obj.getString("price"),
                        obj.optString("imageUrl", obj.optString("image", "")) // Try both field names
                );
                pizzaList.add(item);
            }
            adapter.notifyDataSetChanged();

            if (pizzaList.isEmpty()) {
                Toast.makeText(requireContext(), "No pizzas found", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "JSON Error: " + e.getMessage(), Toast.LENGTH_LONG).show();

            // Debug: Try to see what fields are actually available
            try {
                for (int i = 0; i < response.length(); i++) {
                    JSONObject obj = response.getJSONObject(i);
                    Log.e("API_FIELDS", "Available fields in object " + i + ": " + obj.toString());
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error parsing data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void addPizza() {
        String name = nameInput.getText().toString().trim();
        String price = priceInput.getText().toString().trim();
        String imageUrl = imageUrlInput.getText().toString().trim();

        if (name.isEmpty() || price.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill name and price", Toast.LENGTH_SHORT).show();
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
                        try {
                            // MockAPI.io returns the created object with auto-generated ID
                            Toast.makeText(requireContext(), "Pizza added successfully!", Toast.LENGTH_SHORT).show();
                            nameInput.setText("");
                            priceInput.setText("");
                            imageUrlInput.setText("");
                            fetchPizzas(); // Refresh the list
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        Toast.makeText(requireContext(), "Failed to add pizza: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
            );
            VolleySingleton.getInstance(requireContext()).add(request);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error creating pizza: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deletePizza(String id) {
        // Check if it's a temporary ID (starts with "temp_")
        if (id.startsWith("temp_")) {
            Toast.makeText(requireContext(), "Cannot delete temporary items", Toast.LENGTH_SHORT).show();
            return;
        }

        String deleteUrl = ApiRoutes.deletePizza(id);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                deleteUrl,
                null,
                response -> {
                    Toast.makeText(requireContext(), "Pizza deleted!", Toast.LENGTH_SHORT).show();
                    fetchPizzas(); // Refresh the list
                },
                error -> {
                    Toast.makeText(requireContext(), "Failed to delete pizza: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
        );
        VolleySingleton.getInstance(requireContext()).add(request);
    }

    private void updatePizza(String id, String newName, String newPrice, String newImageUrl) {
        // Check if it's a temporary ID (starts with "temp_")
        if (id.startsWith("temp_")) {
            Toast.makeText(requireContext(), "Cannot update temporary items", Toast.LENGTH_SHORT).show();
            return;
        }

        String updateUrl = ApiRoutes.updatePizza(id);

        try {
            JSONObject updateData = new JSONObject();
            updateData.put("name", newName);
            updateData.put("price", newPrice);
            updateData.put("imageUrl", newImageUrl);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    updateUrl,
                    updateData,
                    response -> {
                        Toast.makeText(requireContext(), "Pizza updated!", Toast.LENGTH_SHORT).show();
                        fetchPizzas(); // Refresh the list
                    },
                    error -> {
                        Toast.makeText(requireContext(), "Failed to update pizza: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
            );
            VolleySingleton.getInstance(requireContext()).add(request);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error updating pizza: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}