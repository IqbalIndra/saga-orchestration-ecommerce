package com.ecommerce.saga.orchestration.orchestrator.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class OrderPlacementSagaTest {

  private final Instant now = Instant.now();

  private OrderPlacementSaga domain;

  @BeforeEach
  void setUp() {
    domain = OrderPlacementSaga.create("id", "ord");
  }

  @Test
  void shouldMarkPaymentProcessing() {
    domain.markPaymentProcessed("pay1");

    assertEquals("pay1", domain.getPaymentId());
    assertEquals(SagaState.PAYMENT_PROCESSED, domain.getState());
  }

  @Test
  void shouldMarkInventoryReserving() {
    domain.markInventoryReserved("reserv1");

    assertEquals("reserv1", domain.getReservationId());
    assertEquals(SagaState.INVENTORY_RESERVED, domain.getState());
  }

  @Test
  void shouldStartCompensatingInventory() {
    domain.startCompensatingInventory("failed payment");
    assertEquals("failed payment", domain.getFailureReason());
    assertEquals(SagaState.COMPENSATING_INVENTORY, domain.getState());
  }

  @Test
  void shouldStartCompensatingPayment() {
    domain.startCompensationPayment("failed shipping");

    assertEquals("failed shipping", domain.getFailureReason());
    assertEquals(SagaState.COMPENSATING_PAYMENT, domain.getState());
  }

  @Test
  void shouldMarkConfirming() {
    assertNull(domain.getCompletedAt());

    domain.markAsConfirmed();

    assertEquals(SagaState.CONFIRMED, domain.getState());
    assertNotNull(domain.getCompletedAt());
    assertTrue(now.isBefore(domain.getCompletedAt()));
  }

  @Test
  void shouldMarkCompensating() {
    assertNull(domain.getCompletedAt());

    domain.markAsCompensated();

    assertEquals(SagaState.COMPENSATED, domain.getState());
    assertNotNull(domain.getCompletedAt());
    assertTrue(now.isBefore(domain.getCompletedAt()));
  }

  @Test
  void shouldMarkFailing() {
    assertNull(domain.getCompletedAt());
    domain.markAsFailed();

    assertEquals(SagaState.FAILED, domain.getState());
    assertNotNull(domain.getCompletedAt());
    assertTrue(now.isBefore(domain.getCompletedAt()));
  }

  @Test
  void shouldFailing() {
    domain.fail("ChargePayment", "decline payment gateway");

    assertNotNull(domain.getFailureReason());
    assertNotNull(domain.getFailedStep());

    assertEquals("ChargePayment", domain.getFailedStep());
    assertEquals("decline payment gateway", domain.getFailureReason());
  }
}