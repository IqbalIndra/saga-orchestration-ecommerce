package com.ecommerce.saga.orchestration.order.domain.exception;

public class InvalidOutboxEventStateException extends RuntimeException {
  public InvalidOutboxEventStateException(String msg) {
    super(msg);
  }
}
