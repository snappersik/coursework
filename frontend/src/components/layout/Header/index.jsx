import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import { authStore } from '../../../store/store.js';
import { logout } from '../../../api/apiClient.js';
import MobileMenu from './MobileMenu';
import './Header.css';

const Header = observer(() => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isMobile, setIsMobile] = useState(window.innerWidth < 1024);
  const navigate = useNavigate();

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

  // Отладочный вывод
  // console.log('User role:', userRole);
  // console.log('Is admin:', isAdmin);
  // console.log('Is authenticated:', isAuthenticated);

  // Обработчик выхода из системы
  const handleLogout = async () => {
    try {
      const result = await logout();
      if (result.success) {
        localStorage.removeItem('userRole');
        authStore.logout();
        navigate('/auth');
      } else {
        console.error('Ошибка при выходе из системы');
      }
    } catch (error) {
      console.error('Произошла ошибка при выходе:', error);
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
                  <Link to="/" className="hover:text-[#ffb100] uppercase text-lg nav-link-transition">
                    О клубе
                  </Link>
                </li>
                <li>
                  <Link to="/#gallery" className="hover:text-[#ffb100] uppercase text-lg nav-link-transition">
                    Прочитанное
                  </Link>
                </li>
                <li>
                  <Link to="/catalog" className="hover:text-[#ffb100] uppercase text-lg nav-link-transition active">
                    Магазин
                  </Link>
                </li>
                {/* Корзина видна только для не-админов */}
                {!isAdmin && (
                    <li>
                      <Link
                          to="/cart"
                          className="hover:text-[#ffb100] uppercase text-lg nav-link-transition"
                          onClick={!isAuthenticated ? handleCartClick : undefined}
                      >
                        Корзина
                      </Link>
                    </li>
                )}
                <li>
                  <Link to="/contact" className="hover:text-[#ffb100] uppercase text-lg nav-link-transition">
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
                                to="/admin/*"
                                className="inline-flex items-center bg-[#ffb100] text-white border-0 rounded-full py-5 px-9 focus:outline-none hover:bg-[#848484] text-base mt-4 md:mt-0 uppercase text-lg nav-link-transition"
                            >
                              Админ панель
                            </Link>
                          </li>
                      ) : (
                          <li>
                            <Link
                                to="/profile"
                                className="inline-flex items-center bg-[#ffb100] text-white border-0 rounded-full py-5 px-9 focus:outline-none hover:bg-[#848484] text-base mt-4 md:mt-0 uppercase text-lg nav-link-transition"
                            >
                              Профиль
                            </Link>
                          </li>
                      )}
                      <li>
                        <button
                            onClick={handleLogout}
                            className="inline-flex items-center bg-[#ffb100] text-white border-0 rounded-full py-5 px-9 focus:outline-none hover:bg-[#848484] text-base mt-4 md:mt-0 uppercase text-lg nav-link-transition"
                        >
                          Выйти
                        </button>
                      </li>
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
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 6h16M4 12h16M4 18h16"></path>
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