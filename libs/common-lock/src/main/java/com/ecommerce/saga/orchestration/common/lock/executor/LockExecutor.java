package com.ecommerce.saga.orchestration.common.lock.executor;

import com.ecommerce.saga.orchestration.common.lock.distributed.RedisDistributeLock;
import com.ecommerce.saga.orchestration.common.lock.distributed.RedisLockRenewal;
import com.ecommerce.saga.orchestration.common.lock.model.LockHandle;
import jakarta.annotation.PreDestroy;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Supplier;

public class LockExecutor {
  private final RedisDistributeLock locks;
  private final RedisLockRenewal lockRenewal;
  private final ThreadPoolTaskScheduler scheduler;

  public LockExecutor(RedisDistributeLock locks, RedisLockRenewal lockRenewal, ThreadPoolTaskScheduler scheduler) {
    this.locks = locks;
    this.lockRenewal = lockRenewal;
    this.scheduler = scheduler;
  }

  public <T> T runWithLock(String lockId, Duration leaseTime, Supplier<T> work) {
    Optional<LockHandle> OptionLockHandle = locks.tryAcquire(lockId, leaseTime);
    if (OptionLockHandle.isEmpty()) {
      return null;
    }
    LockHandle handle = OptionLockHandle.get();
    Duration renewEvery = handle.leaseTime().dividedBy(3);
    long renewEveryMillis = Math.max(1L, renewEvery.toMillis());

    ScheduledFuture<?> lock = scheduler.scheduleAtFixedRate(() -> {
      boolean ok = lockRenewal.renew(handle);
      if (!ok) {
        throw new CancellationException("lock ownership lost");
      }
    }, Duration.ofMillis(renewEveryMillis));

    try {
      return work.get();
    } finally {
      lock.cancel(true);
      locks.release(handle);
    }
  }

  @PreDestroy
  public void shutdown() {
    scheduler.shutdown();
  }

}
