package com.ecommerce.saga.orchestration.orchestrator.adapter.out.persistence;

import com.ecommerce.saga.orchestration.orchestrator.adapter.out.persistence.entity.OrderPlacementSagaEntity;
import com.ecommerce.saga.orchestration.orchestrator.adapter.out.persistence.entity.OutboxEventEntity;
import com.ecommerce.saga.orchestration.orchestrator.domain.model.OrderPlacementSaga;
import com.ecommerce.saga.orchestration.orchestrator.domain.model.OutboxStatus;
import com.ecommerce.saga.orchestration.orchestrator.domain.model.SagaState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderPlacementSagaPersistenceAdapterTest {
  @Mock private JpaOrderPlacementSagaRepository repository;
  @InjectMocks private OrderPlacementSagaPersistenceAdapter adapter;

  private final Instant now = Instant.now();
  private final String id = UUID.randomUUID().toString();

  private OrderPlacementSaga domain;
  private OrderPlacementSagaEntity entity;

  @BeforeEach
  void setUp() {
    domain = new OrderPlacementSaga(
        id,
        SagaState.STARTED,
        "ord1",
        "pay1",
        "reserv1",
        "failStep",
        "failure",
        now,
        now
    );
    entity = new OrderPlacementSagaEntity(
        id,
        "ord1",
        SagaState.STARTED,
        "pay1",
        "reserv1",
        "failStep",
        "failure",
        now,
        now
    );

  }

  @Test
  void shouldSaving() {
    adapter.save(domain);

    ArgumentCaptor<OrderPlacementSagaEntity> orderCaptor = ArgumentCaptor.forClass(OrderPlacementSagaEntity.class);
    verify(repository).save(orderCaptor.capture());

    var newDomain = orderCaptor.getValue();

    assertThat(newDomain).isNotNull();
    assertThat(newDomain.getSagaId()).isEqualTo(id);
    assertThat(newDomain.getState()).isEqualTo(SagaState.STARTED);
    assertThat(newDomain.getOrderId()).isEqualTo("ord1");
    assertThat(newDomain.getPaymentId()).isEqualTo("pay1");
    assertThat(newDomain.getReservationId()).isEqualTo("reserv1");
  }

  @Test
  void shouldFindingById() {
    when(repository.findById(anyString())).thenReturn(Optional.of(entity));

    var result = adapter.findById("1");

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(id);
    assertThat(result.get().getState()).isEqualTo(SagaState.STARTED);
    assertThat(result.get().getOrderId()).isEqualTo("ord1");
    assertThat(result.get().getPaymentId()).isEqualTo("pay1");
    assertThat(result.get().getReservationId()).isEqualTo("reserv1");
  }

  @Test
  void shouldEmptyWhenFindingById() {
    when(repository.findById(anyString())).thenReturn(Optional.empty());

    var result = adapter.findById("xx");

    assertThat(result).isEmpty();

  }
}