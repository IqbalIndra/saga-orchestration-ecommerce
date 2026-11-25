package com.ecommerce.saga.orchestration.order.domain.exception;

public class InvalidOrderStateException extends RuntimeException {
  public InvalidOrderStateException(String msg) {
    super(msg);
  }
}
