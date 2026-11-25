package com.ecommerce.saga.orchestration.orchestrator.adapter.out.persistence.entity;

import com.ecommerce.saga.orchestration.orchestrator.domain.model.DlqStatus;
import com.ecommerce.saga.orchestration.orchestrator.domain.model.OutboxStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dlq_event")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "of")
public class DlqEventEntity {
  @Id
  private String id;
  @Column(nullable = false)
  private String sagaId;

  @Column(nullable = false)
  private String sagaStep;

  @Column(nullable = false)
  private String originalTopic;

  @Column(nullable = false)
  private String messageKey;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false)
  private String payload;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false)
  private String header;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DlqStatus status;

  @Column(nullable = false)
  private Instant nextAttemptAt;

  @Column(nullable = false)
  private Instant createdAt;

  private Instant updatedAt;

  @PrePersist
  protected void onCreate() {
    id = UUID.randomUUID().toString();
  }
}
