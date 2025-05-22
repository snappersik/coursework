import React, { useState, useEffect, useMemo, useCallback } from 'react'; // Added useCallback
import axios from 'axios';
import { toast } from 'react-toastify';
import { API_URL } from '../../../config';
import { FaSort, FaSortAlphaDown, FaSortAlphaUp, FaSearch, FaFilter, FaTimes, FaCheck, FaBan, FaEye, FaChevronLeft, FaChevronRight, FaCheckCircle, FaTimesCircle } from 'react-icons/fa';
import { format, parseISO, isValid } from 'date-fns';
import { ru } from 'date-fns/locale';


const EventApplicationManager = () => {
  const [applications, setApplications] = useState([]);
  const [events, setEvents] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [currentApplication, setCurrentApplication] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [sortConfig, setSortConfig] = useState({ key: null, direction: 'asc' });
  const [filters, setFilters] = useState({
    eventId: '',
    userEmail: '',
    status: ''
  });
  const [pagination, setPagination] = useState({
    page: 0,
    size: 20,
    totalPages: 0
  });

  const formatApplicationDate = (dateString) => {
    if (!dateString) {
      return 'N/A';
    }
    try {
      let parsableDateString = String(dateString);
      // Попытка преобразовать "YYYY-MM-DD HH:mm:ss.SSS" в "YYYY-MM-DDTHH:mm:ss.SSS"
      if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}(\.\d+)?$/.test(parsableDateString)) {
        parsableDateString = parsableDateString.replace(' ', 'T');
      }
      // Попытка преобразовать "ДД.ММ.ГГГГ ЧЧ:мм" в "YYYY-MM-DDTHH:mm" (если такой формат приходит)
      else if (/^\d{2}\.\d{2}\.\d{4} \d{2}:\d{2}(:\d{2})?(\.\d+)?$/.test(parsableDateString)) {
        const parts = parsableDateString.split(' ');
        const dateParts = parts[0].split('.');
        const timePart = parts.length > 1 ? parts[1] : "00:00";
        parsableDateString = `${dateParts[2]}-${dateParts[1]}-${dateParts[0]}T${timePart}`;
      }

      const date = parseISO(parsableDateString);
      if (isValid(date)) {
        return format(date, 'dd.MM.yyyy HH:mm', { locale: ru });
      }
      console.warn("Could not parse application date with parseISO after transformations:", dateString, "->", parsableDateString);
      return 'Invalid Date';
    } catch (error) {
      console.error("Error formatting application date:", dateString, error);
      return 'Ошибка даты';
    }
  };

  // Memoized fetch functions
  const fetchApplicationsInternal = useCallback(async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams({
        page: pagination.page,
        size: pagination.size
      });
      if (filters.eventId) params.append('eventId', filters.eventId);
      if (filters.userEmail) params.append('userEmail', filters.userEmail);
      if (filters.status) params.append('status', filters.status);

      const response = await axios.get(`${API_URL}/event-applications/paginated?${params.toString()}`, {
        withCredentials: true
      });

      setApplications(Array.isArray(response.data.content) ? response.data.content : []);
      setPagination(prev => ({
        ...prev,
        totalPages: response.data.totalPages || 0
      }));
    } catch (error) {
      console.error('Ошибка загрузки заявок:', error);
      toast.error('Не удалось загрузить заявки на мероприятия');
      setApplications([]); // Ensure it's an array on error
    } finally {
      setLoading(false);
    }
  }, [pagination.page, pagination.size, filters.eventId, filters.userEmail, filters.status]);

  const fetchEventsInternal = useCallback(async () => {
    try {
      const response = await axios.get(`${API_URL}/events/paginated?page=0&size=1000`, { // Fetch more events for dropdown
        withCredentials: true
      });
      setEvents(Array.isArray(response.data.content) ? response.data.content : []);
    } catch (error) {
      console.error('Ошибка загрузки мероприятий:', error);
      setEvents([]); // Ensure it's an array on error
    }
  }, []);

  const fetchUsersInternal = useCallback(async () => {
    try {
      const response = await axios.get(`${API_URL}/users`, { // Assuming this fetches all users for names
        withCredentials: true
      });
      setUsers(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error('Ошибка загрузки пользователей:', error);
      setUsers([]); // Ensure it's an array on error
    }
  }, []);

  useEffect(() => {
    fetchApplicationsInternal();
    fetchEventsInternal();
    fetchUsersInternal();
  }, [fetchApplicationsInternal, fetchEventsInternal, fetchUsersInternal]); // Use memoized functions as dependencies

  // Memoized helper functions for useMemo
  const getEventTitle = useCallback((eventId) => {
    if (!eventId || !Array.isArray(events)) return '-';
    const event = events.find(e => e.id === eventId);
    return event ? event.title : `Мероприятие #${eventId}`;
  }, [events]);

  const getUserName = useCallback((userId) => {
    if (!userId || !Array.isArray(users)) return '-';
    const user = users.find(u => u.id === userId);
    return user ? `${user.firstName || ''} ${user.lastName || ''} (${user.email || ''})` : `Пользователь #${userId}`;
  }, [users]);

  // Фильтрация и сортировка заявок
  const filteredAndSortedApplications = useMemo(() => {
    const baseApplications = Array.isArray(applications) ? applications : [];
    let filtered = [...baseApplications];

    if (searchQuery) {
      try {
        filtered = filtered.filter(app => {
          if (!app || typeof app !== 'object') return false;
          const eventTitle = getEventTitle(app.eventId) || '';
          const userName = getUserName(app.userId) || '';
          const query = searchQuery.toLowerCase();
          return eventTitle.toLowerCase().includes(query) || userName.toLowerCase().includes(query);
        });
      } catch (e) {
        console.error("Error during filtering:", e);
        return [...baseApplications]; // Fallback
      }
    }

    if (sortConfig.key) {
      try {
        filtered.sort((a, b) => {
          if (!a || !b) return 0;
          let aValue, bValue;

          if (sortConfig.key === 'eventTitle') {
            aValue = getEventTitle(a.eventId);
            bValue = getEventTitle(b.eventId);
          } else if (sortConfig.key === 'userName') {
            aValue = getUserName(a.userId);
            bValue = getUserName(b.userId);
          } else {
            aValue = a[sortConfig.key];
            bValue = b[sortConfig.key];
          }

          if (aValue === bValue) return 0;
          if (aValue == null) return sortConfig.direction === 'asc' ? -1 : 1;
          if (bValue == null) return sortConfig.direction === 'asc' ? 1 : -1;

          if (typeof aValue === 'string' && typeof bValue === 'string') {
            aValue = aValue.toLowerCase();
            bValue = bValue.toLowerCase();
          }

          if (aValue < bValue) return sortConfig.direction === 'asc' ? -1 : 1;
          return sortConfig.direction === 'asc' ? 1 : -1;
        });
      } catch (e) {
        console.error("Error during sorting:", e);
        // 'filtered' might be partially sorted; returning it as is.
      }
    }
    return filtered;
  }, [applications, searchQuery, sortConfig, getEventTitle, getUserName]);


  const handleApproveApplication = async (applicationId) => {
    try {
      await axios.put(`${API_URL}/event-applications/${applicationId}/approve`, {}, { withCredentials: true });
      toast.success('Заявка одобрена');
      fetchApplicationsInternal(); // Re-fetch
    } catch (error) {
      console.error('Ошибка одобрения заявки:', error);
      toast.error(error.response?.data?.message || 'Не удалось одобрить заявку');
    }
  };

  const handleRejectApplication = async (applicationId) => {
    try {
      await axios.put(`${API_URL}/event-applications/${applicationId}/reject`, {}, { withCredentials: true });
      toast.success('Заявка отклонена');
      fetchApplicationsInternal(); // Re-fetch
    } catch (error) {
      console.error('Ошибка отклонения заявки:', error);
      toast.error(error.response?.data?.message || 'Не удалось отклонить заявку');
    }
  };

  const handleMarkAttended = async (applicationId) => {
    try {
      await axios.put(`${API_URL}/event-applications/${applicationId}/mark-attended`, {}, { withCredentials: true });
      toast.success('Участие отмечено');
      fetchApplicationsInternal(); // Re-fetch
    } catch (error) {
      console.error('Ошибка отметки участия:', error);
      toast.error(error.response?.data?.message || 'Не удалось отметить участие');
    }
  };

  const handleViewDetails = (application) => {
    setCurrentApplication(application);
    setShowDetailsModal(true);
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({ ...prev, [name]: value }));
  };

  const handleApplyFilters = () => {
    setPagination(prev => ({ ...prev, page: 0 })); // Reset to first page on filter apply
    fetchApplicationsInternal(); // This will use the new filters and reset page
  };

  const requestSort = (key) => {
    let direction = 'asc';
    if (sortConfig.key === key && sortConfig.direction === 'asc') {
      direction = 'desc';
    }
    setSortConfig({ key, direction });
  };

  const getSortIcon = (key) => {
    if (sortConfig.key !== key) return <FaSort className="inline ml-1 text-gray-400" />;
    if (sortConfig.direction === 'asc') return <FaSortAlphaUp className="inline ml-1 text-yellow-500" />;
    return <FaSortAlphaDown className="inline ml-1 text-yellow-500" />;
  };

  const translateApplicationStatus = (status) => {
    if (!status) { // Если статус null, undefined или пустая строка
      return 'N/A'; // Возвращаем "N/A" или "Неизвестно"
    }
    const translations = {
      'PENDING': 'На рассмотрении',
      'APPROVED': 'Одобрена',
      'REJECTED': 'Отклонена',
      'CANCELLED': 'Отменена'
    };
    return translations[status] || status; // Если перевода нет, показываем сам статус
  };


  if (loading && applications.length === 0) { // Show loader only if applications are not yet loaded
    return (
      <div className="p-6 bg-gray-800 text-white min-h-screen">
        <div className="text-center py-10">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-yellow-500 mx-auto mb-4"></div>
          Загрузка заявок...
        </div>
      </div>
    );
  }

  return (
    <div className="p-4 md:p-6 bg-gray-800 text-white min-h-screen">
      <h1 className="text-2xl md:text-3xl font-semibold mb-6 text-yellow-500">Управление Заявками на Мероприятия</h1>

      {/* Фильтры */}
      <div className="mb-6 p-4 bg-gray-700 rounded-lg shadow-md">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 items-end">
          <div>
            <label htmlFor="searchQuery" className="block text-sm font-medium text-gray-300 mb-1">Поиск</label>
            <input
              type="text"
              id="searchQuery"
              placeholder="По названию мероприятия или email"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full px-3 py-2 bg-gray-600 border border-gray-500 rounded-md shadow-sm focus:ring-yellow-500 focus:border-yellow-500"
            />
          </div>
          <div>
            <label htmlFor="eventIdFilter" className="block text-sm font-medium text-gray-300 mb-1">Мероприятие</label>
            <select
              name="eventId"
              id="eventIdFilter"
              value={filters.eventId}
              onChange={handleFilterChange}
              className="w-full px-3 py-2 bg-gray-600 border border-gray-500 rounded-md shadow-sm focus:ring-yellow-500 focus:border-yellow-500"
            >
              <option value="">Все мероприятия</option>
              {Array.isArray(events) && events.map(event => (
                <option key={event.id} value={event.id}>{event.title}</option>
              ))}
            </select>
          </div>
          <div>
            <label htmlFor="statusFilter" className="block text-sm font-medium text-gray-300 mb-1">Статус заявки</label>
            <select
              name="status"
              id="statusFilter"
              value={filters.status}
              onChange={handleFilterChange}
              className="w-full px-3 py-2 bg-gray-600 border border-gray-500 rounded-md shadow-sm focus:ring-yellow-500 focus:border-yellow-500"
            >
              <option value="">Все статусы</option>
              <option value="PENDING">На рассмотрении</option>
              <option value="APPROVED">Одобрена</option>
              <option value="REJECTED">Отклонена</option>
              <option value="CANCELLED">Отменена</option>
            </select>
          </div>
          <button
            onClick={handleApplyFilters}
            className="self-end px-4 py-2 bg-yellow-600 hover:bg-yellow-700 text-white font-semibold rounded-md shadow-md transition duration-150 ease-in-out flex items-center justify-center"
          >
            <FaFilter className="mr-2" />Применить
          </button>
        </div>
      </div>

      <div className="overflow-x-auto shadow-xl rounded-lg">
        <table className="min-w-full divide-y divide-gray-700">
          <thead className="bg-gray-750">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider cursor-pointer" onClick={() => requestSort('id')}>ID {getSortIcon('id')}</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider cursor-pointer" onClick={() => requestSort('eventTitle')}>Мероприятие {getSortIcon('eventTitle')}</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider cursor-pointer" onClick={() => requestSort('userName')}>Пользователь {getSortIcon('userName')}</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider cursor-pointer" onClick={() => requestSort('submissionDate')}>Дата подачи {getSortIcon('submissionDate')}</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider cursor-pointer" onClick={() => requestSort('status')}>Статус {getSortIcon('status')}</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Участие</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Действия</th>
            </tr>
          </thead>
          <tbody className="bg-gray-800 divide-y divide-gray-700">
            {(filteredAndSortedApplications || []).map(application => (
              <tr key={application.id} className="hover:bg-gray-700 transition duration-150">
                <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-100">{application.id}</td>
                <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-300">{getEventTitle(application.eventId)}</td>
                <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-300">{getUserName(application.userId)}</td>
                <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-300">
                  {application.submissionDate ? new Date(application.submissionDate).toLocaleDateString('ru-RU') : '-'}
                </td>
                <td className="px-4 py-3 whitespace-nowrap text-sm">
                  <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${application.status === 'APPROVED' ? 'bg-green-500 text-green-100' :
                    application.status === 'REJECTED' ? 'bg-red-500 text-red-100' :
                      application.status === 'PENDING' ? 'bg-yellow-500 text-yellow-100' :
                        'bg-gray-500 text-gray-100'
                    }`}>
                    {translateApplicationStatus(application.status)}
                  </span>
                </td>
                <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-300">
                  {application.attended ? <FaCheck className="text-green-500" /> : <FaTimes className="text-red-500" />}
                </td>
                <td className="px-4 py-3 whitespace-nowrap text-sm font-medium space-x-2">
                  <button onClick={() => handleViewDetails(application)} className="text-blue-400 hover:text-blue-300" title="Просмотр"><FaEye /></button>
                  {application.status === 'PENDING' && (
                    <>
                      <button onClick={() => handleApproveApplication(application.id)} className="text-green-400 hover:text-green-300" title="Одобрить"><FaCheck /></button>
                      <button onClick={() => handleRejectApplication(application.id)} className="text-red-400 hover:text-red-300" title="Отклонить"><FaBan /></button>
                    </>
                  )}
                  {application.status === 'APPROVED' && !application.attended && (
                    <button onClick={() => handleMarkAttended(application.id)} className="text-purple-400 hover:text-purple-300" title="Отметить участие">
                      <FaUserCheck /> {/* Иконка для отметки участия */}
                    </button>
                  )}
                </td>
              </tr>
            ))}
            {(filteredAndSortedApplications || []).length === 0 && (
              <tr>
                <td colSpan="7" className="px-4 py-3 text-center text-sm text-gray-400">Заявки не найдены.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Пагинация */}
      {pagination.totalPages > 1 && (
        <div className="mt-6 flex justify-center items-center space-x-3">
          <button
            onClick={() => setPagination(prev => ({ ...prev, page: Math.max(0, prev.page - 1) }))}
            disabled={pagination.page === 0}
            className="px-4 py-2 bg-gray-600 hover:bg-gray-500 text-white rounded-md disabled:opacity-50 flex items-center"
          >
            <FaChevronLeft className="mr-2 h-4 w-4" /> Пред.
          </button>
          <span className="text-gray-300">
            Стр. {pagination.page + 1} из {pagination.totalPages}
          </span>
          <button
            onClick={() => setPagination(prev => ({ ...prev, page: Math.min(pagination.totalPages - 1, prev.page + 1) }))}
            disabled={pagination.page >= pagination.totalPages - 1}
            className="px-4 py-2 bg-gray-600 hover:bg-gray-500 text-white rounded-md disabled:opacity-50 flex items-center"
          >
            След. <FaChevronRight className="ml-2 h-4 w-4" />
          </button>
        </div>
      )}

      {/* Модальное окно деталей (предполагается, что оно есть) */}
      {showDetailsModal && currentApplication && (
        <div className="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50 p-4">
          <div className="bg-gray-800 p-6 rounded-xl shadow-2xl w-full max-w-md max-h-[80vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-xl font-bold text-yellow-500">Детали Заявки #{currentApplication.id}</h3>
              <button onClick={() => setShowDetailsModal(false)} className="text-gray-400 hover:text-white">
                <FaTimes size={20} />
              </button>
            </div>
            <div className="space-y-3 text-sm">
              <p><strong className="text-gray-400">Мероприятие:</strong> {getEventTitle(currentApplication.eventId)}</p>
              <p><strong className="text-gray-400">Пользователь:</strong> {getUserName(currentApplication.userId)}</p>
              <p><strong className="text-gray-400">Дата подачи:</strong> {new Date(currentApplication.submissionDate).toLocaleString('ru-RU')}</p>
              <p><strong className="text-gray-400">Статус:</strong> {translateApplicationStatus(currentApplication.status)}</p>
              <p><strong className="text-gray-400">Участие отмечено:</strong> {currentApplication.attended ? 'Да' : 'Нет'}</p>
              {currentApplication.rejectionReason && <p><strong className="text-gray-400">Причина отклонения:</strong> {currentApplication.rejectionReason}</p>}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default EventApplicationManager;
