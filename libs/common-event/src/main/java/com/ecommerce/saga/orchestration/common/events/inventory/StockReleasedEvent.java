package com.ecommerce.saga.orchestration.common.events.inventory;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record StockReleasedEvent(
    String sagaId,
    String orderId,
    @JsonFormat(shape = JsonFormat.Shape.STRING) Instant occurredAt
) {
}
