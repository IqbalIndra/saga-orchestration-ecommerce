package com.ecommerce.saga.orchestration.payment.domain.model;

import java.time.Instant;

public record IdempotencyRecord(
    String key,
    String paymentId,
    String responseBody,
    Instant createdAt
) {
}
