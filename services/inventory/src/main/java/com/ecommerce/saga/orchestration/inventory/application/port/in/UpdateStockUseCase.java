package com.ecommerce.saga.orchestration.inventory.application.port.in;

import java.util.List;
import java.util.Map;

public interface UpdateStockUseCase {
  void setStock(String productId, int quantity);
  Map<String, Integer> getStock(List<String> productIds);
}
