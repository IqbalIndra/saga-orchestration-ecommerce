package com.ecommerce.saga.orchestration.orchestrator.domain.model;

import com.ecommerce.saga.orchestration.orchestrator.domain.exception.InvalidOutboxEventStateException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class OutboxEventTest {
  private final Instant now = Instant.now();

  private OutboxEvent domain;

  @Test
  void shouldMarkingAsPublished() {
    domain = createWithStatus(OutboxStatus.PENDING);
    assertNull(domain.getPublishedAt());

    domain.markAsPublished();

    assertEquals(OutboxStatus.PUBLISHED, domain.getStatus());
    assertNotNull(domain.getPublishedAt());
    assertTrue(now.isBefore(domain.getPublishedAt()));
  }

  @Test
  void shouldThrowExceptionWhenMarkingAsPublished() {
    domain = createWithStatus(OutboxStatus.FAILED);

    assertThrows(InvalidOutboxEventStateException.class, domain::markAsPublished);
  }

  @Test
  void shouldMarkingAsFailed() {
    domain = createWithStatus(OutboxStatus.PENDING);
    domain.markAsFailed();

    assertEquals(OutboxStatus.FAILED, domain.getStatus());
  }

  @Test
  void shouldRetrying() {
    domain = createWithStatus(OutboxStatus.PENDING);
    assertNull(domain.getNextAttemptAt());
    domain.retry(5000);

    assertEquals(1, domain.getRetryCount());
    assertNotNull(domain.getNextAttemptAt());
    assertTrue(now.isBefore(domain.getNextAttemptAt()));
  }


  private OutboxEvent createWithStatus(OutboxStatus status) {
    return new OutboxEvent(
        "id",
        "agg1",
        "aggtype",
        "evt",
        "topic",
        "{payload}",
        status,
        0,
        null,
        Instant.now(),
        null
    );
  }

}