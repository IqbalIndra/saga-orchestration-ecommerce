package com.ecommerce.saga.orchestration.common.events.inventory;

public record ReleaseStockCommand(
    String sagaId,
    String reservationId,
    String orderId
) {
}
