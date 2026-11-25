package com.ecommerce.saga.orchestration.payment.adapter.in.messaging;

import com.ecommerce.saga.orchestration.common.events.inventory.StockReservedEvent;
import com.ecommerce.saga.orchestration.common.events.payment.ChargePaymentCommand;
import com.ecommerce.saga.orchestration.common.events.payment.RefundPaymentCommand;
import com.ecommerce.saga.orchestration.payment.application.port.in.ProcessPaymentUseCase;
import com.ecommerce.saga.orchestration.payment.application.port.in.RefundPaymentUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSagaConsumer {

  private final ProcessPaymentUseCase processPaymentUseCase;
  private final RefundPaymentUseCase refundPaymentUseCase;

  @KafkaListener(
      topics = "payment-charge",
      groupId = "payment-service",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void onChargePaymentCommand(@Payload ChargePaymentCommand command,
                                     @Header("event-id") String eventId) {
    log.info(
        "Processing Charge Payment Command , eventId={}, sagaId={}, orderId={}",
        eventId,
        command.sagaId(),
        command.orderId()
    );

    processPaymentUseCase.process(new ProcessPaymentUseCase.Command(
        eventId,
        command.sagaId(),
        "idemp:"+command.orderId(),
        command.orderId(),
        command.totalAmount()
    ));
  }

  @KafkaListener(
      topics = "payment-refund",
      groupId = "payment-service",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void onRefundPaymentUseCase(@Payload RefundPaymentCommand command,
                                     @Header("event-id") String eventId) {
    log.info(
        "Processing Refund Payment Command , eventId={}, sagaId={}, paymentId={}, orderId={}",
        eventId,
        command.sagaId(),
        command.paymentId(),
        command.orderId()
    );
    refundPaymentUseCase.refund(new RefundPaymentUseCase.RefundCommand(
        eventId,
        command.sagaId(),
        command.paymentId()
    ));
  }

}
