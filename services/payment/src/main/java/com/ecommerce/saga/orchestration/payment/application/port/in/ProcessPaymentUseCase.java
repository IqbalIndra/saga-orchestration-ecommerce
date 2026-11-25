package com.ecommerce.saga.orchestration.payment.application.port.in;

import com.ecommerce.saga.orchestration.payment.domain.model.Payment;

import java.math.BigDecimal;

public interface ProcessPaymentUseCase {
  Payment process(Command command);

  record Command(
      String eventId,
      String sagaId,
      String idempotencyKey,
      String orderId,
      BigDecimal amount
  ) {}
}
