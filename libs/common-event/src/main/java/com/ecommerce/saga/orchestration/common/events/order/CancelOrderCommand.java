package com.ecommerce.saga.orchestration.common.events.order;

public record CancelOrderCommand(
    String sagaId,
    String orderId,
    String reason
) {
}
