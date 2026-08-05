package com.market.market.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "products") // Standard plural naming convention for database tables
@Data // Automatically generates Getters, Setters, toString, equals, and hashCode
@NoArgsConstructor // Generates the mandatory empty constructor for JPA
@AllArgsConstructor // Generates a constructor with all fields
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // EAN is mandatory and must be unique
    @Column(name = "ean", nullable = false, unique = true, length = 13)
    private String ean;

    @Column(name = "price")
    private Double price;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "dimension", length = 50)
    private String dimension;

    // Stores only the date (Year-Month-Day) without time zone, perfect for expirations
    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    // Keeps track of available units in the market
    @Column(name = "stock")
    private Integer stock = 0;
}
