package com.ecommerce.saga.orchestration.payment.domain.model;

import com.ecommerce.saga.orchestration.payment.domain.exception.PaymentException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {
  private final String id = UUID.randomUUID().toString();
  private final Instant updatedAt = Instant.now();

  @Test
  void shouldProcessing() {
    var payment = createPayment(PaymentStatus.PENDING);

    payment.process();

    assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
    assertTrue(updatedAt.isBefore(payment.getUpdatedAt()));
  }

  @Test
  void shouldThrowExceptionWhenProcessing() {
    var payment = createPayment(PaymentStatus.COMPLETED);
    assertThrows(PaymentException.class, payment::process);
  }

  @Test
  void shouldFailing() {
    var payment = createPayment(PaymentStatus.PENDING);
    String reason = "error payment";

    payment.fail(reason);

    assertEquals(PaymentStatus.FAILED, payment.getStatus());
    assertFalse(payment.getFailureReason().isBlank());
    assertTrue(updatedAt.isBefore(payment.getUpdatedAt()));
  }

  @Test
  void shouldThrowExceptionWhenFailing() {
    var payment = createPayment(PaymentStatus.COMPLETED);

    assertThrows(PaymentException.class, () -> payment.fail("err"));
  }

  @Test
  void shouldRefunding() {
    var payment = createPayment(PaymentStatus.COMPLETED);

    payment.refund();

    assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
    assertTrue(updatedAt.isBefore(payment.getUpdatedAt()));
  }

  @Test
  void shouldThrowExceptionWhenRefunding() {
    var payment = createPayment(PaymentStatus.FAILED);

    assertThrows(PaymentException.class, payment::refund);
  }

  private Payment createPayment(PaymentStatus paymentStatus) {
    return new Payment(id, "ord1", BigDecimal.TEN, paymentStatus,
        "", Instant.now(), Instant.now());
  }
}