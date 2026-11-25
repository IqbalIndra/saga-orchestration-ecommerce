package com.ecommerce.saga.orchestration.order.application.port.out;

import com.ecommerce.saga.orchestration.order.domain.ProcessedEvent;

public interface ProcessedEventRepository {
  void save(ProcessedEvent event);
  boolean isExistById(String id);
}
