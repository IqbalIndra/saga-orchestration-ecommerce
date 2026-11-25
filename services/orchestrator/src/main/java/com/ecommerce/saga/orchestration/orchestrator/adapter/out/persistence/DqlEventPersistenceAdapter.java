package com.ecommerce.saga.orchestration.orchestrator.adapter.out.persistence;

import com.ecommerce.saga.orchestration.orchestrator.adapter.out.persistence.entity.DlqEventEntity;
import com.ecommerce.saga.orchestration.orchestrator.application.port.out.DlqEventRepository;
import com.ecommerce.saga.orchestration.orchestrator.domain.model.DlqEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DqlEventPersistenceAdapter implements DlqEventRepository {
  private final JpaDlqEventRepository jpaDlqEventRepository;

  @Override
  public void save(DlqEvent event) {
    jpaDlqEventRepository.save(DlqEventEntity.of()
        .sagaStep(event.getSagaStep())
        .originalTopic(event.getOriginalTopic())
        .messageKey(event.getMessageKey())
        .payload(event.getPayload())
        .header(event.getHeader())
        .status(event.getStatus())
        .nextAttemptAt(event.getNextTryAt())
        .createdAt(event.getCreatedAt())
        .updatedAt(event.getUpdatedAt())
        .build());
  }
}
