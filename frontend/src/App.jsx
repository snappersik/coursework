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
import AuthPage from './pages/Auth';
import CartPage from './components/cart/CartPage';
import CatalogPage from './components/catalog/CatalogPage';
import ProfilePage from './pages/Profile';
import AdminPanel from './components/admin/AdminPanel';
import ForbiddenPage from './components/common/ForbiddenPage';
import ScrollToAnchor from './components/common/ScrollToAnchor';

const AppContent = observer(() => {
    useEffect(() => {
        const authToken = Cookies.get('authToken');
        const userRole = localStorage.getItem('userRole');
        // console.log('Проверка состояния при загрузке:', { authToken, userRole, isAuthorized: authStore.isAuthorized });
        if (authToken && userRole && !authStore.isAuthorized) {
            authStore.setAuthorized(true, userRole);
        }
    }, []);

    const ProtectedRoute = ({ children, allowedRoles }) => {
        const userRole = localStorage.getItem('userRole');
        // console.log('ProtectedRoute проверка:', {
        //     isAuthorized: authStore.isAuthorized,
        //     userRole,
        //     allowedRoles,
        // });

        if (!authStore.isAuthorized) {
            console.log('Не авторизован, перенаправление на /auth');
            return <Navigate to="/auth" />;
        }
        if (allowedRoles && !allowedRoles.includes(userRole)) {
            console.log('Роль не разрешена, перенаправление на /');
            return <Navigate to="/" />;
        }
        // console.log('Доступ разрешён, отображение компонента');
        return children;
    };

    return (
        <>
            <ScrollToAnchor />
            <Header />
            <Routes>
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
                <Route path="/auth" element={<AuthPage />} />
                <Route path="/catalog" element={<CatalogPage />} />
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
                <Route
                    path="/admin/*"
                    element={
                        <ProtectedRoute allowedRoles={['ADMIN', 'ORGANIZER']}>
                            <AdminPanel />
                        </ProtectedRoute>
                    }
                />
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