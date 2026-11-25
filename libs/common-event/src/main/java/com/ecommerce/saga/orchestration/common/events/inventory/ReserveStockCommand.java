package com.ecommerce.saga.orchestration.common.events.inventory;

import java.math.BigDecimal;
import java.util.List;

public record ReserveStockCommand(
    String sagaId,
    String orderId,
    BigDecimal totalAmount,
    List<OrderItem> items
) {
  public record OrderItem(String productId, int quantity){}
}
