import React, { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import './Slider.css';
import placeholderImage from '../../../assets/img/book_placeholder.webp';
import { API_URL } from '../../../config';

const Slider = ({ backgroundUrl = null }) => {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentIndex, setCurrentIndex] = useState(0);
  const slidesWrapperRef = useRef(null);
  const slidesInfoRef = useRef(null);

  // Функция для получения книг с бэкенда
  useEffect(() => {
    const fetchSliderBooks = async () => {
      try {
        setLoading(true);
        const response = await fetch(`${API_URL}/rest/slider/books`, {
          credentials: 'include', // Для отправки куки с JWT
        });
        if (!response.ok) {
          throw new Error(`Failed to fetch slider books: ${response.status}`);
        }
        const data = await response.json();
        console.log('Loaded books:', data);

        if (data && data.length > 0) {
          setBooks(data);
          setError(null);
        } else {
          setError('No books available for slider');
          setBooks([
            { id: 1, title: "Книга 1", author: "Автор 1", description: "Описание книги 1", imageUrl: placeholderImage },
            { id: 2, title: "Книга 2", author: "Автор 2", description: "Описание книги 2", imageUrl: placeholderImage },
            { id: 3, title: "Книга 3", author: "Автор 3", description: "Описание книги 3", imageUrl: placeholderImage },
          ]);
        }
      } catch (err) {
        console.error('Error fetching slider books:', err);
        setError('Failed to load featured books');
        setBooks([
          { id: 1, title: "Книга 1", author: "Автор 1", description: "Описание книги 1", imageUrl: placeholderImage },
          { id: 2, title: "Книга 2", author: "Автор 2", description: "Описание книги 2", imageUrl: placeholderImage },
          { id: 3, title: "Книга 3", author: "Автор 3", description: "Описание книги 3", imageUrl: placeholderImage },
        ]);
      } finally {
        setLoading(false);
      }
    };

    fetchSliderBooks();
  }, []);

  // Эффект для настройки тилт-эффекта
  useEffect(() => {
    if (!loading && books.length > 0) {
      const slides = document.querySelectorAll('.slide');
      const slidesInfo = document.querySelectorAll('.slide-info');

      slides.forEach((slide, i) => {
        const slideInner = slide.querySelector('.slide__inner');
        const slideInfoInner = slidesInfo[i]?.querySelector('.slide-info__inner');

        if (slideInner && slideInfoInner) {
          setupTiltEffect(slide, [slideInner, slideInfoInner]);
        }
      });
    }
  }, [loading, books]);

  // Функция для настройки тилт-эффекта
  const setupTiltEffect = (node, targets) => {
    let lerpAmount = 0.06;
    const rotDeg = { current: { x: 0, y: 0 }, target: { x: 0, y: 0 } };
    const bgPos = { current: { x: 0, y: 0 }, target: { x: 0, y: 0 } };

    const lerp = (a, b, t) => a + (b - a) * t;

    const onMouseMove = (e) => {
      const { offsetX, offsetY } = e;
      lerpAmount = 0.1;

      targets.forEach(el => {
        const ox = (offsetX - el.clientWidth * 0.5) / (Math.PI * 3);
        const oy = -(offsetY - el.clientHeight * 0.5) / (Math.PI * 4);
        rotDeg.target.x = ox;
        rotDeg.target.y = oy;
        bgPos.target.x = -ox * 0.3;
        bgPos.target.y = oy * 0.3;
      });
    };

    const onMouseLeave = () => {
      lerpAmount = 0.06;
      rotDeg.target.x = 0;
      rotDeg.target.y = 0;
      bgPos.target.x = 0;
      bgPos.target.y = 0;
    };

    node.addEventListener('mousemove', onMouseMove);
    node.addEventListener('mouseleave', onMouseLeave);

    const updateAnimation = () => {
      rotDeg.current.x = lerp(rotDeg.current.x, rotDeg.target.x, lerpAmount);
      rotDeg.current.y = lerp(rotDeg.current.y, rotDeg.target.y, lerpAmount);
      bgPos.current.x = lerp(bgPos.current.x, bgPos.target.x, lerpAmount);
      bgPos.current.y = lerp(bgPos.current.y, bgPos.target.y, lerpAmount);

      targets.forEach(el => {
        el.style.setProperty('--rotX', `${rotDeg.current.y.toFixed(2)}deg`);
        el.style.setProperty('--rotY', `${rotDeg.current.x.toFixed(2)}deg`);
        el.style.setProperty('--bgPosX', `${bgPos.current.x.toFixed(2)}%`);
        el.style.setProperty('--bgPosY', `${bgPos.current.y.toFixed(2)}%`);
      });

      requestAnimationFrame(updateAnimation);
    };

    updateAnimation();

    return () => {
      node.removeEventListener('mousemove', onMouseMove);
      node.removeEventListener('mouseleave', onMouseLeave);
    };
  };

  // Функция для изменения слайда
  const changeSlide = (direction) => {
    setCurrentIndex(prevIndex => {
      const newIndex = (prevIndex + direction + books.length) % books.length;
      return newIndex;
    });
  };

  if (loading) {
    return (
        <div className="loader">
          <div className="loader__text">Загрузка...</div>
        </div>
    );
  }

  if (error && books.length === 0) {
    return <div className="error">{error}</div>;
  }

  if (!books || books.length === 0) {
    return <div className="no-books">Нет доступных книг</div>;
  }

  const currentBookIndex = currentIndex;
  const prevBookIndex = (currentIndex - 1 + books.length) % books.length;
  const nextBookIndex = (currentIndex + 1) % books.length;

  return (
      <section className="hero" style={{ position: 'relative' }}>
        <div className="slider">
          <button className="slider--btn slider--btn__prev" onClick={() => changeSlide(-1)}>
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M15 6L9 12L15 18" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </button>

          <div className="slides__wrapper" ref={slidesWrapperRef}>
            <div className="slides">
              {books.map((book, index) => {
                // Используем URL бэкенда для загрузки обложки
                const coverUrl = `${API_URL}/rest/books/${book.id}/cover`;
                console.log(`Trying to load image for book ${book.id}: ${coverUrl}`); // Логирование для отладки

                return (
                    <div
                        key={book.id}
                        className="slide"
                        data-current={index === currentBookIndex ? "" : null}
                        data-previous={index === prevBookIndex ? "" : null}
                        data-next={index === nextBookIndex ? "" : null}
                        style={{ zIndex: index === currentBookIndex ? 20 : (index === prevBookIndex ? 10 : 30) }}
                    >
                      <div className="slide__inner">
                        <Link to={`/books/${book.id}`} className="slide--image__wrapper">
                          <img
                              src={coverUrl}
                              alt={book.title}
                              className="slide--image"
                              crossOrigin="anonymous"
                              onLoad={() => console.log(`Image loaded successfully: ${coverUrl}`)} // Логирование успешной загрузки
                              onError={(e) => {
                                console.error(`Failed to load image: ${coverUrl}`);
                                e.target.src = placeholderImage; // Подстановка заглушки при ошибке
                              }}
                          />
                        </Link>
                      </div>
                    </div>
                );
              })}
            </div>

            <div className="slides--infos" ref={slidesInfoRef}>
              {books.map((book, index) => (
                  <div
                      key={`info-${book.id}`}
                      className="slide-info"
                      data-current={index === currentBookIndex ? "" : null}
                      data-previous={index === prevBookIndex ? "" : null}
                      data-next={index === nextBookIndex ? "" : null}
                  >
                    <div className="slide-info__inner">
                      <div className="slide-info--text__wrapper">
                        <div className="slide-info--text" data-title>
                          <span>{book.title}</span>
                        </div>
                        <div className="slide-info--text" data-subtitle>
                          <span>{book.author}</span>
                        </div>
                        <div className="slide-info--text" data-description>
                          <span>{book.description}</span>
                        </div>
                      </div>
                    </div>
                  </div>
              ))}
            </div>
          </div>

          <button className="slider--btn slider--btn__next" onClick={() => changeSlide(1)}>
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M9 6L15 12L9 18" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </button>
        </div>
      </section>
  );
};

export default Slider;