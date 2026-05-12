package com.gourmetgo.kitchen.repository;

import com.gourmetgo.kitchen.entity.KitchenTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface KitchenTicketRepository extends JpaRepository<KitchenTicket, Long> {
    Optional<KitchenTicket> findByOrderId(String orderId);
}
