package com.ecommerce.saga.orchestration.orchestrator.adapter.out.persistence;

import com.ecommerce.saga.orchestration.orchestrator.adapter.out.persistence.entity.OutboxEventEntity;
import com.ecommerce.saga.orchestration.orchestrator.application.port.out.OutboxEventRepository;
import com.ecommerce.saga.orchestration.orchestrator.domain.model.OutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxEventPersistenceAdapter implements OutboxEventRepository {
  private final JpaOutboxEventRepository jpaOutboxEventRepository;

  @Override
  public void save(OutboxEvent outboxEvent) {
    var entity = OutboxEventEntity.of()
        .id(outboxEvent.getId())
        .aggregateId(outboxEvent.getAggregateId())
        .aggregateType(outboxEvent.getAggregateType())
        .eventType(outboxEvent.getEventType())
        .topic(outboxEvent.getTopic())
        .payload(outboxEvent.getPayload())
        .status(outboxEvent.getStatus())
        .retryCount(outboxEvent.getRetryCount())
        .createdAt(outboxEvent.getCreatedAt())
        .nextAttemptAt(outboxEvent.getNextAttemptAt())
        .publishedAt(outboxEvent.getPublishedAt())
        .build();

    jpaOutboxEventRepository.save(entity);
  }

  @Override
  public List<OutboxEvent> findUnprocessed(int limit) {
    return jpaOutboxEventRepository.findUnprocessed(limit).stream()
        .map(o -> new OutboxEvent(
            o.getId(),
            o.getAggregateId(),
            o.getAggregateType(),
            o.getEventType(),
            o.getTopic(),
            o.getPayload(),
            o.getStatus(),
            o.getRetryCount(),
            o.getNextAttemptAt(),
            o.getCreatedAt(),
            o.getPublishedAt()
        )).toList();
  }

  @Override
  public void cleanUpOldMessages(Instant olderThan) {
    jpaOutboxEventRepository.cleanupOldMessages(olderThan);
  }
}
