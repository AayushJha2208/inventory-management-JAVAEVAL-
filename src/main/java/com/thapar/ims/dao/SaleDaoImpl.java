package com.thapar.ims.dao;

import com.thapar.ims.model.Sale;
import com.thapar.ims.util.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation for persisting sales records into SQLite.
 */
public class SaleDaoImpl implements SaleDao {

    @Override
    public void insert(Sale sale) throws SQLException {
        String sql = """
                INSERT INTO sales (product_id, product_name, category, quantity, unit_price, total_amount, sale_date, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sale.getProductId());
            ps.setString(2, sale.getProductName());
            ps.setString(3, sale.getCategory());
            ps.setInt(4, sale.getQuantity());
            ps.setDouble(5, sale.getUnitPrice());
            ps.setDouble(6, sale.getTotalAmount());
            ps.setString(7, sale.getSaleDate());
            ps.setString(8, sale.getNotes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    sale.setId(keys.getInt(1));
                }
            }
        }
    }

    @Override
    public List<Sale> findAll() throws SQLException {
        String sql = "SELECT id, product_id, product_name, category, quantity, unit_price, total_amount, sale_date, notes FROM sales ORDER BY id DESC";
        List<Sale> list = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extract(rs));
            }
        }
        return list;
    }

    @Override
    public List<Sale> findByProductId(int productId) throws SQLException {
        String sql = "SELECT id, product_id, product_name, category, quantity, unit_price, total_amount, sale_date, notes FROM sales WHERE product_id = ? ORDER BY id DESC";
        List<Sale> list = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extract(rs));
                }
            }
        }
        return list;
    }

    @Override
    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0.0) FROM sales";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    @Override
    public int getTotalQuantitySold() throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM sales";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Sale extract(ResultSet rs) throws SQLException {
        return new Sale(
                rs.getInt("id"),
                rs.getInt("product_id"),
                rs.getString("product_name"),
                rs.getString("category"),
                rs.getInt("quantity"),
                rs.getDouble("unit_price"),
                rs.getDouble("total_amount"),
                rs.getString("sale_date"),
                rs.getString("notes")
        );
    }
}
