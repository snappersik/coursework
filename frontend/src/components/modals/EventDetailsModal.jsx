import React, { useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { observer } from 'mobx-react-lite';
import { eventStore } from '../../store/eventStore';

const EventDetailsModal = observer(({ isOpen, onClose, application }) => {
  const [event, setEvent] = useState(null);
  
  useEffect(() => {
    if (isOpen && application) {
      const fetchEventData = async () => {
        const eventData = await eventStore.fetchEvent(application.eventId);
        setEvent(eventData);
      };
      
      fetchEventData();
    }
  }, [isOpen, application]);
  
  if (!isOpen || !application) return null;

  // Если данные о мероприятии еще загружаются, показываем заглушку
  const eventData = event || {
    title: 'Загрузка...',
    description: 'Загрузка информации о мероприятии...',
    date: '...',
    time: '...',
    location: '...',
    organizer: '...'
  };

  const getStatusClass = (status) => {
    switch (status) {
      case 'APPROVED':
        return 'bg-green-100 text-green-800';
      case 'REJECTED':
        return 'bg-red-100 text-red-800';
      case 'PENDING':
      default:
        return 'bg-yellow-100 text-yellow-800';
    }
  };

  const getStatusText = (status) => {
    switch (status) {
      case 'APPROVED':
        return 'Одобрена';
      case 'REJECTED':
        return 'Отклонена';
      case 'PENDING':
      default:
        return 'На рассмотрении';
    }
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <motion.div 
            className="fixed inset-0 bg-black bg-opacity-50"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
          />
          <motion.div 
            className="bg-white rounded-lg shadow-xl overflow-hidden w-full max-w-2xl relative z-10"
            initial={{ scale: 0.9, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            exit={{ scale: 0.9, opacity: 0 }}
            transition={{ type: 'spring', damping: 20 }}
          >
            <div className="p-6 border-b">
              <div className="flex justify-between items-center">
                <h3 className="text-xl font-bold text-gray-900">Детали заявки #{application.id}</h3>
                <button onClick={onClose} className="text-gray-400 hover:text-gray-500">
                  <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
            </div>
            
            <div className="p-6">
              <div className="mb-6">
                <h4 className="text-lg font-medium text-gray-900 mb-2">Информация о заявке</h4>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm text-gray-500">Дата создания:</p>
                    <p className="font-medium">{application.createdWhen}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Статус:</p>
                    <span className={`inline-block px-2 py-1 rounded-full text-xs font-medium ${getStatusClass(application.applicationStatus)}`}>
                      {getStatusText(application.applicationStatus)}
                    </span>
                  </div>
                  {application.isDeleted && (
                    <div className="col-span-2">
                      <p className="text-sm text-gray-500">Дата отмены:</p>
                      <p className="font-medium">{application.deletedWhen}</p>
                    </div>
                  )}
                </div>
              </div>
              
              <div>
                <h4 className="text-lg font-medium text-gray-900 mb-2">Информация о мероприятии</h4>
                <div className="bg-gray-50 p-4 rounded-lg">
                  <h5 className="font-bold text-lg mb-2">{eventData.title}</h5>
                  <p className="text-gray-700 mb-4">{eventData.description}</p>
                  
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <p className="text-sm text-gray-500">Дата:</p>
                      <p className="font-medium">{eventData.date}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-500">Время:</p>
                      <p className="font-medium">{eventData.time}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-500">Место проведения:</p>
                      <p className="font-medium">{eventData.location}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-500">Организатор:</p>
                      <p className="font-medium">{eventData.organizer}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <div className="p-6 border-t bg-gray-50 flex justify-end">
              <button 
                onClick={onClose}
                className="px-4 py-2 bg-gray-200 text-gray-800 rounded-md hover:bg-gray-300 transition-colors"
              >
                Закрыть
              </button>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
});

export default EventDetailsModal;
