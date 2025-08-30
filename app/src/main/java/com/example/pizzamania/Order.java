package com.example.pizzamania;

import java.util.Date;
import java.util.List;

public class Order {
    private String id;
    private String userEmail;
    private double total;
    private String status;
    private List<FoodItem> items;
    private double customerLatitude;
    private double customerLongitude;
    private double branchLatitude;
    private double branchLongitude;
    private String branchName;
    private Date createdAt; // Add this field

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<FoodItem> getItems() { return items; }
    public void setItems(List<FoodItem> items) { this.items = items; }

    public double getCustomerLatitude() { return customerLatitude; }
    public void setCustomerLatitude(double customerLatitude) { this.customerLatitude = customerLatitude; }

    public double getCustomerLongitude() { return customerLongitude; }
    public void setCustomerLongitude(double customerLongitude) { this.customerLongitude = customerLongitude; }

    public double getBranchLatitude() { return branchLatitude; }
    public void setBranchLatitude(double branchLatitude) { this.branchLatitude = branchLatitude; }

    public double getBranchLongitude() { return branchLongitude; }
    public void setBranchLongitude(double branchLongitude) { this.branchLongitude = branchLongitude; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    // Helper method to check if order is older than 1 minute
    public boolean isOlderThanOneMinute() {
        if (createdAt == null) return false;
        long currentTime = System.currentTimeMillis();
        long orderTime = createdAt.getTime();
        return (currentTime - orderTime) > 60000; // 1 minute in milliseconds
    }
}