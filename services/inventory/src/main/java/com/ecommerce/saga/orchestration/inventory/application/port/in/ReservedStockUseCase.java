package com.ecommerce.saga.orchestration.inventory.application.port.in;

import java.math.BigDecimal;
import java.util.List;

public interface ReservedStockUseCase {
  record ReservationItem(String productId, int quantity) {}
  record ReservationResult(boolean success, String reservationId, String errorMessage) {}

  ReservationResult reserve(String eventId, String sagaId, String orderId, BigDecimal totalAmount, List<ReservationItem> items);
}
