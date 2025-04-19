import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import './BookCard.css';
import placeholderImage from '../../../assets/img/book_placeholder.webp';
import { API_URL } from '../../../config';

const BookCard = ({ book }) => {
  const [imageUrl, setImageUrl] = useState(placeholderImage);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);

  useEffect(() => {
    // Если у книги нет ID, используем плейсхолдер
    if (!book || !book.id) {
      setIsLoading(false);
      return;
    }

    const fetchImage = async () => {
      try {
        setIsLoading(true);
        
        // Получаем информацию об изображении книги
        const infoResponse = await fetch(`${API_URL}/books/${book.id}/image-info`);
        
        if (!infoResponse.ok) {
          throw new Error('Не удалось получить информацию об изображении');
        }
        
        const imageInfo = await infoResponse.json();
        
        // В зависимости от типа изображения формируем URL
        if (imageInfo.coverImageUrl) {
          // Если есть прямая ссылка на изображение
          setImageUrl(imageInfo.coverImageUrl);
        } else if (imageInfo.hasLocalImage || imageInfo.coverImageFilename) {
          // Если есть локальное изображение или файл на сервере
          setImageUrl(`${API_URL}/books/${book.id}/cover`);
        } else {
          // Если изображения нет, используем плейсхолдер
          setImageUrl(placeholderImage);
        }
        
        setHasError(false);
      } catch (error) {
        console.error('Ошибка загрузки изображения:', error);
        setImageUrl(placeholderImage);
        setHasError(true);
      } finally {
        setIsLoading(false);
      }
    };

    fetchImage();
  }, [book]);

  // Обработчик ошибки загрузки
  const handleImageError = () => {
    setImageUrl(placeholderImage);
    setHasError(true);
  };

  if (!book) return null;

  return (
    <div className="book-card">
      <div className="book-card__image-container">
        {isLoading ? (
          <div className="book-card__loading">Загрузка...</div>
        ) : (
          <img
            src={imageUrl}
            alt={book.title}
            className="book-card__image"
            onError={handleImageError}
          />
        )}
      </div>
      <div className="book-card__content">
        <h3 className="book-card__title">{book.title}</h3>
        <p className="book-card__author">{book.author}</p>
        {book.genre && (
          <span className="book-card__genre">{book.genre.description}</span>
        )}
        <Link to={`/books/${book.id}`} className="book-card__link">
          Подробнее
        </Link>
      </div>
    </div>
  );
};

export default BookCard;