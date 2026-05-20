package com.example.cart.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.cart.model.Cart;
import com.example.cart.model.CartItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
	Optional<Cart> findByUserId(Long userId);
}