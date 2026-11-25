package com.ecommerce.saga.orchestration.common.events.order;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderCreatedEvent(
    String orderId,
    String userId,
    List<OrderItem> items,
    BigDecimal totalAmount,
    @JsonFormat(shape = JsonFormat.Shape.STRING) Instant occurredAt
) {
  public record OrderItem(
      String productId,
      int quantity,
      BigDecimal unitPrice
  ){}
}
