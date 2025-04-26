import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';

const OrderDetailsModal = ({ isOpen, onClose, order }) => {
  if (!isOpen || !order) return null;

  const totalPrice = order.total || order.products.reduce((sum, product) => sum + product.price, 0);

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <motion.div 
            className="fixed inset-0 bg-black bg-opacity-50"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
          />
          <motion.div 
            className="bg-white rounded-lg shadow-xl overflow-hidden w-full max-w-3xl relative z-10 max-h-[90vh] flex flex-col"
            initial={{ scale: 0.9, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            exit={{ scale: 0.9, opacity: 0 }}
            transition={{ type: 'spring', damping: 20 }}
          >
            <div className="p-6 border-b">
              <div className="flex justify-between items-center">
                <h3 className="text-xl font-bold text-gray-900">Детали заказа #{order.id}</h3>
                <button onClick={onClose} className="text-gray-400 hover:text-gray-500">
                  <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
              <div className="mt-2 flex flex-wrap gap-4">
                <div className="text-sm text-gray-500">
                  <span className="font-medium">Дата заказа:</span> {order.createdWhen}
                </div>
                <div className="text-sm text-gray-500">
                  <span className="font-medium">Статус:</span> {
                    order.orderStatus === 'COMPLETED' ? 'Выполнен' :
                    order.orderStatus === 'CANCELLED' ? 'Отменен' : 'В обработке'
                  }
                </div>
              </div>
            </div>
            
            <div className="overflow-y-auto flex-grow p-6">
              <h4 className="font-medium text-gray-900 mb-4">Товары в заказе</h4>
              <div className="space-y-4">
                {order.products.map(product => (
                  <div key={product.id} className="flex border-b pb-4">
                    <div className="w-16 h-16 bg-gray-200 rounded-md overflow-hidden mr-4 flex-shrink-0">
                      {product.coverImageUrl && (
                        <img 
                          src={product.coverImageUrl} 
                          alt={product.productName} 
                          className="w-full h-full object-cover"
                        />
                      )}
                    </div>
                    <div className="flex-grow">
                      <h5 className="font-medium text-gray-900">{product.productName}</h5>
                      <p className="text-sm text-gray-600 line-clamp-2">{product.productDescription}</p>
                      {product.fileType && (
                        <p className="text-xs text-gray-500 mt-1">Тип файла: {product.fileType}</p>
                      )}
                    </div>
                    <div className="text-right ml-4">
                      <p className="font-bold text-gray-900">{product.price} ₽</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
            
            <div className="p-6 border-t bg-gray-50">
              <div className="flex justify-between items-center">
                <span className="font-bold text-gray-900">Итого:</span>
                <span className="font-bold text-xl text-gray-900">{totalPrice} ₽</span>
              </div>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};

export default OrderDetailsModal;
