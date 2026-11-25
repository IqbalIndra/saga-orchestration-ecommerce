package com.ecommerce.saga.orchestration.order.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
    String id,
    String userId,
    String status,
    List<OrderItemResponse> items,
    BigDecimal total,
    Instant createdAt
) {
  public record OrderItemResponse(
     String productId,
     int quantity,
     BigDecimal price,
     BigDecimal subTotal
  ) {}
}
