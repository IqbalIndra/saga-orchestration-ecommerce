package com.ecommerce.saga.orchestration.order.application.service;

import com.ecommerce.saga.orchestration.common.lock.executor.LockExecutor;
import com.ecommerce.saga.orchestration.order.application.port.in.GetOrderUseCase;
import com.ecommerce.saga.orchestration.order.application.port.out.OrderRepository;
import com.ecommerce.saga.orchestration.order.config.CacheConfigProperties;
import com.ecommerce.saga.orchestration.order.domain.Order;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetOrderService implements GetOrderUseCase {
  private final Cache<String, Order> orderCache;
  private final RedisTemplate<String, Object> commonRedisTemplate;
  private final LockExecutor lockExecutor;
  private final OrderRepository orderRepository;
  private final CacheConfigProperties cacheProperties;

  @Override
  public Order getOrderById(String id) {
    String key = "order:"+id;

    var order = getOrderFromCache(key);

    if (Objects.nonNull(order)) {
      return order;
    }

    order = lockExecutor.runWithLock(
        "lock:order:"+id,
        Duration.ofSeconds(5),
        () -> doCacheOrder(id, key)
    );

    return order;
  }

  private Order doCacheOrder(String id, String key) {
    var order = getOrderFromCache(key);
    if (Objects.nonNull(order)) {
      return order;
    }

    order  = orderRepository.findById(id).orElseThrow();
    commonRedisTemplate.opsForValue().set(key , order , cacheProperties.order().ttl());
    orderCache.put(key, order);
    return order;
  }

  private Order getOrderFromCache(String key) {
    // L1 cache
    Order order = orderCache.getIfPresent(key);
    if (Objects.nonNull(order)) {
      return order;
    }

    // L2 cache
    order = (Order) commonRedisTemplate.opsForValue().get(key);

    if (Objects.nonNull(order)) {
      orderCache.put(key , order);
      return order;
    }
    return null;
  }
}
