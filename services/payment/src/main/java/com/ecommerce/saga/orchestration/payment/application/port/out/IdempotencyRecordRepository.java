package com.ecommerce.saga.orchestration.payment.application.port.out;

import com.ecommerce.saga.orchestration.payment.domain.model.IdempotencyRecord;

import java.util.Optional;

public interface IdempotencyRecordRepository {
  void save(IdempotencyRecord idempotencyRecord);
  Optional<IdempotencyRecord> findByKey(String key);
}
