package com.ecommerce.saga.orchestration.order.adapter.out.persistence;

import com.ecommerce.saga.orchestration.order.application.port.out.OrderRepository;
import com.ecommerce.saga.orchestration.order.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderRepository {
  private final JpaOrderRepository jpaOrderRepository;
  private final OrderMapper orderMapper;

  @Override
  public Order save(Order order) {
    return orderMapper.toDomain(jpaOrderRepository.save(orderMapper.toEntity(order)));
  }

  @Override
  public Optional<Order> findById(String orderId) {
    return jpaOrderRepository.findById(orderId).map(orderMapper::toDomain);
  }
}
