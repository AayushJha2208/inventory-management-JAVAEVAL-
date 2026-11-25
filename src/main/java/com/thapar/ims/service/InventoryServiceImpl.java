package com.thapar.ims.service;

import com.thapar.ims.dao.ProductDao;
import com.thapar.ims.dao.ProductDaoImpl;
import com.thapar.ims.dao.SaleDao;
import com.thapar.ims.dao.SaleDaoImpl;
import com.thapar.ims.exception.InventoryException;
import com.thapar.ims.model.PerishableProduct;
import com.thapar.ims.model.Product;
import com.thapar.ims.model.Sale;
import com.thapar.ims.util.AppLogger;
import com.thapar.ims.util.ReportWriter;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Business rules live here.
 * ArrayList holds the full list. HashMap finds a product quickly by id.
 */
public class InventoryServiceImpl implements InventoryService {

    private static final Logger LOG = AppLogger.get(InventoryServiceImpl.class);

    private final ProductDao dao = new ProductDaoImpl();
    private final SaleDao saleDao = new SaleDaoImpl();
    private final Map<Integer, Product> byId = new HashMap<>();

    public InventoryServiceImpl() throws InventoryException {
        refreshCache();
    }

    @Override
    public void addProduct(String name, String category, double price, int quantity,
                           int reorderLevel, String expiry) throws InventoryException {
        Product product = build(name, category, price, quantity, reorderLevel, expiry);
        try {
            dao.insert(product);
            refreshCache();
            LOG.info("Added product id=" + product.getId() + " name=" + product.getName());
        } catch (SQLException ex) {
            throw databaseError("Could not add the product.", ex);
        }
    }

    @Override
    public void updateProduct(int id, String name, String category, double price, int quantity,
                              int reorderLevel, String expiry) throws InventoryException {
        if (searchById(id) == null) {
            throw new InventoryException("No product found with id " + id + ".");
        }
        Product product = build(name, category, price, quantity, reorderLevel, expiry);
        product.setId(id);
        try {
            dao.update(product);
            refreshCache();
            LOG.info("Updated product id=" + id);
        } catch (SQLException ex) {
            throw databaseError("Could not update the product.", ex);
        }
    }

    @Override
    public void deleteProduct(int id) throws InventoryException {
        if (searchById(id) == null) {
            throw new InventoryException("No product found with id " + id + ".");
        }
        try {
            dao.delete(id);
            refreshCache();
            LOG.info("Deleted product id=" + id);
        } catch (SQLException ex) {
            throw databaseError("Could not delete the product.", ex);
        }
    }

    @Override
    public Product searchById(int id) throws InventoryException {
        if (byId.isEmpty()) {
            refreshCache();
        }
        return byId.get(id);
    }

    @Override
    public List<Product> searchByName(String text) throws InventoryException {
        String lookFor = text == null ? "" : text.trim().toLowerCase();
        List<Product> matches = new ArrayList<>();
        for (Product product : getAllProducts()) {
            if (product.getName().toLowerCase().contains(lookFor)
                    || product.getCategory().toLowerCase().contains(lookFor)) {
                matches.add(product);
            }
        }
        LOG.info("Search \"" + text + "\" matched " + matches.size() + " product(s)");
        return matches;
    }

    @Override
    public List<Product> getAllProducts() throws InventoryException {
        if (byId.isEmpty()) {
            refreshCache();
        }
        List<Product> products = new ArrayList<>(byId.values());
        products.sort(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER));
        return products;
    }

    @Override
    public List<Product> getLowStockProducts() throws InventoryException {
        List<Product> low = new ArrayList<>();
        for (Product product : getAllProducts()) {
            if (product.isLowStock()) {
                low.add(product);
            }
        }
        return low;
    }

    @Override
    public List<String> exportReports() throws InventoryException {
        try {
            List<String> paths = ReportWriter.writeAll(getAllProducts());
            LOG.info("Exported reports to " + paths);
            return paths;
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Report export failed", ex);
            throw new InventoryException("Could not write the report file.", ex);
        }
    }

    // --- Sales Tracking Implementation ---

    @Override
    public Sale recordSale(int productId, int quantity, double unitPrice, String notes, boolean removeIfZero) throws InventoryException {
        if (quantity <= 0) {
            throw new InventoryException("Sale quantity must be at least 1.");
        }
        if (unitPrice < 0) {
            throw new InventoryException("Sale price cannot be negative.");
        }

        Product product = searchById(productId);
        if (product == null) {
            throw new InventoryException("Product not found (ID: " + productId + ").");
        }

        if (product.getQuantity() < quantity) {
            throw new InventoryException("Insufficient stock for \"" + product.getName()
                    + "\". Available: " + product.getQuantity() + ", Requested: " + quantity);
        }

        int remainingStock = product.getQuantity() - quantity;
        double totalAmount = quantity * unitPrice;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Sale sale = new Sale(0, productId, product.getName(), product.getCategory(), quantity, unitPrice, totalAmount, timestamp, notes);

        try {
            // 1. Deduct or remove from products database
            if (removeIfZero && remainingStock == 0) {
                dao.delete(productId);
                LOG.info("Product #" + productId + " (" + product.getName() + ") was completely sold out and removed from catalog.");
            } else {
                product.setQuantity(remainingStock);
                dao.update(product);
                LOG.info("Deducted " + quantity + " units of product #" + productId + ". Remaining stock: " + remainingStock);
            }
            refreshCache();

            // 2. Insert sales transaction record
            saleDao.insert(sale);
            LOG.info("Recorded sale #" + sale.getId() + " (" + sale.getProductName() + ", qty=" + quantity + ", total=Rs " + totalAmount + ")");
            return sale;
        } catch (SQLException ex) {
            throw databaseError("Failed to process sale transaction.", ex);
        }
    }

    @Override
    public List<Sale> getAllSales() throws InventoryException {
        try {
            return saleDao.findAll();
        } catch (SQLException ex) {
            throw databaseError("Could not retrieve sales history.", ex);
        }
    }

    @Override
    public List<Sale> searchSales(String text) throws InventoryException {
        String query = (text == null ? "" : text.trim()).toLowerCase();
        List<Sale> all = getAllSales();
        if (query.isEmpty()) {
            return all;
        }
        List<Sale> filtered = new ArrayList<>();
        for (Sale s : all) {
            if (String.valueOf(s.getId()).equals(query)
                    || s.getProductName().toLowerCase().contains(query)
                    || (s.getCategory() != null && s.getCategory().toLowerCase().contains(query))
                    || s.getNotes().toLowerCase().contains(query)
                    || s.getSaleDate().contains(query)) {
                filtered.add(s);
            }
        }
        return filtered;
    }

    @Override
    public double getTotalSalesRevenue() throws InventoryException {
        try {
            return saleDao.getTotalRevenue();
        } catch (SQLException ex) {
            throw databaseError("Could not calculate total revenue.", ex);
        }
    }

    @Override
    public int getTotalItemsSold() throws InventoryException {
        try {
            return saleDao.getTotalQuantitySold();
        } catch (SQLException ex) {
            throw databaseError("Could not calculate items sold.", ex);
        }
    }

    @Override
    public List<String> exportSalesReport() throws InventoryException {
        try {
            List<String> paths = ReportWriter.writeSalesReport(getAllSales());
            LOG.info("Exported sales reports to " + paths);
            return paths;
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Sales report export failed", ex);
            throw new InventoryException("Could not write sales report file.", ex);
        }
    }

    private void refreshCache() throws InventoryException {
        try {
            byId.clear();
            for (Product product : dao.findAll()) {
                byId.put(product.getId(), product);
            }
        } catch (SQLException ex) {
            throw databaseError("Could not read products from the database.", ex);
        }
    }

    private Product build(String name, String category, double price, int quantity,
                          int reorderLevel, String expiry) throws InventoryException {
        if (name == null || name.isBlank()) {
            throw new InventoryException("Product name is required.");
        }
        if (category == null || category.isBlank()) {
            throw new InventoryException("Category is required.");
        }
        if (price <= 0) {
            throw new InventoryException("Price must be greater than 0.");
        }
        if (quantity < 0) {
            throw new InventoryException("Quantity cannot be negative.");
        }
        if (reorderLevel < 0) {
            throw new InventoryException("Reorder level cannot be negative.");
        }

        String cleanExpiry = expiry == null ? "" : expiry.trim();
        if (!cleanExpiry.isEmpty() && !cleanExpiry.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new InventoryException("Expiry date must look like 2026-12-31, or be left empty.");
        }

        Product product;
        if (cleanExpiry.isEmpty()) {
            product = new Product();
        } else {
            PerishableProduct perishable = new PerishableProduct();
            perishable.setExpiryDate(cleanExpiry);
            product = perishable;
        }
        product.setName(name.trim());
        product.setCategory(category.trim());
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setReorderLevel(reorderLevel);
        return product;
    }

    private InventoryException databaseError(String message, SQLException ex) {
        LOG.log(Level.SEVERE, message, ex);
        return new InventoryException(message + " " + ex.getMessage(), ex);
    }
}
