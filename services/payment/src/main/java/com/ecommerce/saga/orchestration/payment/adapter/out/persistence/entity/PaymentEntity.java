package com.ecommerce.saga.orchestration.payment.adapter.out.persistence.entity;

import com.ecommerce.saga.orchestration.payment.domain.model.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "of")
public class PaymentEntity {

  @Id
  @Column(length = 50)
  private String id;

  @Column(nullable = false, length = 50)
  private String orderId;

  @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal totalAmount;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "failure_reason")
  private String failureReason;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private PaymentStatus status;

  @PrePersist
  protected void onCreate() {
    id = UUID.randomUUID().toString();
    createdAt = Instant.now();
    updatedAt = Instant.now();
  }

}
