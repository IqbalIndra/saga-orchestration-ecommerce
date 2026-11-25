package com.ecommerce.saga.orchestration.order;

import com.ecommerce.saga.orchestration.order.config.CacheConfigProperties;
import com.ecommerce.saga.orchestration.order.config.GrpcClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties({GrpcClientProperties.class, CacheConfigProperties.class})
public class OrderApplication {

  public static void main(String[] args) {
    SpringApplication.run(OrderApplication.class, args);
  }

}
