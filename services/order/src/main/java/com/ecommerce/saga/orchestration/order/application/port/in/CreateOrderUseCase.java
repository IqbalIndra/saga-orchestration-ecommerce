package com.ecommerce.saga.orchestration.order.application.port.in;

import com.ecommerce.saga.orchestration.order.domain.Order;

import java.math.BigDecimal;
import java.util.List;

public interface CreateOrderUseCase {
  Order createOrder(CreateOrderCommand command);

  record CreateOrderCommand(
      String customerId,
      List<OrderItemCommand> items
  ){
    public record OrderItemCommand(
        String productId,
        int quantity,
        BigDecimal price
    ){}
  }
}
