package com.ecommerce.saga.orchestration.order.domain;

import java.math.BigDecimal;

public record OrderItem(
    String productId,
    int quantity,
    BigDecimal price
) {
  public BigDecimal subTotal() {
    return price.multiply(BigDecimal.valueOf(quantity));
  }
}
