package com.ecommerce.saga.orchestration.order.application.service;

import com.ecommerce.saga.orchestration.order.application.port.in.CreateOrderUseCase;
import com.ecommerce.saga.orchestration.order.application.port.out.OrderEventPublisher;
import com.ecommerce.saga.orchestration.order.application.port.out.OrderRepository;
import com.ecommerce.saga.orchestration.order.application.port.out.StockCheckGateway;
import com.ecommerce.saga.orchestration.order.domain.Order;
import com.ecommerce.saga.orchestration.order.domain.OrderItem;
import com.ecommerce.saga.orchestration.order.domain.OrderStatus;
import com.ecommerce.saga.orchestration.order.domain.exception.EmptyItemsException;
import com.ecommerce.saga.orchestration.order.domain.exception.OutOfStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {
  @Mock private OrderRepository repository;
  @Mock private OrderEventPublisher publisher;
  @Mock private StockCheckGateway gateway;
  @InjectMocks private CreateOrderService service;

  private StockCheckGateway.StockCheckResult stockCheck;
  private Order domain;
  private CreateOrderUseCase.CreateOrderCommand command;

  private final String id = UUID.randomUUID().toString();
  private final Instant now = Instant.now();

  @BeforeEach
  void setUp() {
    domain = Order.reconstitute(
        null,
        "ord",
        List.of(
            new OrderItem("prod1", 1, BigDecimal.TEN)
        ),
        OrderStatus.PENDING,
        BigDecimal.TEN,
        now,
        now
    );

    command = createCommand();
  }

  @Test
  void shouldCreatingOrder() {
    stockCheck = createStockCheckAvailable(true);

    when(gateway.checkStock(anyList())).thenReturn(stockCheck);
    when(repository.save(any())).thenReturn(domain);
    doNothing().when(publisher).publish(any(), anyString(), any(BigDecimal.class), anyList());

    var result = service.createOrder(command);

    assertThat(result).isNotNull();
    assertThat(result.getTotalAmount()).isEqualTo(BigDecimal.TEN);
    assertThat(result.getItems()).isEqualTo(domain.getItems());

    verify(gateway).checkStock(anyList());
    verify(repository).save(any());
    verify(publisher, times(1)).publish(
        any(), anyString(), any(BigDecimal.class), anyList()
    );
  }

  @Test
  void shouldThrowExceptionWhenItemsIsEmpty() {
    command = new CreateOrderUseCase.CreateOrderCommand("cust", List.of());
    assertThrows(EmptyItemsException.class, () -> service.createOrder(command));
  }

  @Test
  void shouldThrowExceptionWhenStockNoAvailable() {
    command = createCommand();
    stockCheck = createStockCheckAvailable(false);

    when(gateway.checkStock(anyList())).thenReturn(stockCheck);

    assertThrows(OutOfStockException.class, () -> service.createOrder(command));
  }

  private CreateOrderUseCase.CreateOrderCommand createCommand() {
    return new CreateOrderUseCase.CreateOrderCommand(
        "cust",
        List.of(
            new CreateOrderUseCase.CreateOrderCommand.OrderItemCommand(
                "prod1", 1, BigDecimal.TEN
            )
        )
    );
  }

  private StockCheckGateway.StockCheckResult createStockCheckAvailable(boolean available) {
    return new StockCheckGateway.StockCheckResult(available);
  }
}