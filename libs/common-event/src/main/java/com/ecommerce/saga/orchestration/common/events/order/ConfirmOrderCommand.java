package com.ecommerce.saga.orchestration.common.events.order;

public record ConfirmOrderCommand(
    String sagaId,
    String orderId
) {
}
