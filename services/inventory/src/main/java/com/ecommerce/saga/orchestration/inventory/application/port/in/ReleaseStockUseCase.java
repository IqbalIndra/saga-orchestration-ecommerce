package com.ecommerce.saga.orchestration.inventory.application.port.in;

public interface ReleaseStockUseCase {
  void release(String eventId, String sagaId, String orderId);
}
