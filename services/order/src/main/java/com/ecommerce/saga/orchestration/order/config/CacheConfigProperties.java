package com.ecommerce.saga.orchestration.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "cache")
public record CacheConfigProperties(
    Redis redis,
    Order order
){
  public record Redis(
     @DefaultValue("localhost") String host,
     String password,
     @DefaultValue("6379") int port

  ){}

  public record Order(
      @DefaultValue("PT5s") Duration ttl
  ) {}
}
