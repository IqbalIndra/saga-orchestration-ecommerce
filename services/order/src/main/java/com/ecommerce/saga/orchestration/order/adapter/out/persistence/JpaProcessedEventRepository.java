package com.ecommerce.saga.orchestration.order.adapter.out.persistence;

import com.ecommerce.saga.orchestration.order.adapter.out.persistence.entity.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProcessedEventRepository extends JpaRepository<ProcessedEventEntity, String> {
}
