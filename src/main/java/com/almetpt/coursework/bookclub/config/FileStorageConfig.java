package com.almetpt.coursework.bookclub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class FileStorageConfig {
    
    @Value("${file.upload.directory:${user.home}/bookclub/uploads}")
    private String uploadDirectory;
    
    @Bean
    public FileSystemResource fileStorage() {
        File file = new File(uploadDirectory);
        if (!file.exists()) {
            file.mkdirs();
        }
        return new FileSystemResource(file);
    }
    
    // Добавьте этот метод для настройки доступа к статическим ресурсам
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @SuppressWarnings("null")
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/uploads/**")
                        .addResourceLocations("file:" + uploadDirectory + "/");
            }
        };
    }
}
