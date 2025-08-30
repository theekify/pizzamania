package com.example.pizzamania;

public class ApiRoutes {
    private static final String BASE = "https://68a875babb882f2aa6de9ca3.mockapi.io/";

    public static final String PIZZAS = BASE + "pizzas";
    public static final String ORDERS = BASE + "orders";

    // Helper method to get specific pizza by ID
    public static String getPizzaById(String id) {
        return PIZZAS + "/" + id;
    }
}