package com.ecommerce.saga.orchestration.inventory.domain.exception;

public class InvalidQuantityException extends RuntimeException {
  public InvalidQuantityException(String message) {
    super(message);
  }
}
