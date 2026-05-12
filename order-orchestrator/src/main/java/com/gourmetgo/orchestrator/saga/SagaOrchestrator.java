package com.gourmetgo.orchestrator.saga;

import com.gourmetgo.grpc.accounting.*;
import com.gourmetgo.grpc.kitchen.*;
import com.gourmetgo.grpc.order.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class SagaOrchestrator {

    @GrpcClient("order-service")
    private OrderServiceGrpc.OrderServiceBlockingStub orderStub;

    @GrpcClient("kitchen-service")
    private KitchenServiceGrpc.KitchenServiceBlockingStub kitchenStub;

    @GrpcClient("accounting-service")
    private AccountingServiceGrpc.AccountingServiceBlockingStub accountingStub;

    public SagaResult executeOrderSaga(String orderId, double amount) {
        log.info("=== SAGA START: orderId={}, amount={} ===", orderId, amount);

        // Step 1: Create Order → APPROVAL_PENDING
        log.info("Step 1: Creating order...");
        OrderResponse createResp = orderStub
                .withDeadlineAfter(5, TimeUnit.SECONDS)
                .createOrder(
                CreateOrderRequest.newBuilder()
                        .setOrderId(orderId)
                        .setAmount(amount)
                        .build());

        if (!createResp.getSuccess()) {
            log.error("Step 1 FAILED: {}", createResp.getMessage());
            return SagaResult.failure("Order creation failed: " + createResp.getMessage());
        }
        log.info("Step 1 OK: Order created with status APPROVAL_PENDING");

        // Step 2: Create Kitchen Ticket
        log.info("Step 2: Creating kitchen ticket...");
        TicketResponse kitchenResp = kitchenStub
                .withDeadlineAfter(5, TimeUnit.SECONDS)
                .createTicket(
                CreateTicketRequest.newBuilder()
                        .setOrderId(orderId)
                        .setAmount(amount)
                        .build());

        if (!kitchenResp.getSuccess()) {
            log.error("Step 2 FAILED: {}. Starting compensation...", kitchenResp.getMessage());
            compensate_rejectOrder(orderId);
            return SagaResult.failure("Kitchen ticket failed: " + kitchenResp.getMessage());
        }
        log.info("Step 2 OK: Kitchen ticket created - {}", kitchenResp.getTicketId());

        // Step 3: Authorize Payment
        log.info("Step 3: Authorizing payment...");
        PaymentResponse paymentResp = accountingStub
                .withDeadlineAfter(5, TimeUnit.SECONDS)
                .authorizePayment(
                AuthorizePaymentRequest.newBuilder()
                        .setOrderId(orderId)
                        .setAmount(amount)
                        .build());

        if (!paymentResp.getSuccess()) {
            log.error("Step 3 FAILED: {}. Starting compensation...", paymentResp.getMessage());
            // COMPENSATION: cancel kitchen ticket + reject order
            compensate_cancelKitchenTicket(orderId);
            compensate_rejectOrder(orderId);
            return SagaResult.failure("Payment failed: " + paymentResp.getMessage());
        }
        log.info("Step 3 OK: Payment authorized - {}", paymentResp.getPaymentId());

        // Step 4: Update Order → APPROVED
        log.info("Step 4: Updating order to APPROVED...");
        orderStub
                .withDeadlineAfter(5, TimeUnit.SECONDS)
                .updateOrderStatus(UpdateOrderStatusRequest.newBuilder()
                .setOrderId(orderId)
                .setStatus("APPROVED")
                .build());

        log.info("=== SAGA COMPLETE: Order {} APPROVED ===", orderId);
        return SagaResult.success("Order approved successfully!", orderId, "APPROVED");
    }

    private void compensate_cancelKitchenTicket(String orderId) {
        log.warn("COMPENSATION: Cancelling kitchen ticket for order {}", orderId);
        try {
            kitchenStub
                    .withDeadlineAfter(5, TimeUnit.SECONDS)
                    .cancelTicket(CancelTicketRequest.newBuilder()
                    .setOrderId(orderId)
                    .build());
            log.warn("COMPENSATION OK: Kitchen ticket cancelled");
        } catch (Exception e) {
            log.error("COMPENSATION ERROR: Failed to cancel kitchen ticket: {}", e.getMessage());
        }
    }

    private void compensate_rejectOrder(String orderId) {
        log.warn("COMPENSATION: Rejecting order {}", orderId);
        try {
            orderStub
                    .withDeadlineAfter(5, TimeUnit.SECONDS)
                    .updateOrderStatus(UpdateOrderStatusRequest.newBuilder()
                    .setOrderId(orderId)
                    .setStatus("REJECTED")
                    .build());
            log.warn("COMPENSATION OK: Order set to REJECTED");
        } catch (Exception e) {
            log.error("COMPENSATION ERROR: Failed to reject order: {}", e.getMessage());
        }
    }
}
