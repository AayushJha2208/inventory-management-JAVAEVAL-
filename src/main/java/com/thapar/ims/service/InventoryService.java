package com.thapar.ims.service;

import com.thapar.ims.exception.InventoryException;
import com.thapar.ims.model.Product;

import java.util.List;

/**
 * What the screen is allowed to do. The window calls these methods only.
 */
public interface InventoryService {

    void addProduct(String name, String category, double price, int quantity,
                    int reorderLevel, String expiry) throws InventoryException;

    void updateProduct(int id, String name, String category, double price, int quantity,
                       int reorderLevel, String expiry) throws InventoryException;

    void deleteProduct(int id) throws InventoryException;

    Product searchById(int id) throws InventoryException;

    List<Product> searchByName(String text) throws InventoryException;

    List<Product> getAllProducts() throws InventoryException;

    List<Product> getLowStockProducts() throws InventoryException;

    List<String> exportReports() throws InventoryException;
}
