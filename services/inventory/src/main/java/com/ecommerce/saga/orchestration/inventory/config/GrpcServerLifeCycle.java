package com.ecommerce.saga.orchestration.inventory.config;

import io.grpc.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GrpcServerLifeCycle implements SmartLifecycle {

  private final Server server;
  private final Duration gracePeriod;
  private volatile boolean running;

  public GrpcServerLifeCycle(Server server, Duration gracePeriod) {
    this.server = server;
    this.gracePeriod = gracePeriod;
  }

  @Override
  public void start() {
    try {
      server.start();
      running = true;
      log.info("gRPC server listening on port {}", server.getPort());
    } catch (IOException e) {
      throw new IllegalStateException("Failed to start gRPC Server", e);
    }
  }

  @Override
  public void stop() {
    server.shutdown(); // refuse new calls, let in-flight ones finish
    try {
      if (!server.awaitTermination(gracePeriod.toMillis(), TimeUnit.MILLISECONDS)) {
        log.warn("gRPC server still busy after {}, forcing shutdown", gracePeriod);
        server.shutdownNow();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      server.shutdownNow();
    } finally {
      running = false;
    }
  }

  @Override
  public boolean isRunning() {
    return running;
  }
}
