package com.ecommerce.saga.orchestration.payment.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "idempotency_keys")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "of")
public class IdempotencyEntity {
  @Id
  @Column(length = 100)
  private String key;

  @Column(nullable = false, length = 50)
  private String paymentId;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String responseBody;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;
}
