package com.ecommerce.saga.orchestration.orchestrator.adapter.out.persistence;

import com.ecommerce.saga.orchestration.orchestrator.adapter.out.persistence.entity.OrderPlacementSagaEntity;
import com.ecommerce.saga.orchestration.orchestrator.application.port.out.OrderPlacementSagaRepository;
import com.ecommerce.saga.orchestration.orchestrator.domain.model.OrderPlacementSaga;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderPlacementSagaPersistenceAdapter implements OrderPlacementSagaRepository {
  private final JpaOrderPlacementSagaRepository jpaOrderPlacementSagaRepository;

  @Override
  public void save(OrderPlacementSaga saga) {
    jpaOrderPlacementSagaRepository.save(
        OrderPlacementSagaEntity.of()
            .sagaId(saga.getId())
            .orderId(saga.getOrderId())
            .reservationId(saga.getReservationId())
            .paymentId(saga.getPaymentId())
            .state(saga.getState())
            .failedStep(saga.getFailedStep())
            .failureReason(saga.getFailureReason())
            .startedAt(saga.getStartedAt())
            .completedAt(saga.getCompletedAt())
            .build()
    );
  }

  @Override
  public Optional<OrderPlacementSaga> findById(String id) {
    return jpaOrderPlacementSagaRepository.findById(id).map(saga -> new OrderPlacementSaga(
        saga.getSagaId(),
        saga.getState(),
        saga.getOrderId(),
        saga.getPaymentId(),
        saga.getReservationId(),
        saga.getFailedStep(),
        saga.getFailureReason(),
        saga.getStartedAt(),
        saga.getCompletedAt()
    ));
  }
}
