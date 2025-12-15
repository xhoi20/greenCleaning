package com.laundry.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Entity
    @Builder
    @Table(name = "Customers")
    public class Customer {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id")
        private int id;

        @Column(name = "first_name", nullable = false, length = 50)
        private String firstName;

        @Column(name = "last_name", nullable = false, length = 50)
        private String lastName;

        @Column(name = "phone", nullable = false, length = 20, unique = true)
        private String phone;

        @Column(name = "email", length = 100, unique = true)
        private String email;

        @Column(name = "address", columnDefinition = "VARCHAR(MAX)")
        private String address;

        @Column(name = "loyalty_points", columnDefinition = "INT DEFAULT 0")
        private int loyaltyPoints = 0;

        @Column(name = "created_at", nullable = false)
        private LocalDateTime createdAt = LocalDateTime.now();

        @Column(name = "updated_at", nullable = false)
        private LocalDateTime updatedAt = LocalDateTime.now();

        @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        @JsonIgnore
        private List<Order> orders;

        @Override
        public String toString() {
            return "Customer{" +
                    "id=" + id +
                    ", firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", phone='" + phone + '\'' +
                    ", email='" + email + '\'' +
                    ", address='" + address + '\'' +
                    ", loyaltyPoints=" + loyaltyPoints +
                    '}';
        }
    }

