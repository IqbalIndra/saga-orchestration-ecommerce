package com.ecommerce.saga.orchestration.order.domain;

import com.ecommerce.saga.orchestration.order.domain.exception.InvalidOrderStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {
  private final String id = UUID.randomUUID().toString();
  private final Instant now = Instant.now();

  @Test
  public void shouldConfirming() {
    var order = createOrder(OrderStatus.PENDING);
    order.confirm();

    assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    assertTrue(now.isBefore(order.getUpdatedAt()));
  }

  @Test
  public void shouldThrowExceptionWhenConfirming() {
    var order = createOrder(OrderStatus.PAYMENT_FAILED);
    assertThrows(InvalidOrderStateException.class, order::confirm);
  }

  @Test
  public void shouldCancelling() {
    var order = createOrder(OrderStatus.PENDING);
    order.cancel("error");

    assertEquals(OrderStatus.CANCELLED, order.getStatus());
    assertTrue(now.isBefore(order.getUpdatedAt()));
  }

  @Test
  public void shouldThrowExceptionWhenCancelling() {
    var order = createOrder(OrderStatus.SHIPPED);
    assertThrows(InvalidOrderStateException.class, () -> order.cancel("error"));
  }

  @Test
  public void shouldShipping() {
    var order = createOrder(OrderStatus.CONFIRMED);
    order.ship();

    assertEquals(OrderStatus.SHIPPED, order.getStatus());
    assertTrue(now.isBefore(order.getUpdatedAt()));
  }

  @Test
  public void shouldThrowExceptionWhenShipping() {
    var order = createOrder(OrderStatus.CANCELLED);
    assertThrows(InvalidOrderStateException.class, order::ship);
  }

  @Test
  public void shouldDelivering() {
    var order = createOrder(OrderStatus.SHIPPED);
    order.deliver();

    assertEquals(OrderStatus.DELIVERED, order.getStatus());
    assertTrue(now.isBefore(order.getUpdatedAt()));
  }

  @Test
  public void shouldThrowExceptionWhenDelivering() {
    var order = createOrder(OrderStatus.CONFIRMED);
    assertThrows(InvalidOrderStateException.class, order::deliver);
  }

  @Test
  public void shouldMarkingPaymentFailed() {
    var order = createOrder(OrderStatus.PENDING);
    order.markPaymentFailed();

    assertEquals(OrderStatus.PAYMENT_FAILED, order.getStatus());
    assertTrue(now.isBefore(order.getUpdatedAt()));
  }

  @Test
  public void shouldThrowExceptionWhenMarkingPaymentFailed() {
    var order = createOrder(OrderStatus.CONFIRMED);
    assertThrows(InvalidOrderStateException.class, order::markPaymentFailed);
  }


  private Order createOrder(OrderStatus status) {
    return Order.reconstitute(
        id,
        "usr",
        List.of(),
        status,
        BigDecimal.TEN,
        now,
        now
    );
  }
}