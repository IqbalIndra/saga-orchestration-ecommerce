package com.ecommerce.saga.orchestration.orchestrator.adapter.out.persistence;

import com.ecommerce.saga.orchestration.orchestrator.adapter.out.persistence.entity.OutboxEventEntity;
import com.ecommerce.saga.orchestration.orchestrator.domain.model.OutboxEvent;
import com.ecommerce.saga.orchestration.orchestrator.domain.model.OutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxEventPersistenceAdapterTest {
  @Mock private JpaOutboxEventRepository repository;
  @InjectMocks private OutboxEventPersistenceAdapter adapter;

  private final String id = UUID.randomUUID().toString();
  private final Instant now = Instant.now();

  private OutboxEvent domain;
  private OutboxEventEntity entity;

  @BeforeEach
  void beforeAll() {
    domain = new OutboxEvent(
        id,
        "agg1",
        "aggType",
        "evtType",
        "topic",
        "{payload}",
        OutboxStatus.PENDING,
        0,
        now,
        now,
        now
    );

    entity = new OutboxEventEntity(
        id,
        "aggType",
        "agg1",
        "evtType",
        "topic",
        "{payload}",
        OutboxStatus.PENDING,
        0,
        now,
        now,
        now
    );

  }

  @Test
  void shouldSaving() {
    adapter.save(domain);

    ArgumentCaptor<OutboxEventEntity> orderCaptor = ArgumentCaptor.forClass(OutboxEventEntity.class);
    verify(repository).save(orderCaptor.capture());

    var newDomain = orderCaptor.getValue();

    assertThat(newDomain).isNotNull();
    assertThat(newDomain.getId()).isEqualTo(id);
    assertThat(newDomain.getStatus()).isEqualTo(OutboxStatus.PENDING);
    assertThat(newDomain.getAggregateId()).isEqualTo("agg1");
    assertThat(newDomain.getTopic()).isEqualTo("topic");
  }

  @Test
  void shouldFindingUnprocessed() {
    when(repository.findUnprocessed(anyInt())).thenReturn(List.of(entity));

    var result = adapter.findUnprocessed(10);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getId()).isEqualTo(id);
    assertThat(result.getFirst().getStatus()).isEqualTo(OutboxStatus.PENDING);
    assertThat(result.getFirst().getAggregateId()).isEqualTo("agg1");
    assertThat(result.getFirst().getTopic()).isEqualTo("topic");

    verify(repository).findUnprocessed(anyInt());
  }

  @Test
  void shouldEmptyWhenFindingUnprocessed() {
    when(repository.findUnprocessed(anyInt())).thenReturn(List.of());

    var result = adapter.findUnprocessed(10);

    assertThat(result).isEmpty();
    assertThat(result).hasSize(0);

    verify(repository).findUnprocessed(anyInt());
  }

  @Test
  void shouldCleaningUpOldMessages() {
    doNothing().when(repository).cleanupOldMessages(any(Instant.class));
    adapter.cleanUpOldMessages(Instant.now());

    verify(repository, times(1)).cleanupOldMessages(any(Instant.class));
  }

}