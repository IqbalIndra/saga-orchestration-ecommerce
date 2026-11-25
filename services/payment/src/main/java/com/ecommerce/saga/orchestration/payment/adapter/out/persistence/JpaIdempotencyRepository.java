package com.ecommerce.saga.orchestration.payment.adapter.out.persistence;

import com.ecommerce.saga.orchestration.payment.adapter.out.persistence.entity.IdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaIdempotencyRepository extends JpaRepository<IdempotencyEntity, String> {
  Optional<IdempotencyEntity> findByKey(String key);
}
