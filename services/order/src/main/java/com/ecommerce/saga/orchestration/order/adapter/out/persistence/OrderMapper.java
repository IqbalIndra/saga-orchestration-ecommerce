package com.ecommerce.saga.orchestration.order.adapter.out.persistence;

import com.ecommerce.saga.orchestration.order.adapter.out.persistence.entity.OrderEntity;
import com.ecommerce.saga.orchestration.order.adapter.out.persistence.entity.OrderItemEntity;
import com.ecommerce.saga.orchestration.order.domain.Order;
import com.ecommerce.saga.orchestration.order.domain.OrderItem;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

  @Mapping(target = "items", ignore = true)
  OrderEntity toEntity(Order order);

  @AfterMapping
  default void mappingItems(Order order, @MappingTarget OrderEntity entity) {
    List<OrderItemEntity> items = order.getItems().stream()
        .map(item -> OrderItemEntity.builder()
            .order(entity)
            .productId(item.productId())
            .quantity(item.quantity())
            .unitPrice(item.price())
            .build()
        ).toList();
    entity.setItems(items);
  }

  default Order toDomain(OrderEntity entity) {
    List<OrderItem> items = entity.getItems().stream()
        .map(item -> new OrderItem(
            item.getProductId(),
            item.getQuantity(),
            item.getUnitPrice()
        )).toList();

    return Order.reconstitute(
        entity.getId(),
        entity.getUserId(),
        items,
        entity.getStatus(),
        entity.getTotalAmount(),
        entity.getCreatedAt(),
        entity.getUpdatedAt()
    );
  }
}
