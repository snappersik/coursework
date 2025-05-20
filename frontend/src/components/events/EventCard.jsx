import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { format } from 'date-fns';
import { ru } from 'date-fns/locale';
import { FaCalendarAlt, FaUsers, FaBook } from 'react-icons/fa';
import axios from 'axios';
import { API_URL } from '../../config';
import placeholderImage from '../../assets/img/book_placeholder.webp';

const EventCard = ({ event }) => {
  const [bookInfo, setBookInfo] = useState(null);
  const [imageUrl, setImageUrl] = useState(placeholderImage);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (event.bookId) {
      fetchBookInfo();
    }
  }, [event.bookId]);

  const fetchBookInfo = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${API_URL}/books/${event.bookId}`);
      setBookInfo(response.data);
      
      // Если у книги есть обложка, используем ее для мероприятия
      if (response.data.coverImageUrl) {
        setImageUrl(response.data.coverImageUrl);
      } else if (response.data.id) {
        // Иначе пробуем загрузить обложку через эндпоинт cover
        setImageUrl(`${API_URL}/books/${response.data.id}/cover`);
      }
    } catch (error) {
      console.error('Ошибка при загрузке информации о книге:', error);
      // В случае ошибки оставляем плейсхолдер
    } finally {
      setLoading(false);
    }
  };

  // Форматирование даты
  const formatEventDate = (dateString) => {
    try {
      // Преобразуем строку в формате "dd.MM.yyyy HH:mm" в объект Date
      const [datePart, timePart] = dateString.split(' ');
      const [day, month, year] = datePart.split('.');
      const [hours, minutes] = timePart.split(':');
      
      const date = new Date(year, month - 1, day, hours, minutes);
      
      // Форматируем дату с использованием date-fns
      return format(date, "d MMMM yyyy 'в' HH:mm", { locale: ru });
    } catch (error) {
      console.error('Ошибка при форматировании даты:', error);
      return dateString;
    }
  };

  // Определение стиля для типа мероприятия
  const getEventTypeStyle = (type) => {
    switch (type) {
      case 'MEETING':
        return 'bg-blue-600';
      case 'PRESENTATION':
        return 'bg-green-600';
      case 'WORKSHOP':
        return 'bg-purple-600';
      default:
        return 'bg-gray-600';
    }
  };

  // Перевод типа мероприятия
  const translateEventType = (type) => {
    const translations = {
      'MEETING': 'Встреча',
      'PRESENTATION': 'Презентация',
      'WORKSHOP': 'Мастер-класс',
      'OTHER': 'Другое'
    };
    return translations[type] || type;
  };

  return (
    <div className="bg-gray-800 rounded-lg overflow-hidden shadow-lg hover:shadow-xl transition-shadow duration-300 h-full flex flex-col">
      <div className="relative h-48 overflow-hidden">
        <img 
          src={imageUrl} 
          alt={event.title} 
          className="w-full h-full object-cover transition-transform duration-300 hover:scale-105"
          onError={(e) => {
            e.target.src = placeholderImage;
            e.target.onerror = null; // Предотвращаем бесконечный цикл
          }}
        />
        <div className="absolute top-0 right-0 m-2">
          <span className={`${getEventTypeStyle(event.eventType)} text-white text-xs font-bold px-2 py-1 rounded`}>
            {translateEventType(event.eventType)}
          </span>
        </div>
        {event.isCancelled && (
          <div className="absolute inset-0 bg-black bg-opacity-70 flex items-center justify-center">
            <span className="bg-red-600 text-white text-lg font-bold px-4 py-2 rounded transform -rotate-12">
              ОТМЕНЕНО
            </span>
          </div>
        )}
      </div>
      
      <div className="p-4 flex-grow flex flex-col">
        <h3 className="text-xl font-bold mb-2 text-white">{event.title}</h3>
        
        <div className="text-gray-300 mb-4 flex-grow">
          <p className="line-clamp-3">{event.description || 'Описание отсутствует'}</p>
        </div>
        
        <div className="space-y-2 text-sm text-gray-400">
          <div className="flex items-center">
            <FaCalendarAlt className="mr-2 text-yellow-500" />
            <span>{formatEventDate(event.date)}</span>
          </div>
          
          <div className="flex items-center">
            <FaUsers className="mr-2 text-yellow-500" />
            <span>Мест: {event.maxParticipants}</span>
          </div>
          
          {bookInfo && (
            <div className="flex items-center">
              <FaBook className="mr-2 text-yellow-500" />
              <span className="truncate">{bookInfo.title}</span>
            </div>
          )}
        </div>
      </div>
      
      <div className="p-4 pt-0">
        <Link 
          to={`/events/${event.id}`} 
          className="block w-full text-center bg-yellow-500 hover:bg-yellow-600 text-white font-bold py-2 px-4 rounded transition-colors duration-300"
        >
          Подробнее
        </Link>
      </div>
    </div>
  );
};

export default EventCard;
