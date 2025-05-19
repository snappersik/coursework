// src/pages/Profile/index.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import { getUserInfo, updateUserProfile, getUserOrders, getUserEventApplications, cancelEventApplication, restoreEventApplication } from '../../../src/api/apiClient';
import { authStore } from '../../store/store';
import ProfileTab from './ProfileTab';
import OrdersTab from './OrdersTab';
import EventsTab from './EventsTab';

const ProfilePage = observer(() => {
  const [user, setUser] = useState(null);
  const [activeTab, setActiveTab] = useState('profile');
  const [orders, setOrders] = useState([]);
  const [eventApplications, setEventApplications] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    if (!authStore.isAuthorized) {
      navigate('/auth');
      return;
    }

    const fetchUserProfile = async () => {
      try {
        setLoading(true);
        const userInfo = await getUserInfo();
        setUser(userInfo);

        // Загружаем заказы и заявки на мероприятия
        if (activeTab === 'orders') {
          const userOrders = await getUserOrders();
          setOrders(userOrders);
        } else if (activeTab === 'events') {
          const userEvents = await getUserEventApplications();
          setEventApplications(userEvents);
        }
      } catch (err) {
        setError('Не удалось загрузить данные');
        console.error('Ошибка при загрузке данных:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchUserProfile();
  }, [activeTab, navigate]);

  const handleTabChange = (tab) => {
    setActiveTab(tab);
  };

  const handleUpdateProfile = async (formData) => {
    try {
      const updatedUser = await updateUserProfile(formData);
      setUser(updatedUser);
      return true;
    } catch (err) {
      console.error('Ошибка при обновлении профиля:', err);
      return false;
    }
  };

  const handleCancelApplication = async (applicationId) => {
    try {
      await cancelEventApplication(applicationId);
      // Обновляем список заявок
      const userEvents = await getUserEventApplications();
      setEventApplications(userEvents);
      return true;
    } catch (err) {
      console.error('Ошибка при отмене заявки:', err);
      return false;
    }
  };

  const handleRestoreApplication = async (applicationId) => {
    try {
      await restoreEventApplication(applicationId);
      // Обновляем список заявок
      const userEvents = await getUserEventApplications();
      setEventApplications(userEvents);
      return true;
    } catch (err) {
      console.error('Ошибка при восстановлении заявки:', err);
      return false;
    }
  };

  if (loading && !user) {
    return (
      <div className="min-h-screen bg-gray-800 p-10 text-white">
        <div className="flex justify-center items-center h-64">
          <div className="text-xl">Загрузка...</div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-800 p-10 text-white">
        <div className="flex justify-center items-center h-64">
          <div className="text-xl text-red-500">{error}</div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-800 p-5 md:p-10 text-white">
      <div className="max-w-6xl mx-auto bg-gray-700 rounded-xl overflow-hidden shadow-2xl">
        <div className="w-full relative overflow-hidden">
          {/* Содержимое шапки */}
          <div className="p-6 md:p-8 bg-gray-900 border-b border-gray-600">
            <h1 className="text-4xl md:text-5xl font-bold text-center text-yellow-500 mb-8">Личный кабинет</h1>
            <div className="flex flex-row justify-between items-center w-full">
              <button
                className={`px-6 py-3 text-lg md:text-xl font-medium border-b-2 transition-colors ${activeTab === 'profile'
                  ? 'text-yellow-500 border-yellow-500'
                  : 'text-white border-transparent hover:text-yellow-400'}`}
                onClick={() => handleTabChange('profile')}
              >
                Профиль
              </button>
              <button
                className={`px-6 py-3 text-lg md:text-xl font-medium border-b-2 transition-colors ${activeTab === 'orders'
                  ? 'text-yellow-500 border-yellow-500'
                  : 'text-white border-transparent hover:text-yellow-400'}`}
                onClick={() => handleTabChange('orders')}
              >
                Покупки
              </button>
              <button
                className={`px-6 py-3 text-lg md:text-xl font-medium border-b-2 transition-colors ${activeTab === 'events'
                  ? 'text-yellow-500 border-yellow-500'
                  : 'text-white border-transparent hover:text-yellow-400'}`}
                onClick={() => handleTabChange('events')}
              >
                Мероприятия
              </button>
            </div>
          </div>
        </div>

        <div className="p-6 md:p-8">
          {activeTab === 'profile' && (
            <ProfileTab
              user={user}
              onUpdateProfile={handleUpdateProfile}
            />
          )}

          {activeTab === 'orders' && (
            <OrdersTab orders={orders} />
          )}

          {activeTab === 'events' && (
            <EventsTab
              applications={eventApplications}
              onCancelApplication={handleCancelApplication}
              onRestoreApplication={handleRestoreApplication}
            />
          )}
        </div>
      </div>
    </div>
  );
});

export default ProfilePage;
