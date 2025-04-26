package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.constants.Errors;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webjars.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class UserService extends GenericService<User, UserDTO> {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JavaMailSender javaMailSender;
    private final CartService cartService;
    private final UserRepository userRepository; // Добавлено поле для UserRepository

    public UserService(GenericRepository<User> repository,
                       GenericMapper<User, UserDTO> mapper,
                       BCryptPasswordEncoder bCryptPasswordEncoder,
                       JavaMailSender javaMailSender,
                       CartService cartService,
                       UserRepository userRepository) { // Внедряем UserRepository через конструктор
        super(repository, mapper);
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.javaMailSender = javaMailSender;
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    @Transactional
    public UserDTO createUser(RegisterDTO registerDTO) {
        UserDTO newObject = registerDTO.getUserData();
        if (newObject.getRole() == null || newObject.getRole().getId() == null) {
            throw new IllegalArgumentException("Роль обязательна для заполнения");
        }

        newObject.setCreatedBy("ADMIN");
        newObject.setCreatedWhen(LocalDateTime.now());
        User user = mapper.toEntity(newObject);
        user.setPassword(bCryptPasswordEncoder.encode(registerDTO.getPassword()));
        user = repository.save(user);

        // Создаем корзину для пользователя
        cartService.createCartForUser(user);

        return mapper.toDTO(user);
    }

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

    public UserDTO getUserByEmail(final String email) {
        return userRepository.findByEmail(email)
                .map(mapper::toDTO)
                .orElse(null); // Возвращаем null, если пользователь не найден
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
                MailConstants.MAIL_MESSAGE_FOR_REMEMBER_PASSWORD + uuid);
        javaMailSender.send(mailMessage);
    }

    public void changePassword(String uuid, String password) {
        User user = userRepository.findUserByChangePasswordToken(uuid);
        if (user != null) {
            user.setChangePasswordToken(null);
            user.setPassword(bCryptPasswordEncoder.encode(password));
            repository.save(user);
        }
    }

    public List<String> getUserEmailsWithDelayedRentDate() {
        return userRepository.getDelayedEmails();
    }

    public Page<UserDTO> findUsers(UserDTO userDTO, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(
                userDTO.getFirstName(),
                userDTO.getLastName(),
                userDTO.getEmail(),
                pageable);
        List<UserDTO> result = mapper.toDTOs(users.getContent());
        return new PageImpl<>(result, pageable, users.getTotalElements());
    }

    @Transactional(readOnly = true)
    public UserDTO getCurrentUserProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findUserByEmailAndIsDeletedFalse(email);
        if (currentUser == null) {
            throw new NotFoundException("Текущий пользователь не найден");
        }
        return mapper.toDTO(currentUser);
    }

    @Transactional
    public UserDTO updateProfile(UserDTO userDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findUserByEmailAndIsDeletedFalse(email);
        if (currentUser == null) {
            throw new NotFoundException("Текущий пользователь не найден");
        }

        // Проверяем, что пользователь обновляет свой профиль или является администратором
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!currentUser.getId().equals(userDTO.getId()) && !isAdmin) {
            throw new AccessDeniedException("Вы можете обновлять только свой профиль");
        }

        // Обновляем только разрешенные поля
        User userToUpdate = mapper.toEntity(userDTO);
        currentUser.setFirstName(userToUpdate.getFirstName());
        currentUser.setLastName(userToUpdate.getLastName());
        currentUser.setPhone(userToUpdate.getPhone());
        currentUser.setAddress(userToUpdate.getAddress());

        // Только администратор может менять роль
        if (isAdmin && userToUpdate.getRole() != null) {
            currentUser.setRole(userToUpdate.getRole());
        }

        currentUser.setUpdatedBy(email);
        currentUser.setUpdatedWhen(LocalDateTime.now());

        return mapper.toDTO(repository.save(currentUser));
    }

    protected NotFoundException createNotFoundException(Long id) {
        return new NotFoundException(Errors.Users.USER_NOT_FOUND_ERROR.formatted(id));
    }
}