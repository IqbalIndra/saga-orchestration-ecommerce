package com.ecommerce.saga.orchestration.payment.application.service;

import com.ecommerce.saga.orchestration.payment.application.port.in.PaymentEventPublisher;
import com.ecommerce.saga.orchestration.payment.application.port.in.ProcessPaymentUseCase;
import com.ecommerce.saga.orchestration.payment.application.port.in.RefundPaymentUseCase;
import com.ecommerce.saga.orchestration.payment.application.port.out.IdempotencyRecordRepository;
import com.ecommerce.saga.orchestration.payment.application.port.out.PaymentRepository;
import com.ecommerce.saga.orchestration.payment.application.port.out.ProcessedEventRepository;
import com.ecommerce.saga.orchestration.payment.domain.exception.PaymentDeclineException;
import com.ecommerce.saga.orchestration.payment.domain.exception.PaymentGatewayTimeoutException;
import com.ecommerce.saga.orchestration.payment.domain.model.IdempotencyRecord;
import com.ecommerce.saga.orchestration.payment.domain.model.Payment;
import com.ecommerce.saga.orchestration.payment.domain.model.ProcessedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService implements ProcessPaymentUseCase, RefundPaymentUseCase {
  private final PaymentRepository paymentRepository;
  private final IdempotencyRecordRepository idempotencyRecordRepository;
  private final ProcessedEventRepository processedEventRepository;
  private final PaymentEventPublisher paymentEventPublisher;
  private final ObjectMapper objectMapper;

  @Override
  @Transactional
  public Payment process(Command command) {
    if (processedEventRepository.isExistById(command.eventId())) {
      var payment = Payment.create(command.orderId(), command.amount());
      payment.fail("Event already execute");
      return payment;
    }
    processedEventRepository.save(ProcessedEvent.create(command.eventId()));

    return idempotencyRecordRepository.findByKey(command.idempotencyKey())
        .map(record -> {
          log.info("Duplicate Payment request , idempotencyKey={}", command.idempotencyKey());
          return paymentRepository.findById(record.paymentId()).orElseThrow();
        })
        .orElseGet(() -> doProcessPayment(command));
  }

  private Payment doProcessPayment(Command command) {
    var saved = paymentRepository.findByOrderId(command.orderId())
        .orElseGet(() -> {
          Payment p = Payment.create(
              command.orderId(),
              command.amount()
          );
          return paymentRepository.save(p);
        });

    try {
      boolean success = simulatePaymentGateway(saved);
      if (success) {
        saved.process();
        paymentRepository.save(saved);

        paymentEventPublisher.publishPaymentCharged(command.sagaId(), saved);
      }
    } catch (ResourceAccessException ex) {
      saved.fail("Timeout");
      paymentRepository.save(saved);

      throw new PaymentGatewayTimeoutException("Gateway Timeout ", ex);
    } catch (PaymentDeclineException ex) {
      saved.fail(ex.getMessage());
      paymentRepository.save(saved);

      paymentEventPublisher.publishPaymentFailed(command.sagaId(), saved);
    }

    try {
      String responseBody = objectMapper.writeValueAsString(saved.getId());
      idempotencyRecordRepository.save(
          new IdempotencyRecord(
              command.idempotencyKey(),
              saved.getId(),
              responseBody,
              Instant.now()
          )
      );
    } catch (JsonProcessingException e) {
      log.warn("Failed to serializable to Json String, paymentId={}",saved.getId());
    }

    return saved;
  }

  private boolean simulatePaymentGateway(Payment payment) {
    boolean isNinetyNine = payment.getAmount().remainder(new BigDecimal(100))
        .compareTo(new BigDecimal(99)) == 0;
    if (isNinetyNine) {
      throw new ResourceAccessException("Simulate to Timeout ");
    }

    if (payment.getAmount().toPlainString().endsWith(".99")) {
      throw new PaymentDeclineException("Payment Gateway Decline");
    }

    return true;
  }

  @Override
  public void refund(RefundCommand command) {
    if (processedEventRepository.isExistById(command.eventId())) {
      return;
    }
    processedEventRepository.save(ProcessedEvent.create(command.eventId()));

    Payment payment = paymentRepository.findById(command.paymentId()).orElseThrow();
    payment.refund();

    paymentEventPublisher.publishPaymentRefunded(command.sagaId(), payment);
  }
}
