package com.ecommerce.saga.orchestration.order.adapter.in.web.dto;

import com.ecommerce.saga.orchestration.order.domain.OrderItem;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
    @NotNull String customerId,
    @NotNull List<OrderItem> items
    ) {
}
