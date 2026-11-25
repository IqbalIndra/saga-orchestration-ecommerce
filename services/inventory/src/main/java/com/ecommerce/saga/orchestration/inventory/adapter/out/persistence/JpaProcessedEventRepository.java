package com.ecommerce.saga.orchestration.inventory.adapter.out.persistence;

import com.ecommerce.saga.orchestration.inventory.adapter.out.persistence.entity.ProcessedEventEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JpaProcessedEventRepository extends MongoRepository<ProcessedEventEntity, String> {

}
