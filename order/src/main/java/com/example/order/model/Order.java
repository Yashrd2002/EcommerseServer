package com.example.order.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Order date cannot be null")
    private LocalDate orderDate;

    @NotNull(message = "Total amount cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0")
    private Double totalAmount;

    @NotBlank(message = "Order status cannot be blank")
    private String orderStatus;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @Size(min = 1, message = "Order must contain at least one item")
    private List<OrderItem> orderItems = new ArrayList<>();

    @NotNull(message = "Address ID cannot be null")
    private Long addressId;
    
    private String paymentMethod;
}
