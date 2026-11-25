package com.ecommerce.saga.orchestration.order.application.service;

import com.ecommerce.saga.orchestration.order.application.port.in.UpdateOrderUseCase;
import com.ecommerce.saga.orchestration.order.application.port.out.OrderRepository;
import com.ecommerce.saga.orchestration.order.domain.Order;
import com.ecommerce.saga.orchestration.order.domain.OrderStatus;
import com.ecommerce.saga.orchestration.order.domain.exception.InvalidOrderStateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateStatusOrderServiceTest {
  @Mock private OrderRepository repository;
  @InjectMocks private UpdateStatusOrderService service;

  private Order domain;
  private UpdateOrderUseCase.UpdateOrderCommand command;
  private final Instant now = Instant.now();

  @Test
  void shouldUpdateOrderToShipping() {
    domain = createDomain(OrderStatus.CONFIRMED);
    command = createCommand(OrderStatus.SHIPPED);

    when(repository.findById(anyString())).thenReturn(Optional.of(domain));

    service.updateOrderStatus(command);

    ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

    verify(repository).findById(anyString());
    verify(repository).save(orderCaptor.capture());

    var newDomain = orderCaptor.getValue();

    assertThat(newDomain.getStatus()).isEqualTo(OrderStatus.SHIPPED);

  }

  @Test
  void shouldUpdateOrderToDelivery() {
    domain = createDomain(OrderStatus.SHIPPED);
    command = createCommand(OrderStatus.DELIVERED);

    when(repository.findById(anyString())).thenReturn(Optional.of(domain));

    service.updateOrderStatus(command);

    ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

    verify(repository).findById(anyString());
    verify(repository).save(orderCaptor.capture());

    var newDomain = orderCaptor.getValue();

    assertThat(newDomain.getStatus()).isEqualTo(OrderStatus.DELIVERED);
  }

  @Test
  void shouldThrowExceptionWhenTransitingOrderStatus() {
    domain = createDomain(OrderStatus.CANCELLED);
    command = createCommand(OrderStatus.PENDING);

    when(repository.findById(anyString())).thenReturn(Optional.of(domain));

    assertThrows(ResponseStatusException.class, () -> service.updateOrderStatus(command));

    verify(repository).findById(anyString());
  }

  @Test
  void shouldThrowExceptionWhenEmptyOrder() {
    command = createCommand(OrderStatus.PENDING);
    when(repository.findById(anyString())).thenReturn(Optional.empty());

    assertThrows(Exception.class, () -> service.updateOrderStatus(command));

    verify(repository).findById(anyString());
  }

  @Test
  void shouldThrowExceptionWhenTransitionToShipping() {
    domain = createDomain(OrderStatus.PENDING);
    command = createCommand(OrderStatus.SHIPPED);

    when(repository.findById(anyString())).thenReturn(Optional.of(domain));

    assertThrows(InvalidOrderStateException.class, () -> service.updateOrderStatus(command));

    verify(repository).findById(anyString());
  }

  @Test
  void shouldThrowExceptionWhenTransitionToDelivery() {
    domain = createDomain(OrderStatus.PENDING);
    command = createCommand(OrderStatus.DELIVERED);

    when(repository.findById(anyString())).thenReturn(Optional.of(domain));

    assertThrows(InvalidOrderStateException.class, () -> service.updateOrderStatus(command));

    verify(repository).findById(anyString());
  }

  private UpdateOrderUseCase.UpdateOrderCommand createCommand(OrderStatus status) {
    return new UpdateOrderUseCase.UpdateOrderCommand("ord", status);
  }

  private Order createDomain(OrderStatus status) {
    return domain = Order.reconstitute(
        "id",
        "usr",
        List.of(),
        status,
        BigDecimal.TEN,
        now,
        now
    );
  }
}