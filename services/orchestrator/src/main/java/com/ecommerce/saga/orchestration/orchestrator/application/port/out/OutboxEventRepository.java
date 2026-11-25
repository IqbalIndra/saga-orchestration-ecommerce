package com.ecommerce.saga.orchestration.orchestrator.application.port.out;

import com.ecommerce.saga.orchestration.orchestrator.domain.model.OutboxEvent;

import java.time.Instant;
import java.util.List;

public interface OutboxEventRepository {
  void save(OutboxEvent outboxEvent);
  List<OutboxEvent> findUnprocessed(int limit);
  void cleanUpOldMessages(Instant olderThan);
}
