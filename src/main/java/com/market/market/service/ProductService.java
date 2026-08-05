package com.market.market.service;

import com.market.market.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface ProductService {

    Product create(Product product);

    Page<Product> getAll(Pageable pageable);

    Optional<Product> getByEan(String ean);

    Product updateByEan(String ean, Product updatedData);

    void deleteByEan(String ean);
}
