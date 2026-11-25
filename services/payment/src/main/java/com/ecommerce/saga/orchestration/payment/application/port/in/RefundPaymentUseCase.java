package com.ecommerce.saga.orchestration.payment.application.port.in;

public interface RefundPaymentUseCase {
  void refund(RefundCommand command);

  record RefundCommand(String eventId, String sagaId, String paymentId){}
}
