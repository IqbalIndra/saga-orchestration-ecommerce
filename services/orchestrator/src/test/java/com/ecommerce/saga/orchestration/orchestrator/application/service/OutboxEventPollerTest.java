package com.ecommerce.saga.orchestration.orchestrator.application.service;

import com.ecommerce.saga.orchestration.common.events.order.ConfirmOrderCommand;
import com.ecommerce.saga.orchestration.orchestrator.application.port.out.OutboxEventRepository;
import com.ecommerce.saga.orchestration.orchestrator.domain.model.OutboxEvent;
import com.ecommerce.saga.orchestration.orchestrator.domain.model.OutboxStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.TimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxEventPollerTest {
  @Mock private OutboxEventRepository repository;
  @Mock private KafkaTemplate<String, Object> kafka;
  @Mock private ObjectMapper mapper;
  @InjectMocks private OutboxEventPoller poller;

  private final String id = UUID.randomUUID().toString();
  private final Instant now = Instant.now();

  private OutboxEvent domain;

  @Test
  void shouldEmptyWhenRunningRelay() {
    when(repository.findUnprocessed(anyInt())).thenReturn(List.of());

    poller.relay();

    verify(repository).findUnprocessed(anyInt());
  }

  @Test
  void shouldThrowExceptionWhenClassNotFoundThenRetry() {
    domain = createForPoller(
        "evtType",
        0,
        OutboxStatus.PENDING
    );
    when(repository.findUnprocessed(anyInt())).thenReturn(List.of(domain));

    poller.relay();

    verify(repository).findUnprocessed(anyInt());

    ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
    verify(repository).save(captor.capture());

    var newDomain = captor.getValue();

    assertThat(newDomain).isNotNull();
    assertThat(newDomain.getNextAttemptAt()).isNotNull();
    assertThat(newDomain.getNextAttemptAt().isAfter(now));
    assertThat(newDomain.getRetryCount()).isEqualTo(1);
  }

  @Test
  void shouldThrowExceptionWhenReadValueThenRetry() throws JsonProcessingException {
    domain = createForPoller(
        "com.ecommerce.saga.orchestration.common.events.order.OrderCreatedEvent",
        0,
        OutboxStatus.PENDING
    );

    when(repository.findUnprocessed(anyInt())).thenReturn(List.of(domain));
    when(mapper.readValue(anyString(), any(Class.class))).thenThrow(JsonProcessingException.class);

    poller.relay();

    verify(repository).findUnprocessed(anyInt());

    ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
    verify(repository).save(captor.capture());

    var newDomain = captor.getValue();

    assertThat(newDomain).isNotNull();
    assertThat(newDomain.getNextAttemptAt()).isNotNull();
    assertThat(newDomain.getNextAttemptAt().isAfter(now));
    assertThat(newDomain.getRetryCount()).isEqualTo(1);

  }

  @Test
  void shouldThrowExceptionWhenKafkaErrorThenRetry() throws JsonProcessingException {
    domain = createForPoller(
        "com.ecommerce.saga.orchestration.common.events.order.ConfirmOrderCommand",
        1,
        OutboxStatus.PENDING
    );

    when(repository.findUnprocessed(anyInt())).thenReturn(List.of(domain));
    when(mapper.readValue(anyString(), any(Class.class))).thenReturn(new ConfirmOrderCommand("sagaId", "orderId"));
    when(kafka.send(any(), any(), any())).thenThrow(TimeoutException.class);

    poller.relay();

    verify(repository).findUnprocessed(anyInt());

    ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
    verify(repository).save(captor.capture());

    var newDomain = captor.getValue();

    assertThat(newDomain).isNotNull();
    assertThat(newDomain.getNextAttemptAt()).isNotNull();
    assertThat(newDomain.getNextAttemptAt().isAfter(now));
    assertThat(newDomain.getRetryCount()).isEqualTo(2);

  }

  @Test
  void shouldThrowExceptionWhenMarkingPublishedThenRetry() throws JsonProcessingException {
    domain = createForPoller(
        "com.ecommerce.saga.orchestration.common.events.order.ConfirmOrderCommand",
        1,
        OutboxStatus.PUBLISHED
    );

    when(repository.findUnprocessed(anyInt())).thenReturn(List.of(domain));
    when(mapper.readValue(anyString(), any(Class.class))).thenReturn(new ConfirmOrderCommand("sagaId", "orderId"));
    when(kafka.send(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(new SendResult<>(null, null)));

    poller.relay();

    verify(repository).findUnprocessed(anyInt());

    ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
    verify(repository).save(captor.capture());

    var newDomain = captor.getValue();

    assertThat(newDomain).isNotNull();
    assertThat(newDomain.getNextAttemptAt()).isNotNull();
    assertThat(newDomain.getNextAttemptAt().isAfter(now));
    assertThat(newDomain.getRetryCount()).isEqualTo(2);
  }

  @Test
  void shouldThrowExceptionWhenUpdateStatusThenFailed() throws JsonProcessingException {
    domain = createForPoller(
        "com.ecommerce.saga.orchestration.common.events.order.ConfirmOrderCommand",
        5,
        OutboxStatus.PENDING
    );

    when(repository.findUnprocessed(anyInt())).thenReturn(List.of(domain));
    when(mapper.readValue(anyString(), any(Class.class))).thenReturn(new ConfirmOrderCommand("sagaId", "orderId"));
    when(kafka.send(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(new SendResult<>(null, null)));
    doThrow(DataIntegrityViolationException.class).when(repository).save(any());

    assertThrows(DataIntegrityViolationException.class, poller::relay);

    verify(repository).findUnprocessed(anyInt());
    verify(mapper).readValue(anyString(), any(Class.class));
    verify(kafka).send(any(), any(), any());

    ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
    verify(repository, times(2)).save(captor.capture());

    var newDomain = captor.getValue();

    assertThat(newDomain).isNotNull();
    assertThat(newDomain.getStatus()).isEqualTo(OutboxStatus.FAILED);
    assertThat(newDomain.getRetryCount()).isEqualTo(5);
  }

  private OutboxEvent createForPoller(String eventType, int retry, OutboxStatus status) {
    return new OutboxEvent(
        id,
        "agg1",
        "aggType",
        eventType,
        "topic",
        "{payload}",
        status,
        retry,
        null,
        now,
        now
    );
  }
}