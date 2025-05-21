import React from 'react';
import { Link } from 'react-router-dom';
import { API_URL } from '../../config';

const ProductCard = ({ product }) => {
  const { id, name, description, price, category, hasLocalImage, coverImageUrl } = product;

  // Определяем источник изображения
  const imageSource = hasLocalImage
    ? `${API_URL}/products/${id}/cover`
    : (coverImageUrl || '/images/placeholder.png');

  // Определяем текст категории для отображения
  const getCategoryText = (category) => {
    switch (category) {
      case 'E_BOOK': return 'Электронная книга';
      case 'AUDIO_BOOK': return 'Аудиокнига';
      case 'LECTURE': return 'Лекция';
      case 'MEETING_RECORDING': return 'Запись встречи';
      default: return category;
    }
  };

  return (
    <div className="bg-[#606060] rounded-lg shadow-md overflow-hidden flex flex-col h-full">
      <div className="relative h-48 overflow-hidden">
        <img
          src={imageSource}
          alt={name}
          className="w-full h-full object-cover transition-transform duration-300 hover:scale-105"
          onError={(e) => { e.target.src = '/images/placeholder.png' }}
        />
        <div className="absolute top-2 right-2 bg-yellow-400 text-white text-xs font-bold px-2 py-1 rounded">
          {getCategoryText(category)}
        </div>
      </div>

      <div className="p-4 flex-grow flex flex-col">
        <h3 className="text-lg font-bold mb-2 line-clamp-2 h-14">{name}</h3>
        <p className="text-gray-100 mb-4 line-clamp-3 h-18">{description}</p>
        <div className="mt-auto flex justify-between items-center">
          <span className="text-xl font-bold text-yellow-500">{price} ₽</span>
          <Link
            to={`/product/${id}`}
            className="bg-yellow-400 hover:bg-yellow-500 text-white px-4 py-2 rounded transition duration-300"
          >
            Подробнее
          </Link>
        </div>
      </div>
    </div>
  );
};

export default ProductCard;
