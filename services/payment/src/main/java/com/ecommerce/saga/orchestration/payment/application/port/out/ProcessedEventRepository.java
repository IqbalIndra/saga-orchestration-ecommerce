package com.ecommerce.saga.orchestration.payment.application.port.out;

import com.ecommerce.saga.orchestration.payment.domain.model.ProcessedEvent;

public interface ProcessedEventRepository {
  void save(ProcessedEvent event);
  boolean isExistById(String id);
}
