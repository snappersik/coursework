import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { FaCalendarAlt, FaUsers, FaTimesCircle, FaCheckCircle } from 'react-icons/fa';
import { format } from 'date-fns';
import { ru } from 'date-fns/locale';

const EventApplicationForm = ({ event, onSubmit, onCancel }) => {
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

  return (
    <AnimatePresence>
      <div className="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50 p-4">
        <motion.div
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          exit={{ opacity: 0, scale: 0.9 }}
          className="bg-gray-800 rounded-lg shadow-xl max-w-md w-full overflow-hidden"
        >
          <div className="bg-yellow-500 p-4">
            <h2 className="text-xl font-bold text-white">Заявка на участие</h2>
          </div>
          
          <div className="p-6">
            <h3 className="text-lg font-semibold text-white mb-4">{event.title}</h3>
            
            <div className="space-y-4 mb-6">
              <div className="flex items-center">
                <FaCalendarAlt className="text-yellow-500 mr-3" />
                <span className="text-gray-300">{formatEventDate(event.date)}</span>
              </div>
              
              <div className="flex items-center">
                <FaUsers className="text-yellow-500 mr-3" />
                <span className="text-gray-300">Максимум участников: {event.maxParticipants}</span>
              </div>
            </div>
            
            <div className="bg-gray-700 p-4 rounded-lg mb-6">
              <p className="text-white text-sm">
                Подавая заявку на участие, вы соглашаетесь с правилами проведения мероприятия. 
                В случае одобрения заявки, вам придет уведомление на электронную почту.
              </p>
            </div>
            
            <div className="flex justify-end space-x-3">
              <button
                onClick={onCancel}
                className="flex items-center bg-gray-600 hover:bg-gray-500 text-white px-4 py-2 rounded transition-colors duration-300"
              >
                <FaTimesCircle className="mr-2" />
                Отмена
              </button>
              
              <button
                onClick={onSubmit}
                className="flex items-center bg-yellow-500 hover:bg-yellow-600 text-white px-4 py-2 rounded transition-colors duration-300"
              >
                <FaCheckCircle className="mr-2" />
                Подтвердить
              </button>
            </div>
          </div>
        </motion.div>
      </div>
    </AnimatePresence>
  );
};

export default EventApplicationForm;
