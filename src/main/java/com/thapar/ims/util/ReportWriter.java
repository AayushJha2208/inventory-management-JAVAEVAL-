package com.thapar.ims.util;

import com.thapar.ims.model.Product;
import com.thapar.ims.model.Sale;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates comprehensive inventory reports in TXT, CSV (Excel),
 * and styled HTML formats using standard Java (no external heavy libraries required).
 */
public final class ReportWriter {

    private static final String[] HEADERS = {"ID", "Item Name", "Category", "Price (Rs)", "Qty", "Reorder", "Expiry", "Type", "Stock Value (Rs)"};

    private ReportWriter() { }

    public static List<String> writeAll(List<Product> products) throws IOException {
        File folder = AppPaths.reportsDirectory().toFile();
        Files.createDirectories(folder.toPath());

        List<String> paths = new ArrayList<>();
        paths.add(writeText(products, new File(folder, "inventory-report.txt")));
        paths.add(writeCsv(products, new File(folder, "inventory-report.csv")));
        paths.add(writeHtml(products, new File(folder, "inventory-report.html")));
        return paths;
    }

    public static List<String> writeSalesReport(List<Sale> sales) throws IOException {
        File folder = AppPaths.reportsDirectory().toFile();
        Files.createDirectories(folder.toPath());

        List<String> paths = new ArrayList<>();
        paths.add(writeSalesText(sales, new File(folder, "sales-history-report.txt")));
        paths.add(writeSalesCsv(sales, new File(folder, "sales-history-report.csv")));
        return paths;
    }

    private static String writeSalesText(List<Sale> sales, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("========================================================================================\n");
            writer.write("                             SALES TRANSACTIONS HISTORY REPORT                          \n");
            writer.write("========================================================================================\n");
            writer.write("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n");

            writer.write(String.format("%-8s | %-19s | %-20s | %-12s | %5s | %10s | %12s | %-15s\n",
                    "Sale ID", "Date & Time", "Product Name", "Category", "Qty", "Unit Price", "Total (Rs)", "Notes"));
            writer.write("-----------------------------------------------------------------------------------------------------------\n");

            double totalRevenue = 0;
            int totalUnits = 0;

            for (Sale s : sales) {
                totalRevenue += s.getTotalAmount();
                totalUnits += s.getQuantity();

                writer.write(String.format("%-8d | %-19s | %-20s | %-12s | %5d | %10.2f | %12.2f | %-15s\n",
                        s.getId(),
                        s.getSaleDate(),
                        truncate(s.getProductName(), 20),
                        truncate(s.getCategory() == null ? "" : s.getCategory(), 12),
                        s.getQuantity(),
                        s.getUnitPrice(),
                        s.getTotalAmount(),
                        truncate(s.getNotes(), 15)));
            }

            writer.write("-----------------------------------------------------------------------------------------------------------\n");
            writer.write(String.format("Total Sales Orders: %d  |  Total Units Sold: %d  |  Total Revenue: Rs %.2f\n",
                    sales.size(), totalUnits, totalRevenue));
            writer.write("===========================================================================================================\n");
        }
        return file.getAbsolutePath();
    }

    private static String writeSalesCsv(List<Sale> sales, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("Sale ID,Date Time,Product Name,Category,Quantity,Unit Price (Rs),Total Amount (Rs),Notes\n");
            double totalRevenue = 0;
            for (Sale s : sales) {
                totalRevenue += s.getTotalAmount();
                writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",%d,%.2f,%.2f,\"%s\"\n",
                        s.getId(),
                        s.getSaleDate(),
                        escapeCsv(s.getProductName()),
                        escapeCsv(s.getCategory()),
                        s.getQuantity(),
                        s.getUnitPrice(),
                        s.getTotalAmount(),
                        escapeCsv(s.getNotes())));
            }
            writer.write(String.format("\"TOTAL\",,\"\",\"\",0,0,%.2f,\"\"\n", totalRevenue));
        }
        return file.getAbsolutePath();
    }

    private static String writeText(List<Product> products, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("========================================================================================\n");
            writer.write("                           INVENTORY MANAGEMENT SYSTEM REPORT                           \n");
            writer.write("========================================================================================\n");
            writer.write("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n");

            writer.write(String.format("%-5s | %-20s | %-12s | %10s | %5s | %7s | %-12s | %-10s | %12s\n",
                    HEADERS[0], HEADERS[1], HEADERS[2], HEADERS[3], HEADERS[4], HEADERS[5], HEADERS[6], HEADERS[7], HEADERS[8]));
            writer.write("----------------------------------------------------------------------------------------\n");

            double totalValue = 0;
            int lowStockCount = 0;

            for (Product p : products) {
                totalValue += p.getStockValue();
                if (p.isLowStock()) lowStockCount++;

                writer.write(String.format("%-5d | %-20s | %-12s | %10.2f | %5d | %7d | %-12s | %-10s | %12.2f\n",
                        p.getId(),
                        truncate(p.getName(), 20),
                        truncate(p.getCategory(), 12),
                        p.getPrice(),
                        p.getQuantity(),
                        p.getReorderLevel(),
                        p.getExpiryDate().isEmpty() ? "None" : p.getExpiryDate(),
                        p.getType(),
                        p.getStockValue()));
            }

            writer.write("----------------------------------------------------------------------------------------\n");
            writer.write(String.format("Total Products: %d  |  Low Stock Alerts: %d  |  Total Inventory Value: Rs %.2f\n",
                    products.size(), lowStockCount, totalValue));
            writer.write("========================================================================================\n");
        }
        return file.getAbsolutePath();
    }

    private static String writeCsv(List<Product> products, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(String.join(",", HEADERS) + "\n");

            double totalValue = 0;
            for (Product p : products) {
                totalValue += p.getStockValue();
                writer.write(String.format("%d,\"%s\",\"%s\",%.2f,%d,%d,\"%s\",\"%s\",%.2f\n",
                        p.getId(),
                        escapeCsv(p.getName()),
                        escapeCsv(p.getCategory()),
                        p.getPrice(),
                        p.getQuantity(),
                        p.getReorderLevel(),
                        p.getExpiryDate().isEmpty() ? "None" : p.getExpiryDate(),
                        p.getType(),
                        p.getStockValue()));
            }
            writer.write(String.format("\"TOTAL\",,\"\",0,0,0,\"\",\"\",%.2f\n", totalValue));
        }
        return file.getAbsolutePath();
    }

    private static String writeHtml(List<Product> products, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            double totalValue = 0;
            int lowStockCount = 0;
            for (Product p : products) {
                totalValue += p.getStockValue();
                if (p.isLowStock()) lowStockCount++;
            }

            writer.write("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Inventory Report</title>");
            writer.write("<style>");
            writer.write("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 40px; color: #0F172A; }");
            writer.write("h1 { color: #166534; margin-bottom: 4px; }");
            writer.write(".meta { color: #64748B; font-size: 14px; margin-bottom: 24px; }");
            writer.write(".stats { display: flex; gap: 20px; margin-bottom: 24px; }");
            writer.write(".stat-card { background: #F0FDF4; border: 1px solid #BBF7D0; padding: 12px 20px; border-radius: 8px; }");
            writer.write(".stat-label { font-size: 11px; font-weight: bold; color: #166534; text-transform: uppercase; }");
            writer.write(".stat-val { font-size: 20px; font-weight: bold; color: #166534; }");
            writer.write("table { width: 100%; border-collapse: collapse; margin-top: 10px; }");
            writer.write("th { background: #F0FDF4; color: #166534; padding: 10px; text-align: left; border-bottom: 2px solid #CBD5E1; font-size: 13px; }");
            writer.write("td { padding: 9px 10px; border-bottom: 1px solid #E2E8F0; font-size: 13px; }");
            writer.write(".num { text-align: right; font-family: monospace; }");
            writer.write(".warning { color: #B45309; font-weight: bold; background: #FEF3C7; padding: 2px 6px; border-radius: 4px; }");
            writer.write(".badge { background: #F1F5F9; color: #334155; padding: 3px 8px; border-radius: 12px; font-size: 11px; }");
            writer.write("</style></head><body>");

            writer.write("<h1>Inventory Status Report</h1>");
            writer.write("<div class='meta'>Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</div>");

            writer.write("<div class='stats'>");
            writer.write("<div class='stat-card'><div class='stat-label'>Total Stock Value</div><div class='stat-val'>Rs " + String.format("%.2f", totalValue) + "</div></div>");
            writer.write("<div class='stat-card'><div class='stat-label'>SKU Count</div><div class='stat-val'>" + products.size() + " Items</div></div>");
            writer.write("<div class='stat-card'><div class='stat-label'>Low Stock Alerts</div><div class='stat-val'>" + lowStockCount + " Items</div></div>");
            writer.write("</div>");

            writer.write("<table><thead><tr>");
            for (String h : HEADERS) {
                boolean isNum = h.contains("Price") || h.contains("Qty") || h.contains("Reorder") || h.contains("Value");
                writer.write("<th class='" + (isNum ? "num" : "") + "'>" + h + "</th>");
            }
            writer.write("</tr></thead><tbody>");

            for (Product p : products) {
                writer.write("<tr>");
                writer.write("<td class='num'>#" + p.getId() + "</td>");
                writer.write("<td><b>" + p.getName() + "</b></td>");
                writer.write("<td><span class='badge'>" + p.getCategory() + "</span></td>");
                writer.write("<td class='num'>Rs " + String.format("%.2f", p.getPrice()) + "</td>");
                writer.write("<td class='num'>" + (p.isLowStock() ? "<span class='warning'>" + p.getQuantity() + "</span>" : p.getQuantity()) + "</td>");
                writer.write("<td class='num'>" + p.getReorderLevel() + "</td>");
                writer.write("<td>" + (p.getExpiryDate().isEmpty() ? "<span style='color:#94a3b8'>None</span>" : p.getExpiryDate()) + "</td>");
                writer.write("<td><span class='badge'>" + p.getType() + "</span></td>");
                writer.write("<td class='num'><b>Rs " + String.format("%.2f", p.getStockValue()) + "</b></td>");
                writer.write("</tr>");
            }

            writer.write("</tbody></table></body></html>");
        }
        return file.getAbsolutePath();
    }

    private static String escapeCsv(String s) {
        if (s == null) return "";
        return s.replace("\"", "\"\"");
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}
