package com.thapar.ims.dao;

import com.thapar.ims.model.PerishableProduct;
import com.thapar.ims.model.Product;
import com.thapar.ims.util.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation. Every SQL value goes through a ? placeholder
 * (PreparedStatement), so user text is not pasted into the query.
 */
public class ProductDaoImpl implements ProductDao {

    @Override
    public void insert(Product product) throws SQLException {
        String sql = """
                INSERT INTO products (name, category, price, quantity, reorder_level, expiry_date)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fill(ps, product);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    product.setId(keys.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Product product) throws SQLException {
        String sql = """
                UPDATE products
                SET name = ?, category = ?, price = ?, quantity = ?, reorder_level = ?, expiry_date = ?
                WHERE id = ?
                """;
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            fill(ps, product);
            ps.setInt(7, product.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Product findById(int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
                return null;
            }
        }
    }

    @Override
    public List<Product> findAll() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY name";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                products.add(map(rs));
            }
        }
        return products;
    }

    private void fill(PreparedStatement ps, Product product) throws SQLException {
        ps.setString(1, product.getName());
        ps.setString(2, product.getCategory());
        ps.setDouble(3, product.getPrice());
        ps.setInt(4, product.getQuantity());
        ps.setInt(5, product.getReorderLevel());
        String expiry = product.getExpiryDate();
        if (expiry == null || expiry.isBlank()) {
            ps.setString(6, null);
        } else {
            ps.setString(6, expiry);
        }
    }

    /**
     * If the row has an expiry date we create a PerishableProduct.
     * Otherwise we create a Product. The caller still stores it as Product
     * (polymorphism).
     */
    private Product map(ResultSet rs) throws SQLException {
        String expiry = rs.getString("expiry_date");
        Product product;
        if (expiry == null || expiry.isBlank()) {
            product = new Product();
        } else {
            PerishableProduct perishable = new PerishableProduct();
            perishable.setExpiryDate(expiry);
            product = perishable;
        }
        product.setId(rs.getInt("id"));
        product.setName(rs.getString("name"));
        product.setCategory(rs.getString("category"));
        product.setPrice(rs.getDouble("price"));
        product.setQuantity(rs.getInt("quantity"));
        product.setReorderLevel(rs.getInt("reorder_level"));
        return product;
    }
}
