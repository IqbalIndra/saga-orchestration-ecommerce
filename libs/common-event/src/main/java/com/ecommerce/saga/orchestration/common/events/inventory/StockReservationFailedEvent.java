package com.ecommerce.saga.orchestration.common.events.inventory;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record StockReservationFailedEvent(
    String sagaId,
    String orderId,
    String productId,
    String reason,
    @JsonFormat(shape = JsonFormat.Shape.STRING) Instant occurredAt
) {
}
