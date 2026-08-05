package com.market.market.controller;

import com.market.market.model.Order;
import com.market.market.service.ShoppingCartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shopping-cart")
@CrossOrigin(origins = "*") // Permite la conexión nativa de JavaScript sin trabas de CORS
public class ShoppingCartRestController {

    private final ShoppingCartService shoppingCartService;

    public ShoppingCartRestController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    // Endpoint ejecutado cada vez que el lector escanea un EAN:
    // POST http://localhost:8080/api/shopping-cart/scan?ean=75010553
    @PostMapping("/scan")
    public ResponseEntity<Order> scanProduct(@RequestParam String ean) {
        shoppingCartService.scanProduct(ean);
        return ResponseEntity.ok(shoppingCartService.getActiveShoppingCart());
    }

    // GET http://localhost:8080/api/shopping-cart
    @GetMapping
    public ResponseEntity<Order> getActiveShoppingCart() {
        return ResponseEntity.ok(shoppingCartService.getActiveShoppingCart());
    }

    // Procesa el checkout final y guarda la orden en MongoDB
    // POST http://localhost:8080/api/shopping-cart/checkout
    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout() {
        Order completedOrder = shoppingCartService.checkout();
        return ResponseEntity.ok(completedOrder);
    }

    // DELETE http://localhost:8080/api/shopping-cart
    @DeleteMapping
    public ResponseEntity<Void> clearShoppingCart() {
        shoppingCartService.clearShoppingCart();
        return ResponseEntity.noContent().build();
    }
}
