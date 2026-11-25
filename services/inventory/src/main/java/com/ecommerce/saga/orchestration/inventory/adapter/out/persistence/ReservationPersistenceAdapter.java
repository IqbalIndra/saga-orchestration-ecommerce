package com.ecommerce.saga.orchestration.inventory.adapter.out.persistence;

import com.ecommerce.saga.orchestration.inventory.adapter.out.persistence.entity.ReservationEntity;
import com.ecommerce.saga.orchestration.inventory.application.port.out.ReservationRepository;
import com.ecommerce.saga.orchestration.inventory.domain.model.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReservationPersistenceAdapter implements ReservationRepository {
  private final JpaReservationRepository reservationRepository;

  @Override
  public Reservation save(Reservation reservation) {
    return toDomain(reservationRepository.save(toEntity(reservation)));
  }

  @Override
  public Optional<Reservation> findByOrderId(String orderId) {
    return reservationRepository.findByOrderId(orderId).map(this::toDomain);
  }

  private ReservationEntity toEntity(Reservation reservation) {
    return new ReservationEntity(
        reservation.getId(),
        reservation.getOrderId(),
        reservation.getStatus(),
        reservation.getItems().stream()
            .map(i -> new ReservationEntity.ReservationItem(
                i.productId(),
                i.quantity()
            )).toList(),
        reservation.getCreatedAt(),
        reservation.getUpdatedAt()
    );
  }

  private Reservation toDomain(ReservationEntity entity) {
    return Reservation.reconstitute(
        entity.getId(),
        entity.getOrderId(),
        entity.getItems().stream()
            .map(i -> new Reservation.ReservedItem(
                i.productId(),
                i.quantity()
            )).toList(),
        entity.getStatus(),
        entity.getCreatedAt(),
        entity.getUpdatedAt()
    );
  }
}
