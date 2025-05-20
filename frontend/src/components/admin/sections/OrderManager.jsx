import React, { useState, useEffect, useMemo } from 'react';
import axios from 'axios';
import { toast } from 'react-toastify';
import { API_URL } from '../../../config'; // Убедись, что путь к config правильный
import { FaSort, FaSortAlphaDown, FaSortAlphaUp, FaEye, FaShippingFast, FaCheckCircle, FaTimesCircle, FaBoxOpen } from 'react-icons/fa';
import OrderDetailsModal from '../../modals/OrderDetailsModal'; // Предполагаемый путь к модалке

const OrderManager = () => {
  const [orders, setOrders] = useState([]); // Инициализируем как пустой массив
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [sortConfig, setSortConfig] = useState({ key: null, direction: 'asc' });
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showDetails, setShowDetails] = useState(false);

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${API_URL}/orders`, { // Убедись, что это правильный эндпоинт
        withCredentials: true,
      });
      // Устанавливаем заказы, только если response.data является массивом
      setOrders(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error('Ошибка загрузки заказов:', error);
      toast.error('Не удалось загрузить заказы');
      setOrders([]); // При ошибке также устанавливаем пустой массив
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (orderId, newStatus) => {
    try {
      const response = await axios.put(`${API_URL}/orders/${orderId}/status?status=${newStatus}`, {}, {
        withCredentials: true,
      });
      if (response.status === 200) {
        toast.success('Статус заказа обновлён');
        fetchOrders(); // Обновляем список заказов
      }
    } catch (error) {
      console.error('Ошибка обновления статуса:', error);
      toast.error(error.response?.data?.message || 'Не удалось обновить статус заказа');
    }
  };

  const handleViewDetails = (order) => {
    setSelectedOrder(order);
    setShowDetails(true);
  };

  const filteredAndSortedOrders = useMemo(() => {
    let filtered = Array.isArray(orders) ? [...orders] : [];

    if (searchQuery) {
      filtered = filtered.filter(order =>
        (order.id && String(order.id).includes(searchQuery)) ||
        (order.user?.email && order.user.email.toLowerCase().includes(searchQuery.toLowerCase())) ||
        (order.status && order.status.toLowerCase().includes(searchQuery.toLowerCase()))
      );
    }

    if (sortConfig.key) {
      filtered.sort((a, b) => {
        let valA = a[sortConfig.key];
        let valB = b[sortConfig.key];

        // Пример для сортировки по вложенному полю user.email
        if (sortConfig.key === 'user.email') {
            valA = a.user?.email || '';
            valB = b.user?.email || '';
        }

        if (valA < valB) return sortConfig.direction === 'asc' ? -1 : 1;
        if (valA > valB) return sortConfig.direction === 'asc' ? 1 : -1;
        return 0;
      });
    }
    return filtered;
  }, [orders, searchQuery, sortConfig]);
  
  const getStatusClass = (status) => {
    switch (status) {
      case 'PENDING': return 'bg-yellow-500 text-yellow-100';
      case 'PROCESSING': return 'bg-blue-500 text-blue-100';
      case 'SHIPPED': return 'bg-purple-500 text-purple-100';
      case 'DELIVERED': return 'bg-green-500 text-green-100';
      case 'CANCELLED': return 'bg-red-500 text-red-100';
      default: return 'bg-gray-500 text-gray-100';
    }
  };
  
  const getStatusIcon = (status) => {
    switch (status) {
      case 'PENDING': return <FaBoxOpen className="mr-1"/>;
      case 'PROCESSING': return <FaShippingFast className="mr-1"/>; // Using FaShippingFast for processing as an example
      case 'SHIPPED': return <FaShippingFast className="mr-1"/>;
      case 'DELIVERED': return <FaCheckCircle className="mr-1"/>;
      case 'CANCELLED': return <FaTimesCircle className="mr-1"/>;
      default: return <FaBoxOpen className="mr-1"/>;
    }
  };

  const statusOptions = [
    { value: "PENDING", label: "В ожидании" },
    { value: "PROCESSING", label: "В обработке" },
    { value: "SHIPPED", label: "Отправлен" },
    { value: "DELIVERED", label: "Доставлен" },
    { value: "CANCELLED", label: "Отменен" },
  ];


  if (loading) {
    return <div className="text-center py-10 text-white">Загрузка заказов...</div>;
  }

  return (
    <div className="p-4 bg-gray-800 text-white min-h-screen">
      <h1 className="text-3xl font-semibold mb-6 text-yellow-500">Управление Заказами</h1>
      <div className="mb-6">
        <input
          type="text"
          placeholder="Поиск по ID, email, статусу..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="px-4 py-2 rounded-md bg-gray-700 text-white border border-gray-600 focus:ring-yellow-500 focus:border-yellow-500 w-full"
        />
      </div>

      <div className="overflow-x-auto shadow-xl rounded-lg">
        <table className="min-w-full divide-y divide-gray-700">
          <thead className="bg-gray-750">
            <tr>
              {/* Добавь обработчики сортировки, если нужно */}
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">ID Заказа</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Пользователь</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Дата</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Сумма</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Статус</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Действия</th>
            </tr>
          </thead>
          <tbody className="bg-gray-800 divide-y divide-gray-700">
            {filteredAndSortedOrders.map(order => (
              <tr key={order.id} className="hover:bg-gray-700 transition duration-150">
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-100">{order.id}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-300">{order.user?.email || 'N/A'}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-300">{new Date(order.orderDate).toLocaleDateString()}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-300">{order.totalAmount?.toFixed(2) || '0.00'} руб.</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm">
                  <span className={`px-3 py-1 inline-flex items-center text-xs leading-5 font-semibold rounded-full ${getStatusClass(order.status)}`}>
                    {getStatusIcon(order.status)} {order.status || 'N/A'}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                  <button onClick={() => handleViewDetails(order)} className="text-blue-400 hover:text-blue-300 transition duration-150"><FaEye /></button>
                  {/* Выпадающий список для смены статуса */}
                  <select 
                    value={order.status} 
                    onChange={(e) => handleStatusChange(order.id, e.target.value)}
                    className={`ml-2 p-1 text-xs rounded-md border-gray-600 focus:ring-yellow-500 focus:border-yellow-500 ${getStatusClass(order.status).replace('text-', 'bg-opacity-20 border text-')}`} // Немного стилизации
                  >
                    {statusOptions.map(opt => (
                      <option key={opt.value} value={opt.value} className="bg-gray-700 text-white">{opt.label}</option>
                    ))}
                  </select>
                </td>
              </tr>
            ))}
            {filteredAndSortedOrders.length === 0 && (
              <tr>
                <td colSpan="6" className="px-6 py-4 text-center text-sm text-gray-400">Заказы не найдены.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

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
