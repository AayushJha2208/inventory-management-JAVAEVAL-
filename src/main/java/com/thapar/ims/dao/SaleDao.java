package com.thapar.ims.dao;

import com.thapar.ims.model.Sale;

import java.sql.SQLException;
import java.util.List;

/**
 * Contract for persisting and retrieving sales transactions.
 */
public interface SaleDao {

    void insert(Sale sale) throws SQLException;

    List<Sale> findAll() throws SQLException;

    List<Sale> findByProductId(int productId) throws SQLException;

    double getTotalRevenue() throws SQLException;

    int getTotalQuantitySold() throws SQLException;
}
