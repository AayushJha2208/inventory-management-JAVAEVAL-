package com.thapar.ims.model;

/**
 * Inheritance: PerishableProduct IS-A Product, plus an expiry date.
 * A grocery item such as milk uses this class. A pen uses Product.
 */
public class PerishableProduct extends Product {

    private String expiryDate;

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    public String getExpiryDate() {
        return expiryDate == null ? "" : expiryDate;
    }

    @Override
    public String getType() {
        return "Perishable";
    }
}
