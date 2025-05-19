// src/App.jsx
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import { useEffect } from 'react';
import Cookies from 'js-cookie';
import { authStore } from './store/store.js';
import Header from './components/layout/Header';
import Footer from './components/layout/Footer';
import Slider from './components/ui/Slider';
import ClubHarbor from './components/ui/ClubHarbor';
import SectionsWrapper from './components/layout/SectionsWrapper';
import ClubDescription from './components/ui/ClubDescription';
import ClubBenefits from './components/ui/ClubBenefits';
import Gallery from './components/ui/Gallery';
import AuthPage from './pages/Auth'; // Предполагается, что здесь находится LoginForm
import ForgotPasswordPage from './components/auth/ForgotPasswordPage.jsx'; // Новая страница для восстановления пароля
import ResetPasswordPage from './components/auth/ResetPasswordPage';   // Новая страница для сброса пароля
import CartPage from './components/cart/CartPage';
import CatalogPage from './components/catalog/CatalogPage';
import ProfilePage from './pages/Profile';
import AdminPanel from './components/admin/AdminPanel';
import ForbiddenPage from './components/common/ForbiddenPage';
import NotFoundPage from './components/common/NotFoundPage';
import ScrollToAnchor from './components/common/ScrollToAnchor';
import { ToastContainer } from 'react-toastify'; // Импорт компонента уведомлений
import 'react-toastify/dist/ReactToastify.css';  // Импорт стилей для уведомлений


const AppContent = observer(() => {
    useEffect(() => {
        // Проверка авторизации при загрузке приложения
        const authToken = Cookies.get('jwt');
        const userRole = localStorage.getItem('userRole');
        const userId = localStorage.getItem('userId');

        // Если есть токен, роль и ID пользователя, но состояние не авторизовано - авторизуем
        if (authToken && userRole && userId && !authStore.isAuthorized) {
            // Передаем userId в authStore
            authStore.setAuthorized(true, userRole, userId); 
        }
    }, []);

    // Компонент для защиты маршрутов, требующих авторизации
    const ProtectedRoute = ({ children, allowedRoles }) => {
        const userRole = localStorage.getItem('userRole');
        // Если пользователь не авторизован, перенаправляем на страницу входа
        if (!authStore.isAuthorized) {
            return <Navigate to="/auth" replace />;
        }
        // Если указаны разрешенные роли и роль пользователя не входит в них
        if (allowedRoles && !allowedRoles.includes(userRole)) {
            return <Navigate to="/" replace />; // Или на страницу /forbidden
        }
        // Если все проверки пройдены, отображаем защищенный контент
        return children;
    };

    return (
        <>
            <ScrollToAnchor />
            <Header />
            {/* Настройка контейнера для уведомлений */}
            <ToastContainer 
                position="top-right"
                autoClose={5000}
                hideProgressBar={false}
                newestOnTop={false}
                closeOnClick
                rtl={false}
                pauseOnFocusLoss
                draggable
                pauseOnHover
                theme="colored" // Или "light", "dark"
            />
            <Routes>
                {/* Главная страница */}
                <Route
                    path="/"
                    element={
                        <>
                            <Slider />
                            <SectionsWrapper>
                                <ClubHarbor />
                                <ClubDescription />
                                <ClubBenefits />
                                <Gallery />
                            </SectionsWrapper>
                        </>
                    }
                />
                {/* Страницы авторизации и восстановления пароля */}
                <Route path="/auth" element={<AuthPage />} />
                <Route path="/forgot-password" element={<ForgotPasswordPage />} />
                <Route path="/reset-password" element={<ResetPasswordPage />} /> {/* Токен будет передан как параметр запроса */}
                
                {/* Страница каталога */}
                <Route path="/catalog" element={<CatalogPage />} />
                
                {/* Защищенные маршруты, требующие авторизации */}
                <Route
                    path="/cart"
                    element={
                        <ProtectedRoute>
                            <CartPage />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/profile"
                    element={
                        <ProtectedRoute>
                            <ProfilePage />
                        </ProtectedRoute>
                    }
                />
                
                {/* Маршрут для админ-панели, доступный только для админов и организаторов */}
                <Route
                    path="/admin/*"
                    element={
                        <ProtectedRoute allowedRoles={['ADMIN', 'ORGANIZER']}>
                            <AdminPanel />
                        </ProtectedRoute>
                    }
                />
                <Route path="*" element={<NotFoundPage />} />
            </Routes>
            <Footer />
        </>
    );
});

function App() {
    return (
        <Router>
            <AppContent />
        </Router>
    );
}

export default App;
