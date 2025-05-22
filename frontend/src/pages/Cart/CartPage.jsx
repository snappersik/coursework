import React, { useState, useEffect } from "react";
import { observer } from "mobx-react-lite";
import { cartStore } from "../../store/cartStore";
import { authStore } from "../../store/store"; // <--- ДОБАВЛЕН ЭТОТ ИМПОРТ
import { toast } from "react-toastify";
import SuccessPurchaseModal from "../../components/modals/SuccessPurchaseModal";
import { Link } from "react-router-dom";
import { API_URL, DEFAULT_PRODUCT_IMAGE } from '../../config';

const CartPage = observer(() => {
  const [isLoading, setIsLoading] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);

  useEffect(() => {
    // Теперь authStore должен быть доступен
    if (authStore && authStore.isAuthorized) {
      // Загружаем корзину с сервера. cartStore.loadCartFromServer сам может
      // проверить, нужно ли загружать (например, если cartStore.items.length === 0)
      // или если он еще не загружал для текущего пользователя
      cartStore.loadCartFromServer();
    }
    // Зависимость от authStore.isAuthorized, чтобы эффект перезапускался при изменении статуса авторизации
    // и от cartStore.items.length, чтобы перезагрузить если корзина стала пустой после каких-то действий (опционально)
  }, [authStore.isAuthorized]); 

  const handleRemoveItem = async (productId) => {
    setIsLoading(true);
    try {
      await cartStore.removeItem(productId);
      toast.info('Товар удален из корзины');
    } catch (error) {
      toast.error(error.message || 'Не удалось удалить товар');
    } finally {
      setIsLoading(false);
    }
  };

  const handlePurchase = async () => {
    if (cartStore.items.length === 0) {
      toast.error('Ваша корзина пуста');
      return;
    }

    setIsLoading(true);
    try {
      await cartStore.createOrder();
      setShowSuccessModal(true); 
    } catch (error) {
      toast.error(error.message || 'Не удалось оформить заказ');
      console.error('Ошибка при оформлении заказа:', error);
    } finally {
      setIsLoading(false);
    }
  };
  
  const handleCloseSuccessModal = () => {
    setShowSuccessModal(false);
    // Корзина уже должна быть очищена в cartStore.createOrder -> clearCart
  };


  return (
    <div className="container mx-auto px-4 py-8 text-white">
      <h1 className="text-3xl font-bold mb-8 text-center">Корзина</h1>
      
      {/* Условие для начальной загрузки, authStore.isAuthorized может быть undefined в первый момент */}
      {(cartStore.isLoading && authStore.isAuthorized === undefined) || (cartStore.isLoading && cartStore.items.length === 0 && authStore.isAuthorized) ? (
         <div className="text-center py-16 bg-[#484848] rounded-lg">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-yellow-500 mx-auto mb-4"></div>
          <h2 className="text-xl text-white">Загрузка корзины...</h2>
        </div>
      ) : !authStore.isAuthorized ? ( // Если пользователь не авторизован
        <div className="text-center py-16 bg-[#484848] rounded-lg">
          <h2 className="text-xl text-white">Для доступа к корзине необходимо авторизоваться</h2>
          <p className="mt-2 text-gray-300">
            <Link to="/auth" className="text-yellow-400 hover:text-yellow-300">Войти</Link>
          </p>
        </div>
      ) : cartStore.items.length === 0 ? ( // Авторизован, но корзина пуста
        <div className="text-center py-16 bg-[#484848] rounded-lg">
          <h2 className="text-xl text-white">Ваша корзина пуста</h2>
          <p className="mt-2 text-gray-300">Добавьте товары из <Link to="/catalog" className="text-yellow-400 hover:text-yellow-300">каталога</Link></p>
        </div>
      ) : ( // Авторизован и есть товары
        <div className="grid grid-cols-1 gap-8 md:grid-cols-3">
          <div className="md:col-span-2">
            {cartStore.items.map(item => (
              <div key={item.id} className="flex items-center bg-[#383838] p-4 rounded-lg shadow-md mb-4">
                <div className="w-20 h-20 bg-gray-700 rounded-md overflow-hidden mr-4 flex-shrink-0">
                  <img 
                    src={item.coverImageUrl || DEFAULT_PRODUCT_IMAGE} 
                    alt={item.name || 'Product image'} 
                    className="w-full h-full object-cover"
                    onError={(e) => e.target.src = DEFAULT_PRODUCT_IMAGE}
                  />
                </div>
                <div className="flex-1 min-w-0">
                  <h3 className="font-semibold text-lg text-gray-100 truncate">{item.name || "Название товара отсутствует"}</h3>
                  <p className="text-sm text-gray-400 line-clamp-1">{item.description || "Описание отсутствует"}</p>
                </div>
                <div className="text-right ml-4 flex-shrink-0">
                  <p className="font-bold text-white text-lg">{item.price} ₽</p>
                   {item.quantity && item.quantity > 1 && ( // Если есть поле quantity и оно больше 1
                    <p className="text-sm text-gray-400">x {item.quantity}</p>
                  )}
                  <button 
                    onClick={() => handleRemoveItem(item.id)}
                    disabled={cartStore.isLoading}
                    className="text-sm text-red-500 hover:text-red-700 mt-2 disabled:opacity-50"
                  >
                    Удалить
                  </button>
                </div>
              </div>
            ))}
          </div>
          
          <div className="bg-[#3a3a3a] p-6 rounded-lg shadow-md h-fit">
            <h2 className="text-xl font-semibold mb-4 text-gray-100">Итого</h2>
            <div className="border-t border-b border-gray-600 py-4 mb-4">
              <div className="flex justify-between mb-2 text-gray-200">
                <span>Товары ({cartStore.itemCount}):</span>
                <span>{cartStore.totalPrice.toFixed(2)} ₽</span>
              </div>
            </div>
            <div className="flex justify-between font-bold text-lg mb-6 text-gray-100">
              <span>Итого к оплате:</span>
              <span>{cartStore.totalPrice.toFixed(2)} ₽</span>
            </div>
            <button 
              onClick={handlePurchase}
              disabled={isLoading || cartStore.isLoading}
              className="w-full py-3 bg-yellow-500 text-black font-semibold rounded-md hover:bg-yellow-600 transition-colors disabled:bg-gray-500 disabled:cursor-not-allowed"
            >
              {isLoading ? 'Оформление...' : 'Оформить заказ'}
            </button>
          </div>
        </div>
      )}
      
      <SuccessPurchaseModal 
        isOpen={showSuccessModal} 
        onClose={handleCloseSuccessModal} 
      />
    </div>
  );
});

export default CartPage;