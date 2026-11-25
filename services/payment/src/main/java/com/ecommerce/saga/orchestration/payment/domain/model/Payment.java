package com.ecommerce.saga.orchestration.payment.domain.model;

import com.ecommerce.saga.orchestration.payment.domain.exception.PaymentException;

import java.math.BigDecimal;
import java.time.Instant;

public class Payment {
  private final String id;
  private final String orderId;
  private final BigDecimal amount;
  private PaymentStatus status;
  private String failureReason;
  private final Instant createdAt;
  private Instant updatedAt;

  public Payment(String id, String orderId, BigDecimal amount, PaymentStatus status, String failureReason, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.orderId = orderId;
    this.amount = amount;
    this.status = status;
    this.failureReason = failureReason;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static Payment create(String orderId, BigDecimal amount) {
    return new Payment(null, orderId, amount, PaymentStatus.PENDING, null, Instant.now(), Instant.now());
  }

  public void process() {
    if (status != PaymentStatus.PENDING) {
      throw new PaymentException("Can only process PENDING payments, current:"+status);
    }
    status = PaymentStatus.COMPLETED;
    updatedAt = Instant.now();
  }

  public void fail(String reason) {
    if(status != PaymentStatus.PENDING) {
      throw new PaymentException("Can only fail PENDING payments, current:"+status);
    }
    status = PaymentStatus.FAILED;
    failureReason = reason;
    updatedAt = Instant.now();
  }

  public void refund() {
    if(status != PaymentStatus.COMPLETED) {
      throw new PaymentException("Can only refund COMPLETED payments, current:"+status);
    }
    status = PaymentStatus.REFUNDED;
    updatedAt = Instant.now();
  }

  public String getId() { return id; }
  public String getOrderId() { return orderId; }
  public BigDecimal getAmount() { return amount; }
  public PaymentStatus getStatus() { return status; }
  public String getFailureReason() { return failureReason; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
