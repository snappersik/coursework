import React, { useState } from 'react'; 
import { observer } from 'mobx-react-lite'; 
import OrderDetailsModal from '../modals/OrderDetailsModal'; 

const OrdersTab = observer(({ orders }) => { 
  const [selectedOrder, setSelectedOrder] = useState(null); 
  const [showDetailsModal, setShowDetailsModal] = useState(false); 

  const handleViewDetails = (order) => { 
    setSelectedOrder(order); 
    setShowDetailsModal(true); 
  }; 

  const getStatusClass = (status) => { 
    switch (status) { 
      case 'COMPLETED': return 'bg-green-500 text-white'; 
      case 'CANCELLED': return 'bg-red-500 text-white'; 
      case 'PENDING': default: return 'bg-yellow-500 text-gray-900'; 
    } 
  }; 

  const getStatusText = (status) => { 
    switch (status) { 
      case 'COMPLETED': return 'Выполнен'; 
      case 'CANCELLED': return 'Отменен'; 
      case 'PENDING': default: return 'В обработке'; 
    } 
  }; 

  // Проверяем, что orders существует и является массивом
  if (!orders || !Array.isArray(orders) || orders.length === 0) { 
    return (
      <div className="text-center p-8 bg-[#525252] rounded-lg border border-gray-700">
        <p className="text-xl text-gray-300 mb-2">У вас пока нет заказов</p>
        <p className="text-gray-400">Посетите наш каталог, чтобы найти интересные товары</p>
      </div>
    );
  }

  // Если orders - массив, безопасно используем map
  return (
    <div className="overflow-x-auto">
      <table className="min-w-full bg-[#525252] rounded-lg overflow-hidden border border-gray-700">
        <thead className="bg-[#626262] text-white">
          <tr>
            <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider">№ заказа</th>
            <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider">Дата</th>
            <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider">Сумма</th>
            <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider">Статус</th>
            <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider">Действия</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-700">
          {orders.map(order => (
            <tr key={order.id} className="hover:bg-[#626262] transition-colors duration-200">
              <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-300">#{order.id}</td>
              <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-300">
                {new Date(order.createdWhen).toLocaleDateString()}
              </td>
              <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-300">
                {order.total?.toFixed(2) || '0.00'} ₽
              </td>
              <td className="px-4 py-3 whitespace-nowrap">
                <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusClass(order.status)}`}>
                  {getStatusText(order.status)}
                </span>
              </td>
              <td className="px-4 py-3 whitespace-nowrap text-sm text-right">
                <button
                  onClick={() => handleViewDetails(order)}
                  className="text-yellow-500 hover:text-yellow-400 transition-colors duration-200 transform hover:scale-110"
                >
                  Детали
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      
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
