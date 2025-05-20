import React from 'react';
import EventCard from './EventCard';
import { motion } from 'framer-motion';
import { FaChevronLeft, FaChevronRight } from 'react-icons/fa';

const EventList = ({ events = [], loading, pagination, onPageChange }) => {
  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-yellow-500"></div>
      </div>
    );
  }

  if (!events || events.length === 0) {
    return (
      <div className="text-center py-12 bg-gray-800 rounded-lg">
        <h3 className="text-xl font-semibold text-gray-300 mb-2">Мероприятия не найдены</h3>
        <p className="text-gray-400">Попробуйте изменить параметры поиска или вернитесь позже</p>
      </div>
    );
  }

  const container = {
    hidden: { opacity: 0 },
    show: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1
      }
    }
  };

  const item = {
    hidden: { opacity: 0, y: 20 },
    show: { opacity: 1, y: 0 }
  };

  return (
    <div>
      <motion.div 
        className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8"
        variants={container}
        initial="hidden"
        animate="show"
      >
        {events.map(event => (
          <motion.div key={event.id} variants={item}>
            <EventCard event={event} />
          </motion.div>
        ))}
      </motion.div>

      {/* Пагинация */}
      {pagination && pagination.totalPages > 1 && (
        <div className="flex justify-center items-center space-x-2 mt-8">
          <button
            onClick={() => onPageChange(Math.max(0, pagination.page - 1))}
            disabled={pagination.page === 0}
            className={`p-2 rounded-full ${
              pagination.page === 0 
                ? 'bg-gray-700 text-gray-500 cursor-not-allowed' 
                : 'bg-gray-700 text-white hover:bg-gray-600'
            }`}
            aria-label="Предыдущая страница"
          >
            <FaChevronLeft />
          </button>
          
          <div className="flex space-x-1">
            {[...Array(pagination.totalPages).keys()].map(pageNum => (
              <button
                key={pageNum}
                onClick={() => onPageChange(pageNum)}
                className={`w-8 h-8 flex items-center justify-center rounded-full ${
                  pagination.page === pageNum 
                    ? 'bg-yellow-500 text-white' 
                    : 'bg-gray-700 text-white hover:bg-gray-600'
                }`}
              >
                {pageNum + 1}
              </button>
            ))}
          </div>
          
          <button
            onClick={() => onPageChange(Math.min(pagination.totalPages - 1, pagination.page + 1))}
            disabled={pagination.page === pagination.totalPages - 1}
            className={`p-2 rounded-full ${
              pagination.page === pagination.totalPages - 1 
                ? 'bg-gray-700 text-gray-500 cursor-not-allowed' 
                : 'bg-gray-700 text-white hover:bg-gray-600'
            }`}
            aria-label="Следующая страница"
          >
            <FaChevronRight />
          </button>
        </div>
      )}
    </div>
  );
};

export default EventList;
