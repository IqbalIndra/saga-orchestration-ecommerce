package com.ecommerce.saga.orchestration.inventory.adapter.in.web;

import com.ecommerce.saga.orchestration.inventory.application.port.in.UpdateStockUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class StockItemController {
  private final UpdateStockUseCase updateStockUseCase;

  @GetMapping("/stock")
  public ResponseEntity<Map<String, Integer>> getStockLevels(@RequestParam("product_ids") List<String> productIds) {
    return ResponseEntity.ok(updateStockUseCase.getStock(productIds));
  }
}
