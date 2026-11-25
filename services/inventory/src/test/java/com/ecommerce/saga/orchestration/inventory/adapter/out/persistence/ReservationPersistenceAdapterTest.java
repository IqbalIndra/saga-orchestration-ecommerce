package com.ecommerce.saga.orchestration.inventory.adapter.out.persistence;

import com.ecommerce.saga.orchestration.inventory.adapter.out.persistence.entity.ReservationEntity;
import com.ecommerce.saga.orchestration.inventory.domain.model.Reservation;
import com.ecommerce.saga.orchestration.inventory.domain.model.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationPersistenceAdapterTest {

  @Mock
  private JpaReservationRepository reservationRepository;

  @InjectMocks
  private ReservationPersistenceAdapter adapter;

  private Reservation reservation;
  private ReservationEntity reservationEntity;

  @BeforeEach
  void setUp() {
    String id = UUID.randomUUID().toString();
    Instant now = Instant.now();

    List<Reservation.ReservedItem> reservedItems = List.of(
        new Reservation.ReservedItem("prod1", 1),
        new Reservation.ReservedItem("prod2", 1)
    );
    List<ReservationEntity.ReservationItem> reservationItems = List.of(
        new ReservationEntity.ReservationItem("prod1", 1),
        new ReservationEntity.ReservationItem("prod2", 1)
    );

    reservation = Reservation.reconstitute(id, "order1", reservedItems,
        ReservationStatus.PENDING, now, now);

    reservationEntity = new ReservationEntity(id, "order1", ReservationStatus.PENDING,
        reservationItems, now, now);
  }

  @Test
  void shouldSaveReservation() {
    when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(reservationEntity);

    var result = adapter.save(reservation);

    assertNotNull(result);
    assertThat(result.getId()).isEqualTo(reservationEntity.getId());
    assertThat(result.getOrderId()).isEqualTo(reservationEntity.getOrderId());
    assertThat(result.getStatus()).isEqualTo(ReservationStatus.PENDING);
    assertThat(result.getItems()).hasSize(2);
    assertThat(result.getItems()).isEqualTo(reservation.getItems());
    assertThat(result.getItems().getFirst().productId()).isEqualTo("prod1");
    assertThat(result.getItems().getFirst().quantity()).isEqualTo(1);

    verify(reservationRepository).save(any(ReservationEntity.class));

  }

  @Test
  void shouldFindByOrderId() {
    when(reservationRepository.findByOrderId(any(String.class))).thenReturn(Optional.of(reservationEntity));

    var result = adapter.findByOrderId("order1");

    assertThat(result.isPresent()).isTrue();
    assertThat(result.get().getOrderId()).isEqualTo("order1");
    assertThat(result.get().getId()).isEqualTo(reservationEntity.getId());
    assertThat(result.get().getStatus()).isEqualTo(ReservationStatus.PENDING);
    assertThat(result.get().getItems()).isEqualTo(reservation.getItems());

    verify(reservationRepository).findByOrderId(any(String.class));
  }

  @Test
  void shouldEmptyWhenFindByOrderId() {
    when(reservationRepository.findByOrderId(any(String.class))).thenReturn(Optional.empty());

    var result = adapter.findByOrderId("order1");

    assertThat(result.isPresent()).isFalse();

    verify(reservationRepository).findByOrderId(any(String.class));
  }
}