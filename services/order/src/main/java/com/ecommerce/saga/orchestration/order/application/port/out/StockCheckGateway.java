package com.ecommerce.saga.orchestration.order.application.port.out;

import java.util.List;

public interface StockCheckGateway {
  record StockItem(String productId, int quantity) {}
  record StockCheckResult(boolean allAvailable) {}
  StockCheckResult checkStock(List<StockItem> items);
}
