package com.ecommerce.saga.orchestration.common.events;

import java.time.Instant;

public interface BaseEvent {
    String eventId();
    String correlationId();
    Instant occurredAt();
}
