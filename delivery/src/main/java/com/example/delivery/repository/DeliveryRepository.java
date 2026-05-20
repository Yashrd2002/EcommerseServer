package com.example.delivery.repository;

import com.example.delivery.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findByUserId(Long userId);
    Optional<Delivery> findByOrderId(Long orderId);
    

}
