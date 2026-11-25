package com.ecommerce.saga.orchestration.inventory.adapter.out.persistence;

import com.ecommerce.saga.orchestration.inventory.adapter.out.persistence.entity.StockItemEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface JpaStockItemRepository extends MongoRepository<StockItemEntity,String> {
  Optional<StockItemEntity> findByProductId(String productId);
  List<StockItemEntity> findByProductIdIn(List<String> productIds);
}
