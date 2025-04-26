import { useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';

const MobileMenu = ({ isOpen, onClose, isAuthenticated, isAdmin, userRole, onLogout, handleCartClick }) => {
  const menuRef = useRef(null);

  // Добавляем отладочный вывод для мобильного меню
  // console.log('MobileMenu rendering with:', { isAuthenticated, isAdmin, userRole });

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

  if (!isOpen) return null;

  return (
    <div className="mobile-menu active">
      <div className="mobile-menu-content" ref={menuRef}>
        <button onClick={onClose} className="close-menu text-5xl text-[#848484]">×</button>
        <Link to="/" onClick={onClose} className="nav-link-transition">О клубе</Link>
        <Link to="/#gallery" onClick={onClose} className="nav-link-transition">Прочитанное</Link>
        <Link to="/catalog" onClick={onClose} className="nav-link-transition">Магазин</Link>
        
        {/* Показываем корзину только если пользователь не админ/организатор */}
        {!isAdmin && (
          <Link 
            to="/cart" 
            onClick={(e) => {
              onClose();
              if (!isAuthenticated) handleCartClick(e);
            }} 
            className="nav-link-transition"
          >
            Корзина
          </Link>
        )}
        
        <Link to="/contact" onClick={onClose} className="nav-link-transition">Контакты</Link>
        
        {!isAuthenticated ? (
          <Link
            to="/auth"
            onClick={onClose}
            className="inline-flex items-center bg-[#ffb100] text-white border-0 rounded-full py-5 px-9 focus:outline-none hover:bg-[#464646] text-base mt-4 md:mt-0 uppercase text-lg nav-link-transition"
          >
            Присоединиться
          </Link>
        ) : (
          <>
            {isAdmin ? (
              <Link
                to="/admin/*"
                onClick={onClose}
                className="inline-flex items-center bg-[#ffb100] text-white border-0 rounded-full py-5 px-9 focus:outline-none hover:bg-[#464646] text-base mt-4 md:mt-0 uppercase text-lg nav-link-transition"
              >
                Админ панель
              </Link>
            ) : (
              <Link
                to="/profile"
                onClick={onClose}
                className="inline-flex items-center bg-[#ffb100] text-white border-0 rounded-full py-5 px-9 focus:outline-none hover:bg-[#464646] text-base mt-4 md:mt-0 uppercase text-lg nav-link-transition"
              >
                Профиль
              </Link>
            )}
            <button
              onClick={() => {
                onClose();
                onLogout();
              }}
              className="inline-flex items-center bg-[#ffb100] text-white border-0 rounded-full py-5 px-9 focus:outline-none hover:bg-[#464646] text-base mt-4 md:mt-0 uppercase text-lg nav-link-transition"
            >
              Выйти
            </button>
          </>
        )}
      </div>
      <div className="mobile-menu-overlay active" onClick={onClose}></div>
    </div>
  );
};

export default MobileMenu;
