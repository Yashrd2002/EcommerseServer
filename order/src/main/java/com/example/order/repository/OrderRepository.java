package com.example.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.order.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	List<Order> findByUserId(Long userId);
	
	@Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems i WHERE i.sellerId = :sellerId")
	List<Order> findOrdersBySellerId(@Param("sellerId") Long sellerId);
}