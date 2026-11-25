package com.ecommerce.saga.orchestration.common.events.order;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record OrderCancelledEvent(
    String sagaId,
    String orderId,
    String reason,
    @JsonFormat(shape = JsonFormat.Shape.STRING) Instant occurredAt
) {
}
