import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { observer } from 'mobx-react';
import { authStore } from './store/store.js';
import { checkAuth } from './api/apiClient';

// Общие компоненты
import Header from './components/layout/Header/Header.jsx';
import Footer from './components/layout/Footer';
import ScrollToTop from "react-scroll-to-top";

// Страницы
import HomePage from './pages/Home/HomePage.jsx';
import CatalogPage from './pages/Catalog/CatalogPage';
import EventsPage from './pages/Events/EventsPage';
import EventDetailPage from './pages/Events/EventDetailPage';
// import AboutPage from './pages/AboutPage'; 
// import ContactsPage from './pages/ContactsPage'; 
import AuthPage from './pages/Auth/AuthPage';
import ProfilePage from './pages/Profile/ProfilePage';
import CartPage from './pages/Cart/CartPage';
import MyApplicationsPage from './components/profile/MyApplicationsPage.jsx';
import ForgotPasswordPage from './components/auth/ForgotPasswordPage';
import ResetPasswordPage from './components/auth/ResetPasswordPage';
import NotFoundPage from './components/common/NotFoundPage';
import ForbiddenPage from './components/common/ForbiddenPage';
// import BookDetailPage from './pages/BookDetailPage';

// Админ-панель
import AdminPanel from './components/admin/AdminPanel';

const getRoleNameById = (roleId) => {
  switch (roleId) {
    case 1: return 'USER';
    case 2: return 'ORGANIZER';
    case 3: return 'ADMIN';
    default: return 'USER'; // Default to USER or handle as an unknown role
  }
};

// Защищенный маршрут
const ProtectedRoute = observer(({ children, requiredRoles = [] }) => {
  const { isAuthorized, userRole } = authStore;

  if (!isAuthorized) {
    console.log('[ProtectedRoute] Не авторизован, перенаправление на /auth');
    return <Navigate to="/auth" replace />;
  }

  if (requiredRoles.length > 0 && !requiredRoles.includes(userRole)) {
    console.log(`[ProtectedRoute] Роль ${userRole} не разрешена (требуется: ${requiredRoles.join(', ')}), перенаправление на /forbidden`);
    return <Navigate to="/forbidden" replace />;
  }

  console.log(`[ProtectedRoute] Доступ разрешен для роли ${userRole}`);
  return children;
});


const App = observer(() => {
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      console.log('[App.jsx] initAuth: Начало проверки аутентификации');
      try {
        const { success, userInfo } = await checkAuth();

        if (success && userInfo) {
          console.log('[App.jsx] initAuth: checkAuth успешен, userInfo:', userInfo);
          
          let roleId;
          if (userInfo.role && typeof userInfo.role.id === 'number') {
            roleId = userInfo.role.id;
          } else if (typeof userInfo.role === 'number') {
            roleId = userInfo.role;
          } else if (userInfo.email === 'admin' && (!userInfo.role || typeof userInfo.role.id !== 'number')) {
            console.warn("[App.jsx] Admin user detected, but role.id missing or not a number in userInfo. Assuming ADMIN role ID 3.");
            roleId = 3; 
          } else {
            console.warn("[App.jsx] Could not determine role ID from userInfo. Defaulting to USER role ID 1.", userInfo.role);
            roleId = 1;
          }
          
          const roleName = getRoleNameById(roleId);

          authStore.setAuthorized(
            true,
            roleName, // Use the derived roleName
            userInfo.id
          );
        } else {
          console.log('[App.jsx] initAuth: checkAuth не успешен или нет userInfo. Вызов authStore.logout().');
          authStore.logout();
        }
      } catch (error) {
        console.error('[App.jsx] initAuth: Критическая ошибка при проверке аутентификации:', error);
        authStore.logout();
      } finally {
        console.log('[App.jsx] initAuth: Проверка аутентификации завершена. loading = false');
        setLoading(false);
      }
    };

    initAuth();
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-screen bg-[#424242]">
        <div className="animate-spin rounded-full h-16 w-16 border-t-4 border-b-4 border-yellow-500"></div>
        <p className="ml-4 title-font text-white text-xl">Загрузка...</p>
      </div>
    );
  }

  return (
    <Router>
      <ScrollToTop />
      <div className="flex flex-col min-h-screen bg-[#424242] text-white">
        <Header />
        <main className="flex-grow">
          <Routes>
            {/* Публичные маршруты */}
            <Route path="/" element={<HomePage />} />
            <Route path="/catalog" element={<CatalogPage />} />
            {/* <Route path="/books/:id" element={<BookDetailPage />} /> */}
            <Route path="/events" element={<EventsPage />} />
            <Route path="/events/:id" element={<EventDetailPage />} />
            {/* <Route path="/about" element={<AboutPage />} /> */}
            {/* <Route path="/contacts" element={<ContactsPage />} /> */}
            <Route path="/auth" element={<AuthPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />
            <Route path="/reset-password" element={<ResetPasswordPage />} />
            <Route path="/forbidden" element={<ForbiddenPage />} />

            {/* Защищенные маршруты для авторизованных пользователей */}
            <Route path="/profile" element={
              <ProtectedRoute>
                <ProfilePage />
              </ProtectedRoute>
            } />
            <Route path="/cart" element={
              <ProtectedRoute>
                <CartPage />
              </ProtectedRoute>
            } />
            <Route path="/my-applications" element={
              <ProtectedRoute>
                <MyApplicationsPage />
              </ProtectedRoute>
            } />

            {/* Маршруты админ-панели */}
            <Route
              path="/admin/*"
              element={
                <ProtectedRoute requiredRoles={['ADMIN', 'ORGANIZER']}>
                  <AdminPanel />
                </ProtectedRoute>
              }
            />

            {/* Страница 404 */}
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </main>
        <Footer />
      </div>
      <ToastContainer position="bottom-right" theme="dark" />
    </Router>
  );
});

export default App;
