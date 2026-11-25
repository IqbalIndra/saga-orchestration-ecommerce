package com.ecommerce.saga.orchestration.orchestrator.adapter.out.persistence.entity;

import com.ecommerce.saga.orchestration.orchestrator.domain.model.OutboxStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "of")
public class OutboxEventEntity {
  @Id
  private String id;
  @Column(nullable = false)
  private String aggregateType;
  @Column(nullable = false)
  private String aggregateId;
  @Column(nullable = false)
  private String eventType;
  @Column(nullable = false)
  private String topic;
  @JdbcTypeCode(SqlTypes.JSON)
  private String payload;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OutboxStatus status;
  @Column(nullable = false)
  private int retryCount;
  private Instant nextAttemptAt;
  @Column(nullable = false)
  private Instant createdAt;
  private Instant publishedAt;

  @PrePersist
  protected void onCreate() {
    id = UUID.randomUUID().toString();
  }
}
