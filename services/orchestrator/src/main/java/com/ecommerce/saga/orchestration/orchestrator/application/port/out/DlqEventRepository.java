package com.ecommerce.saga.orchestration.orchestrator.application.port.out;

import com.ecommerce.saga.orchestration.orchestrator.domain.model.DlqEvent;

public interface DlqEventRepository {
  void save(DlqEvent event);
}
