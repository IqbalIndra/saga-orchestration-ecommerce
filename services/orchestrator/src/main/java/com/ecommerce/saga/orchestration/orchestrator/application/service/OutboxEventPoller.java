package com.ecommerce.saga.orchestration.orchestrator.application.service;

import com.ecommerce.saga.orchestration.orchestrator.application.port.out.OutboxEventRepository;
import com.ecommerce.saga.orchestration.orchestrator.domain.model.OutboxEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPoller {
  private static final int BATCH_SIZE = 10;
  private static final int MAX_RETRIES = 5;

  private final OutboxEventRepository outboxEventRepository;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Scheduled(fixedDelay = 1000)
  @Transactional
  public void relay() {
    List<OutboxEvent> unprocessed = outboxEventRepository.findUnprocessed(BATCH_SIZE);
    if (unprocessed.isEmpty()) {
      return;
    }

    for (var event : unprocessed) {
      try {
        Object obj = objectMapper.readValue(event.getPayload(), Class.forName(event.getEventType()));

        ProducerRecord<String, Object> record = new ProducerRecord<>(
            event.getTopic(), event.getAggregateId(), obj);
        record.headers().add("event-id",
            event.getId().getBytes(StandardCharsets.UTF_8));

        kafkaTemplate.send(record).get(5, TimeUnit.SECONDS);
        event.markAsPublished();
        outboxEventRepository.save(event);
      } catch (Exception e) {
        log.warn("Outbox publish failed, incrementing retry: id={} error={}",
            event.getId(), e.getMessage());

        int next = event.getRetryCount() + 1;
        if (next >= MAX_RETRIES) {
          event.markAsFailed();
        } else {
          long backoffSec = (long) Math.min(300, Math.pow(2, next));
          event.retry(backoffSec);
        }
        outboxEventRepository.save(event);
      }
    }

  }
}
