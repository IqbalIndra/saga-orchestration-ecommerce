package com.ecommerce.saga.orchestration.order.adapter.out.persistence.entity;

import com.ecommerce.saga.orchestration.order.domain.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {
  @Id
  @Column(length = 50)
  private String id;

  @Column(name = "customer_id", nullable = false)
  private String userId;

  @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal totalAmount;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @Builder.Default
  private List<OrderItemEntity> items = new ArrayList<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private OrderStatus status;

  @PrePersist
  protected void onCreate() {
    id = UUID.randomUUID().toString();
    updatedAt = Instant.now();
  }

}
