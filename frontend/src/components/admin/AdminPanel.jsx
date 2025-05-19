import React, { useState, useEffect } from 'react';
import { Routes, Route, useNavigate, Navigate } from 'react-router-dom';
import axios from 'axios';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { API_URL } from '../../config';
import Sidebar from './common/Sidebar';
import Dashboard from './Dashboard';
import BookManager from './sections/BookManager';
import UserManager from './sections/UserManager';
import SliderManager from './sections/SliderManager';
import OrderManager from './sections/OrderManager';
import EventManager from './sections/EventManager';
import ProductManager from './sections/ProductManager';

const AdminPanel = () => {
  const [sidebarOpen, setSidebarOpen] = useState(true)
  const [activeSection, setActiveSection] = useState('dashboard')
  const [userRole, setUserRole] = useState(localStorage.getItem('userRole') || '')
  const navigate = useNavigate()

  useEffect(() => {
    let mounted = true
    const checkAuth = async () => {
      try {
        const { data } = await axios.get(
            `${API_URL}/users/profile`,
            { withCredentials: true }
        )
        const role = data.role?.name
        if (!role || !['ADMIN', 'ORGANIZER'].includes(role)) {
          toast.error('У вас нет доступа к админ-панели')
          navigate('/')
        } else if (mounted) {
          setUserRole(role)
          localStorage.setItem('userRole', role)
        }
      } catch {
        toast.error('Необходимо авторизоваться')
        navigate('/auth')
      }
    }
    if (!userRole) {
      checkAuth()
    }
    return () => {
      mounted = false
    }
  }, [navigate, userRole])

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen)
  }

  const hasAccess = (section) => {
    if (userRole === 'ADMIN') return true
    if (userRole === 'ORGANIZER') {
      return ['dashboard', 'books', 'events', 'slider'].includes(section)
    }
    return false
  }

  return (
      <div className="flex">
        <Sidebar
            sidebarOpen={sidebarOpen}
            toggleSidebar={toggleSidebar}
            activeSection={activeSection}
            setActiveSection={setActiveSection}
            hasAccess={hasAccess}
        />

        <div
            className={`
          flex-1 min-h-screen text-gray-100 bg-[#505050]
          transition-all duration-200 text-white
        `}
        >

          <div className="p-6">
            <Routes>
              <Route index element={<Dashboard />} />

              <Route
                  path="books"
                  element={<BookManager />}
              />

              {hasAccess('users') && (
                  <Route
                      path="users"
                      element={<UserManager />}
                  />
              )}

              {hasAccess('products') && (
                  <Route
                      path="products"
                      element={<ProductManager />}
                  />
              )}

              {hasAccess('orders') && (
                  <Route
                      path="orders"
                      element={<OrderManager />}
                  />
              )}

              {hasAccess('events') && (
                  <Route
                      path="events"
                      element={<EventManager />}
                  />
              )}

              {hasAccess('slider') && (
                  <Route
                      path="slider"
                      element={<SliderManager />}
                  />
              )}

              {/* перенаправление на Dashboard для неизвестных путей */}
              <Route path="*" element={<Navigate to="/admin" replace />} />
            </Routes>
          </div>
        </div>

        <ToastContainer position="top-right" />
      </div>
  )
}

export default AdminPanel
