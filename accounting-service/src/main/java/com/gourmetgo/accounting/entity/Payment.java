package com.gourmetgo.accounting.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String paymentId;

    @Column(unique = true, nullable = false)
    private String orderId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String status; // AUTHORIZED, FAILED, CANCELLED
}
