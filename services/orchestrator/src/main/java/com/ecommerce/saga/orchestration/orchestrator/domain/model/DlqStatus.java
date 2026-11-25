package com.ecommerce.saga.orchestration.orchestrator.domain.model;

public enum DlqStatus {
  READY_FOR_RETRY,
  REPLAYED,
  FAILED,
  IGNORED
}
