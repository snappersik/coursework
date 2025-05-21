package com.almetpt.coursework.bookclub.dto;

import com.almetpt.coursework.bookclub.model.ProductCategory;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO extends GenericDTO {
    private String name;
    private String description;
    private BigDecimal price;
    private ProductCategory category;
    private String coverImageUrl;
    private String coverImageFilename; // Name of the saved cover file
    private String originalCoverImageFilename; // Original name of the cover file
    private boolean hasLocalImage;
    private String electronicProductFilename; // Name of the saved electronic file
    private String originalElectronicProductFilename; // Original name of the electronic file
    private boolean hasElectronicFile; // Derived: true if electronicProductFilename is present
}
