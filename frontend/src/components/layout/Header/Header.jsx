import { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import { authStore } from '../../../store/store.js';
import { logout as apiLogout } from '../../../api/apiClient.js';
import MobileMenu from './MobileMenu';
import useActiveLink from '../../../hooks/useActiveLink.js';
import './Header.css';
import { FaUser, FaShoppingCart, FaSignOutAlt, FaCalendarAlt } from 'react-icons/fa';

const Header = observer(() => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isMobile, setIsMobile] = useState(window.innerWidth < 1024);
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { isActive } = useActiveLink();

  // Отслеживание размера экрана
  useEffect(() => {
    const handleResize = () => {
      setIsMobile(window.innerWidth < 1024);
      if (window.innerWidth >= 1024) {
        setIsMenuOpen(false);
        document.body.style.overflow = '';
      }
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  // Закрываем меню при изменении маршрута
  useEffect(() => {
    setIsMenuOpen(false);
    setUserMenuOpen(false);
    document.body.style.overflow = '';
  }, [location.pathname]);

  // Синхронизация состояния авторизации при монтировании компонента
  useEffect(() => {
    const userRole = localStorage.getItem('userRole');
    if (userRole && authStore.userRole !== userRole) {
      console.log('Синхронизация роли из localStorage:', userRole);
      authStore.setAuthorized(true, userRole);
    }
  }, []);

  const toggleMenu = () => {
    setIsMenuOpen(!isMenuOpen);
    document.body.style.overflow = isMenuOpen ? '' : 'hidden';
  };

  const closeMenu = () => {
    setIsMenuOpen(false);
    document.body.style.overflow = '';
  };

  // Проверка авторизации и роли пользователя
  const isAuthenticated = authStore.isAuthorized;
  const userRole = authStore.userRole;
  const isAdmin = userRole === 'ADMIN' || userRole === 'ORGANIZER';

  // Обработчик выхода из системы
  const handleLogout = async () => {
    console.log('[Header.jsx] handleLogout: Нажата кнопка Выход.');
    try {
      const result = await apiLogout(); // Вызываем переименованную функцию из apiClient
      if (result.success) {
        console.log('[Header.jsx] handleLogout: Выход успешен, перенаправление на /auth.');
        navigate('/auth'); // Перенаправляем после успешного логаута
      } else {
        // Если логаут на сервере не удался, authStore.logout() все равно был вызван в apiClient
        console.error('[Header.jsx] handleLogout: Ошибка при выходе на сервере, но состояние на фронте очищено. Ошибка:', result.error);
        // Можно показать toast.error(result.error)
        navigate('/auth'); // Все равно перенаправляем, т.к. фронт "разлогинен"
      }
    } catch (error) {
      // Эта ошибка не должна произойти, т.к. apiClient.logout() обрабатывает свои ошибки
      console.error('[Header.jsx] handleLogout: Непредвиденная ошибка при вызове apiLogout:', error);
      // На всякий случай, если что-то совсем пошло не так
      authStore.logout();
      navigate('/auth');
    }
  };
  
  // Обработчик клика по корзине для неавторизованных пользователей
  const handleCartClick = (e) => {
    if (!isAuthenticated) {
      e.preventDefault();
      navigate('/auth');
    }
  };

  return (
    <header className="text-[#848484] font-sans">
      <div className="container mx-auto flex flex-wrap flex-col md:flex-row items-center w-full justify-between">
        <nav className="md:mr-auto flex flex-wrap items-center text-base justify-between w-full h-24 container relative">
          <div
            className="w-full flex-grow lg:flex lg:items-center lg:w-auto hidden lg:block pt-6 lg:pt-0"
            id="nav-content"
          >
            <ul className="list-none lg:flex justify-between flex-1 items-center">
              <li className="mr-3">
                <Link
                  to="/"
                  className={`${isActive('/') ? 'text-[#ffb100]' : 'hover:text-[#ffb100]'} uppercase text-lg nav-link-transition`}
                >
                  О клубе
                </Link>
              </li>
              <li>
                <Link
                  to="/#gallery"
                  className={`${isActive('/#gallery')? 'text-[#ffb100]' : 'hover:text-[#ffb100]'} uppercase text-lg nav-link-transition`}
                >
                  Прочитанное
                </Link>
              </li>
              <li>
                <Link
                  to="/catalog"
                  className={`${isActive('/catalog') ? 'text-[#ffb100]' : 'hover:text-[#ffb100]'} uppercase text-lg nav-link-transition`}
                >
                  Магазин
                </Link>
              </li>
              <li>
                <Link
                  to="/events"
                  className={`${isActive('/events') ? 'text-[#ffb100]' : 'hover:text-[#ffb100]'} uppercase text-lg nav-link-transition`}
                >
                  Мероприятия
                </Link>
              </li>
              {/* Корзина видна только для не-админов */}
              {!isAdmin && (
                <li>
                  <Link
                    to="/cart"
                    className={`${isActive('/cart') ? 'text-[#ffb100]' : 'hover:text-[#ffb100]'} uppercase text-lg nav-link-transition`}
                    onClick={!isAuthenticated ? handleCartClick : undefined}
                  >
                    Корзина
                  </Link>
                </li>
              )}
              <li>
                <Link
                  to="/contacts"
                  className={`${isActive('/contacts') ? 'text-[#ffb100]' : 'hover:text-[#ffb100]'} uppercase text-lg nav-link-transition`}
                >
                  Контакты
                </Link>
              </li>
              {!isAuthenticated ? (
                <li>
                  <Link
                    to="/auth"
                    className="inline-flex items-center bg-[#ffb100] text-white border-0 rounded-full py-5 px-9 focus:outline-none hover:bg-[#848484] text-base mt-4 md:mt-0 uppercase text-lg nav-link-transition"
                  >
                    Присоединиться
                  </Link>
                </li>
              ) : (
                <>
                  {isAdmin ? (
                    <li>
                      <Link
                        to="/admin"
                        className="inline-flex items-center bg-[#ffb100] text-white border-0 rounded-full py-5 px-9 focus:outline-none hover:bg-[#848484] text-base mt-4 md:mt-0 uppercase text-lg nav-link-transition"
                      >
                        Админ панель
                      </Link>
                    </li>
                  ) : (
                    <li>
                      <div className="relative">
                        <button
                          onClick={() => setUserMenuOpen(!userMenuOpen)}
                          className="inline-flex items-center bg-[#ffb100] text-white border-0 rounded-full py-5 px-9 focus:outline-none hover:bg-[#848484] text-base mt-4 md:mt-0 uppercase text-lg nav-link-transition"
                        >
                          <FaUser className="mr-2" />
                          Профиль
                        </button>
                        {userMenuOpen && (
                          <div className="absolute right-0 mt-2 w-48 bg-gray-800 rounded-md shadow-lg py-1 z-10">
                            <Link
                              to="/profile"
                              className="block px-4 py-2 text-sm text-gray-200 hover:bg-gray-700"
                            >
                              Мой профиль
                            </Link>
                            <Link
                              to="/my-applications"
                              className="block px-4 py-2 text-sm text-gray-200 hover:bg-gray-700"
                            >
                              <FaCalendarAlt className="inline mr-2" />
                              Мои мероприятия
                            </Link>
                            <button
                              onClick={handleLogout}
                              className="block w-full text-left px-4 py-2 text-sm text-gray-200 hover:bg-gray-700"
                            >
                              <FaSignOutAlt className="inline mr-2" />
                              Выйти
                            </button>
                          </div>
                        )}
                      </div>
                    </li>
                  )}
                  {isAdmin && (
                    <li>
                      <button
                        onClick={handleLogout}
                        className="inline-flex items-center bg-[#ffb100] text-white border-0 rounded-full py-5 px-9 focus:outline-none hover:bg-[#848484] text-base mt-4 md:mt-0 uppercase text-lg nav-link-transition ml-4"
                      >
                        Выйти
                      </button>
                    </li>
                  )}
                </>
              )}
            </ul>
          </div>
          <div className="block lg:hidden absolute right-0 top-1/2 transform -translate-y-1/2">
            <button
              onClick={toggleMenu}
              className="flex items-center px-3 py-2 border rounded text-gray-500 border-gray-600 hover:text-white hover:border-white nav-link-transition"
            >
              <svg className="h-10 w-10" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 6h16M4 12  h16M4 18h16"></path>
              </svg>
            </button>
          </div>
        </nav>
      </div>

      {/* Мобильное меню */}
      <MobileMenu
        isOpen={isMenuOpen}
        onClose={closeMenu}
        isAuthenticated={isAuthenticated}
        isAdmin={isAdmin}
        userRole={userRole}
        onLogout={handleLogout}
        handleCartClick={handleCartClick}
      />
    </header>
  );
});

export default Header;
