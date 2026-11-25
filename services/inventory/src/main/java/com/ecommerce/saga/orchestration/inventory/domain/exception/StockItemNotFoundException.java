package com.ecommerce.saga.orchestration.inventory.domain.exception;

public class StockItemNotFoundException extends RuntimeException {
  public StockItemNotFoundException(String message) {
    super(message);
  }
}
