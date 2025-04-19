import React, { useEffect, useState } from 'react';
import placeholderImage from '../../../assets/img/book_placeholder.webp'; 
import { API_URL } from '../../../config';

const BookCard = ({ book }) => {
  const [imageUrl, setImageUrl] = useState(placeholderImage);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);

  useEffect(() => {
    if (!book || !book.id) {
      setImageUrl(placeholderImage);
      setIsLoading(false);
      return;
    }
    const fetchImage = async () => {
      try {
        setIsLoading(true);
        const infoResponse = await fetch(`${API_URL}/books/${book.id}/image-info`);
        if (!infoResponse.ok) throw new Error();
        const imageInfo = await infoResponse.json();
        if (imageInfo.coverImageUrl)
          setImageUrl(imageInfo.coverImageUrl);
        else if (imageInfo.hasLocalImage || imageInfo.coverImageFilename)
          setImageUrl(`${API_URL}/books/${book.id}/cover`);
        else
          setImageUrl(placeholderImage);
        setHasError(false);
      } catch {
        setImageUrl(placeholderImage);
        setHasError(true);
      } finally {
        setIsLoading(false);
      }
    };
    fetchImage();
  }, [book]);

  if (!book) return null;

  return (
    <div className="flex flex-col w-full max-w-xs rounded-lg overflow-hidden shadow-md bg-white m-4 transition-transform duration-300 hover:-translate-y-1 hover:shadow-xl">
      <div className="relative w-full h-72 bg-gray-100 flex items-center justify-center">
        {isLoading && (
          <div className="absolute inset-0 flex items-center justify-center text-gray-400 animate-pulse">
            Загрузка...
          </div>
        )}
        <img
          src={imageUrl}
          alt={book.title}
          className={`w-full h-full object-cover transition-transform duration-300 ${isLoading ? 'opacity-0' : 'opacity-100'} ${hasError ? 'grayscale' : ''}`}
          onError={() => {
            setImageUrl(placeholderImage);
            setHasError(true);
          }}
          style={{ minHeight: '18rem' }}
        />
      </div>
      <div className="flex flex-col p-5 gap-2 flex-grow">
        <h3 className="text-lg font-semibold text-gray-800">{book.title}</h3>
        <p className="text-sm text-gray-600">
          {book.author}
          {book.genre && (
            <span className="ml-2 px-2 py-0.5 bg-gray-200 rounded text-xs text-gray-500">
              {book.genre.description}
            </span>
          )}
        </p>
        {book.description && (
          <div className="mt-1 text-xs text-gray-500 line-clamp-2">{book.description}</div>
        )}
        <a
          href={`/books/${book.id}`}
          className="mt-auto inline-block w-full bg-blue-500 text-white text-center py-2 px-4 rounded hover:bg-blue-600 transition-colors"
        >
          Подробнее
        </a>
      </div>
    </div>
  );
};

export default BookCard;
