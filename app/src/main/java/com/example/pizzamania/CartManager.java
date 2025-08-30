package com.example.pizzamania;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private final List<FoodItem> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(FoodItem item) {
        // Check if item already exists in cart using proper comparison
        for (FoodItem cartItem : cartItems) {
            if (cartItem.equals(item)) {
                cartItem.increaseQuantity();
                return;
            }
        }

        // If not exists, add new item (create a copy to avoid reference issues)
        FoodItem newItem = new FoodItem();
        newItem.setId(item.getId());
        newItem.setName(item.getName());
        newItem.setPrice(item.getPrice());
        newItem.setImageUrl(item.getImageUrl());
        newItem.setQuantity(1);

        cartItems.add(newItem);
    }

    public void removeFromCart(FoodItem item) {
        cartItems.removeIf(cartItem -> cartItem.equals(item));
    }

    public List<FoodItem> getCartItems() {
        return new ArrayList<>(cartItems); // return copy for safety
    }

    public void clearCart() {
        cartItems.clear();
    }

    // Add this method to refresh cart data in fragments
    public void notifyCartUpdated() {
        // This can be used with LiveData or EventBus in more complex apps
    }
}