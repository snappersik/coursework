import React, { useState, useEffect } from 'react';
import { Routes, Route, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { API_URL } from '../../config';
import Sidebar from './common/Sidebar';
import Header from './common/Header';
import Dashboard from './Dashboard';
import BookManager from './sections/BookManager';
import UserManager from './sections/UserManager';
import SliderManager from './sections/SliderManager';
import OrderManager from './sections/OrderManager';
import EventManager from './sections/EventManager';
import ProductManager from './sections/ProductManager';

const AdminPanel = () => {
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [activeSection, setActiveSection] = useState('dashboard');
  const [userRole, setUserRole] = useState(localStorage.getItem('userRole') || '');
  const navigate = useNavigate();

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const response = await axios.get(`${API_URL}/rest/users/profile`, {
          withCredentials: true,
        });
        console.log('Ответ сервера о профиле:', response.data);

        const role = response.data.role?.name;
        if (!role || !['ADMIN', 'ORGANIZER'].includes(role)) {
          toast.error('У вас нет доступа к админ-панели');
          navigate('/');
        } else {
          setUserRole(role);
          localStorage.setItem('userRole', role); // Обновляем localStorage
        }
      } catch (error) {
        console.error('Ошибка проверки авторизации:', error);
        toast.error('Необходимо авторизоваться');
        navigate('/auth');
      }
    };

    if (!userRole) {
      checkAuth();
    } else {
      console.log('Используется роль из localStorage:', userRole);
    }
  }, [navigate, userRole]);

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
  };

  const hasAccess = (section) => {
    if (userRole === 'ADMIN') return true;
    if (userRole === 'ORGANIZER') {
      return ['dashboard', 'books', 'events', 'slider'].includes(section);
    }
    return false;
  };

  return (
      <div className="flex h-screen bg-gray-100">
        <Sidebar
            sidebarOpen={sidebarOpen}
            toggleSidebar={toggleSidebar}
            activeSection={activeSection}
            setActiveSection={setActiveSection}
            hasAccess={hasAccess}
            userRole={userRole}
        />
        <div className="flex-1 overflow-auto">
          <Header activeSection={activeSection} userRole={userRole} />
          <main className="p-6">
            <Routes>
              <Route path="/" element={<Dashboard />} />
              <Route path="/books" element={<BookManager />} />
              <Route path="/users" element={<UserManager />} />
              <Route path="/orders" element={<OrderManager />} />
              <Route path="/events" element={<EventManager />} />
              <Route path="/products" element={<ProductManager />} />
              <Route path="/slider" element={<SliderManager />} />
            </Routes>
          </main>
        </div>
        <ToastContainer position="bottom-right" />
      </div>
  );
};

export default AdminPanel;