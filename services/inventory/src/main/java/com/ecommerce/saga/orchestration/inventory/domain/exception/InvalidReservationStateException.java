package com.ecommerce.saga.orchestration.inventory.domain.exception;

public class InvalidReservationStateException extends RuntimeException {
  public InvalidReservationStateException(String msg) {
    super(msg);
  }
}
