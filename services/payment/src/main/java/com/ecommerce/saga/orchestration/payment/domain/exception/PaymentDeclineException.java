package com.ecommerce.saga.orchestration.payment.domain.exception;

public class PaymentDeclineException extends RuntimeException {
  public PaymentDeclineException(String message) {
    super(message);
  }
}
