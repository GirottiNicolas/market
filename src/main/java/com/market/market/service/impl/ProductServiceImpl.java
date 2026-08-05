package com.market.market.service.impl;

import com.market.market.dao.ProductDaoSQL;
import com.market.market.model.Product;
import com.market.market.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductDaoSQL productDao;

    // Constructor injection
    public ProductServiceImpl(ProductDaoSQL productDao) {
        this.productDao = productDao;
    }

    @Override
    public Product create(Product product) {
        if (productDao.findByEan(product.getEan()).isPresent()) {
            throw new IllegalArgumentException("The EAN code " + product.getEan() + " already exists.");
        }
        return productDao.save(product);
    }

    @Override
    public Page<Product> getAll(Pageable pageable) {
        // Executes an automatic SELECT using LIMIT and OFFSET in PostgreSQL
        return productDao.findAll(pageable);
    }

    @Override
    public Optional<Product> getByEan(String ean) {
        return productDao.findByEan(ean);
    }

    @Override
    public Product updateByEan(String ean, Product updatedData) {
        return productDao.findByEan(ean)
                .map(existingProduct -> {
                    existingProduct.setPrice(updatedData.getPrice());
                    existingProduct.setDescription(updatedData.getDescription());
                    existingProduct.setBrand(updatedData.getBrand());
                    existingProduct.setCategory(updatedData.getCategory());
                    existingProduct.setDimension(updatedData.getDimension());
                    existingProduct.setExpirationDate(updatedData.getExpirationDate());
                    existingProduct.setStock(updatedData.getStock());
                    return productDao.save(existingProduct);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with EAN: " + ean));
    }

    @Override
    public void deleteByEan(String ean) {
        Product product = productDao.findByEan(ean)
                .orElseThrow(() -> new RuntimeException("Cannot delete. Product not found with EAN: " + ean));
        productDao.delete(product);
    }
}
