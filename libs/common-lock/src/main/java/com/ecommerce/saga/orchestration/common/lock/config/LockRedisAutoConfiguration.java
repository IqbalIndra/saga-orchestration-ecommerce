package com.ecommerce.saga.orchestration.common.lock.config;

import com.ecommerce.saga.orchestration.common.lock.distributed.RedisDistributeLock;
import com.ecommerce.saga.orchestration.common.lock.distributed.RedisLockRenewal;
import com.ecommerce.saga.orchestration.common.lock.executor.LockExecutor;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass(RedisTemplate.class)
public class LockRedisAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(StringRedisTemplate.class)
  public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
    StringRedisTemplate stringRedis = new StringRedisTemplate();
    stringRedis.setConnectionFactory(redisConnectionFactory);
    stringRedis.afterPropertiesSet();
    return stringRedis;
  }

  @Bean
  @ConditionalOnMissingBean
  public RedisDistributeLock redisDistributeLock(StringRedisTemplate stringRedisTemplate) {
    return new RedisDistributeLock(stringRedisTemplate);
  }

  @Bean
  @ConditionalOnMissingBean
  public RedisLockRenewal redisLockRenewal(StringRedisTemplate stringRedisTemplate) {
    return new RedisLockRenewal(stringRedisTemplate);
  }

  @Bean
  @ConditionalOnMissingBean(name = "commonRedisTemplate")
  public RedisTemplate<String, Object> commonRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
    GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
    genericJackson2JsonRedisSerializer.configure(objectMapper -> objectMapper.registerModules(new JavaTimeModule()));

    RedisTemplate<String,Object> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(genericJackson2JsonRedisSerializer);
    template.setHashValueSerializer(genericJackson2JsonRedisSerializer);
    template.afterPropertiesSet();
    return template;
  }

  @Bean(name = "redisTaskScheduler")
  @ConditionalOnMissingBean(name = "redisTaskScheduler")
  public ThreadPoolTaskScheduler redisTaskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setThreadNamePrefix("redis-scheduler-");
    // Enables Java 21 virtual threads for scheduled tasks
    scheduler.setVirtualThreads(true);
    // Optional: wait for running Redis tasks to finish on shutdown
    scheduler.setWaitForTasksToCompleteOnShutdown(true);
    scheduler.setAwaitTerminationSeconds(10);
    scheduler.initialize();
    return scheduler;
  }

  @Bean
  @ConditionalOnMissingBean
  public LockExecutor lockExecutor(RedisDistributeLock redisDistributeLock,
                                   RedisLockRenewal redisLockRenewal,
                                   @Qualifier("redisTaskScheduler") ThreadPoolTaskScheduler scheduler) {
    return new LockExecutor(redisDistributeLock, redisLockRenewal, scheduler);
  }
}

