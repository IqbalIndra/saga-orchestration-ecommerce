package com.ecommerce.saga.orchestration.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
public class RedisCacheConfig {

  @Bean
  public RedisConnectionFactory redisConnectionFactory(CacheConfigProperties properties){
    RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
    configuration.setHostName(properties.redis().host());
    configuration.setPassword(properties.redis().password());
    configuration.setPort(properties.redis().port());
    return new LettuceConnectionFactory(configuration);
  }


}
