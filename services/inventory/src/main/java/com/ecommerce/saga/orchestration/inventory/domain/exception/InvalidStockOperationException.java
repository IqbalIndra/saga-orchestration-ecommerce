package com.ecommerce.saga.orchestration.inventory.domain.exception;

public class InvalidStockOperationException extends RuntimeException {
  public InvalidStockOperationException(String message) {
    super(message);
  }
}
