import React from 'react';

const CartSummary = ({ products }) => {
  const totalPrice = products.reduce((acc, product) => acc + product.price, 0);

  return (
    <div className="p-4 bg-white rounded shadow-md">
      <h2 className="text-xl font-semibold mb-4">Итог заказа</h2>
      <ul className="divide-y divide-gray-200 mb-4">
        {products.map((product) => (
          <li key={product.id} className="flex justify-between py-2">
            <span className="text-gray-700">{product.name}</span>
            <span className="font-medium text-gray-900">{product.price} ₽</span>
          </li>
        ))}
      </ul>
      <div className="flex justify-between text-lg font-bold">
        <span>Итого:</span>
        <span>{totalPrice} ₽</span>
      </div>
    </div>
  );
};

export default CartSummary;
