package com.ecommerce.saga.orchestration.order.domain.exception;

public class OutOfStockException extends RuntimeException {
  public OutOfStockException(String msg) {
    super(msg);
  }
}
