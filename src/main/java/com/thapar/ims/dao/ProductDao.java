package com.thapar.ims.dao;

import com.thapar.ims.model.Product;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface: the contract for saving and reading products.
 * The service depends on this interface, not on SQL details.
 */
public interface ProductDao {

    void insert(Product product) throws SQLException;

    void update(Product product) throws SQLException;

    void delete(int id) throws SQLException;

    Product findById(int id) throws SQLException;

    List<Product> findAll() throws SQLException;
}
