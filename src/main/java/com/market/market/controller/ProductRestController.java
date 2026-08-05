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
@CrossOrigin(origins = "*") // Allows direct communication with your local JavaScript client
public class ProductRestController {

    private final ProductService productService;

    public ProductRestController(ProductService productService) {
        this.productService = productService;
    }

    // ===================================================================
    // 1. CREATE (Create a new product)
    // ===================================================================
    // POST http://localhost:8080/api/products
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Product product) {
        try {
            Product newProduct = productService.create(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
        } catch (IllegalArgumentException e) {
            // If the EAN already exists, returns a 400 Bad Request error with the message
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ===================================================================
    // 2. READ (Paginated list or single product by EAN)
    // ===================================================================
    // GET http://localhost:8080/api/products?page=0&size=20
    @GetMapping
    public ResponseEntity<Page<Product>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> paginatedProducts = productService.getAll(pageable);
        return ResponseEntity.ok(paginatedProducts);
    }

    // GET http://localhost:8080/api/products/ean/75010553
    @GetMapping("/ean/{ean}")
    public ResponseEntity<Product> getByEan(@PathVariable String ean) {
        return productService.getByEan(ean)
                .map(product -> ResponseEntity.ok(product))          // 200 OK if found
                .orElse(ResponseEntity.notFound().build());         // 404 Not Found if missing
    }

    // ===================================================================
    // 3. UPDATE (Update product by EAN)
    // ===================================================================
    // PUT http://localhost:8080/api/products/ean/75010553
    @PutMapping("/ean/{ean}")
    public ResponseEntity<?> updateByEan(@PathVariable String ean, @RequestBody Product product) {
        try {
            Product updatedProduct = productService.updateByEan(ean, product);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ===================================================================
    // 4. DELETE (Delete product by EAN)
    // ===================================================================
    // DELETE http://localhost:8080/api/products/ean/75010553
    @DeleteMapping("/ean/{ean}")
    public ResponseEntity<?> deleteByEan(@PathVariable String ean) {
        try {
            productService.deleteByEan(ean);
            return ResponseEntity.noContent().build(); // 204 No Content if successfully deleted
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
