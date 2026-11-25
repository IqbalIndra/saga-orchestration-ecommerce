package com.ecommerce.saga.orchestration.payment.adapter.out.persistence;

import com.ecommerce.saga.orchestration.payment.adapter.out.persistence.entity.IdempotencyEntity;
import com.ecommerce.saga.orchestration.payment.adapter.out.persistence.entity.PaymentEntity;
import com.ecommerce.saga.orchestration.payment.domain.model.IdempotencyRecord;
import com.ecommerce.saga.orchestration.payment.domain.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

  @Mapping(source = "amount", target = "totalAmount")
  PaymentEntity toEntity(Payment payment);
  @Mapping(source = "totalAmount", target = "amount")
  Payment toDomain(PaymentEntity paymentEntity);

  IdempotencyEntity toEntity(IdempotencyRecord idempotencyRecord);
  IdempotencyRecord toDomain(IdempotencyEntity idempotencyEntity);
}
