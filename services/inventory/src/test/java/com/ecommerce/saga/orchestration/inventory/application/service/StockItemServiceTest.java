package com.ecommerce.saga.orchestration.inventory.application.service;

import com.ecommerce.saga.orchestration.inventory.application.port.in.CheckStockUseCase;
import com.ecommerce.saga.orchestration.inventory.application.port.in.ReservedStockUseCase;
import com.ecommerce.saga.orchestration.inventory.application.port.out.InventoryEventPublisher;
import com.ecommerce.saga.orchestration.inventory.application.port.out.ProcessedEventRepository;
import com.ecommerce.saga.orchestration.inventory.application.port.out.ReservationRepository;
import com.ecommerce.saga.orchestration.inventory.application.port.out.StockItemRepository;
import com.ecommerce.saga.orchestration.inventory.domain.model.ProcessedEvent;
import com.ecommerce.saga.orchestration.inventory.domain.model.Reservation;
import com.ecommerce.saga.orchestration.inventory.domain.model.ReservationStatus;
import com.ecommerce.saga.orchestration.inventory.domain.model.StockItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockItemServiceTest {

  @Mock private StockItemRepository stockItemRepository;
  @Mock private ReservationRepository reservationRepository;
  @Mock private InventoryEventPublisher inventoryEventPublisher;
  @Mock private ProcessedEventRepository processedEventRepository;

  @InjectMocks private StockItemService service;

  private StockItem stockDomain;
  private Reservation reservationDomain;

  private final String idStock = UUID.randomUUID().toString();
  private final String idReservation = UUID.randomUUID().toString();

  @BeforeEach
  public void setUp() {

    Instant now = Instant.now();

    stockDomain = new StockItem(idStock, "prod1", 0, 0, now, now);

    reservationDomain = Reservation.reconstitute(idReservation, "ord1",
        List.of(new Reservation.ReservedItem("prod1", 1)),
        ReservationStatus.PENDING, now, now);
  }

  @Test
  void shouldReserving() {
    stockDomain.setStock(10);

    when(stockItemRepository.findAllByProductIds(anyList())).thenReturn(List.of(stockDomain));
    when(reservationRepository.save(any(Reservation.class))).thenReturn(reservationDomain);
    when(processedEventRepository.isExistById(anyString())).thenReturn(false);
    doNothing().when(processedEventRepository).save(any(ProcessedEvent.class));

    var result = service.reserve("evt1","saga1","ord1", new BigDecimal(8000),
        List.of(new ReservedStockUseCase.ReservationItem("prod1", 1)));

    assertThat(result.success()).isTrue();
    assertThat(result.reservationId()).isEqualTo(reservationDomain.getId());
    assertThat(result.errorMessage()).isNull();
    assertThat(stockDomain.getAvailableQuantity()).isEqualTo(9);
    assertThat(stockDomain.getReservedQuantity()).isEqualTo(1);
    assertThat(reservationDomain.getStatus()).isEqualTo(ReservationStatus.PENDING);


    verify(stockItemRepository).findAllByProductIds(anyList());
    verify(stockItemRepository).save(any(StockItem.class));
    verify(reservationRepository).save(any(Reservation.class));
    verify(inventoryEventPublisher).publishStockReserved(anyString(), anyString(), anyString(), any(BigDecimal.class), anyList());
    verify(processedEventRepository).save(any(ProcessedEvent.class));
    verify(processedEventRepository).isExistById(anyString());
  }

  @Test
  void shouldIdempotencyProcessedEventWhileReserving() {
    when(processedEventRepository.isExistById(anyString())).thenReturn(true);

    var result = service.reserve("xxx","saga1","ord1", new BigDecimal(8000),
        List.of(new ReservedStockUseCase.ReservationItem("prod1", 1)));

    assertThat(result.success()).isFalse();
    assertThat(result.reservationId()).isNull();
    assertThat(result.errorMessage()).isNotNull();

    verify(processedEventRepository).isExistById(anyString());
  }

  @Test
  void shouldNotReservingWhenStockItemsNotFound() {
    when(processedEventRepository.isExistById(anyString())).thenReturn(false);
    doNothing().when(processedEventRepository).save(any(ProcessedEvent.class));
    when(stockItemRepository.findAllByProductIds(anyList())).thenReturn(List.of());

    var result = service.reserve("evt1","saga1", "ord1", new BigDecimal(8000),
        List.of(new ReservedStockUseCase.ReservationItem("prod2", 1)));

    assertThat(result.success()).isFalse();
    assertThat(result.reservationId()).isNull();
    assertThat(result.errorMessage()).isNotNull();

    verify(stockItemRepository).findAllByProductIds(anyList());
    verify(processedEventRepository).isExistById(anyString());
    verify(processedEventRepository).save(any(ProcessedEvent.class));
    verify(inventoryEventPublisher).publishStockReservationFailed(anyString(), anyString(), anyString());
  }

  @Test
  void shouldNotReservingWhenStockItemInSufficient() {
    stockDomain.setStock(10);

    when(processedEventRepository.isExistById(anyString())).thenReturn(false);
    doNothing().when(processedEventRepository).save(any(ProcessedEvent.class));
    when(stockItemRepository.findAllByProductIds(anyList())).thenReturn(List.of(stockDomain));

    var result = service.reserve("evt1","saga1", "ord1", new BigDecimal(8000),
        List.of(new ReservedStockUseCase.ReservationItem("prod1", 11)));

    assertThat(result.success()).isFalse();
    assertThat(result.reservationId()).isNull();
    assertThat(result.errorMessage()).isNotNull();

    verify(processedEventRepository).isExistById(anyString());
    verify(processedEventRepository).save(any(ProcessedEvent.class));
    verify(stockItemRepository).findAllByProductIds(anyList());
    verify(inventoryEventPublisher).publishStockReservationFailed(anyString(), anyString(), anyString());
  }

  @Test
  void shouldSetStock() {
    when(stockItemRepository.findByProductId(anyString())).thenReturn(Optional.of(stockDomain));

    service.setStock("prod1", 1);

    verify(stockItemRepository).findByProductId(anyString());
    verify(stockItemRepository).save(stockDomain);

    assertThat(stockDomain.getAvailableQuantity()).isEqualTo(1);
  }

  @Test
  void shouldSetStockWhenStockItemEmpty() {
    when(stockItemRepository.findByProductId(anyString())).thenReturn(Optional.empty());

    service.setStock("prod1", 1);

    verify(stockItemRepository).findByProductId(anyString());
    ArgumentCaptor<StockItem> stockCaptor = ArgumentCaptor.forClass(StockItem.class);

    verify(stockItemRepository).save(stockCaptor.capture());

    var newStock = stockCaptor.getValue();

    assertThat(newStock).isNotNull();
    assertThat(newStock.getAvailableQuantity()).isEqualTo(1);
  }

  @Test
  void shouldGetStock() {
    stockDomain.setStock(10);
    when(stockItemRepository.findAllByProductIds(anyList())).thenReturn(List.of(stockDomain));

    var result = service.getStock(List.of("prod1"));

    assertThat(result.isEmpty()).isFalse();
    assertThat(result.get("prod1")).isNotNull();
    assertThat(result.get("prod1")).isEqualTo(10);
  }

  @Test
  void shouldEmptyWhenGetStock() {
    when(stockItemRepository.findAllByProductIds(anyList())).thenReturn(List.of());

    var result = service.getStock(List.of("prod1"));

    assertThat(result.isEmpty()).isTrue();
  }

  @Test
  void shouldConfirming() {
    stockDomain.setStock(10);
    stockDomain.reserve(1);

    when(processedEventRepository.isExistById(anyString())).thenReturn(false);
    doNothing().when(processedEventRepository).save(any(ProcessedEvent.class));
    when(reservationRepository.findByOrderId(anyString())).thenReturn(Optional.of(reservationDomain));
    when(stockItemRepository.findAllByProductIds(anyList())).thenReturn(List.of(stockDomain));

    service.confirm("evt1","ord1");

    verify(reservationRepository).findByOrderId(anyString());
    verify(stockItemRepository).findAllByProductIds(anyList());
    verify(stockItemRepository).save(stockDomain);
    verify(reservationRepository).save(reservationDomain);

    verify(processedEventRepository).save(any(ProcessedEvent.class));
    verify(processedEventRepository).isExistById(anyString());
    assertThat(stockDomain.getReservedQuantity()).isEqualTo(0);
    assertThat(reservationDomain.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
  }

  @Test
  void shouldIdempotencyProcessedEventWhileConfirming() {
    when(processedEventRepository.isExistById(anyString())).thenReturn(true);

    service.confirm("evt1","ord1");

    verify(processedEventRepository).isExistById(anyString());
  }

  @Test
  void shouldNotConfirmingWhenEmptyOrMissingStock() {
    when(processedEventRepository.isExistById(anyString())).thenReturn(false);
    doNothing().when(processedEventRepository).save(any(ProcessedEvent.class));
    when(reservationRepository.findByOrderId(anyString())).thenReturn(Optional.of(reservationDomain));
    when(stockItemRepository.findAllByProductIds(anyList())).thenReturn(List.of(StockItem.create("prod2")));

    service.confirm("evt1","ord1");

    verify(processedEventRepository).isExistById(anyString());
    verify(processedEventRepository).save(any(ProcessedEvent.class));
    verify(reservationRepository).findByOrderId(anyString());
    verify(stockItemRepository).findAllByProductIds(anyList());
    verify(stockItemRepository, never()).save(stockDomain);
    verify(reservationRepository, never()).save(reservationDomain);
  }

  @Test
  void shouldThrowExceptionInConfirmingWhenReservationEmpty() {
    when(processedEventRepository.isExistById(anyString())).thenReturn(false);
    doNothing().when(processedEventRepository).save(any(ProcessedEvent.class));
    when(reservationRepository.findByOrderId(anyString())).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> service.confirm("evt1","ord1"));

    verify(processedEventRepository).isExistById(anyString());
    verify(processedEventRepository).save(any(ProcessedEvent.class));
  }

  @Test
  void shouldReleasing() {
    stockDomain.setStock(10);
    stockDomain.reserve(1);

    when(processedEventRepository.isExistById(anyString())).thenReturn(false);
    doNothing().when(processedEventRepository).save(any(ProcessedEvent.class));
    when(reservationRepository.findByOrderId(anyString())).thenReturn(Optional.of(reservationDomain));
    when(stockItemRepository.findAllByProductIds(anyList())).thenReturn(List.of(stockDomain));

    service.release("evt1","saga1", "ord1");

    verify(processedEventRepository).isExistById(anyString());
    verify(processedEventRepository).save(any(ProcessedEvent.class));
    verify(reservationRepository).findByOrderId(anyString());
    verify(stockItemRepository).findAllByProductIds(anyList());
    verify(stockItemRepository).save(stockDomain);
    verify(reservationRepository).save(reservationDomain);

    assertThat(stockDomain.getReservedQuantity()).isEqualTo(0);
    assertThat(stockDomain.getAvailableQuantity()).isEqualTo(10);
    assertThat(reservationDomain.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
  }

  @Test
  void shouldIdempotencyProcessedEventWhileReleasing() {
    when(processedEventRepository.isExistById(anyString())).thenReturn(true);

    service.release("evt1","saga1", "ord1");

    verify(processedEventRepository).isExistById(anyString());
  }

  @Test
  void shouldNotReleasingWhenEmptyOrMissingStock() {
    when(processedEventRepository.isExistById(anyString())).thenReturn(false);
    doNothing().when(processedEventRepository).save(any(ProcessedEvent.class));
    when(reservationRepository.findByOrderId(anyString())).thenReturn(Optional.of(reservationDomain));
    when(stockItemRepository.findAllByProductIds(anyList())).thenReturn(List.of(StockItem.create("prod2")));

    service.release("evt1","saga1", "ord1");

    verify(processedEventRepository).isExistById(anyString());
    verify(processedEventRepository).save(any(ProcessedEvent.class));
    verify(reservationRepository).findByOrderId(anyString());
    verify(stockItemRepository).findAllByProductIds(anyList());
    verify(stockItemRepository, never()).save(stockDomain);
    verify(reservationRepository, never()).save(reservationDomain);
  }

  @Test
  void shouldThrowExceptionInReleasingWhenReservationEmpty() {
    when(processedEventRepository.isExistById(anyString())).thenReturn(false);
    doNothing().when(processedEventRepository).save(any(ProcessedEvent.class));
    when(reservationRepository.findByOrderId(anyString())).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> service.release("evt1","saga1", "ord1"));
    verify(processedEventRepository).isExistById(anyString());
    verify(processedEventRepository).save(any(ProcessedEvent.class));
  }

  @Test
  void shouldCheckingStock() {
    stockDomain.setStock(5);
    when(stockItemRepository.findAllByProductIds(anyList())).thenReturn(List.of(stockDomain));

    var result = service.checkStock(List.of(new CheckStockUseCase.StockCheckItem("prod1",1)));

    assertThat(result.allAvailable()).isTrue();
    assertThat(result.items()).isNotNull();
    assertThat(result.items()).hasSize(1);
    assertThat(result.items().getFirst().productId()).isEqualTo("prod1");
    assertThat(result.items().getFirst().available()).isEqualTo(5);
    assertThat(result.items().getFirst().requested()).isEqualTo(1);
    assertThat(result.items().getFirst().sufficient()).isTrue();

    verify(stockItemRepository).findAllByProductIds(anyList());
  }

  @Test
  void shouldCheckingStockIsMissing() {
    stockDomain.setStock(5);
    when(stockItemRepository.findAllByProductIds(anyList())).thenReturn(List.of(stockDomain));

    var result = service.checkStock(List.of(new CheckStockUseCase.StockCheckItem("prod2",1)));

    assertThat(result.allAvailable()).isFalse();
    assertThat(result.items()).isNotNull();
    assertThat(result.items()).hasSize(1);
    assertThat(result.items().getFirst().productId()).isEqualTo("prod2");
    assertThat(result.items().getFirst().available()).isEqualTo(0);
    assertThat(result.items().getFirst().requested()).isEqualTo(1);
    assertThat(result.items().getFirst().sufficient()).isFalse();

    verify(stockItemRepository).findAllByProductIds(anyList());
  }

  @Test
  void shouldCheckingStockIsEmpty() {
    when(stockItemRepository.findAllByProductIds(anyList())).thenReturn(List.of());

    var result = service.checkStock(List.of(new CheckStockUseCase.StockCheckItem("prod2",1)));

    assertThat(result.allAvailable()).isFalse();
    assertThat(result.items()).isNotNull();
    assertThat(result.items()).hasSize(1);
    assertThat(result.items().getFirst().productId()).isEqualTo("prod2");
    assertThat(result.items().getFirst().available()).isEqualTo(0);
    assertThat(result.items().getFirst().requested()).isEqualTo(1);
    assertThat(result.items().getFirst().sufficient()).isFalse();

    verify(stockItemRepository).findAllByProductIds(anyList());
  }

}