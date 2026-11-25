package com.ecommerce.saga.orchestration.inventory.application.port.out;

import java.math.BigDecimal;
import java.util.List;

public interface InventoryEventPublisher {
  void publishStockReserved(String sagaId, String reservationId, String orderId, BigDecimal totalAmount, List<ReservedItem> items);
  void publishStockReservationFailed(String sagaId, String orderId, String reason);
  void publishStockReleased(String sagaId, String orderId);

  record ReservedItem(String productId, int quantity) {}
}
