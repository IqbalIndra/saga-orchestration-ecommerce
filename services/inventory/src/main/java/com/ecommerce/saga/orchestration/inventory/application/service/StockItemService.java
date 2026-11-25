package com.ecommerce.saga.orchestration.inventory.application.service;

import com.ecommerce.saga.orchestration.inventory.application.port.in.*;
import com.ecommerce.saga.orchestration.inventory.application.port.out.InventoryEventPublisher;
import com.ecommerce.saga.orchestration.inventory.application.port.out.ProcessedEventRepository;
import com.ecommerce.saga.orchestration.inventory.application.port.out.ReservationRepository;
import com.ecommerce.saga.orchestration.inventory.application.port.out.StockItemRepository;
import com.ecommerce.saga.orchestration.inventory.domain.model.ProcessedEvent;
import com.ecommerce.saga.orchestration.inventory.domain.model.Reservation;
import com.ecommerce.saga.orchestration.inventory.domain.model.StockItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockItemService implements ReservedStockUseCase, ReleaseStockUseCase,
    ConfirmStockUseCase, UpdateStockUseCase, CheckStockUseCase {

  private final StockItemRepository stockItemRepository;
  private final ReservationRepository reservationRepository;
  private final ProcessedEventRepository processedEventRepository;
  private final InventoryEventPublisher inventoryEventPublisher;

  @Override
  @Transactional
  public ReservationResult reserve(String eventId, String sagaId, String orderId, BigDecimal totalAmount,
                                   List<ReservationItem> items) {
    if (processedEventRepository.isExistById(eventId)) {
      return new ReservationResult(false, null, "Already published event");
    }

    processedEventRepository.save(ProcessedEvent.create(eventId));

    List<String> productIds = items.stream()
        .map(ReservationItem::productId)
        .toList();

    Map<String, StockItem> stockItems = stockItemRepository.findAllByProductIds(productIds).stream()
        .collect(Collectors.toMap(StockItem::getProductId, stockItem -> stockItem));

    List<String> problems = new ArrayList<>();
    for (ReservationItem item : items) {
      StockItem stockItem = stockItems.get(item.productId());

      if(Objects.isNull(stockItem)) {
        problems.add("Stock not found in Inventory for productId=%s".formatted(item.productId()));
      } else if(!stockItem.canReserve(item.quantity())) {
        problems.add("Insufficient stock: available=%d, requested=%d"
            .formatted(stockItem.getAvailableQuantity(), item.quantity()));
      }
    }

    if(!problems.isEmpty()) {
      String reason = String.join("; ", problems);
      inventoryEventPublisher.publishStockReservationFailed(sagaId, orderId, reason);

      return new ReservationResult(false, null, reason);
    }

    List<Reservation.ReservedItem> reserved = new ArrayList<>();
    for (ReservationItem item : items) {
      StockItem stockItem = stockItems.get(item.productId());
      stockItem.reserve(item.quantity());
      stockItemRepository.save(stockItem);
      reserved.add(new Reservation.ReservedItem(
          item.productId(),
          item.quantity()
      ));
    }

    var reservation = Reservation.create(
        orderId,
        reserved,
        Instant.now()
    );

    var saved = reservationRepository.save(reservation);

    List<InventoryEventPublisher.ReservedItem> reservedItems = items.stream()
        .map(i -> new InventoryEventPublisher.ReservedItem(i.productId(), i.quantity()))
        .toList();

    inventoryEventPublisher.publishStockReserved(sagaId, saved.getId(), orderId, totalAmount, reservedItems);

    return new ReservationResult(true, saved.getId(), null);
  }

  @Override
  @Transactional
  public void setStock(String productId, int quantity) {
    StockItem stockItem = stockItemRepository.findByProductId(productId)
        .orElse(StockItem.create(productId));
    stockItem.setStock(quantity);
    stockItemRepository.save(stockItem);
    log.info("Stock set for productId={}, quantity={}", productId, quantity);
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Integer> getStock(List<String> productIds) {
    return stockItemRepository.findAllByProductIds(productIds)
        .stream()
        .collect(Collectors.toMap(StockItem::getProductId, StockItem::getAvailableQuantity));
  }

  @Override
  @Transactional
  public void release(String eventId, String sagaId, String orderId) {
    if (processedEventRepository.isExistById(eventId)) {
      return;
    }
    processedEventRepository.save(ProcessedEvent.create(eventId));

    var reservation = getReservation(orderId);
    var reservedItems = reservation.getItems();

    var products = reservation.getItems().stream()
        .map(Reservation.ReservedItem::productId)
        .toList();

    var stockItems = stockItemRepository.findAllByProductIds(products)
        .stream()
        .collect(Collectors.toMap(StockItem::getProductId, stock -> stock));

    boolean emptyStock = reservedItems.stream()
        .map(i -> stockItems.get(i.productId()))
        .anyMatch(Objects::isNull);

    if (emptyStock) {
      return;
    }

    for (var reserved : reservedItems) {
      StockItem stock = stockItems.get(reserved.productId());
      stock.release(reserved.quantity());
      stockItemRepository.save(stock);
    }

    reservation.cancelled();
    reservationRepository.save(reservation);

    inventoryEventPublisher.publishStockReleased(sagaId, orderId);
  }

  @Override
  @Transactional
  public void confirm(String eventId, String orderId) {
    if (processedEventRepository.isExistById(eventId)) {
      return;
    }
    processedEventRepository.save(ProcessedEvent.create(eventId));

    Reservation reservation = getReservation(orderId);
    List<Reservation.ReservedItem> reservedItems = reservation.getItems();

    List<String> products = reservation.getItems().stream()
        .map(Reservation.ReservedItem::productId)
        .toList();

    Map<String, StockItem> stockItems = stockItemRepository.findAllByProductIds(products)
        .stream()
        .collect(Collectors.toMap(StockItem::getProductId, stock -> stock));

    boolean emptyStock = reservedItems.stream()
        .map(i -> stockItems.get(i.productId()))
        .anyMatch(Objects::isNull);

    if (emptyStock) {
      return;
    }

    for (var reserved : reservedItems) {
      StockItem stock = stockItems.get(reserved.productId());
      stock.confirmReservation(reserved.quantity());
      stockItemRepository.save(stock);
    }

    reservation.confirmed();
    reservationRepository.save(reservation);
  }

  private Reservation getReservation(String orderId) {
    return reservationRepository.findByOrderId(orderId).orElseThrow();
  }

  @Override
  @Transactional(readOnly = true)
  public StockCheckResult checkStock(List<StockCheckItem> items) {
    List<String> productIds = items.stream()
        .map(StockCheckItem::productId)
        .toList();

    Map<String, StockItem> stockItems = stockItemRepository.findAllByProductIds(productIds)
        .stream()
        .collect(Collectors.toMap(StockItem::getProductId, stock -> stock));

    List<StockCheckResult.ItemAvailability> availability = items.stream()
        .map(item -> {
          StockItem stock = stockItems.get(item.productId());
          int available = (Objects.isNull(stock)) ? 0 : stock.getAvailableQuantity();
          return new StockCheckResult.ItemAvailability(
              item.productId(),
              item.quantity(),
              available,
              available >= item.quantity());
        })
        .toList();
    boolean allAvailable = availability.stream().allMatch(StockCheckResult.ItemAvailability::sufficient);
    return new StockCheckResult(allAvailable, availability);
  }
}
