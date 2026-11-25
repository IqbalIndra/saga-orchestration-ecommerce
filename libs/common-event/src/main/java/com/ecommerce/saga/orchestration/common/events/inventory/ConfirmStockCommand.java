package com.ecommerce.saga.orchestration.common.events.inventory;

public record ConfirmStockCommand(
    String sagaId,
    String reservationId,
    String orderId
) {
}
