package com.market.market.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "orders") // Guarda el documento en la coleccion de MongoDB
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    private String id; // ID alfanumérico nativo de MongoDB

    private LocalDateTime orderDate = LocalDateTime.now();
    private List<ShoppingCartItem> items = new ArrayList<>();
    private Double totalAmount = 0.0;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShoppingCartItem {
        private String ean;
        private String description;
        private Double price;
        private Integer quantity;
        private Double subtotal;
    }

    public void calculateTotal() {
        this.totalAmount = this.items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
}
