package com.example.authservice.repository;


import com.example.authservice.model.Address;
import com.example.authservice.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

	List<Address> findByUserUsername(String username);
	Optional<Address> findById(Long id);
	List<Address> findByUserId(Long userId);


}
