package com.market.market.dao;

import com.market.market.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDaoNoSQL extends MongoRepository<Order, String> {
    // Provee operaciones CRUD nativas para MongoDB de forma automática
}
