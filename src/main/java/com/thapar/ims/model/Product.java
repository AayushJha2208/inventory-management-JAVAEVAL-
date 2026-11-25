package com.thapar.ims.model;

/**
 * A normal stock item. Fields are private (encapsulation).
 * Other classes read and change them only through getters and setters.
 */
public class Product {

    private int id;
    private String name;
    private String category;
    private double price;
    private int quantity;
    private int reorderLevel;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    /** Money currently sitting in stock for this product. */
    public double getStockValue() {
        return price * quantity;
    }

    /** True when quantity has fallen to the reorder level or below. */
    public boolean isLowStock() {
        return quantity <= reorderLevel;
    }

    /** Overridden by PerishableProduct. This is the polymorphism demo. */
    public String getType() {
        return "Regular";
    }

    /** Regular products have no expiry. The child class overrides this. */
    public String getExpiryDate() {
        return "";
    }

    @Override
    public String toString() {
        return id + " | " + name + " | " + category + " | Rs " + price
                + " | qty " + quantity + " | " + getType();
    }
}
