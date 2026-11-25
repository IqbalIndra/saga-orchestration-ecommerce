package com.ecommerce.saga.orchestration.inventory.application.port.out;

import com.ecommerce.saga.orchestration.inventory.domain.model.Reservation;

import java.util.Optional;

public interface ReservationRepository {
  Reservation save(Reservation reservation);
  Optional<Reservation> findByOrderId(String orderId);
}
