package com.ecommerce.saga.orchestration.payment.config;

import com.ecommerce.saga.orchestration.payment.domain.exception.PaymentGatewayTimeoutException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.ExponentialBackOff;
import org.springframework.util.backoff.FixedBackOff;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
public class KafkaConfig {
  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${spring.kafka.consumer.group-id:${spring.application.name}}")
  private String groupId;

  @Bean
  public ProducerFactory<String, Object> producerFactory(ObjectMapper objectMapper) {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

    // Use ObjectMapper Jackson for custom JsonSerializer
    JsonSerializer<Object> jsonSerializer = new JsonSerializer<>(objectMapper);
    jsonSerializer.setAddTypeInfo(true);

    return new DefaultKafkaProducerFactory<>(
        configProps,
        new StringSerializer(),
        jsonSerializer
    );
  }

  @Bean
  public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
    return new KafkaTemplate<>(producerFactory);
  }

  @Bean
  public ConsumerFactory<String, Object> consumerFactory(ObjectMapper objectMapper) {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());

    JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>(objectMapper);
    jsonDeserializer.addTrustedPackages("com.ecommerce.saga.orchestration.common.events.*");
    jsonDeserializer.setUseTypeMapperForKey(false);

    // Wrap with  ErrorHandlingDeserializer for save error json
    ErrorHandlingDeserializer<Object> errorHandlingDeserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);

    return new DefaultKafkaConsumerFactory<>(
        props,
        new StringDeserializer(),
        errorHandlingDeserializer
    );
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
      ConsumerFactory<String, Object> consumerFactory,
      KafkaTemplate<String, Object> kafkaTemplate,
      @Value("${spring.application.name}") String serviceName) {
    ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    factory.setCommonErrorHandler(kafkaErrorHandler(kafkaTemplate, serviceName));
    return factory;
  }

  public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, Object> kafkaTemplate,
                                               String serviceName) {
    DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
        kafkaTemplate,
        (record, ex) -> new TopicPartition("order-dlq-events", record.partition())
        // -1 default partitioner by key
    );

    recoverer.setHeadersFunction((record, ex) -> {
      RecordHeaders headers = new RecordHeaders();
      headers.add("X-Failed-Service", serviceName.getBytes(StandardCharsets.UTF_8));
      headers.add("X-Original-Topic", record.topic().getBytes(StandardCharsets.UTF_8));

      // Tag timeouts so the Orchestrator can auto-schedule Outbox retries
      if (ex instanceof PaymentGatewayTimeoutException gatewayTimeout) {
        headers.add("X-Failure-Category", "TRANSIENT_TIMEOUT".getBytes(StandardCharsets.UTF_8));
        headers.add("X-Exception-Message", gatewayTimeout.getMessage().getBytes(StandardCharsets.UTF_8));
        headers.add("X-Exception-Type", gatewayTimeout.getClass().getName().getBytes(StandardCharsets.UTF_8));
      } else {
        headers.add("X-Failure-Category", "NON_RETRYABLE_ERROR".getBytes(StandardCharsets.UTF_8));
        headers.add("X-Exception-Type", ex.getClass().getName().getBytes(StandardCharsets.UTF_8));
        headers.add("X-Exception-Message", ex.getMessage().getBytes(StandardCharsets.UTF_8));
      }

      return headers;
    });

    var handler = new DefaultErrorHandler(recoverer, new FixedBackOff(Duration.ofSeconds(1).toMillis(), 3));
    handler.addNotRetryableExceptions(JsonProcessingException.class, IllegalArgumentException.class);

    return handler;
  }


  private String findHeaderValue(Headers headers, String key) {
    Header header = headers.lastHeader(key);
    return Objects.nonNull(header) ? new String(header.value(), StandardCharsets.UTF_8) : "N/A";
  }
}
