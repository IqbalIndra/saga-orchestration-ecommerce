package com.ecommerce.saga.orchestration.orchestrator.domain.model;

import java.time.Instant;

public class DlqEvent {
  private final String id;
  private final String sagaId;
  private final String sagaStep;
  private final String originalTopic;
  private final String messageKey;
  private final String payload;
  private final String header;
  private final String errorType;
  private final String errorMessage;
  private DlqStatus status;
  private final Instant nextTryAt;
  private final Instant createdAt;
  private  Instant updatedAt;

  public DlqEvent(String id, String sagaId, String sagaStep, String originalTopic, String messageKey, String payload,
                  String header, String errorType, String errorMessage, DlqStatus status, Instant nextTryAt,
                  Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.sagaId = sagaId;
    this.sagaStep = sagaStep;
    this.originalTopic = originalTopic;
    this.messageKey = messageKey;
    this.payload = payload;
    this.header = header;
    this.errorType = errorType;
    this.errorMessage = errorMessage;
    this.status = status;
    this.nextTryAt = nextTryAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static DlqEvent readyForRetry(String id, String sagaId, String sagaStep, String originalTopic,
                                       String messageKey, String payload, String header, String errorType,
                                       String errorMessage) {
    return new DlqEvent(id,sagaId, sagaStep, originalTopic, messageKey, payload, header, errorType, errorMessage,
        DlqStatus.READY_FOR_RETRY, Instant.now().plusSeconds(30) , Instant.now(), Instant.now());
  }

  public void markAsReplayed() {
    this.status = DlqStatus.REPLAYED;
    this.updatedAt = Instant.now();
  }

  public void markAsFailed() {
    this.status = DlqStatus.FAILED;
    this.updatedAt = Instant.now();
  }

  public String getId() { return id; }
  public String getSagaId() { return sagaId; }
  public String getSagaStep() { return sagaStep;}
  public String getOriginalTopic() { return originalTopic; }
  public String getMessageKey() { return messageKey;}
  public String getPayload() { return payload; }
  public String getHeader() { return header; }
  public String getErrorType() { return errorType; }
  public String getErrorMessage() { return errorMessage; }
  public DlqStatus getStatus() { return status; }
  public Instant getNextTryAt() { return nextTryAt; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
