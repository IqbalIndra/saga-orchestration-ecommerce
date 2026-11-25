package com.ecommerce.saga.orchestration.order.adapter.in.web;

import com.ecommerce.saga.orchestration.order.adapter.in.web.dto.OrderResponse;
import com.ecommerce.saga.orchestration.order.domain.Order;
import com.ecommerce.saga.orchestration.order.domain.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderResponseMapper {

  @Mapping(target = "status", expression = "java(order.getStatus().name())")
  @Mapping(target = "total", source = "totalAmount")
  OrderResponse toResponse(Order order);

  @Mapping(target = "subTotal", expression = "java(item.subTotal())")
  OrderResponse.OrderItemResponse toItemResponse(OrderItem item);
}
