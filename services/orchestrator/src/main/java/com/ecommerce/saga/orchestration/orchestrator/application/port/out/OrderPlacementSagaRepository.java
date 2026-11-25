package com.ecommerce.saga.orchestration.orchestrator.application.port.out;

import com.ecommerce.saga.orchestration.orchestrator.domain.model.OrderPlacementSaga;

import java.util.Optional;

public interface OrderPlacementSagaRepository {
  void save(OrderPlacementSaga saga);
  Optional<OrderPlacementSaga> findById(String id);
}
