package com.ecommerce.saga.orchestration.inventory.adapter.in.messaging;

import com.ecommerce.saga.orchestration.common.events.inventory.ConfirmStockCommand;
import com.ecommerce.saga.orchestration.common.events.inventory.ReleaseStockCommand;
import com.ecommerce.saga.orchestration.common.events.inventory.ReserveStockCommand;
import com.ecommerce.saga.orchestration.inventory.application.port.in.ConfirmStockUseCase;
import com.ecommerce.saga.orchestration.inventory.application.port.in.ReleaseStockUseCase;
import com.ecommerce.saga.orchestration.inventory.application.port.in.ReservedStockUseCase;
import com.ecommerce.saga.orchestration.inventory.application.port.out.ProcessedEventRepository;
import com.ecommerce.saga.orchestration.inventory.domain.model.ProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventorySagaConsumer {
  private final ReservedStockUseCase reservedStockUseCase;
  private final ReleaseStockUseCase releaseStockUseCase;
  private final ConfirmStockUseCase confirmStockUseCase;

  @KafkaListener(
      topics = "inventory-reserve",
      groupId = "inventory-service",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void onReserveStockCommand(@Payload ReserveStockCommand command,
                                    @Header("event-id") String eventId) {
    log.info("Processing ReserveStockCommand, sagaId={}, eventId={}, orderId={}" , command.sagaId(), eventId,
        command.orderId());

    List<ReservedStockUseCase.ReservationItem> reservationItems = command.items().stream()
        .map(i -> new ReservedStockUseCase.ReservationItem(i.productId(), i.quantity()))
        .toList();
    reservedStockUseCase.reserve(eventId, command.sagaId(), command.orderId(), command.totalAmount(), reservationItems);
  }

  @KafkaListener(
      topics = "inventory-release",
      groupId = "inventory-service",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void onReleaseStockCommand(@Payload ReleaseStockCommand command,
                                    @Header("event-id") String eventId) {
    log.info("Processing Release Stock, sagaId={} , eventId={}, orderId={}" , command.sagaId(), eventId,
        command.orderId());

    releaseStockUseCase.release(eventId, command.sagaId(), command.orderId());
  }

  @KafkaListener(
      topics = "inventory-confirm",
      groupId = "inventory-service",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void onConfirmStockCommand(@Payload ConfirmStockCommand command,
                                    @Header("event-id") String eventId) {
    log.info("Processing Confirm Stock, sagaId={}, eventId={}, orderId={}" , command.sagaId(), eventId,
        command.orderId());

    confirmStockUseCase.confirm(eventId, command.orderId());
  }
}
