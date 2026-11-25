package com.ecommerce.saga.orchestration.orchestrator.adapter.in.messaging;

import com.ecommerce.saga.orchestration.common.events.inventory.*;
import com.ecommerce.saga.orchestration.common.events.order.*;
import com.ecommerce.saga.orchestration.common.events.payment.ChargePaymentCommand;
import com.ecommerce.saga.orchestration.common.events.payment.PaymentFailedEvent;
import com.ecommerce.saga.orchestration.common.events.payment.PaymentProcessedEvent;
import com.ecommerce.saga.orchestration.orchestrator.application.port.out.OrderPlacementSagaRepository;
import com.ecommerce.saga.orchestration.orchestrator.application.port.out.OutboxEventRepository;
import com.ecommerce.saga.orchestration.orchestrator.domain.model.OrderPlacementSaga;
import com.ecommerce.saga.orchestration.orchestrator.domain.model.OutboxEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderOrchestrator {
  private final OrderPlacementSagaRepository orderPlacementSagaRepository;
  private final OutboxEventRepository outboxEventRepository;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = "order-created", groupId = "orchestrator-service",
      containerFactory = "kafkaListenerContainerFactory")
  @Transactional
  public void onOrderCreatedEvent(@Payload OrderCreatedEvent evt) {
    var orderPlacementSaga = OrderPlacementSaga.create(UUID.randomUUID().toString(), evt.orderId());
    orderPlacementSagaRepository.save(orderPlacementSaga);
    publish(orderPlacementSaga.getId() , "inventory-reserve" , new ReserveStockCommand(
        orderPlacementSaga.getId(),
        evt.orderId(),
        evt.totalAmount(),
        evt.items().stream()
            .map(i -> new ReserveStockCommand.OrderItem(
                i.productId(),
                i.quantity()
            ))
            .toList()
    ));
  }

  @KafkaListener(topics = "order-confirmed", groupId = "orchestrator-service",
      containerFactory = "kafkaListenerContainerFactory")
  @Transactional
  public void onOrderConfirmedEvent(@Payload OrderConfirmedEvent evt) {
    var orderPlacementSaga = orderPlacementSagaRepository.findById(evt.sagaId()).orElseThrow();
    orderPlacementSaga.markAsConfirmed();

    orderPlacementSagaRepository.save(orderPlacementSaga);
  }

  @KafkaListener(topics = "order-cancelled", groupId = "orchestrator-service",
      containerFactory = "kafkaListenerContainerFactory")
  @Transactional
  public void onOrderCancelledEvent(@Payload OrderCancelledEvent evt) {
    var orderPlacementSaga = orderPlacementSagaRepository.findById(evt.sagaId()).orElseThrow();
    orderPlacementSaga.markAsFailed();

    orderPlacementSagaRepository.save(orderPlacementSaga);
  }

  @KafkaListener(topics = "inventory-reserved", groupId = "orchestrator-service",
      containerFactory = "kafkaListenerContainerFactory")
  @Transactional
  public void onStockReservedEvent(@Payload StockReservedEvent evt) {
    var orderPlacementSaga = orderPlacementSagaRepository.findById(evt.sagaId()).orElseThrow();
    orderPlacementSaga.markInventoryReserved(evt.reservationId());
    orderPlacementSagaRepository.save(orderPlacementSaga);

    publish(evt.sagaId(), "payment-charge", new ChargePaymentCommand(
        orderPlacementSaga.getId(),
        orderPlacementSaga.getOrderId(),
        evt.totalAmount(),
        evt.reservedItem().stream()
            .map(i -> new ChargePaymentCommand.ReservedItem(
                i.productId(),
                i.quantity()
            ))
            .toList()
    ));
  }

  @KafkaListener(topics = "inventory-failed", groupId = "orchestrator-service",
      containerFactory = "kafkaListenerContainerFactory")
  @Transactional
  public void onStockReservationFailedEvent(@Payload StockReservationFailedEvent evt) {
    OrderPlacementSaga orderPlacementSaga = orderPlacementSagaRepository.findById(evt.sagaId()).orElseThrow();
    orderPlacementSaga.fail("StockReservation", evt.reason());
    orderPlacementSagaRepository.save(orderPlacementSaga);

    publish(evt.sagaId(), "order-cancel", new CancelOrderCommand(
        orderPlacementSaga.getId(),
        orderPlacementSaga.getOrderId(),
        "Saga failed:"+evt.reason()
    ));
  }

  @KafkaListener(topics = "inventory-released", groupId = "orchestrator-service",
      containerFactory = "kafkaListenerContainerFactory")
  public void onStockReleaseEvent(@Payload StockReleasedEvent evt) {
    OrderPlacementSaga orderPlacementSaga = orderPlacementSagaRepository.findById(evt.sagaId()).orElseThrow();
    orderPlacementSaga.fail(orderPlacementSaga.getFailedStep(), orderPlacementSaga.getFailureReason());
    orderPlacementSaga.markAsCompensated();
    orderPlacementSagaRepository.save(orderPlacementSaga);

    publish(evt.sagaId(), "order-payment-fail", new PaymentFailOrderCommand(
        orderPlacementSaga.getOrderId(),
        "Saga failed:"+orderPlacementSaga.getFailureReason()
    ));
  }


  @KafkaListener(topics = "payment-charged", groupId = "orchestrator-service",
      containerFactory = "kafkaListenerContainerFactory")
  @Transactional
  public void onPaymentProcessedEvent(@Payload PaymentProcessedEvent evt) {
    var orderPlacementSaga = orderPlacementSagaRepository.findById(evt.sagaId()).orElseThrow();
    orderPlacementSaga.markPaymentProcessed(evt.paymentId());
    orderPlacementSagaRepository.save(orderPlacementSaga);

    publish(evt.sagaId(), "order-confirm", new ConfirmOrderCommand(
        orderPlacementSaga.getId(),
        orderPlacementSaga.getOrderId()
    ));
  }

  @KafkaListener(topics = "payment-failed", groupId = "orchestrator-service",
      containerFactory = "kafkaListenerContainerFactory")
  @Transactional
  public void onPaymentFailedEvent(@Payload PaymentFailedEvent evt) {
    var orderPlacementSaga = orderPlacementSagaRepository.findById(evt.sagaId()).orElseThrow();
    orderPlacementSaga.fail("ChargePayment", evt.reason());
    orderPlacementSaga.startCompensatingInventory(evt.reason());
    orderPlacementSagaRepository.save(orderPlacementSaga);

    publish(evt.sagaId(), "inventory-release", new ReleaseStockCommand(
        orderPlacementSaga.getId(),
        orderPlacementSaga.getReservationId(),
        orderPlacementSaga.getOrderId()
    ));
  }


  private void publish(String sagaId, String topic, Object event) {
    try {
      outboxEventRepository.save(
          OutboxEvent.create(
              UUID.randomUUID().toString(),
              sagaId,
              "Saga",
              topic,
              event.getClass().getName(),
              objectMapper.writeValueAsString(event)
          )
      );
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }
}
