package com.ecommerce.saga.orchestration.common.events.order;

public record PaymentFailOrderCommand(
    String orderId,
    String reason
) {
}
