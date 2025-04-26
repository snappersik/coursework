import React from 'react';
import { Link } from 'react-router-dom';

const ForbiddenPage = () => {
  return (
    <div className="bg-gray-800 min-h-screen flex items-center justify-center px-4">
      <div className="max-w-md w-full bg-gray-700 rounded-lg shadow-lg p-8 text-center">
        <h1 className="text-4xl font-bold text-red-500 mb-4">403</h1>
        <h2 className="text-2xl font-semibold text-white mb-6">Доступ запрещен</h2>
        <p className="text-gray-300 mb-8">
          У вас нет прав для просмотра этой страницы. Пожалуйста, вернитесь на главную страницу.
        </p>
        <Link 
          to="/" 
          className="inline-block bg-yellow-500 hover:bg-yellow-600 text-gray-900 font-medium py-2 px-6 rounded-lg transition-colors"
        >
          Вернуться на главную
        </Link>
      </div>
    </div>
  );
};

export default ForbiddenPage;
