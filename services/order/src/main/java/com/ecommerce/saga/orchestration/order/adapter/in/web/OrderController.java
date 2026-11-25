package com.ecommerce.saga.orchestration.order.adapter.in.web;

import com.ecommerce.saga.orchestration.order.adapter.in.web.dto.CreateOrderRequest;
import com.ecommerce.saga.orchestration.order.adapter.in.web.dto.OrderResponse;
import com.ecommerce.saga.orchestration.order.application.port.in.CreateOrderUseCase;
import com.ecommerce.saga.orchestration.order.application.port.in.GetOrderUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
  private final CreateOrderUseCase createOrderUseCase;
  private final GetOrderUseCase getOrderUseCase;
  private final OrderResponseMapper orderResponseMapper;

  @GetMapping("/{id}")
  public ResponseEntity<OrderResponse> getOrderById(@PathVariable String id) {
    var result = getOrderUseCase.getOrderById(id);
    return ResponseEntity.ok(orderResponseMapper.toResponse(result));
  }

  @PostMapping
  public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
    var createOrderCommand = new CreateOrderUseCase.CreateOrderCommand(
        request.customerId(),
        request.items().stream()
            .map(orderItem -> new CreateOrderUseCase.CreateOrderCommand.OrderItemCommand(
                orderItem.productId(),
                orderItem.quantity(),
                orderItem.price()))
            .toList()
    );

    return ResponseEntity.ok(orderResponseMapper.toResponse(
        createOrderUseCase.createOrder(createOrderCommand)
    ));
  }


}
