package com.ecommerce.saga.orchestration.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "grpc.client")
public record GrpcClientProperties(
    InventoryService inventoryService
) {
  public record InventoryService(
      @DefaultValue("localhost") String host,
      @DefaultValue("9090") int port
  ) {

  }
}
