package com.ecommerce.saga.orchestration.order.application.port.in;

import com.ecommerce.saga.orchestration.order.domain.Order;
import com.ecommerce.saga.orchestration.order.domain.OrderStatus;

public interface UpdateOrderUseCase {
  Order updateOrderStatus(UpdateOrderCommand command);

  record UpdateOrderCommand(
      String orderId,
      OrderStatus status
  ) {}
}
