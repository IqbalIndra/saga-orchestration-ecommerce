package com.ecommerce.saga.orchestration.payment.adapter.out.messaging;

import com.ecommerce.saga.orchestration.common.events.payment.PaymentFailedEvent;
import com.ecommerce.saga.orchestration.common.events.payment.PaymentProcessedEvent;
import com.ecommerce.saga.orchestration.common.events.payment.PaymentRefundedEvent;
import com.ecommerce.saga.orchestration.payment.application.port.in.PaymentEventPublisher;
import com.ecommerce.saga.orchestration.payment.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KafkaPaymentEventPublisher implements PaymentEventPublisher {
  private final KafkaTemplate<String,Object> kafkaTemplate;

  @Override
  public void publishPaymentCharged(String sagaId, Payment payment) {
    var event = new PaymentProcessedEvent(
        sagaId,
        payment.getOrderId(),
        payment.getId(),
        payment.getAmount(),
        "SIMULATED",
        Instant.now()
    );
    kafkaTemplate.send("payment-charged", sagaId, event);
  }

  @Override
  public void publishPaymentFailed(String sagaId, Payment payment) {
    var event = new PaymentFailedEvent(
        sagaId,
        payment.getOrderId(),
        payment.getAmount(),
        payment.getFailureReason(),
        Instant.now()
    );
    kafkaTemplate.send("payment-failed", sagaId, event);
  }

  @Override
  public void publishPaymentRefunded(String sagaId, Payment payment) {
    var event = new PaymentRefundedEvent(sagaId, payment.getOrderId(), Instant.now());
    kafkaTemplate.send("payment-refunded", sagaId, event);
  }
}
