package com.ecommerce.saga.orchestration.common.events.order;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record OrderConfirmedEvent(
    String sagaId,
    String orderId,
    @JsonFormat(shape = JsonFormat.Shape.STRING) Instant occurredAt
) {
}
