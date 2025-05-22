import React, { useState } from 'react';
import { observer } from 'mobx-react-lite';
import OrderDetailsModal from '../modals/OrderDetailsModal';
import { format, parseISO, isValid } from 'date-fns'; // Убедись, что isValid импортирован
import { ru } from 'date-fns/locale';

const OrdersTab = observer(({ orders }) => {
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showDetailsModal, setShowDetailsModal] = useState(false);

  const handleViewDetails = (order) => {
    setSelectedOrder(order);
    setShowDetailsModal(true);
  };

  const getStatusClass = (status) => {
    switch (status) {
      case 'PENDING': return 'bg-yellow-500 text-yellow-900';
      case 'COMPLETED': return 'bg-green-500 text-green-900';
      case 'CANCELLED': return 'bg-red-500 text-red-900';
      default: return 'bg-gray-500 text-gray-900';
    }
  };

  const translateStatus = (status) => {
    const statuses = {
      PENDING: "В обработке",
      COMPLETED: "Завершен",
      CANCELLED: "Отменен"
    };
    return statuses[status] || status;
  };

  const formatDateForOrdersTab = (dateString) => {
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
        // Формат времени может быть HH:mm или HH:mm:ss
        const timePart = parts.length > 1 ? parts[1] : "00:00";
        parsableDateString = `${dateParts[2]}-${dateParts[1]}-${dateParts[0]}T${timePart}`;
      }

      const date = parseISO(parsableDateString);
      if (isValid(date)) {
        return format(date, 'dd MMMM yyyy, HH:mm', { locale: ru });
      }
      console.warn("Could not parse date for OrdersTab with parseISO after transformations:", dateString, "->", parsableDateString);
      return 'Неверная дата';
    } catch (error) {
      console.error("Error formatting date in OrdersTab:", dateString, error);
      return 'Ошибка даты';
    }
  };


  if (!orders || orders.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center p-12 bg-gray-700 rounded-lg text-white">
        <div className="text-5xl mb-5">📦</div>
        <h3 className="text-xl font-semibold text-yellow-400 mb-2">У вас пока нет заказов</h3>
        <p className="text-gray-300">Самое время что-нибудь заказать!</p>
      </div>
    );
  }

  return (
    <div className="space-y-6 text-white">
      <h2 className="text-2xl font-semibold text-yellow-400 mb-6">Мои заказы</h2>
      {orders.map((order) => (
        <div key={order.id} className="bg-gray-700 shadow-lg rounded-lg p-6">
          <div className="flex flex-col sm:flex-row justify-between sm:items-center mb-4">
            <div>
              <h3 className="text-xl font-semibold text-gray-100">Заказ #{order.id}</h3>
              <p className="text-sm text-gray-400">
                Дата: {formatDateForOrdersTab(order.createdWhen)} {/* Используем обновленную функцию */}
              </p>
            </div>
            <div className={`mt-2 sm:mt-0 px-3 py-1 text-sm font-medium rounded-full ${getStatusClass(order.orderStatus)}`}>
              {translateStatus(order.orderStatus)}
            </div>
          </div>

          <div className="mb-4">
            <p className="text-lg font-semibold text-gray-200">Сумма: {order.total?.toFixed(2) || '0.00'} ₽</p>
          </div>

          {order.products && order.products.length > 0 && (
            <div className="mb-4">
              <h4 className="text-md font-semibold text-gray-300 mb-2">Товары:</h4>
              <ul className="space-y-1 list-disc list-inside text-gray-400">
                {order.products.map(product => (
                  <li key={product.id}>{product.name} - {product.price?.toFixed(2)} ₽</li>
                ))}
              </ul>
            </div>
          )}

          <button
            onClick={() => handleViewDetails(order)}
            className="w-full sm:w-auto bg-yellow-500 hover:bg-yellow-600 text-black font-medium py-2 px-4 rounded-md transition duration-150 ease-in-out"
          >
            Подробнее
          </button>
        </div>
      ))}

      {showDetailsModal && selectedOrder && (
        <OrderDetailsModal
          order={selectedOrder}
          isOpen={showDetailsModal}
          onClose={() => setShowDetailsModal(false)}
        />
      )}
    </div>
  );
});

export default OrdersTab;
