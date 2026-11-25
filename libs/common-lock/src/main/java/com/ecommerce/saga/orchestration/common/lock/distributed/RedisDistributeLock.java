package com.ecommerce.saga.orchestration.common.lock.distributed;

import com.ecommerce.saga.orchestration.common.lock.config.OwnerToken;
import com.ecommerce.saga.orchestration.common.lock.model.LockHandle;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;

public class RedisDistributeLock {
  private static final DefaultRedisScript<Long> UNLOCK_SCRIPT =
      new DefaultRedisScript<>(
          """
          if redis.call('get', KEYS[1]) == ARGV[1] then
            return redis.call('del', KEYS[1])
          else
            return 0
          end
          """,
          Long.class
      );

  private final StringRedisTemplate redis;


  public RedisDistributeLock(StringRedisTemplate redis) {
    this.redis = redis;
  }

  public Optional<LockHandle> tryAcquire(String lockId, Duration leaseTime) {
    String token = OwnerToken.newToken();
    Boolean ok = redis.opsForValue().setIfAbsent(lockId, token, leaseTime);
    if (Boolean.TRUE.equals(ok)) {
      return Optional.of(new LockHandle(lockId, token, leaseTime));
    }
    return Optional.empty();
  }

  public boolean release(LockHandle lockHandle) {
    Long execute = redis.execute(
        UNLOCK_SCRIPT,
        Collections.singletonList(lockHandle.lockId()),
        lockHandle.token()
    );
    return execute == 1L;
  }

}
