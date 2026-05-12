package com.gourmetgo.order.grpc;

import com.gourmetgo.grpc.order.*;
import com.gourmetgo.order.entity.Order;
import com.gourmetgo.order.repository.OrderRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;

@GrpcService
@RequiredArgsConstructor
public class OrderGrpcService extends OrderServiceGrpc.OrderServiceImplBase {

    private final OrderRepository orderRepository;

    @Override
    public void createOrder(CreateOrderRequest request, StreamObserver<OrderResponse> responseObserver) {
        try {
            Order order = Order.builder()
                    .orderId(request.getOrderId())
                    .amount(request.getAmount())
                    .status("APPROVAL_PENDING")
                    .build();
            orderRepository.save(order);

            responseObserver.onNext(OrderResponse.newBuilder()
                    .setOrderId(order.getOrderId())
                    .setAmount(order.getAmount())
                    .setStatus(order.getStatus())
                    .setMessage("Order created successfully")
                    .setSuccess(true)
                    .build());
        } catch (Exception e) {
            responseObserver.onNext(OrderResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error: " + e.getMessage())
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void updateOrderStatus(UpdateOrderStatusRequest request, StreamObserver<OrderResponse> responseObserver) {
        Optional<Order> optOrder = orderRepository.findByOrderId(request.getOrderId());
        if (optOrder.isPresent()) {
            Order order = optOrder.get();
            order.setStatus(request.getStatus());
            orderRepository.save(order);
            responseObserver.onNext(OrderResponse.newBuilder()
                    .setOrderId(order.getOrderId())
                    .setAmount(order.getAmount())
                    .setStatus(order.getStatus())
                    .setMessage("Status updated to " + request.getStatus())
                    .setSuccess(true)
                    .build());
        } else {
            responseObserver.onNext(OrderResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Order not found: " + request.getOrderId())
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getOrder(GetOrderRequest request, StreamObserver<OrderResponse> responseObserver) {
        Optional<Order> optOrder = orderRepository.findByOrderId(request.getOrderId());
        if (optOrder.isPresent()) {
            Order order = optOrder.get();
            responseObserver.onNext(OrderResponse.newBuilder()
                    .setOrderId(order.getOrderId())
                    .setAmount(order.getAmount())
                    .setStatus(order.getStatus())
                    .setSuccess(true)
                    .build());
        } else {
            responseObserver.onNext(OrderResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Order not found")
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getAllOrders(GetAllOrdersRequest request, StreamObserver<GetAllOrdersResponse> responseObserver) {
        List<Order> orders = orderRepository.findAll();
        GetAllOrdersResponse.Builder builder = GetAllOrdersResponse.newBuilder();
        for (Order order : orders) {
            builder.addOrders(OrderResponse.newBuilder()
                    .setOrderId(order.getOrderId())
                    .setAmount(order.getAmount())
                    .setStatus(order.getStatus())
                    .setSuccess(true)
                    .build());
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}
