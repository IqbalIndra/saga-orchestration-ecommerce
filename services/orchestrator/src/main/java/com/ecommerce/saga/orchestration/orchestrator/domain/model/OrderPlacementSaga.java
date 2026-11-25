package com.ecommerce.saga.orchestration.orchestrator.domain.model;

import java.time.Instant;

public class OrderPlacementSaga {
  private final String id;
  private SagaState state;
  private final String orderId;
  private String paymentId;
  private String reservationId;
  // Failure context
  private String failedStep;
  private String failureReason;

  private final Instant startedAt;
  private Instant completedAt;

  public OrderPlacementSaga(String id, SagaState state, String orderId, String paymentId, String reservationId,
                            String failedStep, String failureReason, Instant startedAt, Instant completedAt) {
    this.id = id;
    this.state = state;
    this.orderId = orderId;
    this.paymentId = paymentId;
    this.reservationId = reservationId;
    this.failedStep = failedStep;
    this.failureReason = failureReason;
    this.startedAt = startedAt;
    this.completedAt = completedAt;
  }

  public static OrderPlacementSaga create(String id, String orderId) {
    return new OrderPlacementSaga(id, SagaState.STARTED, orderId, null, null,
        null,null ,Instant.now(), null);
  }

  public void markPaymentProcessed(String paymentId) {
    this.paymentId = paymentId;
    this.state = SagaState.PAYMENT_PROCESSED;
  }

  public void markInventoryReserved(String reservationId) {
    this.reservationId = reservationId;
    this.state = SagaState.INVENTORY_RESERVED;
  }

  public void startCompensatingInventory(String reason) {
    this.failureReason = reason;
    this.state = SagaState.COMPENSATING_INVENTORY;
  }

  public void startCompensationPayment(String reason) {
    this.failureReason = reason;
    this.state = SagaState.COMPENSATING_PAYMENT;
  }

  public void markAsConfirmed() {
    this.state = SagaState.CONFIRMED;
    this.completedAt = Instant.now();
  }

  public void markAsCompensated() {
    this.state = SagaState.COMPENSATED;
    this.completedAt = Instant.now();
  }

  public void markAsFailed() {
    this.state = SagaState.FAILED;
    this.completedAt = Instant.now();
  }

  public void fail(String failedStep, String reason) {
    this.failedStep = failedStep;
    this.failureReason = reason;
  }

  public String getId() { return id; }
  public SagaState getState() { return state; }
  public String getOrderId() { return orderId; }
  public String getPaymentId() { return paymentId; }
  public String getReservationId() { return reservationId; }
  public String getFailedStep() { return failedStep; }
  public String getFailureReason() { return failureReason; }
  public Instant getStartedAt() { return startedAt; }
  public Instant getCompletedAt() { return completedAt; }
}
