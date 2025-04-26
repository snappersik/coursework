import React from 'react';

const FilterButtons = ({ activeFilter, onFilterChange }) => {
  const categories = [
    { id: 'all', name: 'Все' },
    { id: 'BOOK', name: 'Книги' },
    { id: 'AUDIOBOOK', name: 'Аудиокниги' },
    { id: 'EBOOK', name: 'Электронные книги' },
    { id: 'SOUVENIR', name: 'Сувениры' },
    { id: 'GIFT_CARD', name: 'Подарочные карты' }
  ];

  return (
    <div className="flex flex-wrap justify-center gap-2 mb-8">
      {categories.map(category => (
        <button
          key={category.id}
          onClick={() => onFilterChange(category.id)}
          className={`px-4 py-2 rounded-md transition-colors duration-300 ${
            activeFilter === category.id
              ? 'bg-blue-500 text-white'
              : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
          }`}
        >
          {category.name}
        </button>
      ))}
    </div>
  );
};

export default FilterButtons;
