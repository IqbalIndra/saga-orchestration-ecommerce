package com.ecommerce.saga.orchestration.inventory.adapter.in.grpc;

import com.ecommerce.saga.orchestration.inventory.application.port.in.CheckStockUseCase;
import com.ecommerce.saga.orchestration.grpc.inventory.CheckStockRequest;
import com.ecommerce.saga.orchestration.grpc.inventory.CheckStockResponse;
import com.ecommerce.saga.orchestration.grpc.inventory.InventoryGrpcServiceGrpc;
import com.ecommerce.saga.orchestration.grpc.inventory.StockAvailability;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryGrpcServiceImpl extends InventoryGrpcServiceGrpc.InventoryGrpcServiceImplBase {

  private final CheckStockUseCase checkStockUseCase;

  @Override
  public void checkStock(CheckStockRequest request, StreamObserver<CheckStockResponse> responseObserver) {
    List<CheckStockUseCase.StockCheckItem> items = request.getItemsList().stream()
        .map(item -> new CheckStockUseCase.StockCheckItem(
            item.getProductId(),
            item.getQuantity()
        ))
        .toList();

    CheckStockUseCase.StockCheckResult result = checkStockUseCase.checkStock(items);

    List<StockAvailability> availability = result.items().stream()
        .map(item -> StockAvailability.newBuilder()
            .setProductId(item.productId())
            .setRequested(item.requested())
            .setAvailable(item.available())
            .setSufficient(item.sufficient())
            .build())
        .toList();

    responseObserver.onNext(CheckStockResponse.newBuilder()
        .setAvailable(result.allAvailable())
        .addAllAvailability(availability)
        .build());

    responseObserver.onCompleted();
  }
}
