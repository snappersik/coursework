package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.dto.SliderBookDTO;
import com.almetpt.coursework.bookclub.dto.BookDTO;
import com.almetpt.coursework.bookclub.mapper.SliderBookMapper;
import com.almetpt.coursework.bookclub.model.SliderBook;
import com.almetpt.coursework.bookclub.repository.SliderBookRepository;
import com.almetpt.coursework.bookclub.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webjars.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SliderBookService extends GenericService<SliderBook, SliderBookDTO> {

    private final SliderBookRepository sliderBookRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;

    public SliderBookService(SliderBookRepository sliderBookRepository, 
                            SliderBookMapper sliderBookMapper,
                            BookRepository bookRepository,
                            BookService bookService) {
        super(sliderBookRepository, sliderBookMapper);
        this.sliderBookRepository = sliderBookRepository;
        this.bookRepository = bookRepository;
        this.bookService = bookService;
    }

    // Получить все книги для слайдера
    public List<SliderBookDTO> getAllSliderBooks() {
        return mapper.toDTOs(sliderBookRepository.findAllByOrderByPositionAsc());
    }

    // Создание новой книги для слайдера
    @Transactional
    @Override
    public SliderBookDTO create(SliderBookDTO dto) {
        // Проверяем существование книги
        if (!bookRepository.existsById(dto.getBookId())) {
            throw new NotFoundException("Book not found with id: " + dto.getBookId());
        }
        
        // Проверяем, не добавлена ли книга уже в слайдер
        if (sliderBookRepository.existsByBookId(dto.getBookId())) {
            throw new IllegalArgumentException("Book already exists in slider");
        }
        
        // Если позиция не указана, устанавливаем последнюю
        if (dto.getPosition() == null) {
            Integer maxPosition = sliderBookRepository.findMaxPosition();
            dto.setPosition(maxPosition != null ? maxPosition + 1 : 1);
        }
        
        return super.create(dto);
    }

    // Получить книги для слайдера или подходящую замену
    public List<BookDTO> getSliderBooksOrAlternative() {
        // Сначала пробуем получить книги из слайдера
        List<SliderBook> sliderBooks = sliderBookRepository.findAllByOrderByPositionAsc();
        
        if (!sliderBooks.isEmpty()) {
            // Преобразуем SliderBook в BookDTO с кастомными описаниями
            return sliderBooks.stream()
                    .map(sliderBook -> {
                        BookDTO bookDTO = bookService.getOne(sliderBook.getBook().getId());
                        // Заменяем стандартное описание на кастомное если оно есть
                        if (sliderBook.getCustomDescription() != null && !sliderBook.getCustomDescription().isBlank()) {
                            bookDTO.setDescription(sliderBook.getCustomDescription());
                        }
                        return bookDTO;
                    })
                    .collect(Collectors.toList());
        }
        
        // Если в слайдере нет книг, получаем топ-3 с reading=true
        List<BookDTO> readingBooks = bookService.getTopReadingBooks(3);
        if (!readingBooks.isEmpty()) {
            return readingBooks;
        }
        
        // Если и таких нет, возвращаем последние добавленные книги
        return bookService.getLatestBooks(3);
    }
}
