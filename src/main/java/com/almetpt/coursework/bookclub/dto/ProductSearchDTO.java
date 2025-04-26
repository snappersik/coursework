package com.almetpt.coursework.bookclub.dto;

import com.almetpt.coursework.bookclub.model.ProductCategory;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProductSearchDTO {
    private String productName;
    private ProductCategory productCategory;
}
