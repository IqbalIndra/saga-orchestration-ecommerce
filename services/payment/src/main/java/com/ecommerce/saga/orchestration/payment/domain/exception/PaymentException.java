package com.ecommerce.saga.orchestration.payment.domain.exception;

public class PaymentException extends RuntimeException {
  public PaymentException(String msg) {
    super(msg);
  }
}
