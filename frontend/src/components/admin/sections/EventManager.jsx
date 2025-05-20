import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { toast } from 'react-toastify';
import { API_URL } from '../../../config';
import { FaSort, FaSortAlphaDown, FaSortAlphaUp, FaTrashRestore, FaTrash, FaPen, FaCalendarPlus, FaTimes, FaCalendarAlt } from 'react-icons/fa';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';

const eventTypes = ['MEETING', 'PRESENTATION', 'WORKSHOP', 'OTHER'];

const EventManager = () => {
  const [events, setEvents] = useState([]);
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [showRescheduleModal, setShowRescheduleModal] = useState(false);
  const [current, setCurrent] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [sortConfig, setSortConfig] = useState({ key: null, direction: 'asc' });
  const [softDeletedEvents, setSoftDeletedEvents] = useState({});
  const [cancelReason, setCancelReason] = useState('');
  const [rescheduleData, setRescheduleData] = useState({
    newDate: '',
    reason: ''
  });
  const [form, setForm] = useState({
    title: '',
    eventType: 'MEETING',
    bookId: '',
    date: '',
    description: '',
    maxParticipants: 20,
    isCancelled: false,
    cancellationReason: ''
  });

  useEffect(() => {
    fetchEvents();
    fetchBooks();
  }, []);

  const fetchEvents = async () => {
    setLoading(true);
    try {
      const { data } = await axios.get(
          `${API_URL}/events/paginated?page=0&size=100&sortBy=id&direction=ASC`,
          { withCredentials: true }
      );

      const processedEvents = data.content.map(event => ({
        ...event,
        isCancelled: event.cancelled === true || event.cancelled === "true"
      }));

      console.log('Fetched events:', processedEvents);
      setEvents(processedEvents);
    } catch (error) {
      toast.error('Не удалось загрузить мероприятия');
    } finally {
      setLoading(false);
    }
  };

  const fetchBooks = async () => {
    try {
      const { data } = await axios.get(
          `${API_URL}/books`,
          { withCredentials: true }
      );
      setBooks(data);
    } catch (error) {
      console.error('Ошибка загрузки книг:', error);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setForm({
      ...form,
      [name]: value
    });
  };

  const handleCheckboxChange = (e) => {
    const { name, checked } = e.target;
    setForm({
      ...form,
      [name]: checked
    });
  };

  const openModal = event => {
    if (event) {
      setCurrent(event);
      let formattedDate = '';
      if (event.date) {
        const dateParts = event.date.split(' ')[0].split('.');
        const timeParts = event.date.split(' ')[1];
        formattedDate = `${dateParts[2]}-${dateParts[1]}-${dateParts[0]}T${timeParts}`;
      }

      setForm({
        title: event.title || '',
        eventType: event.eventType || 'MEETING',
        bookId: event.bookId?.toString() || '',
        date: formattedDate,
        description: event.description || '',
        maxParticipants: event.maxParticipants || 20,
        isCancelled: event.isCancelled || false,
        cancellationReason: event.cancellationReason || ''
      });
    } else {
      setCurrent(null);
      setForm({
        title: '',
        eventType: 'MEETING',
        bookId: '',
        date: '',
        description: '',
        maxParticipants: 20,
        isCancelled: false,
        cancellationReason: ''
      });
    }
    setShowModal(true);
  };

  const save = async e => {
    e.preventDefault();
    let formattedDate = null;
    if (form.date) {
      const date = new Date(form.date);
      const day = String(date.getDate()).padStart(2, '0');
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const year = date.getFullYear();
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      formattedDate = `${day}.${month}.${year} ${hours}:${minutes}`;
    }

    const dto = {
      title: form.title,
      eventType: form.eventType,
      bookId: form.bookId ? parseInt(form.bookId, 10) : null,
      date: formattedDate,
      description: form.description,
      maxParticipants: parseInt(form.maxParticipants, 10),
      isCancelled: form.isCancelled,
      cancellationReason: form.isCancelled ? form.cancellationReason : ''
    };

    try {
      if (current) {
        await axios.put(
            `${API_URL}/events/${current.id}`,
            dto,
            { withCredentials: true }
        );
        toast.success('Мероприятие обновлено');
      } else {
        await axios.post(
            `${API_URL}/events`,
            dto,
            { withCredentials: true }
        );
        toast.success('Мероприятие создано');
      }
      setShowModal(false);
      fetchEvents();
    } catch (error) {
      console.error('Ошибка сохранения:', error);
      toast.error('Ошибка сохранения');
    }
  };

  const handleCancelEvent = async (eventId) => {
    setCurrent(events.find(e => e.id === eventId));
    setShowCancelModal(true);
  };

  const confirmCancelEvent = async () => {
    if (!cancelReason.trim()) {
      toast.error('Укажите причину отмены');
      return;
    }

    try {
      await axios.post(
          `${API_URL}/events/${current.id}/cancel`,
          null,
          {
            params: { reason: cancelReason },
            withCredentials: true }
      );
      toast.success('Мероприятие отменено');
      setShowCancelModal(false);
      setCancelReason('');

      setEvents(prevEvents => {
        const updatedEvents = prevEvents.map(event =>
            event.id === current.id
                ? { ...event, isCancelled: true, cancellationReason: cancelReason }
                : event
        );
        fetchEvents();
        return updatedEvents;
      });
    } catch (error) {
      console.error('Ошибка отмены мероприятия:', error);
      toast.error('Не удалось отменить мероприятие');
    }
  };

  const handleRescheduleEvent = (eventId) => {
    const event = events.find(e => e.id === eventId);
    setCurrent(event);

    let formattedDate = '';
    if (event.date) {
      const dateParts = event.date.split(' ')[0].split('.');
      const timeParts = event.date.split(' ')[1];
      formattedDate = `${dateParts[2]}-${dateParts[1]}-${dateParts[0]}T${timeParts}`;
    }

    setRescheduleData({
      newDate: formattedDate,
      reason: ''
    });
    setShowRescheduleModal(true);
  };

  const confirmRescheduleEvent = async () => {
    if (!rescheduleData.newDate) {
      toast.error('Выберите новую дату');
      return;
    }

    if (!rescheduleData.reason.trim()) {
      toast.error('Укажите причину переноса');
      return;
    }

    try {
      const date = new Date(rescheduleData.newDate);
      const day = String(date.getDate()).padStart(2, '0');
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const year = date.getFullYear();
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      const formattedDate = `${day}.${month}.${year} ${hours}:${minutes}`;

      await axios.post(
          `${API_URL}/events/${current.id}/reschedule`,
          {
            newDate: formattedDate,
            reason: rescheduleData.reason
          },
          { withCredentials: true }
      );
      toast.success('Мероприятие перенесено');
      setShowRescheduleModal(false);
      setRescheduleData({ newDate: '', reason: '' });
      fetchEvents();
    } catch (error) {
      console.error('Ошибка переноса мероприятия:', error);
      toast.error('Не удалось перенести мероприятие');
    }
  };

  const handleSoftDelete = (id) => {
    setSoftDeletedEvents(prev => ({
      ...prev,
      [id]: true
    }));
    toast.warning('Мероприятие помечено на удаление');
  };

  const handleRestore = (id) => {
    setSoftDeletedEvents(prev => {
      const newState = { ...prev };
      delete newState[id];
      return newState;
    });
    toast.success('Мероприятие восстановлено');
  };

  const handleDelete = async (id) => {
    if (window.confirm('Вы уверены, что хотите удалить это мероприятие? Это действие нельзя отменить.')) {
      try {
        await axios.delete(`${API_URL}/events/${id}`, { withCredentials: true });
        toast.success('Мероприятие успешно удалено');
        fetchEvents();
        setSoftDeletedEvents(prev => {
          const newState = { ...prev };
          delete newState[id];
          return newState;
        });
      } catch (error) {
        console.error('Ошибка удаления мероприятия:', error);
        toast.error('Не удалось удалить мероприятие');
      }
    }
  };

  const requestSort = (key) => {
    let direction = 'asc';
    if (sortConfig.key === key && sortConfig.direction === 'asc') {
      direction = 'desc';
    }
    setSortConfig({ key, direction });
  };

  const getSortIcon = (key) => {
    if (sortConfig.key !== key) {
      return <FaSort className="ml-1 inline" />;
    }
    return sortConfig.direction === 'asc'
        ? <FaSortAlphaDown className="ml-1 inline text-yellow-400" />
        : <FaSortAlphaUp className="ml-1 inline text-yellow-400" />;
  };

  const filteredAndSortedEvents = React.useMemo(() => {
    let filteredEvents = events.filter(event =>
        event.title?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        event.eventType?.toLowerCase().includes(searchQuery.toLowerCase())
    );

    if (sortConfig.key) {
      filteredEvents.sort((a, b) => {
        if (a[sortConfig.key] < b[sortConfig.key]) {
          return sortConfig.direction === 'asc' ? -1 : 1;
        }
        if (a[sortConfig.key] > b[sortConfig.key]) {
          return sortConfig.direction === 'asc' ? 1 : -1;
        }
        return 0;
      });
    }

    return filteredEvents;
  }, [events, searchQuery, sortConfig]);

  const getBookTitle = (bookId) => {
    if (!bookId && bookId !== 0) return '-';
    const book = books.find(b => b.id === Number(bookId));
    return book ? book.title : `Книга #${bookId}`;
  };

  if (loading) {
    return (
        <div className="p-4 flex justify-center">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-yellow-500"></div>
        </div>
    );
  }

  return (
      <div className="p-4">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold text-white">Управление мероприятиями</h2>
          <button
              onClick={() => openModal(null)}
              className="px-4 py-2 bg-yellow-500 text-white rounded hover:bg-yellow-600 flex items-center"
          >
            <FaCalendarPlus className="mr-2" /> Добавить мероприятие
          </button>
        </div>

        <div className="mb-4">
          <input
              type="text"
              placeholder="Поиск по названию или типу мероприятия..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-gray-200 placeholder-gray-400"
          />
        </div>

        {filteredAndSortedEvents.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="min-w-full text-white [&_th]:text-center [&_td:not(:first-child)]:text-center">
                <thead className="bg-[#626262]">
                <tr>
                  <th
                      className="px-4 py-2 cursor-pointer"
                      onClick={() => requestSort('title')}
                  >
                    Название {getSortIcon('title')}
                  </th>
                  <th
                      className="px-4 py-2 cursor-pointer"
                      onClick={() => requestSort('eventType')}
                  >
                    Тип {getSortIcon('eventType')}
                  </th>
                  <th
                      className="px-4 py-2 cursor-pointer"
                      onClick={() => requestSort('date')}
                  >
                    Дата {getSortIcon('date')}
                  </th>
                  <th className="px-4 py-2">Книга</th>
                  <th className="px-4 py-2">Статус</th>
                  <th className="px-4 py-2">Действия</th>
                </tr>
                </thead>
                <tbody>
                {filteredAndSortedEvents.map(event => (
                    <tr
                        key={event.id}
                        className={`bg-[#585858] border-t border-gray-700 ${softDeletedEvents[event.id] ? 'opacity-60' : ''}`}
                    >
                      <td className="px-4 py-2">{event.title}</td>
                      <td className="px-4 py-2">
                    <span className={`px-2 py-1 rounded ${event.eventType === 'MEETING' ? 'bg-blue-600' :
                        event.eventType === 'PRESENTATION' ? 'bg-green-600' :
                            event.eventType === 'WORKSHOP' ? 'bg-purple-600' : 'bg-gray-600'}`}>
                      {event.eventType}
                    </span>
                      </td>
                      <td className="px-4 py-2">{event.date}</td>
                      <td className="px-4 py-2">{getBookTitle(event.bookId)}</td>
                      <td className="px-4 py-2">
                    <span className={`px-2 py-1 rounded ${event.isCancelled ? 'bg-red-600' : 'bg-green-600'}`}>
                      {event.isCancelled ? 'Отменено' : 'Активно'}
                    </span>
                      </td>
                      <td className="px-4 py-2 flex justify-center space-x-2">
                        {!softDeletedEvents[event.id] ? (
                            <>
                              <button
                                  onClick={() => openModal(event)}
                                  className="p-2 bg-gray-600 text-white rounded hover:bg-gray-500"
                                  title="Редактировать"
                              >
                                <FaPen />
                              </button>
                              {!event.isCancelled && (
                                  <>
                                    <button
                                        onClick={() => handleCancelEvent(event.id)}
                                        className="p-2 bg-red-600 text-white rounded hover:bg-red-700"
                                        title="Отменить мероприятие"
                                    >
                                      <FaTimes />
                                    </button>
                                    <button
                                        onClick={() => handleRescheduleEvent(event.id)}
                                        className="p-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                                        title="Перенести мероприятие"
                                    >
                                      <FaCalendarAlt />
                                    </button>
                                  </>
                              )}
                              <button
                                  onClick={() => handleSoftDelete(event.id)}
                                  className="p-2 bg-yellow-500 text-white rounded hover:bg-yellow-600"
                                  title="Удалить"
                              >
                                <FaTrash />
                              </button>
                            </>
                        ) : (
                            <>
                              <button
                                  onClick={() => handleRestore(event.id)}
                                  className="p-2 bg-transparent border border-yellow-400 text-yellow-400 rounded hover:bg-yellow-400 hover:bg-opacity-10"
                                  title="Восстановить"
                              >
                                <FaTrashRestore />
                              </button>
                              <button
                                  onClick={() => handleDelete(event.id)}
                                  className="p-2 bg-red-600 text-white rounded hover:bg-red-700"
                                  title="Удалить навсегда"
                              >
                                <FaTrash />
                              </button>
                            </>
                        )}
                      </td>
                    </tr>
                ))}
                </tbody>
              </table>
            </div>
        ) : (
            <div className="text-center py-10 bg-[#585858] rounded-lg">
              <p className="text-xl text-gray-300">Мероприятия не найдены</p>
              <p className="text-gray-400 mt-2">Добавьте новое мероприятие или измените параметры поиска</p>
            </div>
        )}

        {showModal && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
              <div className="bg-[#585858] p-6 rounded-lg w-full max-w-2xl max-h-[90vh] overflow-y-auto">
                <h3 className="text-xl font-bold mb-4 text-white">
                  {current ? 'Редактировать мероприятие' : 'Добавить мероприятие'}
                </h3>
                <form onSubmit={save} className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-300 mb-1">Название</label>
                    <input
                        type="text"
                        name="title"
                        value={form.title}
                        onChange={handleInputChange}
                        className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                        required
                    />
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-300 mb-1">Тип мероприятия</label>
                      <select
                          name="eventType"
                          value={form.eventType}
                          onChange={handleInputChange}
                          className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                      >
                        {eventTypes.map(type => (
                            <option key={type} value={type}>{type}</option>
                        ))}
                      </select>
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-300 mb-1">Связанная книга</label>
                      <select
                          name="bookId"
                          value={form.bookId}
                          onChange={handleInputChange}
                          className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                      >
                        <option value="">Не выбрано</option>
                        {books.map(book => (
                            <option key={book.id} value={book.id}>
                              {book.title} ({book.author})
                            </option>
                        ))}
                      </select>
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-300 mb-1">Дата и время</label>
                      <input
                          type="datetime-local"
                          name="date"
                          value={form.date}
                          onChange={handleInputChange}
                          className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                          required
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-300 mb-1">Макс. участников</label>
                      <input
                          type="number"
                          name="maxParticipants"
                          value={form.maxParticipants}
                          onChange={handleInputChange}
                          min="1"
                          className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-300 mb-1">Описание</label>
                    <textarea
                        name="description"
                        value={form.description}
                        onChange={handleInputChange}
                        rows="3"
                        className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                    ></textarea>
                  </div>

                  {current && (
                      <>
                        <div className="flex items-center">
                          <input
                              type="checkbox"
                              id="isCancelled"
                              name="isCancelled"
                              checked={form.isCancelled}
                              onChange={handleCheckboxChange}
                              className="h-4 w-4 text-yellow-600 focus:ring-yellow-500 border-gray-300 rounded"
                          />
                          <label htmlFor="isCancelled" className="ml-2 block text-sm text-gray-300">
                            Мероприятие отменено
                          </label>
                        </div>

                        {form.isCancelled && (
                            <div>
                              <label className="block text-sm font-medium text-gray-300 mb-1">Причина отмены</label>
                              <textarea
                                  name="cancellationReason"
                                  value={form.cancellationReason}
                                  onChange={handleInputChange}
                                  rows="2"
                                  className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                                  required
                              ></textarea>
                            </div>
                        )}
                      </>
                  )}

                  <div className="flex justify-end space-x-2 pt-4">
                    <button
                        type="button"
                        onClick={() => setShowModal(false)}
                        className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-500"
                    >
                      Отмена
                    </button>
                    <button
                        type="submit"
                        className="px-4 py-2 bg-yellow-500 text-white rounded hover:bg-yellow-600"
                    >
                      {current ? 'Сохранить' : 'Добавить'}
                    </button>
                  </div>
                </form>
              </div>
            </div>
        )}

        {showCancelModal && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
              <div className="bg-[#585858] p-6 rounded-lg w-full max-w-md">
                <h3 className="text-xl font-bold mb-4 text-white">Отмена мероприятия</h3>
                <p className="text-gray-300 mb-4">
                  Вы собираетесь отменить мероприятие "{current?.title}".
                  Все участники получат уведомление об отмене.
                </p>
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-300 mb-1">Причина отмены</label>
                  <textarea
                      value={cancelReason}
                      onChange={(e) => setCancelReason(e.target.value)}
                      rows="3"
                      className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                      placeholder="Укажите причину отмены мероприятия"
                      required
                  ></textarea>
                </div>
                <div className="flex justify-end space-x-2">
                  <button
                      onClick={() => {
                        setShowCancelModal(false);
                        setCancelReason('');
                      }}
                      className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-500"
                  >
                    Отмена
                  </button>
                  <button
                      onClick={confirmCancelEvent}
                      className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
                  >
                    Подтвердить отмену
                  </button>
                </div>
              </div>
            </div>
        )}

        {showRescheduleModal && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
              <div className="bg-[#585858] p-6 rounded-lg w-full max-w-md">
                <h3 className="text-xl font-bold mb-4 text-white">Перенос мероприятия</h3>
                <p className="text-gray-300 mb-4">
                  Вы собираетесь перенести мероприятие "{current?.title}".
                  Все участники получат уведомление о переносе.
                </p>
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-300 mb-1">Новая дата и время</label>
                  <input
                      type="datetime-local"
                      value={rescheduleData.newDate}
                      onChange={(e) => setRescheduleData({ ...rescheduleData, newDate: e.target.value })}
                      className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                      required
                  />
                </div>
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-300 mb-1">Причина переноса</label>
                  <textarea
                      value={rescheduleData.reason}
                      onChange={(e) => setRescheduleData({ ...rescheduleData, reason: e.target.value })}
                      rows="3"
                      className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                      placeholder="Укажите причину переноса мероприятия"
                      required
                  ></textarea>
                </div>
                <div className="flex justify-end space-x-2">
                  <button
                      onClick={() => {
                        setShowRescheduleModal(false);
                        setRescheduleData({ newDate: '', reason: '' });
                      }}
                      className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-500"
                  >
                    Отмена
                  </button>
                  <button
                      onClick={confirmRescheduleEvent}
                      className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                  >
                    Подтвердить перенос
                  </button>
                </div>
              </div>
            </div>
        )}
      </div>
  );
};

export default EventManager;