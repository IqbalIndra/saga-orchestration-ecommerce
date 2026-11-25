package com.ecommerce.saga.orchestration.order.adapter.out.persistence;

import com.ecommerce.saga.orchestration.order.adapter.out.persistence.entity.OrderEntity;
import com.ecommerce.saga.orchestration.order.adapter.out.persistence.entity.OrderItemEntity;
import com.ecommerce.saga.orchestration.order.domain.Order;
import com.ecommerce.saga.orchestration.order.domain.OrderItem;
import com.ecommerce.saga.orchestration.order.domain.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderPersistenceAdapterTest {

  @Mock private JpaOrderRepository orderRepository;
  @Mock private OrderMapper mapper;
  @InjectMocks private OrderPersistenceAdapter adapter;

  private Order domain;
  private OrderEntity entity;

  private final String id = UUID.randomUUID().toString();
  private final Instant now = Instant.now();

  @BeforeEach
  void setUp() {
    domain = Order.reconstitute(
        id,
        "usr",
        List.of(
            new OrderItem("prod1", 1, BigDecimal.TEN)
        ),
        OrderStatus.PENDING,
        BigDecimal.TEN,
        now,
        now
    );

    entity = new OrderEntity(
        id,
        "usr1",
        BigDecimal.TEN,
        List.of(
            new OrderItemEntity(1L, entity, "prod1", 1, BigDecimal.TEN)
        ),
        now,
        now,
        OrderStatus.PENDING
    );
  }

  @Test
  void shouldSavingOrder() {
    when(orderRepository.save(any(OrderEntity.class))).thenReturn(entity);
    when(mapper.toEntity(domain)).thenReturn(entity);
    when(mapper.toDomain(entity)).thenReturn(domain);

    var result = adapter.save(domain);

    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(domain);

    verify(orderRepository).save(any(OrderEntity.class));
  }

  @Test
  void shouldFindingOrderById() {
    when(orderRepository.findById(any(String.class))).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(domain);

    var result = adapter.findById("ord1");

    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(domain);

    verify(orderRepository).findById(any(String.class));
  }

  @Test
  void shouldEmptyFindingOrderById() {
    when(orderRepository.findById(any(String.class))).thenReturn(Optional.empty());

    var result = adapter.findById("xxx");

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();

    verify(orderRepository).findById(any(String.class));
    verify(mapper, never()).toDomain(entity);
  }

}