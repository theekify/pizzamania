package com.example.pizzamania;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    RecyclerView recyclerView;
    FoodAdapter adapter;
    List<FoodItem> foodList;
    RequestQueue requestQueue;

    String API_URL = "https://68a875babb882f2aa6de9ca3.mockapi.io/pizzas";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_menu, container, false);

        recyclerView = v.findViewById(R.id.recyclerMenu);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        foodList = new ArrayList<>();
        adapter = new FoodAdapter(getContext(), foodList);
        recyclerView.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(getContext());

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchMenuData();
    }

    private void fetchMenuData() {
        if (!isAdded() || getContext() == null) {
            return; // Fragment is not attached, skip the request
        }

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, API_URL, null,
                response -> {
                    if (!isAdded() || getContext() == null) return;

                    if (response.length() > 0) {
                        parseMenu(response);
                    } else {
                        showToastSafe("No menu items found!");
                    }
                },
                error -> {
                    if (!isAdded() || getContext() == null) return;

                    showToastSafe("Failed to fetch menu! Check your internet connection");
                    error.printStackTrace();
                }
        );

        requestQueue.add(request);
    }

    private void parseMenu(JSONArray response) {
        if (!isAdded() || getContext() == null) {
            return;
        }

        try {
            foodList.clear();
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);

                String name = obj.getString("name");
                String price = obj.getString("price");
                String imageUrl = obj.getString("imageUrl"); // Fixed: using imageUrl instead of image

                foodList.add(new FoodItem(name, price, imageUrl));
            }

            if (isAdded() && getContext() != null) {
                requireActivity().runOnUiThread(() -> {
                    if (isAdded() && getContext() != null) {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToastSafe("Error parsing menu data");
        }
    }

    private void showToastSafe(String message) {
        if (!isAdded() || getContext() == null) {
            return;
        }

        requireActivity().runOnUiThread(() -> {
            if (isAdded() && getContext() != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cancel any pending requests when fragment is destroyed
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }
}