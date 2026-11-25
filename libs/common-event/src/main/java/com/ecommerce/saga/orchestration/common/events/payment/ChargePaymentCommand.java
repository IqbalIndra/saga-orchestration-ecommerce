package com.ecommerce.saga.orchestration.common.events.payment;

import java.math.BigDecimal;
import java.util.List;

public record ChargePaymentCommand(
    String sagaId,
    String orderId,
    BigDecimal totalAmount,
    List<ReservedItem> reservedItem
) {
  public record ReservedItem(String productId, int quantity)
  {}
}
