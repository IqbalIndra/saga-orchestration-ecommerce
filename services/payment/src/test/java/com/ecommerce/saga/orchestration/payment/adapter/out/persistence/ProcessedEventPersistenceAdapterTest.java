package com.ecommerce.saga.orchestration.payment.adapter.out.persistence;

import com.ecommerce.saga.orchestration.payment.adapter.out.persistence.entity.ProcessedEventEntity;
import com.ecommerce.saga.orchestration.payment.domain.model.ProcessedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ProcessedEventPersistenceAdapterTest {

  @Mock private JpaProcessedEventRepository repository;
  @InjectMocks private ProcessedEventPersistenceAdapter adapter;

  private ProcessedEvent domain;

  private final String id = UUID.randomUUID().toString();
  private final Instant now = Instant.now();

  @BeforeEach
  void setUp() {
    domain = new ProcessedEvent(id, now);
  }

  @Test
  void shouldSaving() {
    adapter.save(domain);
    ArgumentCaptor<ProcessedEventEntity> captor = ArgumentCaptor.forClass(ProcessedEventEntity.class);

    verify(repository).save(captor.capture());
    var newEntity = captor.getValue();

    assertThat(newEntity)
        .extracting(
            ProcessedEventEntity::getId,
            ProcessedEventEntity::getProcessedAt
        )
        .containsExactly(id, now);
  }

  @Test
  void shouldThrowExceptionWhenSaving() {
    when(repository.save(any(ProcessedEventEntity.class))).thenThrow(DataIntegrityViolationException.class);

    assertThrows(DataIntegrityViolationException.class, () -> adapter.save(domain));

    verify(repository).save(any(ProcessedEventEntity.class));
  }

  @Test
  void shouldFindingById() {
    when(repository.existsById(anyString())).thenReturn(true);

    var result = adapter.isExistById("1");

    assertThat(result).isTrue();

    verify(repository).existsById(anyString());
  }

  @Test
  void shouldEmptyWhenFindingById() {
    when(repository.existsById(anyString())).thenReturn(false);

    var result = adapter.isExistById("xx");

    assertThat(result).isFalse();

    verify(repository).existsById(anyString());
  }
 }