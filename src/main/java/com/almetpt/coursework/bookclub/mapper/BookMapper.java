package com.almetpt.coursework.bookclub.mapper;

import com.almetpt.coursework.bookclub.dto.BookDTO;
import com.almetpt.coursework.bookclub.model.Book;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class BookMapper extends GenericMapper<Book, BookDTO> {

    public BookMapper(ModelMapper mapper) {
        super(Book.class, BookDTO.class, mapper);
    }

    @PostConstruct
    @Override
    protected void setupMapper() {
        modelMapper.createTypeMap(Book.class, BookDTO.class)
                .addMappings(mapping -> {
                    mapping.map(Book::getCoverImageUrl, BookDTO::setCoverImageUrl);
                    mapping.map(src -> src.hasLocalImage(), BookDTO::setHasLocalImage);
                })
                .setPostConverter(toDTOConverter());
                
        modelMapper.createTypeMap(BookDTO.class, Book.class)
                .addMappings(mapping -> {
                    mapping.map(BookDTO::getCoverImageUrl, Book::setCoverImageUrl);
                })
                .setPostConverter(toEntityConverter());
    }

    @Override
    protected List<Long> getIds(Book entity) {
        return Collections.emptyList();
    }    
}
