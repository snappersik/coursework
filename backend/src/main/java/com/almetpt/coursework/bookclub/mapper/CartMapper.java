package com.almetpt.coursework.bookclub.mapper;

import com.almetpt.coursework.bookclub.dto.CartDTO;
import com.almetpt.coursework.bookclub.dto.ProductDTO;
import com.almetpt.coursework.bookclub.model.Cart;
import com.almetpt.coursework.bookclub.model.Product;
import com.almetpt.coursework.bookclub.model.User;
import com.almetpt.coursework.bookclub.repository.ProductRepository;
import com.almetpt.coursework.bookclub.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.webjars.NotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper extends GenericMapper<Cart, CartDTO> {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartMapper(ModelMapper mapper, UserRepository userRepository, ProductRepository productRepository) {
        super(Cart.class, CartDTO.class, mapper);
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @PostConstruct
    @Override
    protected void setupMapper() {
        modelMapper.createTypeMap(Cart.class, CartDTO.class)
            .addMappings(mapper -> {
                // Безопасное маппирование userId
                mapper.using(ctx -> {
                    Cart source = (Cart) ctx.getSource();
                    return source.getUser() != null ? source.getUser().getId() : null;
                }).map(src -> src, CartDTO::setUserId);
            })
            .setPostConverter(context -> {
                Cart source = context.getSource();
                CartDTO destination = context.getDestination();
                
                // Маппинг продуктов
                if (source.getProducts() != null) {
                    List<ProductDTO> productDTOs = source.getProducts().stream()
                            .map(product -> modelMapper.map(product, ProductDTO.class))
                            .collect(Collectors.toList());
                    destination.setProducts(productDTOs);
                }
                
                return destination;
            });

        modelMapper.createTypeMap(CartDTO.class, Cart.class)
            .addMappings(mapper -> {
                mapper.skip(Cart::setUser);
                mapper.skip(Cart::setProducts);
            })
            .setPostConverter(toEntityConverter());
    }

    @Override
    protected void mapSpecificFields(CartDTO source, Cart destination) {
        if (source.getUserId() != null) {
            User user = userRepository.findById(source.getUserId())
                    .orElseThrow(() -> new NotFoundException("User not found with id: " + source.getUserId()));
            destination.setUser(user);
        }

        if (source.getProducts() != null) {
            List<Product> products = source.getProducts().stream()
                    .map(productDTO -> productRepository.findById(productDTO.getId())
                            .orElseThrow(() -> new NotFoundException("Product not found with id: " + productDTO.getId())))
                    .collect(Collectors.toList());
            destination.setProducts(products);
        }
    }

    @Override
    protected List<Long> getIds(Cart entity) {
        return Collections.emptyList();
    }
}
