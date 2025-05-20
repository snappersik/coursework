package com.almetpt.coursework.bookclub.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import static com.almetpt.coursework.bookclub.constants.FileDirectoriesConstants.BOOKS_UPLOAD_DIRECTORY;

@Slf4j
public class FileHelper {

    private FileHelper() {
    }

    public static String createFile(final MultipartFile file) {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String uniqueFileName = System.currentTimeMillis() + "_" + fileName; // Добавляем уникальный префикс

        try {
            // Используем uploadDirectory из конфигурации
            Path uploadDir = Path.of(BOOKS_UPLOAD_DIRECTORY).toAbsolutePath().normalize();
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path targetPath = uploadDir.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Возвращаем относительный путь для сохранения в БД
            return BOOKS_UPLOAD_DIRECTORY + "/" + uniqueFileName;
        } catch (IOException e) {
            log.error("FileHelper#createFile(): {}", e.getMessage());
            return "";
        }
    }

}
