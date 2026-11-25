package com.ecommerce.saga.orchestration.inventory.application.port.in;

import java.util.List;

public interface CheckStockUseCase {
  record StockCheckItem(
      String productId,
      int quantity
  ) {}

  record StockCheckResult(
      boolean allAvailable,
      List<ItemAvailability> items
  ) {
    public record ItemAvailability(
        String productId,
        int requested,
        int available,
        boolean sufficient
    ) {}
  }

  StockCheckResult checkStock(List<StockCheckItem> items);
}
