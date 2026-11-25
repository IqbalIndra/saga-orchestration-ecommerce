package com.ecommerce.saga.orchestration.inventory.adapter.out.persistence.entity;

import com.ecommerce.saga.orchestration.inventory.domain.exception.InsufficientStockException;
import com.ecommerce.saga.orchestration.inventory.domain.exception.InvalidQuantityException;
import com.ecommerce.saga.orchestration.inventory.domain.exception.InvalidStockOperationException;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "stock_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "of")
public class StockItemEntity {
  private String id;

  private String productId;
  private int availableQuantity;
  private int reservedQuantity;

  @CreatedDate
  private Instant createdAt;
  @LastModifiedDate
  private Instant updatedAt;

  private boolean canReserve(int quantity) {
    return availableQuantity >= quantity;
  }

  public void reserve(int quantity) {
    if(!canReserve(quantity)) {
      throw new InsufficientStockException(
          "Insufficient stock for productId=%s : available=%d, reserved=%d".formatted(
              productId,availableQuantity,reservedQuantity
      ));
    }
    availableQuantity-=quantity;
    reservedQuantity+=quantity;
    updatedAt = Instant.now();
  }

  public void release(int quantity) {
    if(reservedQuantity < quantity) {
      throw new InvalidStockOperationException(
          "Cannot release stock for productId=%s".formatted(productId)
      );
    }
    availableQuantity+=quantity;
    reservedQuantity-=quantity;
    updatedAt = Instant.now();
  }

  public void confirmReservation(int quantity) {
    if(reservedQuantity < quantity) {
      throw new InvalidStockOperationException(
          "Cannot confirm stock for productId=%s".formatted(productId)
      );
    }
    reservedQuantity-=quantity;
    updatedAt = Instant.now();
  }

  public void addStock(int quantity) {
    if(quantity <= 0) {
      throw new InvalidQuantityException("Quantity must be positive");
    }
    availableQuantity+=quantity;
    updatedAt = Instant.now();
  }

  public void setStock(int quantity) {
    if(quantity <= 0) {
      throw new InvalidQuantityException("Quantity must be positive");
    }
    availableQuantity=quantity;
    updatedAt = Instant.now();
  }

  public static StockItemEntity create(String productId) {
    return StockItemEntity.of()
        .productId(productId)
        .availableQuantity(0)
        .reservedQuantity(0)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
  }

}
