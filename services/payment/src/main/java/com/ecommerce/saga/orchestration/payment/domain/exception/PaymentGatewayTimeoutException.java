package com.ecommerce.saga.orchestration.payment.domain.exception;

public class PaymentGatewayTimeoutException extends RuntimeException {
  public PaymentGatewayTimeoutException(String message, Throwable ex) {
    super(message, ex);
  }
}
