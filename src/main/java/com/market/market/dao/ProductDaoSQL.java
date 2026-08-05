package com.market.market.dao;

import com.market.market.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProductDaoSQL extends JpaRepository<Product, Long> {

    // Automatically generates the query: SELECT * FROM products WHERE ean = ?
    Optional<Product> findByEan(String ean);
}
