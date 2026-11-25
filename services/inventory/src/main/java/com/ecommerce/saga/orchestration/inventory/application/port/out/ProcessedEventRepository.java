package com.ecommerce.saga.orchestration.inventory.application.port.out;

import com.ecommerce.saga.orchestration.inventory.domain.model.ProcessedEvent;

public interface ProcessedEventRepository {
  void save(ProcessedEvent event);
  boolean isExistById(String id);
}
