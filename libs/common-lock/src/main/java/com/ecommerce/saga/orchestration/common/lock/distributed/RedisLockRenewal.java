package com.ecommerce.saga.orchestration.common.lock.distributed;

import com.ecommerce.saga.orchestration.common.lock.model.LockHandle;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

public class RedisLockRenewal {
  private static final DefaultRedisScript<Long> RENEW_SCRIPT = new DefaultRedisScript<>(
      """
      if redis.call('GET', KEYS[1]) == ARGV[1] then
        return redis.call('PEXPIRE' , KEYS[1], ARGV[2])
      else
        return 0
      end
      """,
      Long.class
  );

  private final StringRedisTemplate redis;

  public RedisLockRenewal(StringRedisTemplate redis) {
    this.redis = redis;
  }

  public boolean renew(LockHandle handle) {
    long millis = handle.leaseTime().toMillis();
    Long renewed = redis.execute(
        RENEW_SCRIPT,
        Collections.singletonList(handle.lockId()),
        handle.token(),
        String.valueOf(millis)
    );

    return renewed == 1L;
  }
}
