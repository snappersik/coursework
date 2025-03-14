package com.almetpt.coursework.BookClub.mapper;

import com.almetpt.coursework.BookClub.dto.BookDTO;
import com.almetpt.coursework.BookClub.model.Book;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookMapper extends GenericMapper<Book, BookDTO> {

    public BookMapper(ModelMapper mapper) {
        super(Book.class, BookDTO.class, mapper);
    }

    @PostConstruct
    @Override
    public void setupMapper() {
        modelMapper.createTypeMap(Book.class, BookDTO.class)
                .setPostConverter(toDTOConverter());

        modelMapper.createTypeMap(BookDTO.class, Book.class)
                .setPostConverter(toEntityConverter());
    }

    @Override
    protected List<Long> getIds(Book entity) {
        return null;
    }
}
