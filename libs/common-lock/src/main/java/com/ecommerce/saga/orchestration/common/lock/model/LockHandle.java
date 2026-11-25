package com.ecommerce.saga.orchestration.common.lock.model;

import java.time.Duration;
import java.util.Objects;

public record LockHandle(
    String lockId,
    String token,
    Duration leaseTime
) {
  public LockHandle {
    Objects.requireNonNull(lockId);
    Objects.requireNonNull(token);
    Objects.requireNonNull(leaseTime);
  }
}
