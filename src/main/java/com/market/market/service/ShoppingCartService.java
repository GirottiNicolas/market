package com.market.market.service;

import com.market.market.model.Order;

public interface ShoppingCartService {
    void scanProduct(String ean);
    Order getActiveShoppingCart();
    Order checkout();
    void clearShoppingCart();
}
