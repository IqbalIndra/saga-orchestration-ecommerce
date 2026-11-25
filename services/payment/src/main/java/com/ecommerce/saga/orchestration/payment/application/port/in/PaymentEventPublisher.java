package com.ecommerce.saga.orchestration.payment.application.port.in;

import com.ecommerce.saga.orchestration.payment.domain.model.Payment;

public interface PaymentEventPublisher {
  void publishPaymentCharged(String sagaId, Payment payment);
  void publishPaymentFailed(String sagaId, Payment payment);
  void publishPaymentRefunded(String sagaId, Payment payment);
}
