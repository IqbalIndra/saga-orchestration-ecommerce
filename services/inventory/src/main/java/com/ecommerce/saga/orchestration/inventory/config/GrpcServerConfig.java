package com.ecommerce.saga.orchestration.inventory.config;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.protobuf.services.ProtoReflectionService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.ssl.SslManagerBundle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLException;
import java.util.List;
import java.util.concurrent.Executors;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GrpcServerProperties.class)
public class GrpcServerConfig {

  @Bean
  HealthStatusManager healthStatusManager() {
    return new HealthStatusManager();
  }

  @Bean
  Server grpcServer(GrpcServerProperties props,
                    List<BindableService> services,
                    ObjectProvider<ServerInterceptor> interceptors,
                    HealthStatusManager health,
                    ObjectProvider<SslBundles> sslBundles) throws SSLException {

    NettyServerBuilder builder = NettyServerBuilder.forPort(props.port())
        .maxInboundMessageSize((int) props.maxInboundMessageSize().toBytes())
        .executor(Executors.newVirtualThreadPerTaskExecutor());   // Java 21+

    if (props.sslBundle() != null) {
      SslManagerBundle managers = sslBundles.getObject().getBundle(props.sslBundle()).getManagers();
      builder.sslContext(
          GrpcSslContexts.configure(
              SslContextBuilder.forServer(managers.getKeyManagerFactory())
                  .trustManager(managers.getTrustManagerFactory())
          ).build()
      );
    }

    // Builder-level interceptors run in the REVERSE order they were added,
    // so add Spring's sorted list backwards to honour @Order.
    List<ServerInterceptor> sorted = interceptors.orderedStream().toList();
    for (int i = sorted.size() - 1; i >= 0; i--) {
      builder.intercept(sorted.get(i));
    }

    services.forEach(builder::addService);
    builder.addService(health.getHealthService());
    if (props.reflectionEnabled()) {
      builder.addService(ProtoReflectionService.newInstance());
    }
    return builder.build();
  }

  @Bean
  GrpcServerLifeCycle grpcServerLifeCycle(Server server, GrpcServerProperties props) {
    return new GrpcServerLifeCycle(server, props.shutdownGracePeriod());
  }
}
