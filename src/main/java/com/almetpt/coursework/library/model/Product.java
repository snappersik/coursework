package com.almetpt.coursework.library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table (name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "default_generator", sequenceName = "products_sequence", allocationSize = 1)
public class Product extends GenericModel {

    @Column(name = "name", nullable = false)
    private String productName;

    @Column(name = "description")
    private String productDescription;

    @Column(name = "price", nullable = false)
    private LocalDate price;

    @Column(name = "category", nullable = false)
    @Enumerated
    private Category category;

}