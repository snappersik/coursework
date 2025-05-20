import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import axios from 'axios';
import { API_URL } from '../../../config';
import { FaSearch, FaTimes, FaFilter, FaChevronLeft, FaChevronRight, FaEye } from 'react-icons/fa';

// Модальное окно для просмотра деталей
const DetailModal = ({ isOpen, onClose, title, content, actionType, resourceType, timestamp, userEmail }) => {
  if (!isOpen) return null;

  // Функция для форматирования JSON в читаемый вид
  const formatJsonContent = (content) => {
    try {
      // Пытаемся распарсить JSON
      const parsedContent = JSON.parse(content);
      return JSON.stringify(parsedContent, null, 2);
    } catch (e) {
      // Если это не JSON, просто возвращаем как есть
      return content;
    }
  };

  // Извлекаем ключевую информацию из деталей
  const extractKeyInfo = () => {
    try {
      if (content.includes('Class:') && content.includes('Method:')) {
        const classMatch = content.match(/Class: ([^,]+)/);
        const methodMatch = content.match(/Method: ([^,]+)/);
        const argsMatch = content.match(/Args: ([^\]]+\])/);

        return {
          className: classMatch ? classMatch[1].trim() : 'Неизвестный класс',
          methodName: methodMatch ? methodMatch[1].trim() : 'Неизвестный метод',
          args: argsMatch ? argsMatch[1].trim() : 'Нет аргументов'
        };
      }
      return null;
    } catch (e) {
      return null;
    }
  };

  const keyInfo = extractKeyInfo();

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-gray-800 p-6 rounded-lg shadow-xl max-w-3xl w-full max-h-[80vh] overflow-y-auto">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-xl font-semibold text-white">{title}</h3>
          <button onClick={onClose} className="text-gray-400 hover:text-white">
            <FaTimes size={20} />
          </button>
        </div>

        {/* Информация о действии */}
        <div className="mb-4 grid grid-cols-2 gap-4">
          <div className="bg-gray-700 p-3 rounded">
            <p className="text-gray-400 text-sm">Тип действия</p>
            <p className="text-white font-medium">{actionType || 'Не указано'}</p>
          </div>
          <div className="bg-gray-700 p-3 rounded">
            <p className="text-gray-400 text-sm">Ресурс</p>
            <p className="text-white font-medium">{resourceType || 'Не указано'}</p>
          </div>
          <div className="bg-gray-700 p-3 rounded">
            <p className="text-gray-400 text-sm">Время</p>
            <p className="text-white font-medium">{new Date(timestamp).toLocaleString()}</p>
          </div>
          <div className="bg-gray-700 p-3 rounded">
            <p className="text-gray-400 text-sm">Пользователь</p>
            <p className="text-white font-medium">{userEmail || 'Не указано'}</p>
          </div>
        </div>

        {/* Ключевая информация, если доступна */}
        {keyInfo && (
          <div className="mb-4 bg-gray-700 p-4 rounded">
            <h4 className="text-lg font-medium text-white mb-2">Ключевая информация</h4>
            <div className="grid grid-cols-1 gap-2">
              <div>
                <span className="text-gray-400">Класс:</span> <span className="text-white">{keyInfo.className}</span>
              </div>
              <div>
                <span className="text-gray-400">Метод:</span> <span className="text-white">{keyInfo.methodName}</span>
              </div>
              <div>
                <span className="text-gray-400">Аргументы:</span> <span className="text-white">{keyInfo.args}</span>
              </div>
            </div>
          </div>
        )}

        {/* Полные детали */}
        <div>
          <h4 className="text-lg font-medium text-white mb-2">Полные детали</h4>
          <div className="bg-gray-700 p-4 rounded text-sm text-gray-200 whitespace-pre-wrap break-all overflow-auto max-h-60">
            <pre className="whitespace-pre-wrap break-all">{formatJsonContent(content)}</pre>
          </div>
        </div>
      </div>
    </div>
  );
};

const ActionManage = () => {
  const [auditEntries, setAuditEntries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);
  const [filter, setFilter] = useState({
    actionType: '',
    userEmail: '',
    dateFrom: '',
    dateTo: ''
  });

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedEntry, setSelectedEntry] = useState(null);

  // Функция для извлечения краткой информации из деталей
  const extractSummary = (details) => {
    if (!details) return 'Нет деталей';

    // Если это информация о контроллере/методе
    if (details.includes('Class:') && details.includes('Method:')) {
      const classMatch = details.match(/Class: ([^,]+)/);
      const methodMatch = details.match(/Method: ([^,]+)/);

      if (classMatch && methodMatch) {
        return `${classMatch[1].trim()} → ${methodMatch[1].trim()}`;
      }
    }

    // Ограничиваем длину текста
    return details.length > 50 ? details.substring(0, 50) + '...' : details;
  };

  const handleViewDetails = (entry) => {
    setSelectedEntry(entry);
    setIsModalOpen(true);
  };

  const formatDisplayAction = (actionType, resourceType, resourceId, details) => {
    let actionStr = actionType;

    const actionTypeMap = {
      'CREATE': 'Создание',
      'UPDATE': 'Обновление',
      'DELETE': 'Удаление',
      'LOGIN': 'Вход',
      'LOGOUT': 'Выход',
      'CANCEL': 'Отмена',
      'RESCHEDULE': 'Перенос',
      'VIEW': 'Просмотр',
      'OPERATION': 'Операция',
      'REGISTER': 'Регистрация'
    };

    const resourceTypeMap = {
      'BOOK': 'книги',
      'USER': 'пользователя',
      'ORDER': 'заказа',
      'EVENT': 'мероприятия',
      'EVENT_APPLICATION': 'заявки на мероприятие',
      'CART': 'корзины',
      'SLIDER': 'слайдера',
      'PRODUCT': 'товара',
      'AUTH': 'аутентификации'
    };

    const translatedActionType = actionTypeMap[actionType?.toUpperCase()] || actionType;
    const translatedResourceType = resourceTypeMap[resourceType?.toUpperCase()] || resourceType;

    if (actionType?.toUpperCase() === 'LOGIN' && !resourceType) {
      return `Вход пользователя`;
    }
    if (actionType?.toUpperCase() === 'LOGOUT' && !resourceType) {
      return `Выход пользователя`;
    }

    actionStr = translatedActionType;
    if (translatedResourceType) {
      actionStr += ` ${translatedResourceType}`;
    }
    if (resourceId) {
      actionStr += ` #${resourceId}`;
    }

    return actionStr;
  };

  const fetchAuditEntries = async (currentPage = page, currentSize = size, currentFilter = filter) => {
    try {
      setLoading(true);
      const params = new URLSearchParams({
        page: currentPage,
        size: currentSize
      });

      if (currentFilter.actionType) params.append('actionType', currentFilter.actionType);
      if (currentFilter.userEmail) params.append('userEmail', currentFilter.userEmail);
      if (currentFilter.dateFrom) params.append('dateFrom', currentFilter.dateFrom + 'T00:00:00');
      if (currentFilter.dateTo) params.append('dateTo', currentFilter.dateTo + 'T23:59:59');

      const response = await axios.get(`${API_URL}/audit?${params.toString()}`, {
        withCredentials: true
      });

      setAuditEntries(response.data.content);
      setTotalPages(response.data.totalPages);
      setLoading(false);
    } catch (error) {
      console.error('Ошибка загрузки данных аудита:', error);
      toast.error(error.response?.data?.message || error.message || 'Не удалось загрузить данные аудита');
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAuditEntries(page, size, filter);
  }, [page, size]);

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilter(prev => ({ ...prev, [name]: value }));
  };

  const handleFilterSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    fetchAuditEntries(0, size, filter);
  };

  const handleClearFilter = () => {
    const newFilter = { actionType: '', userEmail: '', dateFrom: '', dateTo: '' };
    setFilter(newFilter);
    setPage(0);
    fetchAuditEntries(0, size, newFilter);
  };

  const actionTypesForFilter = [
    { value: '', label: 'Все действия' },
    { value: 'CREATE', label: 'Создание' },
    { value: 'UPDATE', label: 'Обновление' },
    { value: 'DELETE', label: 'Удаление' },
    { value: 'LOGIN', label: 'Вход' },
    { value: 'LOGOUT', label: 'Выход' },
    { value: 'CANCEL', label: 'Отмена' },
    { value: 'RESCHEDULE', label: 'Перенос' },
    { value: 'VIEW', label: 'Просмотр' },
    { value: 'OPERATION', label: 'Операция' },
    { value: 'REGISTER', label: 'Регистрация' }
  ];

  return (
    <div className="p-4 text-white">
      <h1 className="text-2xl font-semibold mb-6">Отслеживание Активности</h1>

      <form onSubmit={handleFilterSubmit} className="mb-6 p-4 bg-gray-700 rounded-lg shadow">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 items-end">
          <div>
            <label htmlFor="actionTypeFilter" className="block text-sm font-medium text-gray-300">Тип действия</label>
            <select
              name="actionType"
              id="actionTypeFilter"
              value={filter.actionType}
              onChange={handleFilterChange}
              className="mt-1 block w-full px-3 py-2 bg-gray-600 border border-gray-500 rounded-md text-sm shadow-sm placeholder-gray-400 focus:outline-none focus:border-yellow-500 focus:ring-1 focus:ring-yellow-500"
            >
              {actionTypesForFilter.map(at => <option key={at.value} value={at.value}>{at.label}</option>)}
            </select>
          </div>
          <div>
            <label htmlFor="userEmail" className="block text-sm font-medium text-gray-300">Email пользователя</label>
            <input
              type="text"
              name="userEmail"
              id="userEmail"
              value={filter.userEmail}
              onChange={handleFilterChange}
              placeholder="user@example.com"
              className="mt-1 block w-full px-3 py-2 bg-gray-600 border border-gray-500 rounded-md text-sm shadow-sm placeholder-gray-400 focus:outline-none focus:border-yellow-500 focus:ring-1 focus:ring-yellow-500"
            />
          </div>
          <div>
            <label htmlFor="dateFrom" className="block text-sm font-medium text-gray-300">Дата С</label>
            <input
              type="date"
              name="dateFrom"
              id="dateFrom"
              value={filter.dateFrom}
              onChange={handleFilterChange}
              className="mt-1 block w-full px-3 py-2 bg-gray-600 border border-gray-500 rounded-md text-sm shadow-sm placeholder-gray-400 focus:outline-none focus:border-yellow-500 focus:ring-1 focus:ring-yellow-500"
            />
          </div>
          <div>
            <label htmlFor="dateTo" className="block text-sm font-medium text-gray-300">Дата По</label>
            <input
              type="date"
              name="dateTo"
              id="dateTo"
              value={filter.dateTo}
              onChange={handleFilterChange}
              className="mt-1 block w-full px-3 py-2 bg-gray-600 border border-gray-500 rounded-md text-sm shadow-sm placeholder-gray-400 focus:outline-none focus:border-yellow-500 focus:ring-1 focus:ring-yellow-500"
            />
          </div>
        </div>
        <div className="mt-4 flex space-x-2">
          <button
            type="submit"
            className="px-4 py-2 bg-yellow-600 hover:bg-yellow-700 text-white font-medium rounded-md shadow-sm flex items-center"
          >
            <FaFilter className="mr-2" />Применить фильтр
          </button>
          <button
            type="button"
            onClick={handleClearFilter}
            className="px-4 py-2 bg-gray-500 hover:bg-gray-400 text-white font-medium rounded-md shadow-sm flex items-center"
          >
            <FaTimes className="mr-2" />Очистить фильтр
          </button>
        </div>
      </form>

      {loading ? (
        <p className="text-center">Загрузка данных аудита...</p>
      ) : (
        <>
          <div className="overflow-x-auto bg-gray-700 rounded-lg shadow">
            <table className="min-w-full divide-y divide-gray-600">
              <thead className="bg-gray-750">
                <tr>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Действие</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Пользователь</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Время</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Краткая информация</th>
                  <th scope="col" className="px-6 py-3 text-center text-xs font-medium text-gray-300 uppercase tracking-wider">Просмотр</th>
                </tr>
              </thead>
              <tbody className="bg-gray-700 divide-y divide-gray-600">
                {auditEntries.map(entry => (
                  <tr key={entry.id} className="hover:bg-gray-600">
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-200">{formatDisplayAction(entry.actionType, entry.resourceType, entry.resourceId, entry.details)}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-300">{entry.userEmail || '-'}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-300">{new Date(entry.timestamp).toLocaleString()}</td>
                    <td className="px-6 py-4 whitespace-normal text-sm text-gray-300 break-all max-w-xs">{extractSummary(entry.details)}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-center">
                      <button
                        onClick={() => handleViewDetails(entry)}
                        className="inline-flex items-center justify-center p-2 bg-blue-600 rounded-full hover:bg-blue-700 transition-colors"
                        title="Просмотреть детали"
                      >
                        <FaEye size={16} className="text-white" />
                      </button>
                    </td>
                  </tr>
                ))}
                {auditEntries.length === 0 && (
                  <tr>
                    <td colSpan="5" className="px-6 py-4 text-center text-sm text-gray-400">Записи аудита не найдены.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          {totalPages > 1 && (
            <div className="mt-6 flex justify-center items-center space-x-2">
              <button
                onClick={() => setPage(prev => Math.max(0, prev - 1))}
                disabled={page === 0}
                className="px-4 py-2 bg-gray-600 hover:bg-gray-500 text-white rounded-md disabled:opacity-50 flex items-center"
              >
                <FaChevronLeft className="mr-1" /> Назад
              </button>
              <span className="text-gray-300">Страница {page + 1} из {totalPages}</span>
              <button
                onClick={() => setPage(prev => Math.min(totalPages - 1, prev + 1))}
                disabled={page === totalPages - 1 || totalPages === 0}
                className="px-4 py-2 bg-gray-600 hover:bg-gray-500 text-white rounded-md disabled:opacity-50 flex items-center"
              >
                Вперед <FaChevronRight className="ml-1" />
              </button>
            </div>
          )}
        </>
      )}

      {selectedEntry && (
        <DetailModal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          title="Полные детали действия"
          content={selectedEntry.details}
          actionType={selectedEntry.actionType}
          resourceType={selectedEntry.resourceType}
          timestamp={selectedEntry.timestamp}
          userEmail={selectedEntry.userEmail}
        />
      )}
    </div>
  );
};

export default ActionManage;
