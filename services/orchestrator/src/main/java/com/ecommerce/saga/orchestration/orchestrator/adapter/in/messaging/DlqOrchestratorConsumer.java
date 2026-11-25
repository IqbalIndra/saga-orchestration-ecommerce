package com.ecommerce.saga.orchestration.orchestrator.adapter.in.messaging;

import com.ecommerce.saga.orchestration.orchestrator.application.port.out.DlqEventRepository;
import com.ecommerce.saga.orchestration.orchestrator.domain.model.DlqEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class DlqOrchestratorConsumer {
  private final DlqEventRepository dlqEventRepository;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = "order-dlq-events", groupId = "orchestrator-service",
      containerFactory = "kafkaListenerContainerFactory")
  @Transactional
  public void handleCentralDlqEvent(ConsumerRecord<String, Object> record) {
    String originalTopic = extractStringHeader(record.headers(), "X-Original-Topic");
    String category = extractStringHeader(record.headers(), "X-Failure-Category");
    String exceptionType = extractStringHeader(record.headers(), "X-Exception-Type");
    String exceptionMessage = extractStringHeader(record.headers(), "X-Exception-Message");

    DlqEvent dlqEvent = DlqEvent.readyForRetry(
        null,
        record.key(),

    )

  }

  private String extractStringHeader(Headers headers, String key) {
    Header header = headers.lastHeader(key);
    return Objects.nonNull(header) ? new String(header.value(), StandardCharsets.UTF_8) : "N/A";
  }
}
