package com.ecommerce.saga.orchestration.payment.adapter.out.persistence;

import com.ecommerce.saga.orchestration.payment.adapter.out.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaPaymentRepository extends JpaRepository<PaymentEntity, String> {
  Optional<PaymentEntity> findByOrderId(String orderId);
}