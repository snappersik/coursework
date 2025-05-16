package com.almetpt.coursework.bookclub.mapper;

import com.almetpt.coursework.bookclub.dto.SliderBookDTO;
import com.almetpt.coursework.bookclub.model.Book;
import com.almetpt.coursework.bookclub.model.SliderBook;
import com.almetpt.coursework.bookclub.repository.BookRepository;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.webjars.NotFoundException;

import java.util.Collections;
import java.util.List;

@Component
public class SliderBookMapper extends GenericMapper<SliderBook, SliderBookDTO> {

    private final BookRepository bookRepository;

    public SliderBookMapper(ModelMapper mapper, BookRepository bookRepository) {
        super(SliderBook.class, SliderBookDTO.class, mapper);
        this.bookRepository = bookRepository;
    }

    @PostConstruct
    @Override
    protected void setupMapper() {
        modelMapper.createTypeMap(SliderBook.class, SliderBookDTO.class)
                .addMappings(mapping -> {
                    mapping.map(src -> src.getBook().getId(), SliderBookDTO::setBookId);
                    mapping.map(src -> src.getBook().getTitle(), SliderBookDTO::setBookTitle);
                    mapping.map(src -> src.getBook().getAuthor(), SliderBookDTO::setBookAuthor);
                    mapping.map(src -> src.getBackgroundImageUrl(), SliderBookDTO::setBackgroundImageUrl);
                    mapping.map(src -> src.hasLocalBackgroundImage(), SliderBookDTO::setHasLocalBackgroundImage);
                    mapping.map(src -> src.getBook().getCoverImageUrl(), SliderBookDTO::setCoverImageUrl);
                })
                .setPostConverter(toDTOConverter());
                
        modelMapper.createTypeMap(SliderBookDTO.class, SliderBook.class)
                .addMappings(mapping -> {
                    mapping.skip(SliderBook::setBook);
                })
                .setPostConverter(toEntityConverter());
    }

    @Override
    protected void mapSpecificFields(SliderBookDTO source, SliderBook destination) {
        if (source.getBookId() != null) {
            Book book = bookRepository.findById(source.getBookId())
                    .orElseThrow(() -> new NotFoundException("Book not found with id: " + source.getBookId()));
            destination.setBook(book);
        }
    }

    @Override
    protected List<Long> getIds(SliderBook entity) {
        return Collections.singletonList(entity.getBook().getId());
    }
}
