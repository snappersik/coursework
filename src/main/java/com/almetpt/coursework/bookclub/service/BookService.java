package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.constants.Errors;
import com.almetpt.coursework.bookclub.dto.BookDTO;
import com.almetpt.coursework.bookclub.exception.MyDeleteException;
import com.almetpt.coursework.bookclub.mapper.BookMapper;
import com.almetpt.coursework.bookclub.model.Book;
import com.almetpt.coursework.bookclub.repository.BookRepository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;

@Service
public class BookService extends GenericService<Book, BookDTO> {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public BookService(BookRepository bookRepository, BookMapper bookMapper) {
        super(bookRepository, bookMapper);
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    public Page<BookDTO> searchBooks(String title, String author, String genre, Pageable pageable) {
        Page<Book> books = bookRepository.findAllByTitleAndAuthorAndGenre(title, author, genre, pageable);
        return books.map(bookMapper::toDTO);
    }

    @Override
    public void deleteSoft(final Long id) throws MyDeleteException {
    Book book = repository.findById(id).orElseThrow(
            () -> new NotFoundException("Книга не найдена"));
    boolean bookCanBeDeleted = ((BookRepository) repository).isBookCanBeDeleted(id);
    if (bookCanBeDeleted) {
        markAsDeleted(book);
        repository.save(book);
    } else {
        throw new MyDeleteException(Errors.Books.BOOK_DELETED_ERROR);
    }
    }
    
    protected NotFoundException createNotFoundException(Long id) {
        return new NotFoundException(Errors.Books.BOOK_NOT_FOUND_ERROR.formatted(id));
    }    

    private void markAsDeleted(Book book) {
        book.setDeleted(true);
        book.setDeletedWhen(LocalDateTime.now());
    }

}
