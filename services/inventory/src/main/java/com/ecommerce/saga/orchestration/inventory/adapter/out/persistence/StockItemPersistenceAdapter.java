package com.ecommerce.saga.orchestration.inventory.adapter.out.persistence;

import com.ecommerce.saga.orchestration.inventory.adapter.out.persistence.entity.StockItemEntity;
import com.ecommerce.saga.orchestration.inventory.application.port.out.StockItemRepository;
import com.ecommerce.saga.orchestration.inventory.domain.model.StockItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StockItemPersistenceAdapter implements StockItemRepository {
  private final JpaStockItemRepository jpaStockItemRepository;

  @Override
  public StockItem save(StockItem stockItem) {
    return toDomain(jpaStockItemRepository.save(toEntity(stockItem)));
  }

  @Override
  public Optional<StockItem> findByProductId(String productId) {
    return jpaStockItemRepository.findByProductId(productId).map(this::toDomain);
  }

  @Override
  public List<StockItem> findAllByProductIds(List<String> productIds) {
    return jpaStockItemRepository.findByProductIdIn(productIds)
        .stream()
        .map(this::toDomain)
        .toList();
  }

  private StockItemEntity toEntity(StockItem stockItem) {
    return StockItemEntity.of()
        .id(stockItem.getId())
        .productId(stockItem.getProductId())
        .availableQuantity(stockItem.getAvailableQuantity())
        .reservedQuantity(stockItem.getReservedQuantity())
        .createdAt(stockItem.getCreatedAt())
        .updatedAt(stockItem.getUpdatedAt())
        .build();
  }

  private StockItem toDomain(StockItemEntity entity) {
    return StockItem.of()
        .id(entity.getId())
        .productId(entity.getProductId())
        .availableQuantity(entity.getAvailableQuantity())
        .reservedQuantity(entity.getReservedQuantity())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }
  
}
