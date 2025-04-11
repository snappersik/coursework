package com.almetpt.coursework.bookclub.mapper;

import com.almetpt.coursework.bookclub.dto.EventApplicationDTO;

import com.almetpt.coursework.bookclub.model.EventApplication;

import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class EventApplicationMapper extends GenericMapper<EventApplication, EventApplicationDTO> {

    public EventApplicationMapper(ModelMapper modelMapper) {
        super(EventApplication.class, EventApplicationDTO.class, modelMapper);
    }

    @PostConstruct
    @Override
    protected void setupMapper() {
        modelMapper.createTypeMap(EventApplication.class, EventApplicationDTO.class)
                .setPostConverter(toDTOConverter());
        modelMapper.createTypeMap(EventApplicationDTO.class, EventApplication.class)
                .setPostConverter(toEntityConverter());
    }

    @Override
    protected List<Long> getIds(EventApplication entity) {
        return Collections.emptyList();
    }
}
