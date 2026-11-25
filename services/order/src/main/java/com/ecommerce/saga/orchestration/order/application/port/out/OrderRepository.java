package com.ecommerce.saga.orchestration.order.application.port.out;

import com.ecommerce.saga.orchestration.order.domain.Order;

import java.util.Optional;

public interface OrderRepository {
  Order save(Order order);
  Optional<Order> findById(String orderId);
}
