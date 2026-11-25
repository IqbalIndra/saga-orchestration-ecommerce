package com.ecommerce.saga.orchestration.inventory.application.port.in;

public interface ConfirmStockUseCase {
  void confirm(String eventId, String orderId);
}
