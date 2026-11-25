package com.ecommerce.saga.orchestration.order.adapter.out.messaging;

import com.ecommerce.saga.orchestration.common.events.order.*;
import com.ecommerce.saga.orchestration.order.application.port.out.OrderEventPublisher;
import com.ecommerce.saga.orchestration.order.domain.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KafkaOrderEventPublisher implements OrderEventPublisher {
  private final KafkaTemplate<String, Object> kafkaTemplate;
  @Override
  public void publish(String orderId, String userId, BigDecimal totalAmount, List<OrderItem> items) {
    var event = new OrderCreatedEvent(
        orderId,
        userId,
        items.stream()
            .map(i -> new OrderCreatedEvent.OrderItem(
                i.productId(),
                i.quantity(),
                i.price()
            )).toList(),
        totalAmount,
        Instant.now()
    );
    kafkaTemplate.send("order-created", orderId, event);
  }

  @Override
  public void publishOrderConfirmed(String sagaId, String orderId) {
    var event = new OrderConfirmedEvent(sagaId, orderId, Instant.now());
    kafkaTemplate.send("order-confirmed", sagaId, event);
  }

  @Override
  public void publishOrderCancelled(String sagaId, String orderId, String reason) {
    var event = new OrderCancelledEvent(sagaId, orderId, reason, Instant.now());
    kafkaTemplate.send("order-cancelled", sagaId, event);
  }
}
