package com.ecommerce.saga.orchestration.inventory.adapter.out.persistence.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "processed_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "of")
public class ProcessedEventEntity {
  private String id;
  private Instant processedAt;
}
