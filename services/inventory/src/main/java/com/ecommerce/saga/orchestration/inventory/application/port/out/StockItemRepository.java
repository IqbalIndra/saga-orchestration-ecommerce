package com.ecommerce.saga.orchestration.inventory.application.port.out;

import com.ecommerce.saga.orchestration.inventory.domain.model.StockItem;

import java.util.List;
import java.util.Optional;

public interface StockItemRepository {
  StockItem save(StockItem stockItem);
  Optional<StockItem> findByProductId(String productId);
  List<StockItem> findAllByProductIds(List<String> productIds);
}
