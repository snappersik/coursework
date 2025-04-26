import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { API_URL } from '../../config';

const Dashboard = () => {
  const [stats, setStats] = useState({
    users: 0,
    books: 0,
    orders: 0,
    events: 0
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        // Здесь можно сделать запросы к API для получения статистики\
        setStats({
          users: 120,
          books: 450,
          orders: 89,
          events: 15
        });
        setLoading(false);
      } catch (error) {
        console.error('Ошибка загрузки статистики:', error);
        setLoading(false);
      }
    };

    fetchStats();
  }, []);

  if (loading) {
    return <div className="text-center py-10">Загрузка...</div>;
  }

  return (
    <div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="bg-[#424242] rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-3 rounded-full bg-indigo-100 text-indigo-600 mr-4">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
            </div>
            <div>
              <p className="text-gray-500 text-sm">Пользователи</p>
              <p className="text-2xl font-semibold">{stats.users}</p>
            </div>
          </div>
        </div>
        <div className="bg-[#424242] rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-3 rounded-full bg-green-100 text-green-600 mr-4">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
              </svg>
            </div>
            <div>
              <p className="text-gray-500 text-sm">Книги</p>
              <p className="text-2xl font-semibold">{stats.books}</p>
            </div>
          </div>
        </div>
        <div className="bg-[#424242] rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-3 rounded-full bg-yellow-100 text-yellow-600 mr-4">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
              </svg>
            </div>
            <div>
              <p className="text-gray-500 text-sm">Заказы</p>
              <p className="text-2xl font-semibold">{stats.orders}</p>
            </div>
          </div>
        </div>
        <div className="bg-[#424242] rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-3 rounded-full bg-red-100 text-red-600 mr-4">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
            <div>
              <p className="text-gray-500 text-sm">Мероприятия</p>
              <p className="text-2xl font-semibold">{stats.events}</p>
            </div>
          </div>
        </div>
      </div>

      <div className="bg-[#424242] rounded-lg shadow p-6">
        <h2 className="text-xl font-semibold mb-4">Последние действия</h2>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Действие</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Пользователь</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Дата</th>
              </tr>
            </thead>
            <tbody className="bg-[#424242] divide-y divide-gray-200">
              <tr>
                <td className="px-6 py-4 whitespace-nowrap">Добавлена новая книга "Война и мир"</td>
                <td className="px-6 py-4 whitespace-nowrap">admin@example.com</td>
                <td className="px-6 py-4 whitespace-nowrap">25.04.2025 14:30</td>
              </tr>
              <tr>
                <td className="px-6 py-4 whitespace-nowrap">Обновлен статус заказа #1234</td>
                <td className="px-6 py-4 whitespace-nowrap">admin@example.com</td>
                <td className="px-6 py-4 whitespace-nowrap">25.04.2025 13:15</td>
              </tr>
              <tr>
                <td className="px-6 py-4 whitespace-nowrap">Создано новое мероприятие "Встреча с автором"</td>
                <td className="px-6 py-4 whitespace-nowrap">organizer@example.com</td>
                <td className="px-6 py-4 whitespace-nowrap">25.04.2025 11:45</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
