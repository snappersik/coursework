package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.constants.MailConstants;
import com.almetpt.coursework.bookclub.dto.RegisterDTO;
import com.almetpt.coursework.bookclub.dto.RoleDTO;
import com.almetpt.coursework.bookclub.dto.UserDTO;
import com.almetpt.coursework.bookclub.mapper.GenericMapper;
import com.almetpt.coursework.bookclub.model.User;
import com.almetpt.coursework.bookclub.repository.GenericRepository;
import com.almetpt.coursework.bookclub.repository.UserRepository;
import com.almetpt.coursework.bookclub.utils.MailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class UserService extends GenericService<User, UserDTO> {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JavaMailSender javaMailSender;
    private final CartService cartService;

    public UserService(GenericRepository<User> repository,
                       GenericMapper<User, UserDTO> mapper,
                       BCryptPasswordEncoder bCryptPasswordEncoder,
                       JavaMailSender javaMailSender,
                       CartService cartService) {
        super(repository, mapper);
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.javaMailSender = javaMailSender;
        this.cartService = cartService;
    }

//    @Override
//    @Transactional
//    public UserDTO create(RegisterDTO registerDTO) {
//        UserDTO newObject = registerDTO.getUserData();
//        if (newObject.getRole() == null || newObject.getRole().getId() == null) {
//            throw new IllegalArgumentException("Роль обязательна для заполнения");
//        }
//        newObject.setCreatedBy("ADMIN");
//        newObject.setCreatedWhen(LocalDateTime.now());
//        User user = mapper.toEntity(newObject);
//        if (registerDTO.getPassword() != null) {
//            user.setPassword(bCryptPasswordEncoder.encode(registerDTO.getPassword()));
//        } else {
//            throw new IllegalArgumentException("Пароль обязателен для заполнения");
//        }
//        user = repository.save(user);
//        return mapper.toDTO(user);
//    }

    @Transactional
    public UserDTO registerUser(UserDTO newObject, String password) {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(1L);
        newObject.setRole(roleDTO);
        newObject.setCreatedBy("REGISTRATION FORM");
        newObject.setCreatedWhen(LocalDateTime.now());
        User user = mapper.toEntity(newObject);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user = repository.save(user);

        cartService.createCartForUser(user);

        return mapper.toDTO(user);
    }


    public UserDTO createOrganizer(UserDTO newObject, String password) {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(2L);
        newObject.setRole(roleDTO);
        newObject.setCreatedBy("ORGANIZER CREATION FORM");
        User user = mapper.toEntity(newObject);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        return mapper.toDTO(repository.save(user));
    }

    public UserDTO getUserByEmail(final String email) {
        return mapper.toDTO(((UserRepository) repository).findUserByEmail(email));
    }

    public boolean checkPassword(String password, UserDetails foundUser) {
        return bCryptPasswordEncoder.matches(password, foundUser.getPassword());
    }

    public void sendChangePasswordEmail(final UserDTO userDTO) {
        UUID uuid = UUID.randomUUID();
        log.info("Generated Token: {}", uuid);
        userDTO.setChangePasswordToken(uuid.toString());
        update(userDTO);
        SimpleMailMessage mailMessage = MailUtils.createMailMessage(
                userDTO.getEmail(),
                MailConstants.MAIL_SUBJECT_FOR_REMEMBER_PASSWORD,
                MailConstants.MAIL_MESSAGE_FOR_REMEMBER_PASSWORD + uuid
        );
        javaMailSender.send(mailMessage);
    }

    public void changePassword(String uuid, String password) {
        User user = ((UserRepository) repository).findUserByChangePasswordToken(uuid);
        user.setChangePasswordToken(null);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        repository.save(user);
    }

    public List<String> getUserEmailsWithDelayedRentDate() {
        return ((UserRepository) repository).getDelayedEmails();
    }

    public Page<UserDTO> findUsers(UserDTO userDTO, Pageable pageable) {
        Page<User> users = ((UserRepository) repository).searchUsers(
                userDTO.getFirstName(),
                userDTO.getLastName(),
                userDTO.getEmail(),
                pageable
        );
        List<UserDTO> result = mapper.toDTOs(users.getContent());
        return new PageImpl<>(result, pageable, users.getTotalElements());
    }
}
