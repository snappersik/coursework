import React, { useState, useEffect } from 'react';
import { Helmet } from 'react-helmet';
import EventList from '../../components/events/EventList';
import EventFilters from '../../components/events/EventFilters';
import { motion } from 'framer-motion';
import { toast } from 'react-toastify';
import { API_URL } from '../../config';
import axios from 'axios';

const EventsPage = () => {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({
    title: '',
    eventType: '',
    startDate: null,
    endDate: null
  });
  const [pagination, setPagination] = useState({
    page: 0,
    size: 9,
    totalPages: 0,
    totalElements: 0
  });

  useEffect(() => {
    fetchEvents();
  }, [pagination.page, pagination.size, filters]);

  const fetchEvents = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams({
        page: pagination.page,
        size: pagination.size
      });

      if (filters.title) params.append('title', filters.title);
      if (filters.eventType) params.append('eventType', filters.eventType);
      if (filters.startDate) params.append('startDate', filters.startDate);
      if (filters.endDate) params.append('endDate', filters.endDate);

      const response = await axios.get(`${API_URL}/events/paginated?${params.toString()}`);
      setEvents(response.data.content || []);
      setPagination(prev => ({
        ...prev,
        totalPages: response.data.totalPages || 0,
        totalElements: response.data.totalElements || 0
      }));
    } catch (error) {
      console.error('Ошибка при загрузке мероприятий:', error);
      toast.error('Не удалось загрузить мероприятия');
      setEvents([]); // Устанавливаем пустой массив в случае ошибки
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = (newFilters) => {
    setFilters(newFilters);
    setPagination(prev => ({ ...prev, page: 0 })); // Сбрасываем на первую страницу при изменении фильтров
  };

  const handlePageChange = (newPage) => {
    setPagination(prev => ({ ...prev, page: newPage }));
  };

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="container mx-auto px-4 py-8"
    >
      <Helmet>
        <title>Мероприятия | Книжная Гавань</title>
        <meta name="description" content="Список предстоящих мероприятий в Книжной Гавани" />
      </Helmet>

      <h1 className="text-3xl font-bold mb-8 text-center">Мероприятия</h1>
      
      <EventFilters filters={filters} onFilterChange={handleFilterChange} />
      
      <EventList 
        events={events} 
        loading={loading} 
        pagination={pagination} 
        onPageChange={handlePageChange} 
      />
    </motion.div>
  );
};

export default EventsPage;
