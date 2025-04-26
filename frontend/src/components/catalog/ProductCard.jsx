import React, { useState } from 'react';
import { observer } from 'mobx-react-lite';
import { toast } from 'react-toastify';
import { authStore } from '../../store/store';
import { cartStore } from '../../store/cartStore';

const ProductCard = observer(({ product }) => {
  const [isAdding, setIsAdding] = useState(false);
  const isInCart = cartStore.items.some(item => item.id === product.id);

  const handleAddToCart = async () => {
    if (!authStore.isAuthorized) {
      toast.error('Пожалуйста, войдите в систему, чтобы добавить товар в корзину');
      return;
    }
    
    setIsAdding(true);
    try {
      cartStore.addItem(product);
      toast.success('Товар добавлен в корзину');
    } catch (error) {
      toast.error('Не удалось добавить товар в корзину');
    } finally {
      setIsAdding(false);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-md overflow-hidden">
      <div className="relative h-48 overflow-hidden">
        {product.coverImageUrl ? (
          <img 
            src={product.coverImageUrl} 
            alt={product.name} 
            className="w-full h-full object-cover transition-transform duration-300 hover:scale-105"
          />
        ) : (
          <img 
            src={`/api/rest/products/${product.id}/cover`} 
            alt={product.name} 
            className="w-full h-full object-cover transition-transform duration-300 hover:scale-105"
            onError={(e) => {
              e.target.onerror = null;
              e.target.src = '/images/default-product.png';
            }}
          />
        )}
      </div>
      <div className="p-4">
        <h3 className="text-lg font-semibold mb-2">{product.name}</h3>
        <p className="text-gray-600 text-sm mb-4 line-clamp-3">{product.description}</p>
        <div className="flex justify-between items-center">
          <span className="text-xl font-bold">{product.price} ₽</span>
          <button
            onClick={handleAddToCart}
            disabled={isAdding || isInCart}
            className={`px-4 py-2 rounded-md text-white ${
              isInCart 
                ? 'bg-green-500 hover:bg-green-600' 
                : 'bg-blue-500 hover:bg-blue-600'
            } transition-colors duration-300 disabled:opacity-50`}
          >
            {isInCart ? 'В корзине' : isAdding ? 'Добавление...' : 'В корзину'}
          </button>
        </div>
      </div>
    </div>
  );
});

export default ProductCard;
