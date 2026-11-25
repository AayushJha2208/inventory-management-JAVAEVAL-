package com.thapar.ims.model;

/**
 * Represents a completed sales transaction.
 */
public class Sale {

    private int id;
    private int productId;
    private String productName;
    private String category;
    private int quantity;
    private double unitPrice;
    private double totalAmount;
    private String saleDate;
    private String notes;

    public Sale() {
    }

    public Sale(int id, int productId, String productName, String category,
                int quantity, double unitPrice, double totalAmount,
                String saleDate, String notes) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.saleDate = saleDate;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(String saleDate) {
        this.saleDate = saleDate;
    }

    public String getNotes() {
        return notes == null ? "" : notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Sale #" + id + " | " + productName + " | Qty: " + quantity
                + " | Total: Rs " + String.format("%.2f", totalAmount) + " | Date: " + saleDate;
    }
}
