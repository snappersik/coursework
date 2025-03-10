package com.almetpt.coursework.library.mapper;

import com.aptproject.springlibraryproject.library.dto.BookDTO;
import com.aptproject.springlibraryproject.library.model.Book;
import com.aptproject.springlibraryproject.library.model.GenericModel;
import com.aptproject.springlibraryproject.library.repository.AuthorRepository;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class BookMapper
        extends GenericMapper<Book, BookDTO> {
    private final AuthorRepository authorRepository;

    private final AuthorMapper authorMapper;

    protected BookMapper(ModelMapper mapper, AuthorRepository authorRepository, AuthorMapper authorMapper) {
        super(Book.class, BookDTO.class, mapper);
        this.authorRepository = authorRepository;
        this.authorMapper = authorMapper;
    }

    @PostConstruct
    @Override
    public void setupMapper() {
        modelMapper.createTypeMap(Book.class, BookDTO.class)
                .addMappings(
                        mapping -> {
                            mapping.skip(BookDTO::setAuthorIds);
                            mapping.skip(BookDTO::setAuthorInfo);
                        })
                .setPostConverter(toDTOConverter());


        modelMapper.createTypeMap(BookDTO.class, Book.class)
                .addMappings(mapping -> mapping.skip(Book::setAuthors)).setPostConverter(toEntityConverter());
    }


    @Override
    protected void mapSpecificFields(BookDTO source, Book destination) {
        if (!Objects.isNull(source.getAuthorIds())) {
            destination.setAuthors(authorRepository.findAllById(source.getAuthorIds()));
        }
        else {
            destination.setAuthors(Collections.emptyList());
        }
    }

    @Override
    protected void mapSpecificFields(Book source, BookDTO destination) {
        destination.setAuthorIds(getIds(source));
        destination.setAuthorInfo(authorMapper.toDTOs(source.getAuthors()));
    }

    @Override
    protected List<Long> getIds(Book book) {
        return Objects.isNull(book) || Objects.isNull(book.getAuthors())
                ? null
                : book.getAuthors().stream()
                .map(GenericModel::getId)
                .collect(Collectors.toList());
    }
}
