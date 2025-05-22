import React, { useState, useEffect } from 'react';
import { Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { observer } from 'mobx-react';
import { authStore } from '../../store/store'; // Убедитесь, что путь правильный

// Общие компоненты админ-панели
import Sidebar from './common/Sidebar';
import Dashboard from './Dashboard';

// Менеджеры разделов
import BookManager from './sections/BookManager';
import UserManager from './sections/UserManager';
import SliderManager from './sections/SliderManager';
import OrderManager from './sections/OrderManager';
import EventManager from './sections/EventManager';
import ProductManager from './sections/ProductManager';
import ActionManage from './sections/ActionManage';
import EventApplicationManager from './sections/EventApplicationManager'; // Добавляем импорт

const AdminPanel = observer(() => {
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [activeSection, setActiveSection] = useState('dashboard');
  const location = useLocation();

  // Обновляем активный раздел при изменении URL
  useEffect(() => {
    const path = location.pathname.split('/admin/')[1] || 'dashboard';
    setActiveSection(path.split('/')[0]);
  }, [location.pathname]);

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
  };

  // Функция проверки доступа к разделу
  const hasAccess = (section) => {
    if (!authStore.isAuthorized) return false;
    if (authStore.userRole === 'ADMIN') return true;
    if (authStore.userRole === 'ORGANIZER') {
      // Разделы, доступные Организатору
      return ['books', 'events', 'event-applications', 'slider'].includes(section);
    }
    return false;
  };

  return (
    <div className="flex h-screen "> {/* Основной контейнер админ-панели */}
      <Sidebar
        sidebarOpen={sidebarOpen}
        toggleSidebar={toggleSidebar}
        activeSection={activeSection}
        setActiveSection={setActiveSection}
        hasAccess={hasAccess}
        userRole={authStore.userRole}
      />
      {/* Область контента */}
      <div className="flex-1 overflow-y-auto p-6">
        <Routes>
          {/* Маршрут по умолчанию для /admin */}
          <Route index element={hasAccess('dashboard') ? <Dashboard /> : <Navigate to="/" replace />} />
          
          {/* Конкретные маршруты */}
          {hasAccess('dashboard') && <Route path="dashboard" element={<Dashboard />} />}
          {hasAccess('users') && <Route path="users" element={<UserManager />} />}
          {hasAccess('books') && <Route path="books" element={<BookManager />} />}
          {hasAccess('orders') && <Route path="orders" element={<OrderManager />} />}
          {hasAccess('events') && <Route path="events" element={<EventManager />} />}
          
          {/* Добавляем маршрут для EventApplicationManager */}
          {hasAccess('event-applications') && 
            <Route path="event-applications" element={<EventApplicationManager />} />
          }

          {hasAccess('slider') && <Route path="slider" element={<SliderManager />} />}
          {hasAccess('products') && <Route path="products" element={<ProductManager />} />}
          {hasAccess('activity') && <Route path="activity" element={<ActionManage />} />}
          
          {/* Запасной маршрут для неизвестных путей в /admin */}
          <Route path="*" element={<Navigate to="/admin/dashboard" replace />} />
        </Routes>
      </div>
    </div>
  );
});

export default AdminPanel;
