package com.gourmetgo.orchestrator.service;

import com.gourmetgo.grpc.order.*;
import com.gourmetgo.orchestrator.saga.SagaOrchestrator;
import com.gourmetgo.orchestrator.saga.SagaResult;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OrchestratorController {

    private final SagaOrchestrator sagaOrchestrator;

    @GrpcClient("order-service")
    private OrderServiceGrpc.OrderServiceBlockingStub orderStub;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> body) {
        String orderId = (String) body.get("orderId");
        double amount = Double.parseDouble(body.get("amount").toString());

        SagaResult result = sagaOrchestrator.executeOrderSaga(orderId, amount);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());
        response.put("orderId", orderId);
        response.put("status", result.getStatus());

        return result.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable String orderId) {
        OrderResponse resp = orderStub.getOrder(
                GetOrderRequest.newBuilder().setOrderId(orderId).build());

        Map<String, Object> response = new HashMap<>();
        response.put("success", resp.getSuccess());
        response.put("orderId", resp.getOrderId());
        response.put("amount", resp.getAmount());
        response.put("status", resp.getStatus());
        response.put("message", resp.getMessage());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllOrders() {
        GetAllOrdersResponse resp = orderStub.getAllOrders(
                GetAllOrdersRequest.newBuilder().build());

        List<Map<String, Object>> orders = resp.getOrdersList().stream().map(o -> {
            Map<String, Object> m = new HashMap<>();
            m.put("orderId", o.getOrderId());
            m.put("amount", o.getAmount());
            m.put("status", o.getStatus());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Orchestrator running");
    }
}
