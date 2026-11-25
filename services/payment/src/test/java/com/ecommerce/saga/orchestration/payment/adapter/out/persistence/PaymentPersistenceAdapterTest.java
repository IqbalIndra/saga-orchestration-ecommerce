package com.ecommerce.saga.orchestration.payment.adapter.out.persistence;

import com.ecommerce.saga.orchestration.payment.adapter.out.persistence.entity.IdempotencyEntity;
import com.ecommerce.saga.orchestration.payment.adapter.out.persistence.entity.PaymentEntity;
import com.ecommerce.saga.orchestration.payment.domain.model.IdempotencyRecord;
import com.ecommerce.saga.orchestration.payment.domain.model.Payment;
import com.ecommerce.saga.orchestration.payment.domain.model.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentPersistenceAdapterTest {

  @Mock private JpaPaymentRepository paymentRepository;
  @Mock private JpaIdempotencyRepository idempotencyRepository;
  @Mock private PaymentMapper mapper;
  @InjectMocks private PaymentPersistenceAdapter adapter;

  private Payment paymentDomain;
  private PaymentEntity paymentEntity;
  private IdempotencyRecord idempotencyDomain;
  private IdempotencyEntity idempotencyEntity;

  private final String id = UUID.randomUUID().toString();
  private final Instant now = Instant.now();
  private final String orderId = "ord1";
  private final String key = "key";
  private final String paymentId = UUID.randomUUID().toString();
  private final String response = "{response}";

  @BeforeEach
  void setUp() {
    paymentDomain = new Payment(id, orderId, BigDecimal.TEN, PaymentStatus.PENDING,
        "", now, now);
    paymentEntity = new PaymentEntity(id, orderId, BigDecimal.TEN, now, now,
        "", PaymentStatus.PENDING);
    idempotencyDomain = new IdempotencyRecord(key, paymentId, response, now);
    idempotencyEntity = new IdempotencyEntity(key, paymentId, response, now);

  }

  @Test
  void shouldSavingIdempotencyRecord() {
    when(mapper.toEntity(idempotencyDomain)).thenReturn(idempotencyEntity);

    adapter.save(idempotencyDomain);

    verify(mapper).toEntity(idempotencyDomain);
    verify(idempotencyRepository).save(idempotencyEntity);

    assertThat(idempotencyDomain.key()).isEqualTo(key);
    assertThat(idempotencyDomain.paymentId()).isEqualTo(paymentId);
    assertThat(idempotencyDomain.responseBody()).isEqualTo(response);
    assertThat(idempotencyDomain.createdAt()).isEqualTo(now);
  }

  @Test
  void shouldFindIdempotencyByKey() {
    when(idempotencyRepository.findByKey(any())).thenReturn(Optional.of(idempotencyEntity));
    when(mapper.toDomain(idempotencyEntity)).thenReturn(idempotencyDomain);

    var result = adapter.findByKey("key");
    assertThat(result.isPresent()).isTrue();
    assertThat(result.get()).isEqualTo(idempotencyDomain);

    verify(idempotencyRepository).findByKey(any());
  }

  @Test
  void shouldEmptyWhenFindIdempotencyByKey() {
    when(idempotencyRepository.findByKey(any())).thenReturn(Optional.empty());

    var result = adapter.findByKey("key1");

    assertThat(result.isEmpty()).isTrue();

    verify(idempotencyRepository).findByKey(any());
    verify(mapper, never()).toDomain(idempotencyEntity);
  }

  @Test
  void shouldSavingPayment() {
    when(mapper.toEntity(paymentDomain)).thenReturn(paymentEntity);
    when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);
    when(mapper.toDomain(paymentEntity)).thenReturn(paymentDomain);

    var result = adapter.save(paymentDomain);
    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(paymentDomain);

    verify(paymentRepository).save(any(PaymentEntity.class));
  }

  @Test
  void shouldFindPaymentById() {
    when(paymentRepository.findById(any())).thenReturn(Optional.of(paymentEntity));
    when(mapper.toDomain(paymentEntity)).thenReturn(paymentDomain);

    var result = adapter.findById(id);

    assertThat(result).isNotNull();
    assertThat(result).isPresent();
    assertThat(result).isEqualTo(Optional.of(paymentDomain));

    verify(paymentRepository).findById(any());
  }

  @Test
  void shouldEmptyFindPaymentById() {
    when(paymentRepository.findById(any())).thenReturn(Optional.empty());

    var result = adapter.findById("id");

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();

    verify(paymentRepository).findById(any());
    verify(mapper, never()).toDomain(idempotencyEntity);
  }

  @Test
  void shouldFindPaymentByOrderId() {
    when(paymentRepository.findByOrderId(any())).thenReturn(Optional.of(paymentEntity));
    when(mapper.toDomain(paymentEntity)).thenReturn(paymentDomain);

    var result = adapter.findByOrderId(orderId);

    assertThat(result).isNotNull();
    assertThat(result).isPresent();
    assertThat(result).isEqualTo(Optional.of(paymentDomain));

    verify(paymentRepository).findByOrderId(any());
  }

  @Test
  void shouldEmptyFindPaymentByOrderId() {
    when(paymentRepository.findByOrderId(any())).thenReturn(Optional.empty());

    var result = adapter.findByOrderId("orderId");

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();

    verify(paymentRepository).findByOrderId(any());
    verify(mapper, never()).toDomain(idempotencyEntity);
  }
}