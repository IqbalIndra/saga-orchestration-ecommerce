package com.ecommerce.saga.orchestration.order.domain.exception;

public class EmptyItemsException extends RuntimeException {
  public EmptyItemsException(String msg) {
    super(msg);
  }
}
