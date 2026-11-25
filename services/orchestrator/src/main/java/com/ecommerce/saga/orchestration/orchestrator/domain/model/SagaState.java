package com.ecommerce.saga.orchestration.orchestrator.domain.model;

public enum SagaState {
  STARTED,
  PAYMENT_PROCESSED,
  INVENTORY_RESERVED,
  CONFIRMED,                  // happy-path terminal state
  COMPENSATING_INVENTORY,     // failure paths
  COMPENSATING_PAYMENT,
  COMPENSATED,
  FAILED
}
