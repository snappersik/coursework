package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.dto.BookImageDTO;
import com.almetpt.coursework.bookclub.dto.BookImageUploadDTO;
import com.almetpt.coursework.bookclub.model.Book;
import com.almetpt.coursework.bookclub.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookImageService {

    private final BookRepository bookRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public BookImageDTO uploadImage(Long bookId, MultipartFile file) throws IOException {
        log.debug("Загрузка изображения для книги с ID: {}", bookId);
        Book book = getBookById(bookId);

        // Если у книги уже было загруженное изображение, удаляем его
        if (book.getCoverImageFilename() != null) {
            fileStorageService.deleteFile(book.getCoverImageFilename());
        }

        // Сохраняем новое изображение
        String filename = fileStorageService.saveFile(file);
        book.setCoverImageFilename(filename);
        // Сохраняем оригинальное имя файла
        book.setOriginalCoverImageFilename(file.getOriginalFilename());
        // Если ранее была установлена URL-ссылка, удаляем её
        book.setCoverImageUrl(null);
        book.setCoverImageData(null);
        bookRepository.save(book);

        return new BookImageDTO(
                book.getId(),
                book.getCoverImageUrl(),
                book.getCoverImageFilename(),
                book.hasLocalImage(),
                book.getOriginalCoverImageFilename());
    }

    @Transactional
    public BookImageDTO uploadImageFromUrl(BookImageUploadDTO dto) {
        log.info("Загрузка изображения по URL для книги с ID: {}", dto.getBookId());
        Book book = getBookById(dto.getBookId());

        // Запускаем асинхронную загрузку изображения
        processExternalImageAsync(dto.getImageUrl(), dto.getBookId());

        // Устанавливаем URL изображения
        book.setCoverImageUrl(dto.getImageUrl());

        // Если ранее было загруженное изображение, удаляем его
        if (book.getCoverImageFilename() != null) {
            fileStorageService.deleteFile(book.getCoverImageFilename());
            book.setCoverImageFilename(null);
        }

        book.setCoverImageData(null);
        // Оригинального имени файла нет, так как загружаем по URL
        book.setOriginalCoverImageFilename(null);
        bookRepository.save(book);

        return new BookImageDTO(
                book.getId(),
                book.getCoverImageUrl(),
                book.getCoverImageFilename(),
                book.hasLocalImage(),
                book.getOriginalCoverImageFilename());
    }

    @Async
    public void processExternalImageAsync(String url, Long bookId) {
        try {
            log.info("Начало асинхронной загрузки изображения с URL: {}", url);
            byte[] imageData = downloadExternalImage(url);

            if (imageData != null && imageData.length > 0) {
                // Сохраняем загруженное изображение
                saveDownloadedImage(bookId, imageData);
                log.info("Изображение успешно загружено и сохранено для книги: {}", bookId);
            } else {
                log.warn("Загруженное изображение пустое или null");
            }
        } catch (Exception e) {
            log.error("Ошибка при асинхронной загрузке изображения: {}", url, e);
        }
    }

    @Transactional
    public void saveDownloadedImage(Long bookId, byte[] imageData) {
        try {
            Book book = getBookById(bookId);
            // Если у книги уже есть локальный файл, не сохраняем
            if (book.getCoverImageFilename() != null) {
                log.debug("У книги уже есть локальный файл, пропускаем сохранение");
                return;
            }

            // Генерируем уникальное имя файла
            String extension = ".jpg"; // Предполагаем, что это JPEG
            String filename = "book_" + bookId + "_" + UUID.randomUUID() + extension;

            // Создаем MultipartFile из байтов
            MockMultipartFile mockFile = new MockMultipartFile(
                    "cover",
                    filename,
                    "image/jpeg",
                    imageData);

            // Сохраняем файл
            String savedFilename = fileStorageService.saveFile(mockFile);

            // Обновляем запись в БД
            book.setCoverImageFilename(savedFilename);
            // Оставляем URL для истории и в случае ошибки загрузки локального файла
            // book.setCoverImageUrl(null);
            bookRepository.save(book);

            log.info("Загруженное изображение сохранено локально: {}", savedFilename);
        } catch (Exception e) {
            log.error("Ошибка при сохранении загруженного изображения для книги с id={}", bookId, e);
        }
    }

    @Transactional
    public BookImageDTO uploadImageData(Long bookId, MultipartFile file) throws IOException {
        Book book = getBookById(bookId);

        // Если у книги уже было загруженное файловое изображение, удаляем его
        if (book.getCoverImageFilename() != null) {
            fileStorageService.deleteFile(book.getCoverImageFilename());
            book.setCoverImageFilename(null);
        }

        // Сохраняем бинарные данные изображения
        book.setCoverImageData(file.getBytes());
        book.setCoverImageUrl(null);
        // Сохраняем оригинальное имя файла
        book.setOriginalCoverImageFilename(file.getOriginalFilename());
        bookRepository.save(book);

        return new BookImageDTO(
                book.getId(),
                book.getCoverImageUrl(),
                book.getCoverImageFilename(),
                book.hasLocalImage(),
                book.getOriginalCoverImageFilename());
    }

    public Resource getBookCoverImage(Long bookId) {
        Book book = getBookById(bookId);
        if (book.getCoverImageFilename() != null) {
            return fileStorageService.loadAsResource(book.getCoverImageFilename());
        }

        return null;
    }

    public byte[] getBookCoverImageData(Long bookId) throws IOException {
        log.debug("Получение данных изображения для книги с ID: {}", bookId);
        Book book = getBookById(bookId);

        if (book.getCoverImageFilename() != null) {
            try {
                // Логируем полный путь к файлу для отладки
                log.info("Пытаемся загрузить файл: {}", book.getCoverImageFilename());
                return fileStorageService.getFileData(book.getCoverImageFilename());
            } catch (IOException e) {
                log.error("Ошибка при чтении файла {}: {}", book.getCoverImageFilename(), e.getMessage());
                throw e;
            }
        }

        return null;
    }

    public byte[] getBookCoverImageDataFromDB(Long bookId) {
        Book book = getBookById(bookId);
        return book.getCoverImageData();
    }

    public BookImageDTO getBookImageInfo(Long bookId) {
        Book book = getBookById(bookId);
        return new BookImageDTO(
                book.getId(),
                book.getCoverImageUrl(),
                book.getCoverImageFilename(),
                book.hasLocalImage(),
                book.getOriginalCoverImageFilename());
    }

    private Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Книга с id " + id + " не найдена"));
    }

    private byte[] downloadExternalImage(String url) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
            byte[] imageData = restTemplate.getForObject(url, byte[].class);

            if (imageData == null || imageData.length == 0) {
                log.warn("Загружено пустое изображение с URL: {}", url);
            } else {
                log.info("Успешно загружено изображение размером {} байт с URL: {}",
                        imageData.length, url);
            }

            return imageData;
        } catch (Exception e) {
            log.error("Ошибка при загрузке изображения с URL: {}", url, e);
            throw new RuntimeException("Не удалось загрузить изображение с URL: " + url, e);
        }
    }
}
