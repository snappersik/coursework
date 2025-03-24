package com.almetpt.coursework.bookclub.mapper;

import com.almetpt.coursework.bookclub.dto.EventDTO;
import com.almetpt.coursework.bookclub.model.Event;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventMapper extends GenericMapper<Event, EventDTO> {

    protected EventMapper(ModelMapper modelMapper) {
        super(Event.class, EventDTO.class, modelMapper);
    }

    @Override
    protected void setupMapper() {
        modelMapper.createTypeMap(Event.class, EventDTO.class)
                .setPostConverter(toDTOConverter());
        modelMapper.createTypeMap(EventDTO.class, Event.class)
                .setPostConverter(toEntityConverter());
    }

    @Override
    protected void mapSpecificFields(EventDTO source, Event destination) {
        // Дополнительный маппинг для полей Event, если необходимо
    }

    @Override
    protected void mapSpecificFields(Event source, EventDTO destination) {
        // Дополнительный маппинг для полей EventDTO, если необходимо
    }

    @Override
    protected List<Long> getIds(Event entity) {
        return null;
    }
}
