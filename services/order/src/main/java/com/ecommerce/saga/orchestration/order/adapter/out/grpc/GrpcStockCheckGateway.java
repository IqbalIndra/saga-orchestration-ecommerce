package com.ecommerce.saga.orchestration.order.adapter.out.grpc;

import com.ecommerce.saga.orchestration.grpc.inventory.CheckStockRequest;
import com.ecommerce.saga.orchestration.grpc.inventory.InventoryGrpcServiceGrpc;
import com.ecommerce.saga.orchestration.order.application.port.out.StockCheckGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class GrpcStockCheckGateway implements StockCheckGateway {
  private final InventoryGrpcServiceGrpc.InventoryGrpcServiceBlockingStub inventoryStub;

  @Override
  public StockCheckResult checkStock(List<StockItem> items) {
    var list = items.stream()
        .map(i ->
            com.ecommerce.saga.orchestration.grpc.inventory.StockItem.newBuilder()
                .setProductId(i.productId())
                .setQuantity(i.quantity())
                .build()
        ).toList();
    var checkStockResponse = inventoryStub.withDeadlineAfter(2, TimeUnit.SECONDS)
        .checkStock(CheckStockRequest.newBuilder().addAllItems(list).build());

    return new StockCheckResult(checkStockResponse.getAvailable());
  }
}
