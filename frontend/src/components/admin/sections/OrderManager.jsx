import React, { useState, useEffect, useMemo } from 'react';
import axios from 'axios';
import { toast } from 'react-toastify';
import { API_URL } from '../../../config';
import { FaSort, FaSortAlphaDown, FaSortAlphaUp, FaEye, FaShippingFast, FaCheckCircle, FaTimesCircle, FaBoxOpen, FaEdit } from 'react-icons/fa';
import OrderDetailsModal from '../../modals/OrderDetailsModal';
import { format, parseISO, isValid } from 'date-fns'; // Убедись, что isValid импортирован
import { ru } from 'date-fns/locale';

const OrderManager = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [sortConfig, setSortConfig] = useState({ key: 'createdWhen', direction: 'desc' });
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showDetails, setShowDetails] = useState(false);
  const [editingOrderId, setEditingOrderId] = useState(null);
  const [newStatus, setNewStatus] = useState('');

  const orderStatuses = ["PENDING", "COMPLETED", "CANCELLED"];

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${API_URL}/orders/all`, {
        withCredentials: true,
      });
      setOrders(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error('Ошибка загрузки заказов:', error);
      toast.error(error.response?.data?.message || 'Не удалось загрузить заказы');
      setOrders([]);
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (orderId, statusToSet) => {
    try {
      const response = await axios.patch(`${API_URL}/orders/${orderId}/status?status=${statusToSet.toUpperCase()}`, {}, {
        withCredentials: true,
      });
      if (response.status === 200) {
        toast.success('Статус заказа обновлён');
        fetchOrders();
        setEditingOrderId(null);
      }
    } catch (error) {
      console.error('Ошибка обновления статуса:', error);
      toast.error(error.response?.data?.message || 'Не удалось обновить статус заказа');
    }
  };

  const startEditStatus = (order) => {
    setEditingOrderId(order.id);
    setNewStatus(order.orderStatus);
  };
  
  const formatDateForOrderManager = (dateString) => {
    if (!dateString) {
      return 'N/A';
    }
    try {
      let parsableDateString = String(dateString);
      // Попытка преобразовать "YYYY-MM-DD HH:mm:ss.SSS" в "YYYY-MM-DDTHH:mm:ss.SSS"
      if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}(\.\d+)?$/.test(parsableDateString)) {
        parsableDateString = parsableDateString.replace(' ', 'T');
      }
      // Попытка преобразовать "ДД.ММ.ГГГГ ЧЧ:мм" в "YYYY-MM-DDTHH:mm"
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
      console.warn("Could not parse date for OrderManager with parseISO after transformations:", dateString, "->", parsableDateString);
      return 'Неверная дата';
    } catch (error) {
      console.error("Error formatting date in OrderManager:", dateString, error);
      return 'Ошибка даты';
    }
  };

  const filteredAndSortedOrders = useMemo(() => {
    let sortableOrders = [...orders];
    if (searchQuery) {
      sortableOrders = sortableOrders.filter(order =>
        order.id.toString().includes(searchQuery) ||
        (order.userEmail && order.userEmail.toLowerCase().includes(searchQuery.toLowerCase())) ||
        (order.orderStatus && order.orderStatus.toLowerCase().includes(searchQuery.toLowerCase()))
      );
    }

    if (sortConfig.key !== null) {
      sortableOrders.sort((a, b) => {
        let aValue = a[sortConfig.key];
        let bValue = b[sortConfig.key];

        if (sortConfig.key === 'userEmail' && a.user && b.user) {
            aValue = a.user.email;
            bValue = b.user.email;
        } else if (sortConfig.key === 'userEmail') {
            aValue = a.userEmail;
            bValue = b.userEmail;
        }

        if (aValue === null || aValue === undefined) aValue = '';
        if (bValue === null || bValue === undefined) bValue = '';

        if (typeof aValue === 'string' && typeof bValue === 'string') {
            return sortConfig.direction === 'asc' ? aValue.localeCompare(bValue) : bValue.localeCompare(aValue);
        } else {
            if (aValue < bValue) return sortConfig.direction === 'asc' ? -1 : 1;
            if (aValue > bValue) return sortConfig.direction === 'asc' ? 1 : -1;
            return 0;
        }
      });
    }
    return sortableOrders;
  }, [orders, searchQuery, sortConfig]);

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
  
  const getStatusDisplay = (status) => {
    const statuses = {
      PENDING: { text: "В обработке", icon: <FaBoxOpen className="mr-1"/>, color: "bg-yellow-600" },
      COMPLETED: { text: "Завершен", icon: <FaCheckCircle className="mr-1"/>, color: "bg-green-600" },
      CANCELLED: { text: "Отменен", icon: <FaTimesCircle className="mr-1"/>, color: "bg-red-600" },
      PROCESSING: { text: "Собирается", icon: <FaShippingFast className="mr-1"/>, color: "bg-blue-600" },
      SHIPPED: { text: "Отправлен", icon: <FaShippingFast className="mr-1"/>, color: "bg-teal-600" },
      DELIVERED: { text: "Доставлен", icon: <FaCheckCircle className="mr-1"/>, color: "bg-emerald-600" }
    };
    return statuses[status] || { text: status, icon: <FaBoxOpen className="mr-1"/>, color: "bg-gray-500"};
  };

  return (
    <div className="p-4 bg-[#313131] rounded-lg shadow-xl text-white">
      <h1 className="text-2xl font-bold mb-6 text-yellow-400">Управление Заказами</h1>
      <div className="mb-4">
        <input
          type="text"
          placeholder="Поиск по ID, Email, Статусу..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="w-full p-2 rounded bg-[#424242] border border-gray-600 focus:border-yellow-500"
        />
      </div>
      {loading ? (
        <div className="text-center py-10">
            <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-yellow-500 mx-auto"></div>
            <p className="mt-4">Загрузка заказов...</p>
        </div>
      ) : filteredAndSortedOrders.length === 0 ? (
         <div className="text-center py-10 bg-[#424242] rounded-lg">
          <p className="text-xl text-gray-300">Заказы не найдены</p>
          <p className="text-gray-400 mt-2">Измените параметры поиска или еще нет заказов.</p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="min-w-full text-sm">
            <thead className="bg-[#4a4a4a] text-gray-300">
              <tr>
                <th className="px-4 py-2 text-left cursor-pointer" onClick={() => requestSort('userEmail')}>Email Клиента {getSortIcon('userEmail')}</th>
                <th className="px-4 py-2 text-left cursor-pointer" onClick={() => requestSort('createdWhen')}>Дата {getSortIcon('createdWhen')}</th>
                <th className="px-4 py-2 text-right cursor-pointer" onClick={() => requestSort('total')}>Сумма {getSortIcon('total')}</th>
                <th className="px-4 py-2 text-center cursor-pointer" onClick={() => requestSort('orderStatus')}>Статус {getSortIcon('orderStatus')}</th>
                <th className="px-4 py-2 text-center">Действия</th>
              </tr>
            </thead>
            <tbody className="bg-[#383838] divide-y divide-gray-700">
              {filteredAndSortedOrders.map(order => {
                const statusInfo = getStatusDisplay(order.orderStatus);
                return (
                  <tr key={order.id} className="hover:bg-[#424242]">
                    <td className="px-4 py-2">{order.userEmail || 'N/A'}</td>
                    <td className="px-4 py-2">
                      {formatDateForOrderManager(order.createdWhen)} {/* Используем обновленную функцию */}
                    </td>
                    <td className="px-4 py-2 text-right">{order.total?.toFixed(2) || '0.00'} ₽</td>
                    <td className="px-4 py-2 text-center">
                       {editingOrderId === order.id ? (
                        <select
                          value={newStatus}
                          onChange={(e) => setNewStatus(e.target.value)}
                          className="p-1 rounded bg-[#525252] border border-gray-600 text-xs"
                        >
                          {orderStatuses.map(s => <option key={s} value={s}>{getStatusDisplay(s).text}</option>)}
                        </select>
                      ) : (
                        <span className={`px-2 py-1 text-xs font-semibold rounded-full text-white ${statusInfo.color} flex items-center justify-center`}>
                          {statusInfo.icon} {statusInfo.text}
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-2 text-center">
                      <button
                        onClick={() => { setSelectedOrder(order); setShowDetails(true); }}
                        className="p-1.5 bg-blue-600 hover:bg-blue-700 text-white rounded mr-2"
                        title="Подробнее"
                      >
                        <FaEye />
                      </button>
                       {editingOrderId === order.id ? (
                        <button 
                            onClick={() => handleStatusChange(order.id, newStatus)}
                            className="p-1.5 bg-green-600 hover:bg-green-700 text-white rounded"
                            title="Сохранить статус"
                        >
                            <FaCheckCircle />
                        </button>
                      ) : (
                        <button 
                            onClick={() => startEditStatus(order)}
                            className="p-1.5 bg-yellow-500 hover:bg-yellow-600 text-black rounded"
                            title="Изменить статус"
                        >
                            <FaEdit />
                        </button>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
      {selectedOrder && (
        <OrderDetailsModal
          isOpen={showDetails}
          onClose={() => setShowDetails(false)}
          order={selectedOrder}
        />
      )}
    </div>
  );
};

export default OrderManager;
