// src/pages/Profile/OrdersTab.jsx
import React, { useState } from 'react';
import { observer } from 'mobx-react-lite';
import OrderDetailsModal from '../../components/modals/OrderDetailsModal';

const OrdersTab = observer(({ orders }) => {
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showDetailsModal, setShowDetailsModal] = useState(false);

  const handleViewDetails = (order) => {
    setSelectedOrder(order);
    setShowDetailsModal(true);
  };

  const getStatusClass = (status) => {
    switch (status) {
      case 'COMPLETED':
        return 'bg-green-500 text-white';
      case 'CANCELLED':
        return 'bg-red-500 text-white';
      case 'PENDING':
      default:
        return 'bg-yellow-500 text-gray-900';
    }
  };

  const getStatusText = (status) => {
    switch (status) {
      case 'COMPLETED':
        return 'Выполнен';
      case 'CANCELLED':
        return 'Отменен';
      case 'PENDING':
      default:
        return 'В обработке';
    }
  };

  if (orders.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center p-12 bg-gray-700 rounded-lg">
        <div className="text-5xl mb-5">🛒</div>
        <h3 className="text-xl font-semibold text-yellow-500 mb-2">У вас пока нет покупок</h3>
        <p className="text-gray-400">Посетите наш каталог, чтобы найти интересные товары</p>
      </div>
    );
  }

  return (
    <div className="text-white">
      <h2 className="text-2xl font-bold text-yellow-500 mb-5">Ваши заказы</h2>
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {orders.map(order => (
          <div key={order.id} className="bg-gray-700 rounded-lg overflow-hidden shadow-md">
            <div className="bg-gray-800 p-4 flex justify-between items-center">
              <div className="flex flex-col">
                <span className="font-bold">Заказ #{order.id}</span>
                <span className="text-sm text-gray-400">{order.createdWhen}</span>
              </div>
              <div className={`px-3 py-1 rounded-full text-sm font-semibold ${getStatusClass(order.orderStatus)}`}>
                {getStatusText(order.orderStatus)}
              </div>
            </div>
            
            <div className="p-4">
              <div className="flex items-center mb-4">
                <div className="flex space-x-2">
                  {order.products.slice(0, 3).map(product => (
                    <div key={product.id} className="w-12 h-12 bg-gray-600 rounded overflow-hidden">
                      {product.coverImageUrl ? (
                        <img 
                          src={product.coverImageUrl} 
                          alt={product.productName} 
                          className="w-full h-full object-cover"
                        />
                      ) : (
                        <div className="w-full h-full"></div>
                      )}
                    </div>
                  ))}
                  {order.products.length > 3 && (
                    <div className="w-12 h-12 bg-gray-600 rounded flex items-center justify-center text-sm font-bold">
                      +{order.products.length - 3}
                    </div>
                  )}
                </div>
              </div>
            </div>
            
            <div className="p-4 bg-gray-800 border-t border-gray-600 flex justify-between items-center">
              <div>
                <span className="text-gray-400 mr-2">Итого:</span>
                <span className="font-bold text-yellow-500">{order.total || 
                  order.products.reduce((sum, product) => sum + product.price, 0)} ₽</span>
              </div>
              <button 
                className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors"
                onClick={() => handleViewDetails(order)}
              >
                Просмотр
              </button>
            </div>
          </div>
        ))}
      </div>
      
      <OrderDetailsModal 
        isOpen={showDetailsModal} 
        onClose={() => setShowDetailsModal(false)} 
        order={selectedOrder} 
      />
    </div>
  );
});

export default OrdersTab;
