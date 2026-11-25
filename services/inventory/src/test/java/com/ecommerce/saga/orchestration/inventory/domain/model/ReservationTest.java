package com.ecommerce.saga.orchestration.inventory.domain.model;

import com.ecommerce.saga.orchestration.inventory.domain.exception.InvalidReservationStateException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationTest {

  @Test
  void shouldCreatePendingReservation(){
    var orderId = "order";
    var createdAt = Instant.now();
    var items = List.of(
        new Reservation.ReservedItem("product1", 1),
        new Reservation.ReservedItem("product2", 1)
    );
    
    Reservation reservation = Reservation.create(orderId, items, createdAt);

    assertEquals(orderId, reservation.getOrderId());
    assertEquals(createdAt, reservation.getCreatedAt());
    assertEquals(items, reservation.getItems());
    assertEquals(2 , items.size());
    assertEquals(ReservationStatus.PENDING, reservation.getStatus());
  }

  @Test
  void shouldConfirmReservation() {
    var orderId = "order";
    var createdAt = Instant.now();
    var items = List.of(
        new Reservation.ReservedItem("product1", 1),
        new Reservation.ReservedItem("product2", 1)
    );

    Reservation reservation = Reservation.create(orderId, items, createdAt);
    assertNull(reservation.getUpdatedAt());

    reservation.confirmed();

    assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
    assertNotNull(reservation.getUpdatedAt());
    assertTrue(reservation.getUpdatedAt().isBefore(Instant.now()));
  }

  @Test
  void shouldThrowExceptionWhenConfirmingReservationWithCancelledStatus() {
    var id = "id";
    var orderId = "order";
    var createdAt = Instant.now();
    var updatedAt = Instant.now();
    var items = List.of(
        new Reservation.ReservedItem("product1", 1),
        new Reservation.ReservedItem("product2", 1)
    );

    var reservation = Reservation.reconstitute(
        id, orderId, items, ReservationStatus.CANCELLED, createdAt, updatedAt
    );

    assertThrows(InvalidReservationStateException.class , reservation::confirmed);
  }

  @Test
  void shouldCancelledReservation() {
    var orderId = "order";
    var createdAt = Instant.now();
    var items = List.of(
        new Reservation.ReservedItem("product1", 1),
        new Reservation.ReservedItem("product2", 1)
    );

    Reservation reservation = Reservation.create(orderId, items, createdAt);
    assertNull(reservation.getUpdatedAt());

    reservation.cancelled();

    assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
    assertNotNull(reservation.getUpdatedAt());
    assertTrue(reservation.getUpdatedAt().isBefore(Instant.now()));
  }

  @Test
  void shouldThrowExceptionWhenCancellingReservationWithConfirmedStatus() {
    var id = "id";
    var orderId = "order";
    var createdAt = Instant.now();
    var updatedAt = Instant.now();
    var items = List.of(
        new Reservation.ReservedItem("product1", 1),
        new Reservation.ReservedItem("product2", 1)
    );

    var reservation = Reservation.reconstitute(
        id, orderId, items, ReservationStatus.CONFIRMED, createdAt, updatedAt
    );

    assertThrows(InvalidReservationStateException.class , reservation::cancelled);
  }

}