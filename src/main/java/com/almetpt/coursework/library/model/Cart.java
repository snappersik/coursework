package com.almetpt.coursework.library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "cart")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "default_generator", sequenceName = "cart_seq", allocationSize = 1)
public class Cart extends GenericModel {

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_CART_USER"))
    private User user;

    // Хранение списка товаров в корзине и их количества
    @ElementCollection
    @CollectionTable(name = "cart_items", joinColumns = @JoinColumn(name = "cart_id"))
    @MapKeyJoinColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<Product, Integer> items = new HashMap<>();

    // Метод для добавления товара в корзину
    public void addItem(Product product, int quantity) {
        items.put(product, items.getOrDefault(product, 0) + quantity);
    }

    // Метод для подсчета итоговой суммы
    public double getTotalPrice() {
        return items.entrySet().stream()
                .map(entry -> entry.getKey().getPrice().multiply(BigDecimal.valueOf(entry.getValue()))) // Умножаем BigDecimal на количество
                .reduce(BigDecimal.ZERO, BigDecimal::add) // Суммируем все значения
                .doubleValue(); // Преобразуем итоговую сумму в double
    }
}
