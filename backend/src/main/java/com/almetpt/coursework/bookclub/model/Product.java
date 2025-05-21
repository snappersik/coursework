package com.almetpt.coursework.bookclub.model;

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
@SequenceGenerator(name = "default_generator", sequenceName = "products_seq", allocationSize = 1)
public class Product extends GenericModel {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "cover_image_filename")
    private String coverImageFilename;

    @Lob
    @Column(name = "cover_image_data")
    private byte[] coverImageData;
        
    @Column(name = "original_cover_image_filename")
    private String originalCoverImageFilename;

    // New fields for electronic product
    @Column(name = "electronic_product_filename")
    private String electronicProductFilename;

    @Column(name = "original_electronic_product_filename")
    private String originalElectronicProductFilename;

    @Transient
    public boolean hasLocalImage() {
        return (coverImageFilename != null && !coverImageFilename.isBlank()) 
               || (coverImageData != null && coverImageData.length > 0);
    }

    @Transient
    public boolean hasElectronicFile() {
        return electronicProductFilename != null && !electronicProductFilename.isBlank();
    }

    @ManyToMany(mappedBy = "products")
    private List<Cart> carts;

    @ManyToMany(mappedBy = "products")
    private List<Order> orders;
}
