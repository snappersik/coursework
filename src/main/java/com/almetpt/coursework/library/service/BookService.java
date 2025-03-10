package com.almetpt.coursework.library.service;

import com.aptproject.springlibraryproject.library.constants.Errors;
import com.aptproject.springlibraryproject.library.dto.BookDTO;
import com.aptproject.springlibraryproject.library.dto.BookSearchDTO;
import com.aptproject.springlibraryproject.library.exception.MyDeleteException;
import com.aptproject.springlibraryproject.library.mapper.BookMapper;
import com.aptproject.springlibraryproject.library.model.Author;
import com.aptproject.springlibraryproject.library.model.Book;
import com.aptproject.springlibraryproject.library.repository.AuthorRepository;
import com.aptproject.springlibraryproject.library.repository.BookRepository;
import com.aptproject.springlibraryproject.utils.FileHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookService
        extends GenericService<Book, BookDTO> {
    private final AuthorRepository authorRepository;

    protected BookService(BookRepository repository,
                          BookMapper mapper,
                          AuthorRepository authorRepository) {
        super(repository, mapper);
        this.authorRepository = authorRepository;
    }

    public BookDTO create(final BookDTO newBook, MultipartFile file) {
        String fileName = FileHelper.createFile(file);
        newBook.setOnlineCopyPath(fileName);
        newBook.setCreatedWhen(LocalDateTime.now());
        newBook.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        return mapper.toDTO(repository.save(mapper.toEntity(newBook)));
    }

    public Page<BookDTO> getAllBooks(Pageable pageable) {
        Page<Book> booksPaginated = repository.findAll(pageable);
        List<BookDTO> result = mapper.toDTOs(booksPaginated.getContent());
        return new PageImpl<>(result, pageable, booksPaginated.getTotalElements());
    }

    public Page<BookDTO> searchBook(BookSearchDTO bookSearchDTO,
                                    Pageable pageRequest) {

        String genre = bookSearchDTO.getGenre() != null
                ? String.valueOf(bookSearchDTO.getGenre().ordinal())
                : null;

        Page<Book> booksPaginated = ((BookRepository) repository).searchBooks(
                bookSearchDTO.getBookTitle(),
                genre,
                bookSearchDTO.getAuthorName(),
                pageRequest
        );

        List<BookDTO> result = mapper.toDTOs(booksPaginated.getContent());
        return new PageImpl<>(result, pageRequest, booksPaginated.getTotalElements());

    }

    public BookDTO addAuthor(final Long bookId, final Long authorId) {
        BookDTO book = getOne(bookId);
        Author author = authorRepository.findById(authorId).orElseThrow(
                () -> new NotFoundException("автор не найден"));
        book.getAuthorIds().add(author.getId());
        update(book);
        return book;
    }

    @Override
    public void deleteSoft(final Long id) throws MyDeleteException {
        Book book = repository.findById(id).orElseThrow(
                () -> new NotFoundException("книги не найдено"));
        boolean bookCanBeDeleted = ((BookRepository) repository).isBookCanBeDeleted(id);
        if (bookCanBeDeleted) {
            markAsDeleted(book);
            repository.save(book);
        } else {
            throw new MyDeleteException(Errors.Books.BOOK_DELETED_ERROR);
        }
    }

}

