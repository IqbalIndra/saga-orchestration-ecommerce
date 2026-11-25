package com.ecommerce.saga.orchestration.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.unit.DataSize;

import java.time.Duration;

@ConfigurationProperties(prefix = "grpc.server")
public record GrpcServerProperties(
    @DefaultValue("9090") int port,
    @DefaultValue("30s") Duration shutdownGracePeriod,
    @DefaultValue("4MB") DataSize maxInboundMessageSize,
    @DefaultValue("true") boolean reflectionEnabled,
    String sslBundle
    ) {
}
