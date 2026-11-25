package com.ecommerce.saga.orchestration.common.events.payment;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentProcessedEvent(
    String sagaId,
    String orderId,
    String paymentId,
    BigDecimal amount,
    String paymentMethod,
    @JsonFormat(shape = JsonFormat.Shape.STRING) Instant occurredAt
) {
}
