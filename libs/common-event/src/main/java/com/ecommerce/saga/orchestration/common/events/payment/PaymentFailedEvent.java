package com.ecommerce.saga.orchestration.common.events.payment;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentFailedEvent(
    String sagaId,
    String orderId,
    BigDecimal amount,
    String reason,
    @JsonFormat(shape = JsonFormat.Shape.STRING) Instant occurredAt
) {
}
