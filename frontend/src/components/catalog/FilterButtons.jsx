import React from 'react';

const FilterButtons = ({ activeFilter, onFilterChange, categories }) => {
  // categories теперь приходит как props: [{name: "E_BOOK", description: "Электронная книга"}, ...]

  return (
    <div className="flex flex-wrap justify-center gap-2 mb-8">
      <button
        key="all"
        onClick={() => onFilterChange('all')} // "all" для сброса фильтра по категории
        className={`px-4 py-2 rounded-md transition-colors duration-300 text-sm md:text-base ${
          activeFilter === 'all' || activeFilter === ''
            ? 'bg-yellow-500 text-white'
            : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
        }`}
      >
        Все
      </button>
      {categories.map(category => (
        <button
          key={category.name} // Используем Enum name (e.g., E_BOOK) как ключ и значение для фильтра
          onClick={() => onFilterChange(category.name)}
          className={`px-4 py-2 rounded-md transition-colors duration-300 text-sm md:text-base ${
            activeFilter === category.name
              ? 'bg-yellow-500 text-white'
              : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
          }`}
        >
          {category.description} {/* Отображаем user-friendly описание */}
        </button>
      ))}
    </div>
  );
};

export default FilterButtons;
