package com.ecommerce.saga.orchestration.inventory.adapter.out.persistence.migration;

import com.ecommerce.saga.orchestration.inventory.adapter.out.persistence.JpaProcessedEventRepository;
import com.mongodb.client.MongoDatabase;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "processed-event-migration-v001", order = "001", author = "ecommerce")
public class ProcessedEventEntityMigrationV001 {

  @Execution
  public void createProcessedEventCollection(MongoDatabase database, JpaProcessedEventRepository eventRepository) {
    database.createCollection("processed_events");
  }

  @RollbackExecution
  public void rollbackExecution(MongoDatabase mongoDatabase) {
    mongoDatabase.getCollection("processed_events").drop();
  }

}
