package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.constants.MailConstants;
import com.almetpt.coursework.bookclub.dto.RegisterDTO;
import com.almetpt.coursework.bookclub.dto.RoleDTO;
import com.almetpt.coursework.bookclub.dto.UserDTO;
import com.almetpt.coursework.bookclub.mapper.GenericMapper;
import com.almetpt.coursework.bookclub.model.Role;
import com.almetpt.coursework.bookclub.model.User;
import com.almetpt.coursework.bookclub.repository.GenericRepository;
import com.almetpt.coursework.bookclub.repository.UserRepository;
import com.almetpt.coursework.bookclub.utils.MailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class UserService extends GenericService<User, UserDTO> {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JavaMailSender javaMailSender;
    private final CartService cartService;
    private final UserRepository userRepository;

    @Value("${file.upload.directory:${user.home}/bookclub/uploads}")
    private String uploadDirectoryRoot;

    @Value(MailConstants.FRONTEND_BASE_URL_PROPERTY)
    private String frontendBaseUrl;

    private static final long TOKEN_VALIDITY_HOURS = 24;

    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message) {
            super(message);
        }
    }

    public static class TokenExpiredException extends RuntimeException {
        public TokenExpiredException(String message) {
            super(message);
        }
    }

    public UserService(GenericRepository<User> repository, // This is for GenericService (likely UserRepository
                                                           // instance)
            GenericMapper<User, UserDTO> mapper, // This is for GenericService
            BCryptPasswordEncoder bCryptPasswordEncoder,
            JavaMailSender javaMailSender,
            CartService cartService,
            UserRepository userRepository) { // Specific UserRepository for user-related queries
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
        user = userRepository.save(user); // Use userRepository

        cartService.createCartForUser(user);

        return mapper.toDTO(user);
    }

    @Transactional
    public UserDTO registerUser(UserDTO newObject, String password) {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(1L); // Assuming Role ID 1L is 'USER'
        newObject.setRole(roleDTO);
        newObject.setCreatedBy("REGISTRATION FORM");
        newObject.setCreatedWhen(LocalDateTime.now());
        User user = mapper.toEntity(newObject);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user = userRepository.save(user); // Use userRepository

        cartService.createCartForUser(user);

        return mapper.toDTO(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(final String email) {
        return userRepository.findByEmail(email)
                .map(mapper::toDTO)
                .orElse(null);
    }

    public boolean checkPassword(String password, UserDetails foundUser) {
        return bCryptPasswordEncoder.matches(password, foundUser.getPassword());
    }

    @Transactional
    public void initiatePasswordReset(String email) {
        User user = userRepository.findUserByEmailAndIsDeletedFalse(email);
        if (user == null) {
            log.warn("Запрос на сброс пароля для несуществующего или удаленного email: {}", email);
            return;
        }

        String token = UUID.randomUUID().toString();
        user.setChangePasswordToken(token);
        user.setUpdatedWhen(LocalDateTime.now());
        user.setUpdatedBy("SYSTEM_PASSWORD_RESET_INIT");
        userRepository.save(user);

        String emailBody = String.format(
                MailConstants.MAIL_MESSAGE_FOR_PASSWORD_RESET_LINK_TEMPLATE,
                frontendBaseUrl,
                token);

        SimpleMailMessage mailMessage = MailUtils.createMailMessage(
                user.getEmail(),
                MailConstants.MAIL_SUBJECT_FOR_PASSWORD_RESET,
                emailBody);
        javaMailSender.send(mailMessage);
        log.info("Письмо для сброса пароля отправлено на email: {} с токеном.", email);
    }

    @Override
    public UserDTO update(UserDTO dto) {
        // Получаем существующего пользователя из БД
        User existingUser = userRepository.findById(dto.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + dto.getId() + " не найден"));

        // Обновляем только нужные поля
        existingUser.setEmail(dto.getEmail());
        existingUser.setFirstName(dto.getFirstName());
        existingUser.setLastName(dto.getLastName());
        existingUser.setPatronymic(dto.getPatronymic());
        existingUser.setPhone(dto.getPhone());
        existingUser.setAddress(dto.getAddress());
        existingUser.setBirthDate(dto.getBirthDate());

        // Обновляем роль, если она указана
        if (dto.getRole() != null && dto.getRole().getId() != null) {
            Role role = new Role();
            role.setId(dto.getRole().getId());
            existingUser.setRole(role);
        }

        // Обновляем аудит-поля
        setAuditFields(existingUser, false);

        // Сохраняем и возвращаем обновленного пользователя
        return mapper.toDTO(userRepository.save(existingUser));
    }

    @Transactional(readOnly = true)
    public boolean isPasswordResetTokenValid(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        User user = userRepository.findUserByChangePasswordToken(token);
        if (user == null) {
            log.warn("Попытка валидации несуществующего токена: {}", token);
            return false;
        }

        LocalDateTime tokenCreationTime = user.getUpdatedWhen();
        if (tokenCreationTime == null) {
            log.error("У пользователя {} с токеном {} отсутствует время обновления (tokenCreationTime is null).",
                    user.getEmail(), token);
            return false;
        }

        boolean isExpired = tokenCreationTime.isBefore(LocalDateTime.now().minusHours(TOKEN_VALIDITY_HOURS));
        if (isExpired) {
            log.warn("Токен {} для пользователя {} истек (проверка валидности).", token, user.getEmail());
            return false;
        }
        return true;
    }

    @Transactional
    public void finalizePasswordReset(String token, String newPassword) {
        if (token == null || token.trim().isEmpty()) {
            throw new InvalidTokenException("Токен сброса пароля не может быть пустым.");
        }
        User user = userRepository.findUserByChangePasswordToken(token);

        if (user == null) {
            log.warn("Попытка сброса пароля с невалидным токеном: {}", token);
            throw new InvalidTokenException("Невалидный или уже использованный токен сброса пароля.");
        }

        LocalDateTime tokenCreationTime = user.getUpdatedWhen();
        if (tokenCreationTime == null
                || tokenCreationTime.isBefore(LocalDateTime.now().minusHours(TOKEN_VALIDITY_HOURS))) {
            user.setChangePasswordToken(null);
            user.setUpdatedWhen(LocalDateTime.now());
            user.setUpdatedBy("SYSTEM_PASSWORD_RESET_EXPIRED_TOKEN_CLEAR");
            userRepository.save(user);
            log.warn("Токен {} для пользователя {} истек при попытке смены пароля.", token, user.getEmail());
            throw new TokenExpiredException("Срок действия токена для сброса пароля истек.");
        }

        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        user.setChangePasswordToken(null);
        user.setUpdatedWhen(LocalDateTime.now());
        user.setUpdatedBy("SYSTEM_PASSWORD_RESET_SUCCESS");
        userRepository.save(user);
        log.info("Пароль для пользователя {} успешно сброшен.", user.getEmail());
    }

    @Transactional(readOnly = true)
    public List<String> getUserEmailsWithDelayedRentDate() {
        return userRepository.getDelayedEmails();
    }

    @Transactional(readOnly = true)
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
            throw new NotFoundException("Текущий пользователь не найден для email: " + email);
        }
        return mapper.toDTO(currentUser);
    }

    @Transactional
    public UserDTO updateProfile(UserDTO userDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findUserByEmailAndIsDeletedFalse(email);
        if (currentUser == null) {
            throw new NotFoundException("Текущий пользователь не найден для email: " + email);
        }

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!currentUser.getId().equals(userDTO.getId()) && !isAdmin) {
            throw new AccessDeniedException("Вы можете обновлять только свой профиль");
        }

        currentUser.setFirstName(userDTO.getFirstName());
        currentUser.setLastName(userDTO.getLastName());
        currentUser.setPatronymic(userDTO.getPatronymic());
        currentUser.setPhone(userDTO.getPhone());
        currentUser.setAddress(userDTO.getAddress());
        // currentUser.setAvatarUrl(userDTO.getAvatarUrl()); // Обновление URL аватара,
        // если он передается напрямую

        if (isAdmin && userDTO.getRole() != null && userDTO.getRole().getId() != null) {
            // Здесь должна быть логика для получения сущности Role по ID,
            // прежде чем устанавливать ее пользователю.
            // Например,
            // `roleRepository.findById(userDTO.getRole().getId()).orElseThrow(...)`
            // Для простоты, если RoleDTO содержит имя, можно искать по имени.
            // Пока что создадим плейсхолдер, предполагая, что ID достаточно для маппера,
            // но это не всегда так без кастомной логики маппинга или загрузки сущности.
            Role role = new Role(); // Placeholder, requires fetching actual Role entity
            role.setId(userDTO.getRole().getId());
            // role.setName(userDTO.getRole().getName()); // Если имя тоже есть в DTO
            currentUser.setRole(role);
        }
        setAuditFields(currentUser, false); // Обновляем updatedBy/updatedWhen
        return mapper.toDTO(userRepository.save(currentUser));
    }

    @Transactional
    public void changePasswordForAuthenticatedUser(String email, String oldPassword, String newPassword) {
        User user = userRepository.findUserByEmailAndIsDeletedFalse(email);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        if (!bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("Неверный старый пароль");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Новый пароль должен содержать не менее 6 символов.");
        }
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        user.setUpdatedWhen(LocalDateTime.now());
        user.setUpdatedBy(email); // Аутентифицированный пользователь сам себя обновляет
        userRepository.save(user);
        log.info("Пароль для пользователя {} успешно изменен.", email);
    }

    @Transactional
    public UserDTO uploadUserAvatar(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл не может быть пустым");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Можно загружать только изображения (например, PNG, JPG, WEBP)");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findUserByEmailAndIsDeletedFalse(email);
        if (currentUser == null) {
            throw new NotFoundException("Текущий пользователь не найден для email: " + email);
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null) {
                fileExtension = StringUtils.getFilenameExtension(originalFilename);
            }
            String newFileName = UUID.randomUUID().toString()
                    + (fileExtension != null && !fileExtension.isEmpty() ? "." + fileExtension : "");

            Path avatarUploadPath = Paths.get(this.uploadDirectoryRoot, "avatars");

            if (!Files.exists(avatarUploadPath)) {
                Files.createDirectories(avatarUploadPath);
                log.info("Создана директория для аватаров: {}", avatarUploadPath.toAbsolutePath());
            }

            // Удаляем старый аватар, если он существует и не является URL по
            // умолчанию/внешним
            if (currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isEmpty()
                    && currentUser.getAvatarUrl().startsWith("/uploads/avatars/")) {
                try {
                    String relativePathFromUploadsDir = currentUser.getAvatarUrl().replaceFirst("^/uploads/", "");
                    Path oldFilePath = Paths.get(this.uploadDirectoryRoot, relativePathFromUploadsDir);

                    if (Files.exists(oldFilePath)) {
                        Files.delete(oldFilePath);
                        log.info("Старый аватар удален: {}", oldFilePath.toString());
                    }
                } catch (Exception e) {
                    log.warn("Не удалось удалить старый аватар ({}). Ошибка: {}. Путь к файлу: {}",
                            currentUser.getAvatarUrl(), e.getMessage(),
                            Paths.get(this.uploadDirectoryRoot,
                                    currentUser.getAvatarUrl().replaceFirst("^/uploads/", "")));
                }
            }

            Path newFilePath = avatarUploadPath.resolve(newFileName);
            Files.copy(file.getInputStream(), newFilePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Новый аватар успешно сохранен: {}", newFilePath.toAbsolutePath());

            String avatarAccessUrl = "/uploads/avatars/" + newFileName;
            currentUser.setAvatarUrl(avatarAccessUrl);
            setAuditFields(currentUser, false); // Обновляем updatedBy/updatedWhen
            userRepository.save(currentUser);

            return mapper.toDTO(currentUser);
        } catch (IOException e) {
            Path targetPathForLog = Paths.get(this.uploadDirectoryRoot, "avatars", "ПРЕДПОЛАГАЕМЫЙ_ФАЙЛ");
            log.error(
                    "Ошибка IOException при сохранении файла аватара: {}. Целевой путь мог быть в районе: {}. Проверьте права доступа и существование базовой директории '{}'",
                    e.getMessage(), targetPathForLog.toAbsolutePath(), this.uploadDirectoryRoot, e);
            throw new RuntimeException("Ошибка при сохранении аватара: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при загрузке аватара для пользователя {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Непредвиденная ошибка при загрузке аватара: " + e.getMessage(), e);
        }
    }

    protected NotFoundException createNotFoundException(Long id) {
        return new NotFoundException("Пользователь с ID " + id + " не найден.");
    }
}
