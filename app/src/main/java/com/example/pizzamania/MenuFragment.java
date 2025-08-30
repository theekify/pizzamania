package com.example.pizzamania;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends Fragment {
    RecyclerView recyclerView;
    FoodAdapter adapter;
    List<FoodItem> foodList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_menu, container, false);

        recyclerView = v.findViewById(R.id.recyclerMenu);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        foodList = new ArrayList<>();
        adapter = new FoodAdapter(getContext(), foodList);
        recyclerView.setAdapter(adapter);

        fetchMenu();
        return v;
    }

    private void fetchMenu() {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                ApiRoutes.PIZZAS,
                null,
                response -> {
                    if (response.length() > 0) {
                        parseMenu(response);
                    } else {
                        Toast.makeText(getContext(), "No pizzas available", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(getContext(), "Failed to fetch menu. Check internet connection", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
        );
        VolleySingleton.getInstance(requireContext()).add(request);
    }

    private void parseMenu(JSONArray response) {
        try {
            foodList.clear();
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);

                String id = obj.getString("id"); // Get the ID from API
                String name = obj.getString("name");
                String price = obj.getString("price");
                String imageUrl = obj.getString("imageUrl");

                // Create FoodItem with ID
                FoodItem item = new FoodItem(id, name, price, imageUrl);
                foodList.add(item);
            }
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading menu", Toast.LENGTH_SHORT).show();
        }
    }

}