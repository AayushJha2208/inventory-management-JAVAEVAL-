package com.thapar.ims.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.thapar.ims.model.Product;
import com.thapar.ims.service.InventoryService;
import com.thapar.ims.util.AppLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Built-in lightweight HTTP server serving the Web Dashboard
 * and providing a REST API backed by the SQLite database.
 */
public class WebServer {

    private static final Logger LOG = AppLogger.get(WebServer.class);
    private static HttpServer server;
    private static int activePort = 8080;

    public static synchronized int start(InventoryService service, int port) {
        if (server != null) {
            return activePort;
        }

        int targetPort = port;
        for (int attempt = 0; attempt < 5; attempt++) {
            try {
                server = HttpServer.create(new InetSocketAddress(targetPort), 0);
                activePort = targetPort;
                break;
            } catch (IOException e) {
                targetPort++;
            }
        }

        if (server == null) {
            LOG.warning("Could not bind HTTP server on ports " + port + "-" + targetPort);
            return -1;
        }

        server.createContext("/", new StaticHandler());
        server.createContext("/api/products", new ProductsHandler(service));
        server.createContext("/api/export", new ExportHandler(service));
        server.createContext("/api/stats", new StatsHandler(service));

        server.setExecutor(null);
        server.start();
        LOG.info("Inventory Web Server running at http://localhost:" + activePort);
        return activePort;
    }

    public static synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public static int getPort() {
        return activePort;
    }

    /** Serves the frontend web dashboard */
    private static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path) || "/index.html".equals(path) || path.isEmpty()) {
                File htmlFile = new File("src/main/resources/web/index.html");
                if (!htmlFile.exists()) {
                    htmlFile = new File("inventory_dashboard.html");
                }
                if (!htmlFile.exists()) {
                    htmlFile = new File("/Users/aayushjha/Downloads/inventory_dashboard.html");
                }

                if (htmlFile.exists()) {
                    byte[] bytes = Files.readAllBytes(htmlFile.toPath());
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                    return;
                }
            }

            String notFound = "Dashboard file not found";
            exchange.sendResponseHeaders(404, notFound.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(notFound.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    /** REST API for Products CRUD */
    private static class ProductsHandler implements HttpHandler {
        private final InventoryService service;

        public ProductsHandler(InventoryService service) {
            this.service = service;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

            String method = exchange.getRequestMethod();

            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                if ("GET".equalsIgnoreCase(method)) {
                    List<Product> products = service.getAllProducts();
                    StringBuilder json = new StringBuilder("[");
                    for (int i = 0; i < products.size(); i++) {
                        Product p = products.get(i);
                        if (i > 0) json.append(",");
                        json.append(productToJson(p));
                    }
                    json.append("]");
                    sendJson(exchange, 200, json.toString());
                } else if ("POST".equalsIgnoreCase(method)) {
                    String body = readBody(exchange.getRequestBody());
                    Map<String, String> data = parseJsonOrForm(body);
                    String name = data.getOrDefault("name", "").trim();
                    String category = data.getOrDefault("category", "Other");
                    double price = Double.parseDouble(data.getOrDefault("price", "0"));
                    int quantity = Integer.parseInt(data.getOrDefault("quantity", "0"));
                    int reorder = Integer.parseInt(data.getOrDefault("reorder", "5"));
                    String expiry = data.getOrDefault("expiry", "");

                    service.addProduct(name, category, price, quantity, reorder, expiry);
                    sendJson(exchange, 200, "{\"success\":true,\"message\":\"Product added\"}");
                } else if ("PUT".equalsIgnoreCase(method)) {
                    String body = readBody(exchange.getRequestBody());
                    Map<String, String> data = parseJsonOrForm(body);
                    int id = Integer.parseInt(data.getOrDefault("id", "0"));
                    String name = data.getOrDefault("name", "").trim();
                    String category = data.getOrDefault("category", "Other");
                    double price = Double.parseDouble(data.getOrDefault("price", "0"));
                    int quantity = Integer.parseInt(data.getOrDefault("quantity", "0"));
                    int reorder = Integer.parseInt(data.getOrDefault("reorder", "5"));
                    String expiry = data.getOrDefault("expiry", "");

                    service.updateProduct(id, name, category, price, quantity, reorder, expiry);
                    sendJson(exchange, 200, "{\"success\":true,\"message\":\"Product updated\"}");
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    String query = exchange.getRequestURI().getQuery();
                    Map<String, String> params = parseQuery(query);
                    int id = Integer.parseInt(params.getOrDefault("id", "0"));
                    service.deleteProduct(id);
                    sendJson(exchange, 200, "{\"success\":true,\"message\":\"Product deleted\"}");
                } else {
                    sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                }
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "API Error: " + ex.getMessage(), ex);
                sendJson(exchange, 400, "{\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    private static class ExportHandler implements HttpHandler {
        private final InventoryService service;

        public ExportHandler(InventoryService service) {
            this.service = service;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            try {
                List<String> paths = service.exportReports();
                StringBuilder json = new StringBuilder("{\"success\":true,\"files\":[");
                for (int i = 0; i < paths.size(); i++) {
                    if (i > 0) json.append(",");
                    json.append("\"").append(escapeJson(paths.get(i))).append("\"");
                }
                json.append("]}");
                sendJson(exchange, 200, json.toString());
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    private static class StatsHandler implements HttpHandler {
        private final InventoryService service;

        public StatsHandler(InventoryService service) {
            this.service = service;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            try {
                List<Product> all = service.getAllProducts();
                double total = 0;
                int low = 0;
                for (Product p : all) {
                    total += p.getStockValue();
                    if (p.isLowStock()) low++;
                }
                String json = String.format("{\"totalValue\":%.2f,\"skuCount\":%d,\"lowStockCount\":%d}",
                        total, all.size(), low);
                sendJson(exchange, 200, json);
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String productToJson(Product p) {
        return String.format(
                "{\"id\":%d,\"name\":\"%s\",\"category\":\"%s\",\"price\":%.2f,\"quantity\":%d,\"reorder\":%d,\"expiry\":\"%s\",\"type\":\"%s\",\"stockValue\":%.2f,\"isLowStock\":%b}",
                p.getId(),
                escapeJson(p.getName()),
                escapeJson(p.getCategory()),
                p.getPrice(),
                p.getQuantity(),
                p.getReorderLevel(),
                escapeJson(p.getExpiryDate()),
                escapeJson(p.getType()),
                p.getStockValue(),
                p.isLowStock()
        );
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static String readBody(InputStream is) throws IOException {
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static Map<String, String> parseJsonOrForm(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.trim().isEmpty()) return map;

        String trimmed = body.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            // Simple key-value JSON extractor
            String inner = trimmed.substring(1, trimmed.length() - 1);
            String[] pairs = inner.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            for (String pair : pairs) {
                String[] kv = pair.split(":", 2);
                if (kv.length == 2) {
                    String k = kv[0].trim().replace("\"", "");
                    String v = kv[1].trim().replace("\"", "");
                    map.put(k, v);
                }
            }
        } else {
            // URL Encoded form
            String[] pairs = trimmed.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                    String v = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                    map.put(k, v);
                }
            }
        }
        return map;
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return map;
    }
}
