package com.ecommerce.saga.orchestration.common.lock.config;

import java.util.UUID;

public final class OwnerToken {
  private OwnerToken() { }

  public static String newToken() {
    return UUID.randomUUID().toString();
  }
}
