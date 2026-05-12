package com.gourmetgo.accounting.grpc;

import com.gourmetgo.grpc.accounting.*;
import com.gourmetgo.accounting.entity.Payment;
import com.gourmetgo.accounting.repository.PaymentRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import java.util.Optional;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class AccountingGrpcService extends AccountingServiceGrpc.AccountingServiceImplBase {

    private final PaymentRepository paymentRepository;

    @Value("${payment.failure-threshold:1000.0}")
    private double failureThreshold;

    @Override
    public void authorizePayment(AuthorizePaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
        // Payment fails if amount exceeds threshold (simulates payment failure)
        boolean paymentApproved = request.getAmount() <= failureThreshold;

        String status = paymentApproved ? "AUTHORIZED" : "FAILED";
        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID().toString())
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .status(status)
                .build();
        paymentRepository.save(payment);

        responseObserver.onNext(PaymentResponse.newBuilder()
                .setPaymentId(payment.getPaymentId())
                .setOrderId(payment.getOrderId())
                .setStatus(status)
                .setMessage(paymentApproved ? "Payment authorized" : "Payment failed: insufficient funds")
                .setSuccess(paymentApproved)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void cancelPayment(CancelPaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
        Optional<Payment> optPayment = paymentRepository.findByOrderId(request.getOrderId());
        if (optPayment.isPresent()) {
            Payment payment = optPayment.get();
            payment.setStatus("CANCELLED");
            paymentRepository.save(payment);
            responseObserver.onNext(PaymentResponse.newBuilder()
                    .setPaymentId(payment.getPaymentId())
                    .setOrderId(payment.getOrderId())
                    .setStatus("CANCELLED")
                    .setMessage("Payment cancelled")
                    .setSuccess(true)
                    .build());
        } else {
            responseObserver.onNext(PaymentResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Payment not found")
                    .build());
        }
        responseObserver.onCompleted();
    }
}
