package com.almetpt.coursework.BookClub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "products")
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
    private BigDecimal price;

    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private Category category;

    @Lob
    @Column(name = "image")
    private byte[] image;

    @ManyToMany(mappedBy = "products")
    private List<CartItem> cartItems;
}
