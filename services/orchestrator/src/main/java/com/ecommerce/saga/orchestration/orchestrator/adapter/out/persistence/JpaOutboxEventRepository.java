package com.ecommerce.saga.orchestration.orchestrator.adapter.out.persistence;

import com.ecommerce.saga.orchestration.orchestrator.adapter.out.persistence.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface JpaOutboxEventRepository extends JpaRepository<OutboxEventEntity, String> {

  @Modifying
  @Query(value = """
    DELETE FROM outbox_event
            WHERE published_at IS NOT NULL
            AND published_at < :olderThan
""", nativeQuery = true)
  void cleanupOldMessages(@Param("olderThan") Instant olderThan);

  @Query(value = """
    SELECT *
    FROM outbox_event
    WHERE status = 'PENDING' AND next_attempt_at <= now()
    ORDER BY created_at
    LIMIT :limit
    FOR UPDATE SKIP LOCKED
""", nativeQuery = true)
  List<OutboxEventEntity> findUnprocessed(@Param("limit") int limit);

}
