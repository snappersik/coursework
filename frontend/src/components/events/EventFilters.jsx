import React, { useState } from 'react';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import { FaSearch, FaFilter, FaTimes } from 'react-icons/fa';
import { registerLocale } from 'react-datepicker';
import ru from 'date-fns/locale/ru';

// Регистрируем русскую локаль для DatePicker
registerLocale('ru', ru);

const EventFilters = ({ filters, onFilterChange }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [localFilters, setLocalFilters] = useState(filters);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setLocalFilters(prev => ({ ...prev, [name]: value }));
  };

  const handleDateChange = (name, date) => {
    setLocalFilters(prev => ({ ...prev, [name]: date }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onFilterChange(localFilters);
    setIsOpen(false);
  };

  const handleClear = () => {
    const clearedFilters = {
      title: '',
      eventType: '',
      startDate: null,
      endDate: null
    };
    setLocalFilters(clearedFilters);
    onFilterChange(clearedFilters);
    setIsOpen(false);
  };

  return (
    <div className="mb-8">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-xl font-semibold text-white">Поиск мероприятий</h2>
        <button
          onClick={() => setIsOpen(!isOpen)}
          className="flex items-center bg-yellow-500 hover:bg-yellow-600 text-white px-4 py-2 rounded transition-colors duration-300"
        >
          <FaFilter className="mr-2" />
          <span>Фильтры</span>
        </button>
      </div>

      {/* Поиск по названию (всегда видимый) */}
      <div className="relative mb-4">
        <input
          type="text"
          name="title"
          value={localFilters.title}
          onChange={handleInputChange}
          placeholder="Поиск по названию..."
          className="w-full p-3 pl-10 bg-gray-700 border border-gray-600 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-yellow-500"
        />
        <FaSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
        {localFilters.title && (
          <button
            onClick={() => {
              setLocalFilters(prev => ({ ...prev, title: '' }));
              onFilterChange({ ...localFilters, title: '' });
            }}
            className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-white"
          >
            <FaTimes />
          </button>
        )}
      </div>

      {/* Расширенные фильтры (скрываемые) */}
      {isOpen && (
        <form onSubmit={handleSubmit} className="bg-gray-800 p-4 rounded-lg mb-4 animate-fadeIn">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-1">Тип мероприятия</label>
              <select
                name="eventType"
                value={localFilters.eventType}
                onChange={handleInputChange}
                className="w-full p-2 bg-gray-700 border border-gray-600 rounded text-white"
              >
                <option value="">Все типы</option>
                <option value="MEETING">Встреча</option>
                <option value="PRESENTATION">Презентация</option>
                <option value="WORKSHOP">Мастер-класс</option>
                <option value="OTHER">Другое</option>
              </select>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-1">Дата от</label>
              <DatePicker
                selected={localFilters.startDate}
                onChange={(date) => handleDateChange('startDate', date)}
                dateFormat="dd.MM.yyyy"
                locale="ru"
                placeholderText="Выберите дату"
                className="w-full p-2 bg-gray-700 border border-gray-600 rounded text-white"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-1">Дата до</label>
              <DatePicker
                selected={localFilters.endDate}
                onChange={(date) => handleDateChange('endDate', date)}
                dateFormat="dd.MM.yyyy"
                locale="ru"
                placeholderText="Выберите дату"
                className="w-full p-2 bg-gray-700 border border-gray-600 rounded text-white"
              />
            </div>
          </div>
          
          <div className="flex justify-end mt-4 space-x-2">
            <button
              type="button"
              onClick={handleClear}
              className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-500 transition-colors duration-300"
            >
              Сбросить
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-yellow-500 hover:bg-yellow-600 text-white rounded transition-colors duration-300"
            >
              Применить
            </button>
          </div>
        </form>
      )}
    </div>
  );
};

export default EventFilters;
