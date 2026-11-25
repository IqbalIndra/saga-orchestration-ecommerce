package com.ecommerce.saga.orchestration.inventory.adapter.out.messaging;

import com.ecommerce.saga.orchestration.common.events.inventory.StockReleasedEvent;
import com.ecommerce.saga.orchestration.common.events.inventory.StockReservationFailedEvent;
import com.ecommerce.saga.orchestration.common.events.inventory.StockReservedEvent;
import com.ecommerce.saga.orchestration.inventory.application.port.out.InventoryEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KafkaInventoryEventPublisher implements InventoryEventPublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Override
  public void publishStockReserved(String sagaId, String reservationId, String orderId, BigDecimal totalAmount, List<ReservedItem> items) {
    var event = new StockReservedEvent(
        sagaId,
        reservationId,
        orderId,
        totalAmount,
        items.stream().map(this::toReservedItem).toList(),
        Instant.now()
    );

    kafkaTemplate.send("inventory-reserved", sagaId, event);
  }

  private StockReservedEvent.ReservedItem toReservedItem(ReservedItem reservedItem) {
    return new StockReservedEvent.ReservedItem(reservedItem.productId(), reservedItem.quantity());
  }

  @Override
  public void publishStockReservationFailed(String sagaId, String orderId, String reason) {
    var event = new StockReservationFailedEvent(
        sagaId,
        orderId,
        null,
        reason,
        Instant.now()
    );
    kafkaTemplate.send("inventory-failed", sagaId, event);
  }

  @Override
  public void publishStockReleased(String sagaId, String orderId) {
    var event = new StockReleasedEvent(
        sagaId,
        orderId,
        Instant.now()
    );

    kafkaTemplate.send("inventory-released", sagaId, event);
  }

}
