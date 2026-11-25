package com.ecommerce.saga.orchestration.orchestrator.domain.model;

import com.ecommerce.saga.orchestration.orchestrator.domain.exception.InvalidOutboxEventStateException;

import java.time.Instant;

public class OutboxEvent {
  private final String id;
  private final String aggregateId;
  private final String aggregateType;
  private final String eventType;
  private final String topic;
  private final String payload;
  private OutboxStatus status;
  private int retryCount;
  private Instant nextAttemptAt;
  private final Instant createdAt;
  private Instant publishedAt;

  public OutboxEvent(String id, String aggregateId, String aggregateType, String eventType, String topic, String payload,
                     OutboxStatus status, int retryCount, Instant nextAttemptAt, Instant createdAt,
                     Instant publishedAt) {
    this.id = id;
    this.aggregateId = aggregateId;
    this.aggregateType = aggregateType;
    this.eventType = eventType;
    this.topic = topic;
    this.payload = payload;
    this.status = status;
    this.retryCount = retryCount;
    this.nextAttemptAt = nextAttemptAt;
    this.createdAt = createdAt;
    this.publishedAt = publishedAt;
  }

  public static OutboxEvent create(String id, String aggregateId, String aggregateType,
                                   String topic, String eventType, String payload) {
    return new OutboxEvent(id, aggregateId, aggregateType, eventType, topic, payload,
        OutboxStatus.PENDING, 0, Instant.now(), Instant.now(), null);
  }

  public void markAsPublished() {
    if (status != OutboxStatus.PENDING) {
      throw new InvalidOutboxEventStateException("Status must be PENDING first !");
    }
    status = OutboxStatus.PUBLISHED;
    publishedAt = Instant.now();
  }

  public void markAsFailed() {
    status = OutboxStatus.FAILED;
  }

  public void retry(long backoffInSecond) {
    status = OutboxStatus.PENDING;
    retryCount++;
    nextAttemptAt = Instant.now().plusSeconds(backoffInSecond);
  }

  public String getId() { return id; }
  public String getAggregateId() { return aggregateId; }
  public String getAggregateType() { return aggregateType; }
  public String getEventType() { return eventType; }
  public String getPayload() { return payload; }
  public OutboxStatus getStatus() { return status; }
  public int getRetryCount() { return retryCount; }
  public Instant getNextAttemptAt() { return nextAttemptAt; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getPublishedAt() { return publishedAt; }
  public String getTopic() { return topic; }
}
