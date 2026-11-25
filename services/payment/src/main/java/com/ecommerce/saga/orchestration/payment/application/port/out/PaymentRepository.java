package com.ecommerce.saga.orchestration.payment.application.port.out;

import com.ecommerce.saga.orchestration.payment.domain.model.Payment;

import java.util.Optional;

public interface PaymentRepository {
  Payment save(Payment payment);
  Optional<Payment> findById(String id);
  Optional<Payment> findByOrderId(String orderId);
}
