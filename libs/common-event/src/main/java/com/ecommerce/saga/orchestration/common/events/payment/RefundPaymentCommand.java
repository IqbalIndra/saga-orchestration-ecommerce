package com.ecommerce.saga.orchestration.common.events.payment;

public record RefundPaymentCommand(
    String sagaId,
    String paymentId,
    String orderId
) {
}
