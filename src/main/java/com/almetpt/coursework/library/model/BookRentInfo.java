package com.almetpt.coursework.library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_rent_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "default_generator", sequenceName = "book_rent_seq_info", allocationSize = 1)
public class BookRentInfo extends GenericModel {

    @ManyToOne (cascade = {CascadeType.PERSIST, CascadeType.MERGE}) // у одного пользователя могут много аренд
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_RENT_BOOK_INFO_USER"))
    private User user;

    @ManyToOne (cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "book_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_RENT_BOOK_INFO_BOOK"))
    private Product product;

    @Column(name = "rent_date", nullable = false)
    private LocalDateTime rentDate;

    @Column(name = "return_date", nullable = false)
    private LocalDateTime returnDate;

    @Column(name = "rent_period", nullable = false)
    private Integer rentPeriod;

    @Column(name = "returned", nullable = false)
    private boolean returned;

}