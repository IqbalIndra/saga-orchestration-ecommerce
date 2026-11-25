package com.ecommerce.saga.orchestration.order.domain;

import com.ecommerce.saga.orchestration.order.domain.exception.InvalidOrderStateException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
public class Order {
  private String id;
  private String userId;
  private List<OrderItem> items;
  private OrderStatus status;
  private BigDecimal totalAmount;
  private Instant createdAt;
  private Instant updatedAt;

  public Order() {
  }

  public Order(String id, String userId, List<OrderItem> items, OrderStatus status, BigDecimal totalAmount, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.userId = userId;
    this.items = items;
    this.status = status;
    this.totalAmount = totalAmount;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Order(String userId, List<OrderItem> items, BigDecimal total, OrderStatus status, Instant createdAt) {
    this.userId = userId;
    this.totalAmount = total;
    this.items = List.copyOf(items);
    this.status = status;
    this.createdAt = createdAt;
  }

  public static Order reconstitute(String id, String userId, List<OrderItem> items, OrderStatus status,
                                   BigDecimal totalAmount, Instant createdAt, Instant updatedAt) {
    return new Order(id, userId, items, status, totalAmount, createdAt, updatedAt);
  }

  public static Order create(String userId, List<OrderItem> items, BigDecimal total) {
    if (items == null || items.isEmpty()) {
      throw new IllegalArgumentException("Order must have at least one item");
    }
    return new Order(
        userId,
        items,
        total,
        OrderStatus.PENDING,
        Instant.now()
    );
  }

  public void confirm() {
    if(status != OrderStatus.PENDING) {
      throw new InvalidOrderStateException("Can only confirm PENDING order, current ="+status);
    }
    status = OrderStatus.CONFIRMED;
    updatedAt = Instant.now();
  }

  public void cancel(String reason) {
    if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
      throw new InvalidOrderStateException("Cannot cancel order, current ="+status);
    }
    status = OrderStatus.CANCELLED;
    updatedAt = Instant.now();
  }

  public void ship() {
    if (status != OrderStatus.CONFIRMED) {
      throw new InvalidOrderStateException("Can only ship CONFIRMED order, current="+status);
    }
    status = OrderStatus.SHIPPED;
    updatedAt = Instant.now();
  }

  public void deliver() {
    if (status != OrderStatus.SHIPPED) {
      throw new InvalidOrderStateException("Can only deliver SHIPPED order, current="+status);
    }
    status = OrderStatus.DELIVERED;
    updatedAt = Instant.now();
  }

  public void markPaymentFailed() {
    if (status != OrderStatus.PENDING) {
      throw new InvalidOrderStateException("Can only payment failed for PENDING order, current="+status);
    }
    status = OrderStatus.PAYMENT_FAILED;
    updatedAt = Instant.now();
  }

}
