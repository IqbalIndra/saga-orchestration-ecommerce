package com.ecommerce.saga.orchestration.inventory.adapter.out.persistence.migration;

import com.ecommerce.saga.orchestration.inventory.adapter.out.persistence.JpaStockItemRepository;
import com.ecommerce.saga.orchestration.inventory.adapter.out.persistence.entity.StockItemEntity;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;

import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

@ChangeUnit(id = "product-migration-v001", order = "001", author = "ecommerce")
public class StockEntityMigrationV001 {

  @Execution
  public void createStockItemCollection(MongoDatabase database, JpaStockItemRepository stockItemRepository) {
    database.createCollection("stock_items");
    MongoCollection<Document> stockItems = database.getCollection("stock_items");

    IndexOptions indexOptions = new IndexOptions().unique(true);
    stockItems.createIndex(Indexes.ascending("productId"), indexOptions);

    List<StockItemEntity> collect = IntStream.rangeClosed(1, 5)
        .mapToObj(i -> StockItemEntity.of()
            .productId("product" + i)
            .availableQuantity(10)
            .reservedQuantity(0)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build()
        ).toList();

    stockItemRepository.saveAll(collect);
  }

  @RollbackExecution
  public void rollbackExecution(MongoDatabase mongoDatabase) {
    mongoDatabase.getCollection("stock_items").drop();
  }
}
