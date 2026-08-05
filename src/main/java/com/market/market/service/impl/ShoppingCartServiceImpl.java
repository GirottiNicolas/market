package com.market.market.service.impl;

import com.market.market.dao.OrderDaoNoSQL;
import com.market.market.model.Product;
import com.market.market.model.Order;
import com.market.market.service.ProductService;
import com.market.market.service.ShoppingCartService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ProductService productService; // Busca catálogos en PostgreSQL
    private final OrderDaoNoSQL orderDao;       // Almacena órdenes finales en MongoDB

    // Carrito de compras activo en memoria
    private Order currentShoppingCart = new Order();

    public ShoppingCartServiceImpl(ProductService productService, OrderDaoNoSQL orderDao) {
        this.productService = productService;
        this.orderDao = orderDao;
    }

    @Override
    public void scanProduct(String ean) {
        // 1. Validar existencia del código de barras en PostgreSQL
        Product product = productService.getByEan(ean)
                .orElseThrow(() -> new RuntimeException("Product with EAN " + ean + " not found in store catalog."));

        // 2. Verificar si el artículo ya se encuentra en el carrito de compras actual
        Optional<Order.ShoppingCartItem> existingItemOpt = currentShoppingCart.getItems().stream()
                .filter(item -> item.getEan().equals(ean))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            Order.ShoppingCartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + 1);
            existingItem.setSubtotal(existingItem.getPrice() * existingItem.getQuantity());
        } else {
            Order.ShoppingCartItem newItem = new Order.ShoppingCartItem(
                    product.getEan(),
                    product.getDescription(),
                    product.getPrice(),
                    1,
                    product.getPrice()
            );
            currentShoppingCart.getItems().add(newItem);
        }

        // 3. Actualizar el total general acumulado del carrito
        currentShoppingCart.calculateTotal();
    }

    @Override
    public Order getActiveShoppingCart() {
        return this.currentShoppingCart;
    }

    @Override
    public Order checkout() {
        if (currentShoppingCart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot process checkout. Shopping cart is empty.");
        }

        currentShoppingCart.setOrderDate(LocalDateTime.now());

        // Persistir el documento JSON final en MongoDB
        Order finalizedOrder = orderDao.save(currentShoppingCart);

        // Reiniciar el carrito para la próxima transacción
        clearShoppingCart();

        return finalizedOrder;
    }

    @Override
    public void clearShoppingCart() {
        this.currentShoppingCart = new Order();
    }
}
