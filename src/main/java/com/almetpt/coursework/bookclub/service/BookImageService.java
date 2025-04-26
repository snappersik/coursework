package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.dto.BookImageDTO;
import com.almetpt.coursework.bookclub.dto.BookImageUploadDTO;
import com.almetpt.coursework.bookclub.model.Book;
import com.almetpt.coursework.bookclub.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;

import java.io.IOException;

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
                book.getOriginalCoverImageFilename()
        );
    }

    @Transactional
    public BookImageDTO uploadImageFromUrl(BookImageUploadDTO dto) {
        Book book = getBookById(dto.getBookId());

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
                book.getOriginalCoverImageFilename()
        );
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
                book.getOriginalCoverImageFilename()
        );
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
            return fileStorageService.getFileData(book.getCoverImageFilename());
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
                book.getOriginalCoverImageFilename()
        );
    }

    private Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Книга с id " + id + " не найдена"));
    }   

}
