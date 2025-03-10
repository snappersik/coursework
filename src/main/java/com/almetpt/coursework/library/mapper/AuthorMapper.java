package com.almetpt.coursework.library.mapper;

import com.aptproject.springlibraryproject.library.dto.AuthorDTO;
import com.aptproject.springlibraryproject.library.model.Author;
import com.aptproject.springlibraryproject.library.model.GenericModel;
import com.aptproject.springlibraryproject.library.repository.BookRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class AuthorMapper extends GenericMapper <Author, AuthorDTO> {
    private final BookRepository bookRepository;

    public AuthorMapper(ModelMapper modelMapper
            , BookRepository bookRepository) {
        super(Author.class, AuthorDTO.class, modelMapper);
        this.bookRepository = bookRepository;
    }

    @Override
    protected void setupMapper() {
        modelMapper.createTypeMap(Author.class, AuthorDTO.class)
                .addMappings(mapping -> mapping.skip(AuthorDTO::setBooksIds))
                .setPostConverter(toDTOConverter());
        modelMapper.createTypeMap(AuthorDTO.class, Author.class)
                .addMappings(mapping -> mapping.skip(Author::setBooks))
                .setPostConverter(toEntityConverter());
    }

    @Override
    protected void mapSpecificFields(AuthorDTO source, Author destination) {
        if (!Objects.isNull(source.getBooksIds())) {
            destination.setBooks(bookRepository.findAllById(source.getBooksIds()));
        } else
            destination.setBooks(Collections.emptyList());
    }

    @Override
    protected void mapSpecificFields(Author source, AuthorDTO destination) {
        destination.setBooksIds(getIds(source));
    }

    @Override
    protected List<Long> getIds(Author source) {
        return Objects.isNull(source) || Objects.isNull(source.getBooks())
                ? Collections.emptyList()
                : source.getBooks().stream()
                .map(GenericModel::getId)
                .collect(Collectors.toList());
    }
}
