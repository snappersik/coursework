package com.almetpt.coursework.bookclub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    @Value("${file.upload.directory:${user.home}/bookclub/uploads}")
    private String uploadDirectory;

    public String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Не удалось сохранить пустой файл");
        }
        
        // Генерируем уникальное имя файла, сохраняя расширение
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + extension;
        
        // Сохраняем файл
        Path destinationFile = Paths.get(uploadDirectory).resolve(filename);
        
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Файл сохранен: {}", filename);
            return filename;
        }
    }
    
    public Resource loadAsResource(String filename) {
        try {
            Path file = Paths.get(uploadDirectory).resolve(filename);
            Resource resource = new FileSystemResource(file);
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new IOException("Не удалось прочитать файл: " + filename);
            }
        } catch (IOException e) {
            log.error("Ошибка загрузки файла: {}", e.getMessage());
            return null;
        }
    }
    
    public void deleteFile(String filename) {
        try {
            Path file = Paths.get(uploadDirectory).resolve(filename);
            Files.deleteIfExists(file);
            log.info("Файл удален: {}", filename);
        } catch (IOException e) {
            log.error("Ошибка удаления файла: {}", e.getMessage());
        }
    }
}