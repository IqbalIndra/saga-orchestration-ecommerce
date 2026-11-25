package com.ecommerce.saga.orchestration.orchestrator.adapter.out.persistence;

import com.ecommerce.saga.orchestration.orchestrator.adapter.out.persistence.entity.OrderPlacementSagaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrderPlacementSagaRepository extends JpaRepository<OrderPlacementSagaEntity, String> {
}
