package com.ecommerce.saga.orchestration.order.config;

import com.ecommerce.saga.orchestration.order.domain.Order;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CaffeineCacheConfig {

  @Bean
  Cache<String, Order> orderCache() {
    return Caffeine.newBuilder()
        .maximumSize(100)
        .expireAfterWrite(Duration.ofSeconds(5))
        .recordStats()
        .build();
  }
}
