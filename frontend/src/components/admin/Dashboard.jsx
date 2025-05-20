import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { API_URL } from '../../config';
import { toast } from 'react-toastify';
import { Bar, Line, Pie } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  ArcElement,
  Title,
  Tooltip,
  Legend
} from 'chart.js';

// Регистрация компонентов Chart.js
ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  ArcElement,
  Title,
  Tooltip,
  Legend
);

const Dashboard = () => {
  const [stats, setStats] = useState({ users: 0, books: 0, orders: 0, events: 0 });
  const [userChartData, setUserChartData] = useState(null);
  const [orderChartData, setOrderChartData] = useState(null);
  const [eventStatsData, setEventStatsData] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setLoading(true);

        // Fetch general stats
        const statsRes = await axios.get(`${API_URL}/admin/stats`, { withCredentials: true });
        setStats({
          users: statsRes.data.users || 0,
          books: statsRes.data.books || 0,
          orders: statsRes.data.orders || 0,
          events: statsRes.data.events || 0
        });

        // Fetch user registration chart data
        const usersChartRes = await axios.get(`${API_URL}/admin/stats/users-chart`, { withCredentials: true });
        setUserChartData({
          labels: usersChartRes.data.labels,
          datasets: [{
            label: 'Регистрации пользователей',
            data: usersChartRes.data.data,
            backgroundColor: 'rgba(75, 192, 192, 0.6)',
            borderColor: 'rgba(75, 192, 192, 1)',
            borderWidth: 1
          }]
        });

        // Fetch orders chart data
        const ordersChartRes = await axios.get(`${API_URL}/admin/stats/orders-chart`, { withCredentials: true });
        setOrderChartData({
          labels: ordersChartRes.data.labels,
          datasets: [{
            label: 'Заказы',
            data: ordersChartRes.data.data,
            backgroundColor: 'rgba(153, 102, 255, 0.6)',
            borderColor: 'rgba(153, 102, 255, 1)',
            borderWidth: 1
          }]
        });

        // Fetch event statistics
        const eventsStatsRes = await axios.get(`${API_URL}/admin/stats/events`, { withCredentials: true });
        setEventStatsData(eventsStatsRes.data);

        setLoading(false);
      } catch (error) {
        console.error('Ошибка загрузки данных для дашборда:', error);
        toast.error('Не удалось загрузить данные для дашборда');
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  // Создаем данные для круговой диаграммы
  const pieChartData = {
    labels: ['Пользователи', 'Книги', 'Заказы', 'Мероприятия'],
    datasets: [
      {
        data: [stats.users, stats.books, stats.orders, stats.events],
        backgroundColor: [
          'rgba(255, 99, 132, 0.6)',
          'rgba(54, 162, 235, 0.6)',
          'rgba(255, 206, 86, 0.6)',
          'rgba(75, 192, 192, 0.6)',
        ],
        borderColor: [
          'rgba(255, 99, 132, 1)',
          'rgba(54, 162, 235, 1)',
          'rgba(255, 206, 86, 1)',
          'rgba(75, 192, 192, 1)',
        ],
        borderWidth: 1,
      },
    ],
  };

  if (loading) {
    return (
      <div className="p-4 flex justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-yellow-500"></div>
      </div>
    );
  }

  return (
    <div className="p-6 bg-[#505050] text-white">
      <h1 className="text-2xl font-bold mb-6">Панель управления</h1>

      {/* Статистика */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <div className="bg-[#585858] p-4 rounded-lg shadow-md">
          <h2 className="text-lg font-semibold text-gray-300">Пользователи</h2>
          <p className="text-3xl font-bold text-yellow-400">{stats.users}</p>
        </div>
        <div className="bg-[#585858] p-4 rounded-lg shadow-md">
          <h2 className="text-lg font-semibold text-gray-300">Книги</h2>
          <p className="text-3xl font-bold text-yellow-400">{stats.books}</p>
        </div>
        <div className="bg-[#585858] p-4 rounded-lg shadow-md">
          <h2 className="text-lg font-semibold text-gray-300">Заказы</h2>
          <p className="text-3xl font-bold text-yellow-400">{stats.orders}</p>
        </div>
        <div className="bg-[#585858] p-4 rounded-lg shadow-md">
          <h2 className="text-lg font-semibold text-gray-300">Мероприятия</h2>
          <p className="text-3xl font-bold text-yellow-400">{stats.events}</p>
        </div>
      </div>

      {/* Графики */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        <div className="bg-[#585858] p-4 rounded-lg shadow-md">
          <h2 className="text-lg font-semibold mb-4 text-gray-300">Регистрации пользователей</h2>
          {userChartData ? (
            <div className="h-64">
              <Line data={userChartData} options={{
                maintainAspectRatio: false,
                scales: {
                  y: { beginAtZero: true, grid: { color: 'rgba(255, 255, 255, 0.1)' } },
                  x: { grid: { color: 'rgba(255, 255, 255, 0.1)' } }
                },
                plugins: { legend: { labels: { color: 'white' } } }
              }} />
            </div>
          ) : (
            <p className="text-gray-400">Нет данных для отображения</p>
          )}
        </div>

        <div className="bg-[#585858] p-4 rounded-lg shadow-md">
          <h2 className="text-lg font-semibold mb-4 text-gray-300">Статистика заказов</h2>
          {orderChartData ? (
            <div className="h-64">
              <Bar data={orderChartData} options={{
                maintainAspectRatio: false,
                scales: {
                  y: { beginAtZero: true, grid: { color: 'rgba(255, 255, 255, 0.1)' } },
                  x: { grid: { color: 'rgba(255, 255, 255, 0.1)' } }
                },
                plugins: { legend: { labels: { color: 'white' } } }
              }} />
            </div>
          ) : (
            <p className="text-gray-400">Нет данных для отображения</p>
          )}
        </div>
      </div>

      {/* Круговая диаграмма */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        <div className="bg-[#585858] p-4 rounded-lg shadow-md">
          <h2 className="text-lg font-semibold mb-4 text-gray-300">Общая статистика</h2>
          <div className="h-64">
            <Pie data={pieChartData} options={{
              maintainAspectRatio: false,
              plugins: { legend: { position: 'right', labels: { color: 'white' } } }
            }} />
          </div>
        </div>

        {/* Статистика мероприятий */}
        <div className="bg-[#585858] p-4 rounded-lg shadow-md">
          <h2 className="text-lg font-semibold mb-4 text-gray-300">Заполненность мероприятий</h2>
          {eventStatsData && Array.isArray(eventStatsData) && eventStatsData.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="min-w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-700">
                    <th className="py-2 px-3 text-left">Название</th>
                    <th className="py-2 px-3 text-left">Тип</th>
                    <th className="py-2 px-3 text-left">Участники</th>
                    <th className="py-2 px-3 text-left">Заполнено</th>
                  </tr>
                </thead>
                <tbody>
                  {eventStatsData.map((event, index) => (
                    <tr key={index} className="border-b border-gray-700">
                      <td className="py-2 px-3">{event.title}</td>
                      <td className="py-2 px-3">{event.eventType}</td>
                      <td className="py-2 px-3">{event.approvedApplications}/{event.maxParticipants}</td>
                      <td className="py-2 px-3">
                        <div className="w-full bg-gray-700 rounded-full h-2.5">
                          <div
                            className="bg-yellow-400 h-2.5 rounded-full"
                            style={{ width: `${event.fillPercentage}%` }}>
                          </div>
                        </div>
                        <span className="text-xs">{event.fillPercentage.toFixed(1)}%</span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-gray-400">Нет данных о мероприятиях</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
