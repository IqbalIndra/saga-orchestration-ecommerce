package com.ecommerce.saga.orchestration.payment.domain.model;

import java.time.Instant;

public class ProcessedEvent {
  private final String id;
  private final Instant processedAt;

  public ProcessedEvent(String id, Instant processedAt) {
    this.id = id;
    this.processedAt = processedAt;
  }

  public static ProcessedEvent create(String id) {
    return new ProcessedEvent(id, Instant.now());
  }

  public String getId() { return id; }
  public Instant getProcessedAt() { return processedAt; }
}
