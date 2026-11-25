package com.ecommerce.saga.orchestration.order.application.service;

import com.ecommerce.saga.orchestration.order.application.port.in.CreateOrderUseCase;
import com.ecommerce.saga.orchestration.order.application.port.out.OrderEventPublisher;
import com.ecommerce.saga.orchestration.order.application.port.out.OrderRepository;
import com.ecommerce.saga.orchestration.order.application.port.out.StockCheckGateway;
import com.ecommerce.saga.orchestration.order.domain.Order;
import com.ecommerce.saga.orchestration.order.domain.OrderItem;
import com.ecommerce.saga.orchestration.order.domain.exception.EmptyItemsException;
import com.ecommerce.saga.orchestration.order.domain.exception.OutOfStockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateOrderService implements CreateOrderUseCase {
  private final OrderRepository orderRepository;
  private final OrderEventPublisher orderEventPublisher;
  private final StockCheckGateway stockCheckGateway;

  @Override
  public Order createOrder(CreateOrderCommand command) {
    if(Objects.isNull(command.items()) || command.items().isEmpty()) {
      throw new EmptyItemsException("Item is empty");
    }

    var list = command.items().stream()
        .map(i -> new StockCheckGateway.StockItem(i.productId(), i.quantity()))
        .toList();

    var stockCheckResult = stockCheckGateway.checkStock(list);

    if(!stockCheckResult.allAvailable()) {
      throw new OutOfStockException("One or more item out of stock");
    }

    List<OrderItem> items = command.items().stream()
        .map(item -> new OrderItem(item.productId(), item.quantity(), item.price()))
        .toList();

    BigDecimal total = items.stream()
        .map(OrderItem::subTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    Order order = Order.create(
        command.customerId(),
        items,
        total
    );

    Order saved = orderRepository.save(order);
    log.info("Order created orderId={} userId={} total={}", saved.getId(), saved.getUserId(), total);

    orderEventPublisher.publish(
        saved.getId(),
        saved.getUserId(),
        saved.getTotalAmount(),
        saved.getItems()
    );

    return saved;
  }
}
