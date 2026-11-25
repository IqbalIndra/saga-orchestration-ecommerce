package com.ecommerce.saga.orchestration.order.application.port.in;

import com.ecommerce.saga.orchestration.order.domain.Order;

public interface GetOrderUseCase {
  Order getOrderById(String id);
}
