package com.gourmetgo.kitchen.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kitchen_tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitchenTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String ticketId;

    @Column(unique = true, nullable = false)
    private String orderId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String status; // CREATED, CANCELLED
}
