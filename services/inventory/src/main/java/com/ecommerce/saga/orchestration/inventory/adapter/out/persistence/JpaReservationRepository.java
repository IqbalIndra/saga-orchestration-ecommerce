package com.ecommerce.saga.orchestration.inventory.adapter.out.persistence;

import com.ecommerce.saga.orchestration.inventory.adapter.out.persistence.entity.ReservationEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface JpaReservationRepository extends MongoRepository<ReservationEntity, String> {
  Optional<ReservationEntity> findByOrderId(String orderId);
}
