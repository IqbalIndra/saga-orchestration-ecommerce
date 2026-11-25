package com.ecommerce.saga.orchestration.inventory.adapter.out.persistence;

import com.ecommerce.saga.orchestration.inventory.adapter.out.persistence.entity.StockItemEntity;
import com.ecommerce.saga.orchestration.inventory.domain.model.StockItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockItemPersistenceAdapterTest {

  @Mock
  private JpaStockItemRepository repository;

  @InjectMocks
  private StockItemPersistenceAdapter adapter;

  private StockItem domain;
  private StockItemEntity entity;
  private List<StockItem> domains;
  private List<StockItemEntity> entities;

  @BeforeEach
  public void setUp() {
    String id = UUID.randomUUID().toString();
    Instant now = Instant.now();

    domain = new StockItem(id, "prod1", 10, 0, now, now);
    entity = new StockItemEntity(id, "prod1", 10, 0, now, now);

    domains = List.of(domain);
    entities = List.of(entity);
  }

  @Test
  void shouldSaveStockItem() {
    when(repository.save(any(StockItemEntity.class))).thenReturn(entity);

    var result = adapter.save(domain);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(domain.getId());
    assertThat(result.getProductId()).isEqualTo(domain.getProductId());
    assertThat(result.getAvailableQuantity()).isEqualTo(domain.getAvailableQuantity());
    assertThat(result.getReservedQuantity()).isEqualTo(domain.getReservedQuantity());
    assertThat(result.getCreatedAt()).isEqualTo(domain.getCreatedAt());
    assertThat(result.getUpdatedAt()).isEqualTo(domain.getUpdatedAt());

    verify(repository).save(any(StockItemEntity.class));
  }

  @Test
  void shouldFindByProductId() {
    when(repository.findByProductId(any(String.class))).thenReturn(Optional.of(entity));

    var result = adapter.findByProductId("prod1");

    assertThat(result.isPresent()).isTrue();
    assertThat(result.get().getId()).isEqualTo(domain.getId());
    assertThat(result.get().getId()).isEqualTo(domain.getId());
    assertThat(result.get().getProductId()).isEqualTo(domain.getProductId());
    assertThat(result.get().getAvailableQuantity()).isEqualTo(domain.getAvailableQuantity());
    assertThat(result.get().getReservedQuantity()).isEqualTo(domain.getReservedQuantity());
    assertThat(result.get().getCreatedAt()).isEqualTo(domain.getCreatedAt());
    assertThat(result.get().getUpdatedAt()).isEqualTo(domain.getUpdatedAt());

    verify(repository).findByProductId(any(String.class));
  }

  @Test
  void shouldEmptyWhenFindByProductId() {
    when(repository.findByProductId(any(String.class))).thenReturn(Optional.empty());

    var result = adapter.findByProductId("prod1");

    assertThat(result.isPresent()).isFalse();

    verify(repository).findByProductId(any(String.class));
  }

  @Test
  void shouldFindAllByProductIds() {
    when(repository.findByProductIdIn(anyList())).thenReturn(entities);

    var result = adapter.findAllByProductIds(List.of("prod1"));

    assertThat(result).isNotNull();
    assertThat(result.isEmpty()).isFalse();
    assertThat(result.size()).isEqualTo(1);
    assertThat(result.getFirst().getProductId()).isEqualTo(domains.getFirst().getProductId());
    assertThat(result.getFirst().getAvailableQuantity()).isEqualTo(domains.getFirst().getAvailableQuantity());
    assertThat(result.getFirst().getReservedQuantity()).isEqualTo(domains.getFirst().getReservedQuantity());
    assertThat(result.getFirst().getCreatedAt()).isEqualTo(domains.getFirst().getCreatedAt());
    assertThat(result.getFirst().getUpdatedAt()).isEqualTo(domains.getFirst().getUpdatedAt());

    verify(repository).findByProductIdIn(anyList());
  }

  @Test
  void shouldEmptyWhenFindAllByProductIds() {
    when(repository.findByProductIdIn(anyList())).thenReturn(List.of());

    var result = adapter.findAllByProductIds(List.of("prod2"));

    assertThat(result).isEmpty();

    verify(repository).findByProductIdIn(anyList());
  }

}