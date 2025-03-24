package com.almetpt.coursework.bookclub.mapper;

import com.almetpt.coursework.bookclub.dto.UserDTO;
import com.almetpt.coursework.bookclub.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper extends GenericMapper<User, UserDTO> {

    protected UserMapper(ModelMapper modelMapper) {
        super(User.class, UserDTO.class, modelMapper);
    }

    @Override
    protected void setupMapper() {
        modelMapper.createTypeMap(User.class, UserDTO.class)
                .setPostConverter(toDTOConverter());
        modelMapper.createTypeMap(UserDTO.class, User.class)
                .setPostConverter(toEntityConverter());
    }

    @Override
    protected void mapSpecificFields(UserDTO source, User destination) {
        // Дополнительный маппинг для полей User, если необходимо
    }

    @Override
    protected void mapSpecificFields(User source, UserDTO destination) {
        // Дополнительный маппинг для полей UserDTO, если необходимо
    }

    @Override
    protected List<Long> getIds(User entity) {
        return null;
    }
}
