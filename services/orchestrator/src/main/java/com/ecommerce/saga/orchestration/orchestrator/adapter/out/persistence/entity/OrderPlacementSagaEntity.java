package com.ecommerce.saga.orchestration.orchestrator.adapter.out.persistence.entity;

import com.ecommerce.saga.orchestration.orchestrator.domain.model.SagaState;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "order_placement_saga")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "of")
public class OrderPlacementSagaEntity {
  @Id
  private String sagaId;

  @Column(nullable = false)
  private String orderId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SagaState state;

  // Data needed for compensations
  private String paymentId;
  private String reservationId;

  // Failure context
  private String failedStep;
  private String failureReason;

  @Column(nullable = false)
  private Instant startedAt;
  private Instant completedAt;

}
