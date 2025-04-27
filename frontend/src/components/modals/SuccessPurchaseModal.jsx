import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';

const SuccessPurchaseModal = ({ isOpen, onClose }) => {
  if (!isOpen) return null;

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
            className="bg-[#424242] rounded-lg shadow-xl overflow-hidden w-full max-w-md relative z-10"
            initial={{ scale: 0.9, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            exit={{ scale: 0.9, opacity: 0 }}
            transition={{ type: 'spring', damping: 20 }}
          >
            <div className="p-6">
              <div className="w-16 h-16 mx-auto mb-4 bg-green-100 rounded-full flex items-center justify-center">
                <svg className="w-8 h-8 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <h3 className="text-xl font-bold text-center text-yellow-400 mb-2">Покупка совершена успешно!</h3>
              <p className="text-center text-white mb-6">
                Спасибо за ваш заказ. Вы можете просмотреть детали заказа в разделе "Покупки" вашего профиля.
              </p>
              <div className="flex justify-center">
                <button
                  onClick={onClose}
                  className="px-6 py-2 bg-[#505050] text-white rounded-md hover:bg-[#555555] transition-colors"
                >
                  Закрыть
                </button>
              </div>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};

export default SuccessPurchaseModal;
