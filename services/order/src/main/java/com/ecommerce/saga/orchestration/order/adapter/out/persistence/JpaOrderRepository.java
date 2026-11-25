package com.ecommerce.saga.orchestration.order.adapter.out.persistence;

import com.ecommerce.saga.orchestration.order.adapter.out.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrderRepository extends JpaRepository<OrderEntity, String> {
}
