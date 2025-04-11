package com.almetpt.coursework.bookclub.mapper;

import com.almetpt.coursework.bookclub.dto.UserDTO;

import com.almetpt.coursework.bookclub.model.User;

import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UserMapper extends GenericMapper<User, UserDTO> {

    public UserMapper(ModelMapper modelMapper) {
        super(User.class, UserDTO.class, modelMapper);
    }

    @PostConstruct
    @Override
    protected void setupMapper() {
        modelMapper.createTypeMap(User.class, UserDTO.class)
                .setPostConverter(toDTOConverter());
        modelMapper.createTypeMap(UserDTO.class, User.class)
                .setPostConverter(toEntityConverter());
    }

    @Override
    protected List<Long> getIds(User entity) {
        return Collections.emptyList();
    }
}
