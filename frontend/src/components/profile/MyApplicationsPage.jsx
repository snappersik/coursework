import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { toast } from 'react-toastify';
import { FaCalendarAlt, FaTimesCircle, FaCheckCircle, FaExclamationCircle } from 'react-icons/fa';
import axios from 'axios';
import { API_URL } from '../../config';
import { format } from 'date-fns';
import { ru } from 'date-fns/locale';

const MyApplicationsPage = () => {
  const [applications, setApplications] = useState([]);
  const [events, setEvents] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchApplications();
  }, []);

  const fetchApplications = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${API_URL}/event-applications/my`, {
        withCredentials: true
      });
      
      setApplications(response.data);
      
      // Загружаем информацию о мероприятиях
      const eventIds = [...new Set(response.data.map(app => app.eventId))];
      const eventsData = {};
      
      await Promise.all(eventIds.map(async (eventId) => {
        try {
          const eventResponse = await axios.get(`${API_URL}/events/${eventId}`);
          eventsData[eventId] = eventResponse.data;
        } catch (error) {
          console.error(`Ошибка при загрузке информации о мероприятии ${eventId}:`, error);
        }
      }));
      
      setEvents(eventsData);
    } catch (error) {
      console.error('Ошибка при загрузке заявок:', error);
      toast.error('Не удалось загрузить заявки на мероприятия');
    } finally {
      setLoading(false);
    }
  };

  const handleCancelApplication = async (applicationId) => {
    try {
      await axios.put(`${API_URL}/event-applications/${applicationId}/cancel`, {}, {
        withCredentials: true
      });
      
      toast.success('Заявка успешно отменена');
      fetchApplications(); // Обновляем список заявок
    } catch (error) {
      console.error('Ошибка при отмене заявки:', error);
      toast.error('Не удалось отменить заявку');
    }
  };

  // Форматирование даты
  const formatEventDate = (dateString) => {
    if (!dateString) return '';
    
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

  // Перевод статуса заявки
  const translateApplicationStatus = (status) => {
    const translations = {
      'PENDING': 'На рассмотрении',
      'APPROVED': 'Одобрена',
      'REJECTED': 'Отклонена',
      'CANCELLED': 'Отменена'
    };
    return translations[status] || status;
  };

  // Определение стиля для статуса заявки
  const getApplicationStatusStyle = (status) => {
    switch (status) {
      case 'APPROVED':
        return 'bg-green-600';
      case 'PENDING':
        return 'bg-yellow-600';
      case 'REJECTED':
        return 'bg-red-600';
      case 'CANCELLED':
        return 'bg-gray-600';
      default:
        return 'bg-gray-600';
    }
  };

  // Получение иконки для статуса
  const getStatusIcon = (status) => {
    switch (status) {
      case 'APPROVED':
        return <FaCheckCircle className="text-green-500" />;
      case 'PENDING':
        return <FaExclamationCircle className="text-yellow-500" />;
      case 'REJECTED':
        return <FaTimesCircle className="text-red-500" />;
      case 'CANCELLED':
        return <FaTimesCircle className="text-gray-500" />;
      default:
        return null;
    }
  };

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8 flex justify-center items-center min-h-[50vh]">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-yellow-500"></div>
      </div>
    );
  }

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="container mx-auto px-4 py-8"
    >
      <h1 className="text-3xl font-bold mb-8 text-center">Мои заявки на мероприятия</h1>

      {applications.length === 0 ? (
        <div className="bg-gray-800 rounded-lg p-8 text-center">
          <h2 className="text-xl font-semibold text-white mb-4">У вас пока нет заявок на мероприятия</h2>
          <p className="text-gray-300 mb-6">Вы можете подать заявку на участие в любом из доступных мероприятий.</p>
          <Link 
            to="/events" 
            className="inline-block bg-yellow-500 hover:bg-yellow-600 text-white font-bold py-2 px-4 rounded transition-colors duration-300"
          >
            Перейти к мероприятиям
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {applications.map(application => {
            const event = events[application.eventId];
            
            return (
              <div key={application.id} className="bg-gray-800 rounded-lg overflow-hidden shadow-lg">
                <div className="p-6">
                  <div className="flex justify-between items-start mb-4">
                    <h3 className="text-xl font-bold text-white">
                      {event ? event.title : `Мероприятие #${application.eventId}`}
                    </h3>
                    <div className={`${getApplicationStatusStyle(application.applicationStatus)} px-2 py-1 rounded text-xs font-bold text-white flex items-center`}>
                      {getStatusIcon(application.applicationStatus)}
                      <span className="ml-1">{translateApplicationStatus(application.applicationStatus)}</span>
                    </div>
                  </div>
                  
                  {event && (
                    <div className="mb-4">
                      <div className="flex items-center text-gray-300 mb-2">
                        <FaCalendarAlt className="text-yellow-500 mr-2" />
                        <span>{formatEventDate(event.date)}</span>
                      </div>
                      <p className="text-gray-400 line-clamp-2">
                        {event.description || 'Описание отсутствует'}
                      </p>
                    </div>
                  )}
                  
                  <div className="flex flex-col space-y-2">
                    {(application.applicationStatus === 'APPROVED' || application.applicationStatus === 'PENDING') && (
                      <button
                        onClick={() => handleCancelApplication(application.id)}
                        className="w-full flex justify-center items-center bg-red-600 hover:bg-red-700 text-white py-2 px-4 rounded transition-colors duration-300"
                      >
                        <FaTimesCircle className="mr-2" />
                        Отменить заявку
                      </button>
                    )}
                    
                    {event && (
                      <Link
                        to={`/events/${event.id}`}
                        className="w-full flex justify-center items-center bg-gray-700 hover:bg-gray-600 text-white py-2 px-4 rounded transition-colors duration-300"
                      >
                        Подробнее о мероприятии
                      </Link>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </motion.div>
  );
};

export default MyApplicationsPage;
