package com.example.authservice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.authservice.dto.AddressRequest;
import com.example.authservice.dto.AddressResponse;
import com.example.authservice.exceptions.APIException;
import com.example.authservice.exceptions.ResourceNotFoundException;
import com.example.authservice.model.Address;
import com.example.authservice.model.User;
import com.example.authservice.repository.AddressRepository;
import com.example.authservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public void addAddress(AddressRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Address address = Address.builder()
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .user(user)
                .build();

        addressRepository.save(address);
    }

    public void editAddress(AddressRequest request, Long userId, Long addressId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        // Only allow editing if the address belongs to the user
        if (!address.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You are not authorized to edit this address");
        }

        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());

        addressRepository.save(address);
    }

    public List<AddressResponse> getAddresses(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return addressRepository.findByUserId(userId).stream()
                .map(addr -> AddressResponse.builder()
                        .id(addr.getId())
                        .street(addr.getStreet())
                        .city(addr.getCity())
                        .state(addr.getState())
                        .postalCode(addr.getPostalCode())
                        .country(addr.getCountry())
                        .build())
                .collect(Collectors.toList());
    }
    
    public AddressResponse getAddressById(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", id));

        return mapToResponse(address);
    }

    private AddressResponse mapToResponse(Address address) {
        AddressResponse dto = new AddressResponse();
        dto.setId(address.getId());
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setPostalCode(address.getPostalCode());
        dto.setCountry(address.getCountry());
        return dto;
    }
    
    public void deleteAddress(Long userId, Long addressId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        // Ensure the address belongs to the requesting user
        if (!address.getUser().getId().equals(userId)) {
            throw new APIException("You are not authorized to delete this address");
        }

        addressRepository.delete(address);
    }

}
