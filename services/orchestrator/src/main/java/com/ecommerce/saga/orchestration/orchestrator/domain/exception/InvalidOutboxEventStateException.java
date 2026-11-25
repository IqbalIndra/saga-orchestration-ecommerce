package com.ecommerce.saga.orchestration.orchestrator.domain.exception;

public class InvalidOutboxEventStateException extends RuntimeException {
  public InvalidOutboxEventStateException(String msg) {
    super(msg);
  }
}
