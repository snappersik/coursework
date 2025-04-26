import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { toast } from 'react-toastify';
import { API_URL } from '../../../config';

const EventManager = () => {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [currentEvent, setCurrentEvent] = useState(null);
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    eventDate: '',
    location: '',
    maxParticipants: 20
  });

  useEffect(() => {
    fetchEvents();
  }, []);

  const fetchEvents = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${API_URL}/rest/events`, {
        withCredentials: true
      });
      setEvents(response.data);
      setLoading(false);
    } catch (error) {
      console.error('Ошибка загрузки мероприятий:', error);
      toast.error('Не удалось загрузить мероприятия');
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (currentEvent) {
        // Обновление мероприятия
        await axios.put(`${API_URL}/rest/events/${currentEvent.id}`, formData, {
          withCredentials: true
        });
        toast.success('Мероприятие успешно обновлено');
      } else {
        // Создание нового мероприятия
        await axios.post(`${API_URL}/rest/events`, formData, {
          withCredentials: true
        });
        toast.success('Мероприятие успешно создано');
      }
      setShowModal(false);
      fetchEvents();
    } catch (error) {
      console.error('Ошибка сохранения мероприятия:', error);
      toast.error('Не удалось сохранить мероприятие');
    }
  };

  const handleEdit = (event) => {
    setCurrentEvent(event);
    setFormData({
      title: event.title,
      description: event.description || '',
      eventDate: event.eventDate ? new Date(event.eventDate).toISOString().slice(0, 16) : '',
      location: event.location || '',
      maxParticipants: event.maxParticipants || 20
    });
    setShowModal(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Вы уверены, что хотите удалить это мероприятие?')) {
      try {
        await axios.delete(`${API_URL}/rest/events/${id}`, {
          withCredentials: true
        });
        toast.success('Мероприятие успешно удалено');
        fetchEvents();
      } catch (error) {
        console.error('Ошибка удаления мероприятия:', error);
        toast.error('Не удалось удалить мероприятие');
      }
    }
  };

  const handleCancel = async (id) => {
    const reason = prompt('Укажите причину отмены мероприятия:');
    if (reason) {
      try {
        await axios.post(`${API_URL}/rest/events/${id}/cancel?reason=${encodeURIComponent(reason)}`, {}, {
          withCredentials: true
        });
        toast.success('Мероприятие отменено');
        fetchEvents();
      } catch (error) {
        console.error('Ошибка отмены мероприятия:', error);
        toast.error('Не удалось отменить мероприятие');
      }
    }
  };

  const handleReschedule = async (id) => {
    const newDate = prompt('Введите новую дату и время (YYYY-MM-DD HH:MM):');
    const reason = prompt('Укажите причину переноса мероприятия:');
    
    if (newDate && reason) {
      try {
        await axios.post(`${API_URL}/rest/events/${id}/reschedule`, {
          newDate: new Date(newDate).toISOString(),
          reason
        }, {
          withCredentials: true
        });
        toast.success('Мероприятие перенесено');
        fetchEvents();
      } catch (error) {
        console.error('Ошибка переноса мероприятия:', error);
        toast.error('Не удалось перенести мероприятие');
      }
    }
  };

  const handleAddNew = () => {
    setCurrentEvent(null);
    setFormData({
      title: '',
      description: '',
      eventDate: '',
      location: '',
      maxParticipants: 20
    });
    setShowModal(true);
  };

  if (loading) {
    return <div className="text-center py-10">Загрузка...</div>;
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-xl font-semibold">Управление мероприятиями</h2>
        <button
          onClick={handleAddNew}
          className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
        >
          Добавить мероприятие
        </button>
      </div>

      <div className="bg-[#424242] shadow-md rounded-lg overflow-hidden text-black">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Название</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Дата</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Место</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Участники</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Статус</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Действия</th>
              </tr>
            </thead>
            <tbody className="bg-[#424242] divide-y divide-gray-200">
              {events.map(event => (
                <tr key={event.id}>
                  <td className="px-6 py-4 whitespace-nowrap">{event.title}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {event.eventDate ? new Date(event.eventDate).toLocaleString() : 'Не указана'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">{event.location || 'Не указано'}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {event.participants?.length || 0} / {event.maxParticipants || 'Не ограничено'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                      ${event.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 
                        event.status === 'CANCELLED' ? 'bg-red-100 text-red-800' : 
                        'bg-yellow-100 text-yellow-800'}`}>
                      {event.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <button
                      onClick={() => handleEdit(event)}
                      className="text-indigo-600 hover:text-indigo-900 mr-2"
                    >
                      Редактировать
                    </button>
                    <button
                      onClick={() => handleCancel(event.id)}
                      className="text-yellow-600 hover:text-yellow-900 mr-2"
                    >
                      Отменить
                    </button>
                    <button
                      onClick={() => handleReschedule(event.id)}
                      className="text-blue-600 hover:text-blue-900 mr-2"
                    >
                      Перенести
                    </button>
                    <button
                      onClick={() => handleDelete(event.id)}
                      className="text-red-600 hover:text-red-900"
                    >
                      Удалить
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Модальное окно для добавления/редактирования мероприятия */}
      {showModal && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-[#424242] rounded-lg shadow-xl p-6 w-full max-w-md">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-semibold">
                {currentEvent ? 'Редактировать мероприятие' : 'Добавить мероприятие'}
              </h3>
              <button
                onClick={() => setShowModal(false)}
                className="text-gray-500 hover:text-gray-700"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="mb-4">
                <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="title">
                  Название
                </label>
                <input
                  type="text"
                  id="title"
                  name="title"
                  value={formData.title}
                  onChange={handleInputChange}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  required
                />
              </div>
              <div className="mb-4">
                <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="description">
                  Описание
                </label>
                <textarea
                  id="description"
                  name="description"
                  value={formData.description}
                  onChange={handleInputChange}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  rows="3"
                ></textarea>
              </div>
              <div className="mb-4">
                <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="eventDate">
                  Дата и время
                </label>
                <input
                  type="datetime-local"
                  id="eventDate"
                  name="eventDate"
                  value={formData.eventDate}
                  onChange={handleInputChange}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  required
                />
              </div>
              <div className="mb-4">
                <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="location">
                  Место проведения
                </label>
                <input
                  type="text"
                  id="location"
                  name="location"
                  value={formData.location}
                  onChange={handleInputChange}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                />
              </div>
              <div className="mb-6">
                <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="maxParticipants">
                  Максимальное количество участников
                </label>
                <input
                  type="number"
                  id="maxParticipants"
                  name="maxParticipants"
                  value={formData.maxParticipants}
                  onChange={handleInputChange}
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  min="1"
                />
              </div>
              <div className="flex items-center justify-end">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="mr-3 px-4 py-2 text-sm font-medium text-gray-700 bg-gray-200 rounded-md hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-400"
                >
                  Отмена
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-md hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                >
                  Сохранить
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default EventManager;
