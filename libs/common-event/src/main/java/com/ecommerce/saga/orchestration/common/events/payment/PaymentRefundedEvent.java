package com.ecommerce.saga.orchestration.common.events.payment;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record PaymentRefundedEvent(
    String sagaId,
    String orderId,
    @JsonFormat(shape = JsonFormat.Shape.STRING) Instant occurredAt
) {
}
