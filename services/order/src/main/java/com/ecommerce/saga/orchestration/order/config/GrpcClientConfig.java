package com.ecommerce.saga.orchestration.order.config;

import com.ecommerce.saga.orchestration.grpc.inventory.InventoryGrpcServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

  @Bean
  public InventoryGrpcServiceGrpc.InventoryGrpcServiceBlockingStub inventoryGrpcStub(
      GrpcClientProperties props) {
    GrpcClientProperties.InventoryService prop = props.inventoryService();

    ManagedChannel channel = ManagedChannelBuilder.forAddress(prop.host(), prop.port())
        .usePlaintext()
        .build();
    return InventoryGrpcServiceGrpc.newBlockingStub(channel);
  }
}
