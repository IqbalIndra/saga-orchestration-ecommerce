package com.ecommerce.saga.orchestration.order.application.service;

import com.ecommerce.saga.orchestration.order.application.port.in.UpdateOrderUseCase;
import com.ecommerce.saga.orchestration.order.application.port.out.OrderRepository;
import com.ecommerce.saga.orchestration.order.domain.Order;
import com.ecommerce.saga.orchestration.order.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UpdateStatusOrderService implements UpdateOrderUseCase {

  private final OrderRepository orderRepository;

  @Override
  public Order updateOrderStatus(UpdateOrderCommand command) {
    Order order = orderRepository.findById(command.orderId())
        .orElseThrow();
    if (OrderStatus.SHIPPED == command.status()) {
      order.ship();
      return orderRepository.save(order);
    } else if(OrderStatus.DELIVERED == command.status()) {
      order.deliver();
      return orderRepository.save(order);
    } else {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Only Shipped and Delivered transitions are allowed via this endpoint");
    }

  }
}
