package com.ecommerce.saga.orchestration.orchestrator.adapter.out.persistence;

import com.ecommerce.saga.orchestration.orchestrator.adapter.out.persistence.entity.DlqEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaDlqEventRepository extends JpaRepository<DlqEventEntity, String> {
}
