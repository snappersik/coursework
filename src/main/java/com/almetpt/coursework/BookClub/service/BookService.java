package com.almetpt.coursework.BookClub.service;

import com.almetpt.coursework.BookClub.dto.BookDTO;
import com.almetpt.coursework.BookClub.mapper.BookMapper;
import com.almetpt.coursework.BookClub.model.Book;
import com.almetpt.coursework.BookClub.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BookService extends GenericService<Book, BookDTO> {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public BookService(BookRepository bookRepository, BookMapper bookMapper) {
        super(bookRepository, bookMapper);
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    public Page<BookDTO> searchBooks(String title, String author, String genre, Pageable pageable){
        Page<Book> books = bookRepository.findAllByTitleAndAuthorAndGenre(title, author, genre, pageable);
        return books.map(bookMapper::toDTO);
    }
}

