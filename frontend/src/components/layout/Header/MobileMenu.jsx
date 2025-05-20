import { useEffect, useRef } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { FaUser, FaShoppingCart, FaSignOutAlt, FaCalendarAlt } from 'react-icons/fa';
import useActiveLink from '../../../hooks/useActiveLink.js';

const MobileMenu = ({ isOpen, onClose, isAuthenticated, isAdmin, userRole, onLogout, handleCartClick }) => {
  const menuRef = useRef(null);
  const location = useLocation();
  const { isActive } = useActiveLink();

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        onClose();
      }
    };

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isOpen, onClose]);

  // Закрываем меню при изменении маршрута
  useEffect(() => {
    onClose();
  }, [location.pathname, onClose]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 z-50 lg:hidden">
      <div ref={menuRef} className=" text-white h-full w-64 p-5 overflow-y-auto">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-xl font-bold">Меню</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-white">
            <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>
        <nav>
          <ul className="space-y-4">
            <li>
              <Link 
                to="/" 
                className={`block py-2 ${isActive('/') ? 'text-[#ffb100]' : 'text-gray-300 hover:text-[#ffb100]'}`}
                onClick={onClose}
              >
                О клубе
              </Link>
            </li>
            <li>
              <Link 
                to="/#gallery" 
                className={`block py-2 ${isActive('#gallery') ? 'text-[#ffb100]' : 'text-gray-300 hover:text-[#ffb100]'}`}
                onClick={onClose}
              >
                Прочитанное
              </Link>
            </li>
            <li>
              <Link 
                to="/catalog" 
                className={`block py-2 ${isActive('/catalog') ? 'text-[#ffb100]' : 'text-gray-300 hover:text-[#ffb100]'}`}
                onClick={onClose}
              >
                Магазин
              </Link>
            </li>
            <li>
              <Link 
                to="/events" 
                className={`block py-2 ${isActive('/events') ? 'text-[#ffb100]' : 'text-gray-300 hover:text-[#ffb100]'}`}
                onClick={onClose}
              >
                Мероприятия
              </Link>
            </li>
            {!isAdmin && (
              <li>
                <Link
                  to="/cart"
                  className={`block py-2 ${isActive('/cart') ? 'text-[#ffb100]' : 'text-gray-300 hover:text-[#ffb100]'}`}
                  onClick={(e) => {
                    onClose();
                    if (!isAuthenticated) handleCartClick(e);
                  }}
                >
                  <FaShoppingCart className="inline mr-2" />
                  Корзина
                </Link>
              </li>
            )}
            <li>
              <Link 
                to="/contacts" 
                className={`block py-2 ${isActive('/contacts') ? 'text-[#ffb100]' : 'text-gray-300 hover:text-[#ffb100]'}`}
                onClick={onClose}
              >
                Контакты
              </Link>
            </li>
            
            <div className="border-t border-gray-700 my-4"></div>
            
            {!isAuthenticated ? (
              <li>
                <Link
                  to="/auth"
                  className="block py-2 px-4 bg-[#ffb100] text-white rounded hover:bg-opacity-90 text-center"
                  onClick={onClose}
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
                      className="block py-2 px-4 bg-[#ffb100] text-white rounded hover:bg-opacity-90 text-center mb-2"
                      onClick={onClose}
                    >
                      Админ панель
                    </Link>
                  </li>
                ) : (
                  <>
                    <li>
                      <Link
                        to="/profile"
                        className="block py-2 text-gray-300 hover:text-[#ffb100]"
                        onClick={onClose}
                      >
                        <FaUser className="inline mr-2" />
                        Мой профиль
                      </Link>
                    </li>
                    <li>
                      <Link
                        to="/my-applications"
                        className="block py-2 text-gray-300 hover:text-[#ffb100]"
                        onClick={onClose}
                      >
                        <FaCalendarAlt className="inline mr-2" />
                        Мои мероприятия
                      </Link>
                    </li>
                  </>
                )}
                <li>
                  <button
                    onClick={() => {
                      onClose();
                      onLogout();
                    }}
                    className="block w-full py-2 text-gray-300 hover:text-[#ffb100] text-left"
                  >
                    <FaSignOutAlt className="inline mr-2" />
                    Выйти
                  </button>
                </li>
              </>
            )}
          </ul>
        </nav>
      </div>
    </div>
  );
};

export default MobileMenu;
