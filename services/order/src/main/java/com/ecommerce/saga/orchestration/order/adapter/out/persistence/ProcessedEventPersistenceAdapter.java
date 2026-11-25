package com.ecommerce.saga.orchestration.order.adapter.out.persistence;

import com.ecommerce.saga.orchestration.order.adapter.out.persistence.entity.ProcessedEventEntity;
import com.ecommerce.saga.orchestration.order.application.port.out.ProcessedEventRepository;
import com.ecommerce.saga.orchestration.order.domain.ProcessedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProcessedEventPersistenceAdapter implements ProcessedEventRepository {
  private final JpaProcessedEventRepository jpaProcessedEventRepository;

  @Override
  public void save(ProcessedEvent event) {
    jpaProcessedEventRepository.save(toEntity(event));
  }

  @Override
  public boolean isExistById(String id) {
    return jpaProcessedEventRepository.existsById(id);
  }

  private ProcessedEventEntity toEntity(ProcessedEvent event) {
    return ProcessedEventEntity.of()
        .id(event.getId())
        .processedAt(event.getProcessedAt())
        .build();
  }
}
