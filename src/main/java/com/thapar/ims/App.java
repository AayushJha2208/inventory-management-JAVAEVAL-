package com.thapar.ims;

import com.thapar.ims.exception.InventoryException;
import com.thapar.ims.model.Product;
import com.thapar.ims.service.InventoryService;
import com.thapar.ims.service.InventoryServiceImpl;
import com.thapar.ims.ui.InventoryFrame;
import com.thapar.ims.util.AppLogger;
import com.thapar.ims.util.Database;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Program entry. Opens the inventory window.
 * Pass the argument "check" to print a short logic test without the window.
 * Opens the desktop inventory window.
 */
public class App {

    private static final Logger LOG = AppLogger.get(App.class);

    public static void main(String[] args) {
        LOG.info("Inventory Management System started");
        try {
            Database.init();
            InventoryService service = new InventoryServiceImpl();

            if (args.length > 0 && "check".equals(args[0])) {
                runCheck(service);
                return;
            }
            SwingUtilities.invokeLater(() -> new InventoryFrame(service).setVisible(true));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Startup failed", ex);
            JOptionPane.showMessageDialog(null,
                    "Could not start the application.\n" + ex.getMessage(),
                    "Startup error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Prints add, search, low-stock, bad input, and file export so we can see the flow. */
    private static void runCheck(InventoryService service) throws InventoryException {
        service.addProduct("Demo Soap", "Other", 30, 12, 4, "");
        System.out.println("--- All products ---");
        for (Product product : service.getAllProducts()) {
            System.out.println(product + " | expiry=" + product.getExpiryDate()
                    + " | low=" + product.isLowStock());
        }
        System.out.println("--- Search id of Milk using the list ---");
        for (Product product : service.searchByName("Milk")) {
            Product fromMap = service.searchById(product.getId());
            System.out.println("HashMap hit: " + fromMap);
        }
        System.out.println("--- Low stock ---");
        for (Product product : service.getLowStockProducts()) {
            System.out.println(product.getName() + " qty=" + product.getQuantity());
        }
        try {
            service.addProduct("", "Other", 10, 1, 1, "");
            System.out.println("ERROR: empty name was accepted");
        } catch (InventoryException ex) {
            System.out.println("Caught expected error: " + ex.getMessage());
        }
        System.out.println("Report files: " + service.exportReports());
        for (Product product : service.getAllProducts()) {
            if ("Demo Soap".equals(product.getName())) {
                service.deleteProduct(product.getId());
            }
        }
        System.out.println("CHECK OK");
    }
}
