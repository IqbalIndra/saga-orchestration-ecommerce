package com.ecommerce.saga.orchestration.payment.adapter.out.persistence;

import com.ecommerce.saga.orchestration.payment.application.port.out.IdempotencyRecordRepository;
import com.ecommerce.saga.orchestration.payment.application.port.out.PaymentRepository;
import com.ecommerce.saga.orchestration.payment.domain.model.IdempotencyRecord;
import com.ecommerce.saga.orchestration.payment.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentPersistenceAdapter implements PaymentRepository, IdempotencyRecordRepository {
  private final JpaPaymentRepository jpaPaymentRepository;
  private final JpaIdempotencyRepository jpaIdempotencyRepository;
  private final PaymentMapper paymentMapper;

  @Override
  public void save(IdempotencyRecord idempotencyRecord) {
    jpaIdempotencyRepository.save(paymentMapper.toEntity(idempotencyRecord));
  }

  @Override
  public Optional<IdempotencyRecord> findByKey(String key) {
    return jpaIdempotencyRepository.findByKey(key).map(paymentMapper::toDomain);
  }

  @Override
  public Payment save(Payment payment) {
    return paymentMapper.toDomain(jpaPaymentRepository.save(paymentMapper.toEntity(payment)));
  }

  @Override
  public Optional<Payment> findById(String id) {
    return jpaPaymentRepository.findById(id).map(paymentMapper::toDomain);
  }

  @Override
  public Optional<Payment> findByOrderId(String orderId) {
    return jpaPaymentRepository.findByOrderId(orderId).map(paymentMapper::toDomain);
  }
}
