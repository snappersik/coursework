import React from 'react';
import { API_URL, DEFAULT_PRODUCT_IMAGE } from '../../config';

const ProductCard = ({
  product,
  inCart,
  onAddToCart
}) => {
  const {
    id, name, description, price, category,
    hasLocalImage, coverImageUrl, originalCoverImageFilename
  } = product;

  let imageSource = DEFAULT_PRODUCT_IMAGE;
  if (hasLocalImage && originalCoverImageFilename) {
    imageSource = `${API_URL}/products/${id}/cover?t=${new Date().getTime()}`;
  } else if (coverImageUrl) {
    imageSource = coverImageUrl;
  }

  const handleImageError = (e) => {
    e.target.onerror = null;
    e.target.src = DEFAULT_PRODUCT_IMAGE;
  };

  return (
    <div className="bg-[#232b39] rounded-xl shadow-lg flex flex-col justify-between h-full p-4">
      <img
        src={imageSource}
        alt={name}
        onError={handleImageError}
        className="w-full h-44 object-cover rounded-lg mb-3 border border-gray-700"
        width={300}
        height={176}
        loading="lazy"
      />
      <div className="flex-1 flex flex-col">
        <h3 className="text-lg font-semibold text-white mb-2 truncate">{name}</h3>
        <p className="text-gray-300 text-sm mb-3 flex-1">
          {description ? (description.length > 70 ? description.substring(0, 70) + '...' : description) : 'Нет описания'}
        </p>
        <div className="flex items-center justify-between mb-4">
          <span className="text-yellow-400 font-bold text-lg">{price?.toFixed(2) || '0.00'} руб.</span>
          <span className="text-xs bg-[#374151] text-gray-200 px-3 py-1 rounded-full uppercase">{category}</span>
        </div>
        <button
          onClick={() => !inCart && onAddToCart(product)}
          disabled={inCart}
          className={`w-full py-2 rounded-lg font-bold transition-all duration-200 
            ${inCart
              ? 'bg-green-600 text-white cursor-default'
              : 'bg-yellow-500 hover:bg-yellow-600 text-white hover:scale-105'}
          `}
        >
          {inCart ? 'В корзине' : 'Добавить в корзину'}
        </button>
      </div>
    </div>
  );
};

export default ProductCard;
