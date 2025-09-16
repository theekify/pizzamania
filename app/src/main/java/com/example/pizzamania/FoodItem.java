package com.example.pizzamania;

public class FoodItem {
    private String id;
    private String name;
    private String price;
    private int imageResId;
    private String imageUrl;
    private int quantity;

    public FoodItem() {
        this.quantity = 1;
    }

    public FoodItem(String name, String price, int imageResId) {
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
        this.imageUrl = null;
        this.quantity = 1;
    }

    public FoodItem(String name, String price, String imageUrl) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.imageResId = 0;
        this.quantity = 1;
    }

    public FoodItem(String id, String name, String price, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.imageResId = 0;
        this.quantity = 1;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPrice() { return price; }
    public int getImageResId() { return imageResId; }
    public String getImageUrl() { return imageUrl; }
    public int getQuantity() { return quantity; }

    public String getImage() {
        return imageUrl != null ? imageUrl : String.valueOf(imageResId);
    }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPrice(String price) { this.price = price; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public void setImage(String image) {
        this.imageUrl = image;
    }

    public void increaseQuantity() { this.quantity++; }
    public void decreaseQuantity() {
        if (quantity > 1) this.quantity--;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        FoodItem foodItem = (FoodItem) obj;

        if (id != null && foodItem.id != null) {
            return id.equals(foodItem.id);
        }

        return name.equals(foodItem.name) &&
                price.equals(foodItem.price) &&
                java.util.Objects.equals(imageUrl, foodItem.imageUrl);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return java.util.Objects.hash(name, price, imageUrl);
    }
}