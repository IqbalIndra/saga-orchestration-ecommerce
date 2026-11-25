package com.ecommerce.saga.orchestration.inventory.adapter.out.persistence.entity;

import com.ecommerce.saga.orchestration.inventory.domain.model.ReservationStatus;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "of")
public class ReservationEntity {

  private String id;
  private String orderId;
  private ReservationStatus status;
  private List<ReservationItem> items;
  private Instant createdAt;
  private Instant updatedAt;


  public record ReservationItem(
      String productId,
      int quantity
  ) {}
}
