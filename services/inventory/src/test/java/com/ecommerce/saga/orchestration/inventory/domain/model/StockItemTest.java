package com.ecommerce.saga.orchestration.inventory.domain.model;

import com.ecommerce.saga.orchestration.inventory.domain.exception.InsufficientStockException;
import com.ecommerce.saga.orchestration.inventory.domain.exception.InvalidQuantityException;
import com.ecommerce.saga.orchestration.inventory.domain.exception.InvalidStockOperationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class StockItemTest {

  @Test
  void shouldCanReserveStockItem() {
    var stock = StockItem.of()
        .availableQuantity(10)
        .build();

    assertTrue(stock.canReserve(9));
  }

  @Test
  void shouldReservingStockItem(){
    var stock = StockItem.of()
        .availableQuantity(10)
        .reservedQuantity(0)
        .build();

    stock.reserve(1);

    assertEquals(9, stock.getAvailableQuantity());
    assertEquals(1, stock.getReservedQuantity());
    assertTrue(stock.getUpdatedAt().isBefore(Instant.now()));
  }

  @Test
  void shouldThrowExceptionWhenReservingStockItem() {
    var stock = StockItem.of()
        .availableQuantity(10)
        .reservedQuantity(0)
        .build();

    assertThrows(InsufficientStockException.class, () -> {
      stock.reserve(11);
    });
  }

  @Test
  void shouldReleasingStockItem() {
    var stock = StockItem.of()
        .availableQuantity(9)
        .reservedQuantity(1)
        .build();

    stock.release(1);

    assertEquals(10, stock.getAvailableQuantity());
    assertEquals(0, stock.getReservedQuantity());
    assertTrue(stock.getUpdatedAt().isBefore(Instant.now()));
  }

  @Test
  void shouldThrowExceptionWhenReleasingStockItem() {
    var stock = StockItem.of()
        .availableQuantity(9)
        .reservedQuantity(1)
        .build();

    assertThrows(InvalidStockOperationException.class, () -> {
      stock.release(2);
    });
  }

  @Test
  void shouldConfirmingStockItem() {
    var stock = StockItem.of()
        .availableQuantity(9)
        .reservedQuantity(1)
        .build();

    stock.confirmReservation(1);

    assertEquals(0, stock.getReservedQuantity());
    assertTrue(stock.getUpdatedAt().isBefore(Instant.now()));
  }

  @Test
  void shouldThrowExceptionWhenConfirmingStockItem() {
    var stock = StockItem.of()
        .availableQuantity(9)
        .reservedQuantity(1)
        .build();

    assertThrows(InvalidStockOperationException.class, () -> {
      stock.confirmReservation(2);
    });
  }

  @Test
  void shouldAddingStockItem() {
    var stock = StockItem.of()
        .availableQuantity(2)
        .build();

    stock.addStock(2);

    assertEquals(4, stock.getAvailableQuantity());
    assertTrue(stock.getUpdatedAt().isBefore(Instant.now()));
  }

  @Test
  void shouldThrowExceptionWhenAddingStockItem() {
    var stock = StockItem.of()
        .availableQuantity(0)
        .build();

    assertThrows(InvalidQuantityException.class, () -> {
      stock.addStock(-1);
    });
  }

  @Test
  void shouldSetStockItem() {
    var stock = StockItem.of()
        .availableQuantity(0)
        .build();

    stock.setStock(2);
    assertEquals(2, stock.getAvailableQuantity());
    assertTrue(stock.getUpdatedAt().isBefore(Instant.now()));
  }

  @Test
  void shouldThrowExceptionWhenSetStockItem() {
    var stock = StockItem.of()
        .availableQuantity(0)
        .build();

    assertThrows(InvalidQuantityException.class, () -> {
      stock.setStock(-1);
    });
  }

  @Test
  void shouldCreatingStockItem() {
    var productId = "product1";
    var zeroStock = 0;

    var stock = StockItem.create(productId);

    assertEquals(productId, stock.getProductId());
    assertEquals(zeroStock, stock.getAvailableQuantity());
    assertEquals(zeroStock, stock.getReservedQuantity());
    assertNotNull(stock.getCreatedAt());
    assertNotNull(stock.getUpdatedAt());
  }


}