package com.example.product.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ratings")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double ratingValue;

    @Column(length = 1000)
    private String review;

    private LocalDateTime createdAt;

    private Long userId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonBackReference
    private Product product;


}
