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
            .addMappings(mapper -> {
                // Используем безопасный маппинг с проверкой на null
                mapper.map(src -> src.getUser() != null ? src.getUser().getId() : null,
                           EventApplicationDTO::setUserId);
                mapper.map(src -> src.getEvent() != null ? src.getEvent().getId() : null,
                           EventApplicationDTO::setEventId);
                mapper.map(src -> src.getStatus() != null ? src.getStatus().name() : null,
                           EventApplicationDTO::setApplicationStatus);
            })
            .setPostConverter(toDTOConverter());

        modelMapper.createTypeMap(EventApplicationDTO.class, EventApplication.class)
            .setPostConverter(toEntityConverter());
    }

    @Override
    protected void mapSpecificFields(EventApplication source, EventApplicationDTO destination) {
        if (source.getUser() != null) {
            destination.setUserId(source.getUser().getId());
        }
        
        if (source.getEvent() != null) {
            destination.setEventId(source.getEvent().getId());
        }
        
        if (source.getStatus() != null) {
            destination.setApplicationStatus(source.getStatus().name());
        }
    }

    @Override
    protected void mapSpecificFields(EventApplicationDTO source, EventApplication destination) {
        // Эти поля будут установлены в сервисе на основе userId и eventId
    }

    @Override
    protected List<Long> getIds(EventApplication entity) {
        return Collections.emptyList();
    }
}
