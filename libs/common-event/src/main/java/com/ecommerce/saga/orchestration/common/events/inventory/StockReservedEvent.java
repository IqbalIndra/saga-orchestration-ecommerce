package com.ecommerce.saga.orchestration.common.events.inventory;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record StockReservedEvent(
    String sagaId,
    String orderId,
    String reservationId,
    BigDecimal totalAmount,
    List<ReservedItem> reservedItem,
    @JsonFormat(shape = JsonFormat.Shape.STRING) Instant occurredAt
) {
  public record ReservedItem(
      String productId,
      int quantity
  ){}
}
