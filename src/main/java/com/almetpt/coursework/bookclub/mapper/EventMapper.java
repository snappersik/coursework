package com.almetpt.coursework.bookclub.mapper;

import com.almetpt.coursework.bookclub.dto.EventDTO;

import com.almetpt.coursework.bookclub.model.Event;

import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class EventMapper extends GenericMapper<Event, EventDTO> {

    public EventMapper(ModelMapper modelMapper) {
        super(Event.class, EventDTO.class, modelMapper);
    }

    @PostConstruct
    @Override
    protected void setupMapper() {
        modelMapper.createTypeMap(Event.class, EventDTO.class)
                .setPostConverter(toDTOConverter());
        modelMapper.createTypeMap(EventDTO.class, Event.class)
                .setPostConverter(toEntityConverter());
    }

    @Override
    protected List<Long> getIds(Event entity) {
        return Collections.emptyList(); 
    }
}
