package com.almetpt.coursework.bookclub.mapper;

import com.almetpt.coursework.bookclub.dto.EventDTO;
import com.almetpt.coursework.bookclub.model.Event;
import com.almetpt.coursework.bookclub.model.Book;
import com.almetpt.coursework.bookclub.repository.BookRepository;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.modelmapper.Converter;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class EventMapper extends GenericMapper<Event, EventDTO> {

    private final BookRepository bookRepository;

    public EventMapper(ModelMapper modelMapper, BookRepository bookRepository) {
        super(Event.class, EventDTO.class, modelMapper);
        this.bookRepository = bookRepository;
    }

    @PostConstruct
    @Override
    protected void setupMapper() {
        modelMapper.createTypeMap(Event.class, EventDTO.class)
                .setPostConverter(toDTOConverter());
        modelMapper.createTypeMap(EventDTO.class, Event.class)
                .setPostConverter(toEntityConverter());
    }

    // Конвертер из Event в EventDTO
    @Override
    protected Converter<Event, EventDTO> toDTOConverter() {
        return context -> {
            Event source = context.getSource();
            EventDTO destination = context.getDestination();
            if (source.getBook() != null) {
                destination.setBookId(source.getBook().getId());
            } else {
                destination.setBookId(null);
            }
            return destination;
        };
    }

    // Конвертер из EventDTO в Event
    @Override
    protected Converter<EventDTO, Event> toEntityConverter() {
        return context -> {
            EventDTO source = context.getSource();
            Event destination = context.getDestination();
            if (source.getBookId() != null) {
                Optional<Book> book = bookRepository.findById(source.getBookId());
                book.ifPresent(destination::setBook);
            } else {
                destination.setBook(null);
            }
            return destination;
        };
    }

    // Этот метод остается без изменений, так как он может быть нужен в GenericMapper
    @Override
    protected List<Long> getIds(Event entity) {
        return Collections.emptyList();
    }
}