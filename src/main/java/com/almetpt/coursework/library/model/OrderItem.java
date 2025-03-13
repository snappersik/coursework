package com.almetpt.coursework.library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "default_generator", sequenceName = "order_items_sequence", allocationSize = 1)
public class OrderItem extends GenericModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order; // Связь с заказом

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // Связь с продуктом

}
