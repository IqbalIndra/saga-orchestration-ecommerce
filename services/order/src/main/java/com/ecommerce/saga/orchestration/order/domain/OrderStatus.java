package com.ecommerce.saga.orchestration.order.domain;

public enum OrderStatus {
  PENDING,
  CONFIRMED,
  PAYMENT_FAILED,
  CANCELLED,
  SHIPPED,
  DELIVERED
}
