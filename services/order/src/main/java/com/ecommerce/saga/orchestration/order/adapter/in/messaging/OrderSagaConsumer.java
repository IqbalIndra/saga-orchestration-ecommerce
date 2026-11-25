package com.ecommerce.saga.orchestration.order.adapter.in.messaging;

import com.ecommerce.saga.orchestration.common.events.order.CancelOrderCommand;
import com.ecommerce.saga.orchestration.common.events.order.ConfirmOrderCommand;
import com.ecommerce.saga.orchestration.common.events.order.PaymentFailOrderCommand;
import com.ecommerce.saga.orchestration.order.application.port.out.OrderEventPublisher;
import com.ecommerce.saga.orchestration.order.application.port.out.OrderRepository;
import com.ecommerce.saga.orchestration.order.application.port.out.ProcessedEventRepository;
import com.ecommerce.saga.orchestration.order.domain.ProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSagaConsumer {
  private final OrderRepository orderRepository;
  private final ProcessedEventRepository processedEventRepository;
  private final OrderEventPublisher orderEventPublisher;

  @KafkaListener(
      topics = "order-confirm",
      groupId = "order-service",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void onConfirmOrderCommand(@Payload ConfirmOrderCommand command,
                                    @Header("event-id") String eventId) {
    log.info(
        "Processing ConfirmOrderCommand, eventId={}, sagaId={}, orderId={}",
        eventId,
        command.sagaId(),
        command.orderId()
    );

    if (processedEventRepository.isExistById(eventId)) {
      return;
    }
    processedEventRepository.save(ProcessedEvent.create(eventId));

    orderRepository.findById(command.orderId()).ifPresent(order -> {
      order.confirm();
      orderRepository.save(order);
      log.info("Order Confirm, orderId={}", order.getId());
      orderEventPublisher.publishOrderConfirmed(command.sagaId(), command.orderId());
    });
  }

  @KafkaListener(
      topics = "order-cancel",
      groupId = "order-service",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void onCancelOrderCommand(@Payload CancelOrderCommand command,
                                   @Header("event-id") String eventId) {
    log.info(
        "Processing CancelOrderCommand, eventId={}, sagaId={}, orderId={}, reason={}",
        eventId,
        command.sagaId(),
        command.orderId(),
        command.reason()
    );
    if (processedEventRepository.isExistById(eventId)) {
      return;
    }
    processedEventRepository.save(ProcessedEvent.create(eventId));

    orderRepository.findById(command.orderId()).ifPresent(order -> {
      order.cancel(command.reason());
      orderRepository.save(order);
      log.info("Order cancelled,  orderId={}", order.getId());
      orderEventPublisher.publishOrderCancelled(command.sagaId(), command.orderId(), command.reason());
    });
  }

  @KafkaListener(
      topics = "order-payment-fail",
      groupId = "order-service",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void onPaymentFailOrderCommand(@Payload PaymentFailOrderCommand command,
                                        @Header("event-id") String eventId) {
    log.info(
        "Processing CancelOrderCommand, eventId={}, orderId={}, reason={}",
        eventId,
        command.orderId(),
        command.reason()
    );
    if (processedEventRepository.isExistById(eventId)) {
      return;
    }
    processedEventRepository.save(ProcessedEvent.create(eventId));

    orderRepository.findById(command.orderId()).ifPresent(order -> {
      order.markPaymentFailed();
      orderRepository.save(order);
      log.info("Order Payment Failed,  orderId={}", order.getId());
    });
  }

}
