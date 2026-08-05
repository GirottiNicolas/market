package com.market.market.controller;

import com.market.market.model.Product;
import com.market.market.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductRestController {

    private final ProductService productService;

    public ProductRestController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        // Si el service lanza un error, Spring lo intercepta automáticamente afuera
        Product newProduct = productService.create(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
    }

    @GetMapping
    public ResponseEntity<Page<Product>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getAll(pageable));
    }

    @GetMapping("/ean/{ean}")
    public ResponseEntity<Product> getByEan(@PathVariable String ean) {
        return productService.getByEan(ean)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/ean/{ean}")
    public ResponseEntity<Product> updateByEan(@PathVariable String ean, @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateByEan(ean, product));
    }

    @DeleteMapping("/ean/{ean}")
    public ResponseEntity<Void> deleteByEan(@PathVariable String ean) {
        productService.deleteByEan(ean);
        return ResponseEntity.noContent().build();
    }
}
