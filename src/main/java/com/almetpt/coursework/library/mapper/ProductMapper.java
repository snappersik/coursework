package com.almetpt.coursework.library.mapper;

import com.almetpt.coursework.library.dto.ProductDTO;
import com.almetpt.coursework.library.model.Product;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ProductMapper extends GenericMapper<Product, ProductDTO> {

    public ProductMapper(ModelMapper mapper) {
        super(Product.class, ProductDTO.class, mapper);
    }

    @PostConstruct
    @Override
    public void setupMapper() {
        modelMapper.createTypeMap(Product.class, ProductDTO.class)
                .setPostConverter(toDTOConverter());

        modelMapper.createTypeMap(ProductDTO.class, Product.class)
                .setPostConverter(toEntityConverter());
    }

    @Override
    protected void mapSpecificFieldsToEntity(ProductDTO source, Product destination) {
    }

}
