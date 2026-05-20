package com.example.delivery.services;

import com.example.delivery.client.OrderClient;
import com.example.delivery.client.UserClient;
import com.example.delivery.dto.AddressResponse;
import com.example.delivery.dto.DeliveryResponseDto;
import com.example.delivery.dto.NotificationRequest;
import com.example.delivery.dto.OrderDto;
import com.example.delivery.model.Delivery;
import com.example.delivery.model.DeliveryStatus;
import com.example.delivery.repository.DeliveryRepository;
import com.example.delivery.exceptions.APIException;
import com.example.delivery.exceptions.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private OrderClient orderClient;

    @Autowired
    private UserClient addressClient;

 
    // Create delivery from order (seller)
    public DeliveryResponseDto createDeliveryFromOrder(Long sellerId, Long orderId, String courierName) {
        OrderDto order = orderClient.getOrderByIdForSeller(sellerId, orderId);
        if (order == null) throw new ResourceNotFoundException("Order", "orderId", orderId);

        AddressResponse address = addressClient.getAddressesById(order.getAddress().getId());
        if (address == null) throw new ResourceNotFoundException("Address", "addressId", order.getAddress().getId());

        Delivery delivery = new Delivery();
        delivery.setOrderId(order.getId());
        delivery.setUserId(order.getUserId());
        delivery.setAddressId(address.getId());
        delivery.setCourierName(courierName);
        delivery.setDeliveryStatus(DeliveryStatus.PENDING);
        delivery.setEstimatedDeliveryDate(LocalDate.now().plusDays(5));

        Delivery saved = deliveryRepository.save(delivery);

        return mapToDto(saved, address);
    }

    // Admin - get all deliveries
    public List<DeliveryResponseDto> getAllDeliveries() {
        return deliveryRepository.findAll().stream()
                .map(delivery -> {
                    AddressResponse address = addressClient.getAddressesById(delivery.getAddressId());
                    return mapToDto(delivery, address);
                })
                .collect(Collectors.toList());
    }

    // Admin - get delivery by ID
    public DeliveryResponseDto getDeliveryById(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "id", deliveryId));
        AddressResponse address = addressClient.getAddressesById(delivery.getAddressId());
        return mapToDto(delivery, address);
    }

    // User - get delivery by userId
    public DeliveryResponseDto getDeliveryByUser(Long userId, Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .filter(d -> d.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "id", deliveryId));
        AddressResponse address = addressClient.getAddressesById(delivery.getAddressId());
        return mapToDto(delivery, address);
    }

    @Autowired
    private KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    private static final String TOPIC = "notification-topic";

    public DeliveryResponseDto updateDeliveryStatus(Long sellerId, Long deliveryId, DeliveryStatus status) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "id", deliveryId));

        OrderDto order = orderClient.getOrderByIdForSeller(sellerId, delivery.getOrderId());
        if (order == null) throw new APIException("You are not authorized to update this delivery");

        delivery.setDeliveryStatus(status);

        if (status == DeliveryStatus.DELIVERED) {
            delivery.setActualDeliveryDate(LocalDate.now());

            try {
                var userResponse = addressClient.getUser(order.getUserId()).getBody();

                NotificationRequest notification = new NotificationRequest();
                notification.setEmail(userResponse.getEmail());
                notification.setSubject("Your Order #" + delivery.getOrderId() + " Has Been Delivered!");
                notification.setMessage(
                    "Hello " + userResponse.getUsername() + ",\n\n" +
                    "We’re happy to inform you that your order #" + delivery.getOrderId() + 
                    " has been successfully delivered.\nEnjoy your purchase!\n\n" +
                    "Thank you for shopping with E-Commerce Hub.\n\n" +
                    "Warm regards,\nE-Commerce Hub Team"
                );

                // ✅ Publish event to Kafka topic
                kafkaTemplate.send(TOPIC, notification);
                System.out.println("📤 Notification event sent to Kafka for user: " + userResponse.getEmail());

            } catch (Exception e) {
                System.err.println("⚠️ Failed to publish Kafka notification event: " + e.getMessage());
            }
        }

        Delivery updatedDelivery = deliveryRepository.save(delivery);
        AddressResponse address = addressClient.getAddressesById(delivery.getAddressId());
        return mapToDto(updatedDelivery, address);
    }



    // Utility mapping method
    private DeliveryResponseDto mapToDto(Delivery delivery, AddressResponse address) {
        DeliveryResponseDto dto = new DeliveryResponseDto();
        dto.setId(delivery.getId());
        dto.setOrderId(delivery.getOrderId());
        dto.setUserId(delivery.getUserId());
        dto.setCourierName(delivery.getCourierName());
        dto.setDeliveryStatus(delivery.getDeliveryStatus().name());
        dto.setEstimatedDeliveryDate(delivery.getEstimatedDeliveryDate());
        dto.setActualDeliveryDate(delivery.getActualDeliveryDate());
        dto.setAddress(address);
        return dto;
    }

    public List<DeliveryResponseDto> getBySellerDeliveries(Long sellerId) {
        // 1️⃣ Fetch all deliveries
        List<Delivery> allDeliveries = deliveryRepository.findAll();

        // 2️⃣ Filter deliveries belonging to the seller
        List<Delivery> sellerDeliveries = allDeliveries.stream()
            .filter(delivery -> {
                try {
                    // Try fetching order for this seller; if null or exception, skip
                    OrderDto order = orderClient.getOrderByIdForSeller(sellerId, delivery.getOrderId());
                    return order != null; 
                } catch (Exception e) {
                    return false; // Not this seller's order
                }
            })
            .collect(Collectors.toList());

        // 3️⃣ Map to DTOs
        return sellerDeliveries.stream()
            .map(delivery -> {
                AddressResponse address = addressClient.getAddressesById(delivery.getAddressId());
                return mapToDto(delivery, address);
            })
            .collect(Collectors.toList());
    }
    
    public DeliveryResponseDto getDeliveryByUserAndOrder(Long userId, Long orderId) {
        // 1️⃣ Find delivery by orderId
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "orderId", orderId));

        // 2️⃣ Ensure this delivery belongs to the given user
        if (!delivery.getUserId().equals(userId)) {
            throw new APIException("You are not authorized to view this delivery");
        }

        // 3️⃣ Fetch address for response
        AddressResponse address = addressClient.getAddressesById(delivery.getAddressId());

        // 4️⃣ Map and return DTO
        return mapToDto(delivery, address);
    }


    
    
}
