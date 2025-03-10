package com.almetpt.coursework.library.mapper;


import com.aptproject.springlibraryproject.library.dto.UserDTO;
import com.aptproject.springlibraryproject.library.model.GenericModel;
import com.aptproject.springlibraryproject.library.model.User;
import com.aptproject.springlibraryproject.library.repository.BookRentInfoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class UserMapper
        extends GenericMapper<User, UserDTO> {

    private final BookRentInfoRepository bookRentInfoRepository;

    protected UserMapper(ModelMapper modelMapper,
                         BookRentInfoRepository bookRentInfoRepository) {
        super(User.class, UserDTO.class, modelMapper);
        this.bookRentInfoRepository = bookRentInfoRepository;
    }

    @Override
    protected void setupMapper() {
        modelMapper.createTypeMap(User.class, UserDTO.class)
                .addMappings(m -> m.skip(UserDTO::setUserBooksRent)).setPostConverter(toDTOConverter());
        modelMapper.createTypeMap(UserDTO.class, User.class)
                .addMappings(m -> m.skip(User::setBookRentInfos)).setPostConverter(toEntityConverter());
    }

    @Override
    protected void mapSpecificFields(UserDTO source, User destination) {
        if (!Objects.isNull(source.getUserBooksRent())) {
            destination.setBookRentInfos(bookRentInfoRepository.findAllById(source.getUserBooksRent()));
        }
        else {
            destination.setBookRentInfos(Collections.emptyList());
        }
    }

    @Override
    protected void mapSpecificFields(User source, UserDTO destination) {
        destination.setUserBooksRent(getIds(source));
    }

    @Override
    protected List<Long> getIds(User entity) {
        return Objects.isNull(entity) || Objects.isNull(entity.getBookRentInfos())
                ? null
                : entity.getBookRentInfos().stream()
                .map(GenericModel::getId)
                .collect(Collectors.toList());
    }
}
