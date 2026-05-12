package com.gourmetgo.kitchen.grpc;

import com.gourmetgo.grpc.kitchen.*;
import com.gourmetgo.kitchen.entity.KitchenTicket;
import com.gourmetgo.kitchen.repository.KitchenTicketRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import lombok.RequiredArgsConstructor;
import java.util.Optional;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class KitchenGrpcService extends KitchenServiceGrpc.KitchenServiceImplBase {

    private final KitchenTicketRepository ticketRepository;

    @Override
    public void createTicket(CreateTicketRequest request, StreamObserver<TicketResponse> responseObserver) {
        try {
            KitchenTicket ticket = KitchenTicket.builder()
                    .ticketId(UUID.randomUUID().toString())
                    .orderId(request.getOrderId())
                    .amount(request.getAmount())
                    .status("CREATED")
                    .build();
            ticketRepository.save(ticket);

            responseObserver.onNext(TicketResponse.newBuilder()
                    .setTicketId(ticket.getTicketId())
                    .setOrderId(ticket.getOrderId())
                    .setStatus(ticket.getStatus())
                    .setMessage("Kitchen ticket created")
                    .setSuccess(true)
                    .build());
        } catch (Exception e) {
            responseObserver.onNext(TicketResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error: " + e.getMessage())
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void cancelTicket(CancelTicketRequest request, StreamObserver<TicketResponse> responseObserver) {
        Optional<KitchenTicket> optTicket = ticketRepository.findByOrderId(request.getOrderId());
        if (optTicket.isPresent()) {
            KitchenTicket ticket = optTicket.get();
            ticket.setStatus("CANCELLED");
            ticketRepository.save(ticket);
            responseObserver.onNext(TicketResponse.newBuilder()
                    .setTicketId(ticket.getTicketId())
                    .setOrderId(ticket.getOrderId())
                    .setStatus("CANCELLED")
                    .setMessage("Kitchen ticket cancelled (compensation)")
                    .setSuccess(true)
                    .build());
        } else {
            responseObserver.onNext(TicketResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Ticket not found for order: " + request.getOrderId())
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getTicket(GetTicketRequest request, StreamObserver<TicketResponse> responseObserver) {
        Optional<KitchenTicket> optTicket = ticketRepository.findByOrderId(request.getOrderId());
        if (optTicket.isPresent()) {
            KitchenTicket ticket = optTicket.get();
            responseObserver.onNext(TicketResponse.newBuilder()
                    .setTicketId(ticket.getTicketId())
                    .setOrderId(ticket.getOrderId())
                    .setStatus(ticket.getStatus())
                    .setSuccess(true)
                    .build());
        } else {
            responseObserver.onNext(TicketResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Ticket not found")
                    .build());
        }
        responseObserver.onCompleted();
    }
}
