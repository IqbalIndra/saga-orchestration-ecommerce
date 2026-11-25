package com.ecommerce.saga.orchestration.inventory.domain.exception;

public class InsufficientStockException extends RuntimeException {
  public InsufficientStockException(String message) {
    super(message);
  }
}
