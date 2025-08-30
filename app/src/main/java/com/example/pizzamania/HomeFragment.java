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
        fetchMenuData();

        return v;
    }

    private void fetchMenuData() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, API_URL, null,
                response -> {
                    if (response.length() > 0) {
                        parseMenu(response);
                    } else {
                        Toast.makeText(getContext(), "No menu items found!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(getContext(), "Failed to fetch menu! Check your internet connection", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
        );

        requestQueue.add(request);
    }

    private void parseMenu(JSONArray response) {
        try {
            foodList.clear();
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);

                String name = obj.getString("name");
                String price = obj.getString("price");
                String imageUrl = obj.getString("imageUrl"); // Fixed: using imageUrl instead of image

                foodList.add(new FoodItem(name, price, imageUrl));
            }
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error parsing menu data", Toast.LENGTH_SHORT).show();
        }
    }
}