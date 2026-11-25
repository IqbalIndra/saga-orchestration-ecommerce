package com.ecommerce.saga.orchestration.order.application.port.out;

import com.ecommerce.saga.orchestration.order.domain.OrderItem;

import java.math.BigDecimal;
import java.util.List;

public interface OrderEventPublisher {
  void publish(String orderId, String userId, BigDecimal totalAmount, List<OrderItem> items);
  void publishOrderConfirmed(String sagaId, String orderId);
  void publishOrderCancelled(String sagaId, String orderId, String reason);
}
