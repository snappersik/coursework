import React, { useState } from "react";
import { observer } from "mobx-react-lite";
import { cartStore } from "../../store/cartStore";
import { toast } from "react-toastify";
import SuccessPurchaseModal from "../modals/SuccessPurchaseModal";

const CartPage = observer(() => {
  const [isLoading, setIsLoading] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);

  const handleRemoveItem = (productId) => {
    cartStore.removeItem(productId);
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
      toast.error('Не удалось оформить заказ');
      console.error('Ошибка при оформлении заказа:', error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8 text-center">Корзина</h1>
      
      {cartStore.items.length === 0 ? (
        <div className="text-center py-16 bg-[#484848] rounded-lg">
          <h2 className="text-xl text-white">Ваша корзина пуста</h2>
          <p className="mt-2 text-gray-100">Добавьте товары из каталога</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-8 md:grid-cols-3">
          <div className="md:col-span-2">
            {cartStore.items.map(item => (
              <div key={item.id} className="flex items-center bg-[#484848] p-4 rounded-lg shadow-sm mb-4">
                <div className="w-20 h-20 bg-gray-200 rounded-md overflow-hidden mr-4">
                  {item.coverImageUrl && (
                    <img 
                      src={item.coverImageUrl} 
                      alt={item.productName} 
                      className="w-full h-full object-cover"
                    />
                  )}
                </div>
                <div className="flex-1">
                  <h3 className="font-semibold text-gray-800">{item.productName}</h3>
                  <p className="text-sm text-gray-600 line-clamp-1">{item.productDescription}</p>
                </div>
                <div className="text-right">
                  <p className="font-bold text-white">{item.price} ₽</p>
                  <button 
                    onClick={() => handleRemoveItem(item.id)}
                    className="text-sm text-red-500 hover:text-red-700 mt-2"
                  >
                    Удалить
                  </button>
                </div>
              </div>
            ))}
          </div>
          
          <div className="bg-[#424242] p-6 rounded-lg shadow-sm h-fit">
            <h2 className="text-xl font-semibold mb-4">Итого</h2>
            <div className="border-t border-b py-4 mb-4">
              <div className="flex justify-between mb-2">
                <span>Товары ({cartStore.itemCount}):</span>
                <span>{cartStore.totalPrice} ₽</span>
              </div>
            </div>
            <div className="flex justify-between font-bold text-lg mb-6">
              <span>Итого к оплате:</span>
              <span>{cartStore.totalPrice} ₽</span>
            </div>
            <button 
              onClick={handlePurchase}
              disabled={isLoading}
              className="w-full py-3 bg-yellow-500 text-white rounded-md hover:bg-yellow-600 transition-colors disabled:bg-gray-400"
            >
              {isLoading ? 'Оформление...' : 'Оформить заказ'}
            </button>
          </div>
        </div>
      )}
      
      <SuccessPurchaseModal 
        isOpen={showSuccessModal} 
        onClose={() => setShowSuccessModal(false)} 
      />
    </div>
  );
});

export default CartPage;
