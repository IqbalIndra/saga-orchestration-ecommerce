package com.ecommerce.saga.orchestration.inventory.domain.model;

import com.ecommerce.saga.orchestration.inventory.domain.exception.InvalidReservationStateException;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public class Reservation {
  private String id;
  private String orderId;
  private List<ReservedItem> items;
  private ReservationStatus status;
  private Instant createdAt;
  private Instant updatedAt;

  public Reservation(String id, String orderId, List<ReservedItem> items, ReservationStatus status,
                     Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.orderId = orderId;
    this.items = items;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Reservation(String orderId, List<ReservedItem> items, ReservationStatus reservationStatus,
                     Instant createdAt) {
    this.orderId = orderId;
    this.items = items;
    this.status = reservationStatus;
    this.createdAt = createdAt;
  }

  public record ReservedItem(
      String productId,
      int quantity
  ) {
  }

  public static Reservation create(String orderId, List<ReservedItem> items, Instant createdAt) {
    return new Reservation(
        orderId,
        items,
        ReservationStatus.PENDING,
        createdAt
    );
  }

  public static Reservation reconstitute(String id, String orderId, List<ReservedItem> items,
                                         ReservationStatus status, Instant createdAt, Instant updatedAt) {
    return new Reservation(id, orderId, items, status, createdAt, updatedAt);
  }

  public void confirmed() {
    if (status != ReservationStatus.PENDING) {
      throw new InvalidReservationStateException("Status must PENDING before confirmed");
    }
    status = ReservationStatus.CONFIRMED;
    updatedAt = Instant.now();
  }

  public void cancelled() {
    if (status != ReservationStatus.PENDING) {
      throw new InvalidReservationStateException("Status must PENDING before cancelled");
    }
    status = ReservationStatus.CANCELLED;
    updatedAt = Instant.now();
  }
}
