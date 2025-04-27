import React from 'react';
import { Link } from 'react-router-dom';

const Header = ({ activeSection, userRole }) => {
  return (
    <header className="bg-white shadow-sm">
      <div className="px-4 py-3 flex justify-between items-center">
        <h1 className="text-2xl font-semibold text-gray-800">
          {activeSection === 'dashboard' && 'Дашборд'}
          {activeSection === 'books' && 'Управление книгами'}
          {activeSection === 'users' && 'Управление пользователями'}
          {activeSection === 'orders' && 'Управление заказами'}
          {activeSection === 'events' && 'Управление мероприятиями'}
          {activeSection === 'products' && 'Управление продуктами'}
          {activeSection === 'slider' && 'Управление слайдером'}
        </h1>
        <div className="flex items-center">
          <span className="mr-4 text-gray-600">{userRole}</span>
          <Link to="/" className="text-indigo-600 hover:text-indigo-800">
            На сайт
          </Link>
        </div>
      </div>
    </header>
  );
};

export default Header;
