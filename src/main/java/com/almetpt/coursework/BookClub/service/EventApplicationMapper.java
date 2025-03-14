package com.almetpt.coursework.BookClub.mapper;

import com.almetpt.coursework.BookClub.dto.EventApplicationDTO;
import com.almetpt.coursework.BookClub.model.EventApplication;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventApplicationMapper extends GenericMapper<EventApplication, EventApplicationDTO> {

    protected EventApplicationMapper(ModelMapper modelMapper) {
        super(EventApplication.class, EventApplicationDTO.class, modelMapper);
    }

    @Override
    protected void setupMapper() {
        modelMapper.createTypeMap(EventApplication.class, EventApplicationDTO.class)
                .setPostConverter(toDTOConverter());
        modelMapper.createTypeMap(EventApplicationDTO.class, EventApplication.class)
                .setPostConverter(toEntityConverter());
    }

    @Override
    protected void mapSpecificFields(EventApplicationDTO source, EventApplication destination) {
        // Дополнительный маппинг для полей EventApplication, если необходимо
    }

    @Override
    protected void mapSpecificFields(EventApplication source, EventApplicationDTO destination) {
        // Дополнительный маппинг для полей EventApplicationDTO, если необходимо
    }

    @Override
    protected List<Long> getIds(EventApplication entity) {
        return null;
    }
}
