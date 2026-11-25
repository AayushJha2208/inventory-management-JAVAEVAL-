package com.thapar.ims.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Opens the SQLite database file and creates the table the first time we run.
 * JDBC flow used in class: DriverManager -> Connection -> Statement -> SQL.
 */
public final class Database {

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + AppPaths.dataDirectory().resolve("inventory.db").toAbsolutePath());
    }

    public static void init() throws SQLException {
        File folder = AppPaths.dataDirectory().toFile();
        if (!folder.exists()) {
            folder.mkdirs();
        }

        try (Connection con = getConnection();
             Statement st = con.createStatement()) {

            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS products (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        category TEXT NOT NULL,
                        price REAL NOT NULL,
                        quantity INTEGER NOT NULL,
                        reorder_level INTEGER NOT NULL,
                        expiry_date TEXT
                    )
                    """);

            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS sales (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        product_id INTEGER,
                        product_name TEXT NOT NULL,
                        category TEXT,
                        quantity INTEGER NOT NULL,
                        unit_price REAL NOT NULL,
                        total_amount REAL NOT NULL,
                        sale_date TEXT NOT NULL,
                        notes TEXT
                    )
                    """);

            // A few rows so the table is not empty on the first demo.
            try (var rs = st.executeQuery("SELECT COUNT(*) FROM products")) {
                rs.next();
                if (rs.getInt(1) == 0) {
                    st.executeUpdate("""
                            INSERT INTO products (name, category, price, quantity, reorder_level, expiry_date) VALUES
                            ('Notebook', 'Stationery', 40, 25, 10, NULL),
                            ('Pen Box', 'Stationery', 120, 15, 5, NULL),
                            ('USB Cable', 'Electronics', 199, 40, 5, NULL),
                            ('Milk 1L', 'Grocery', 55, 8, 10, '2026-12-01'),
                            ('Rice 5kg', 'Grocery', 320, 4, 6, '2027-01-15')
                            """);
                }
            }
        }
    }
}
