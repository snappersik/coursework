import { useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';

const MobileMenu = ({ isOpen, onClose, isActive }) => {
  const menuRef = useRef(null);
  
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
  
  return (
    <>
      <div 
        className={`fixed inset-0 bg-black bg-opacity-50 z-40 transition-opacity duration-300 ${
          isOpen ? 'opacity-100' : 'opacity-0 pointer-events-none'
        }`}
        onClick={onClose}
      />
      
      <div 
        ref={menuRef}
        className={`fixed top-0 right-0 h-full w-4/5 max-w-sm bg-gray-800 z-50 transform transition-transform duration-300 ease-in-out ${
          isOpen ? 'translate-x-0' : 'translate-x-full'
        }`}
      >
        <div className="p-5">
          <button 
            onClick={onClose}
            className="absolute top-5 right-5 text-white"
          >
            <svg 
              className="h-8 w-8" 
              viewBox="0 0 24 24" 
              fill="none" 
              stroke="currentColor" 
              strokeWidth="2"
            >
              <path d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
          
          <ul className="mt-16 space-y-6">
            <li>
              <Link
                to="/#about"
                className={`block text-xl uppercase transition-colors ${
                  isActive('#about') ? 'text-[#ffb100] font-medium' : 'text-white hover:text-[#ffb100]'
                }`}
                onClick={onClose}
              >
                о клубе
              </Link>
            </li>
            <li>
              <Link
                to="/#gallery"
                className={`block text-xl uppercase transition-colors ${
                  isActive('#gallery') ? 'text-[#ffb100] font-medium' : 'text-white hover:text-[#ffb100]'
                }`}
                onClick={onClose}
              >
                прочитанное
              </Link>
            </li>
            <li>
              <Link
                to="/catalog"
                className={`block text-xl uppercase transition-colors ${
                  isActive('/catalog') ? 'text-[#ffb100] font-medium' : 'text-white hover:text-[#ffb100]'
                }`}
                onClick={onClose}
              >
                магазин
              </Link>
            </li>
            <li>
              <Link
                to="/cart"
                className={`block text-xl uppercase transition-colors ${
                  isActive('/cart') ? 'text-[#ffb100] font-medium' : 'text-white hover:text-[#ffb100]'
                }`}
                onClick={onClose}
              >
                корзина
              </Link>
            </li>
            <li>
              <Link
                to="/contact"
                className={`block text-xl uppercase transition-colors ${
                  isActive('/contact') ? 'text-[#ffb100] font-medium' : 'text-white hover:text-[#ffb100]'
                }`}
                onClick={onClose}
              >
                контакты
              </Link>
            </li>
            <li className="mt-8">
              <Link
                to="/login"
                className="block text-center bg-[#555555] text-white py-4 px-6 rounded-full hover:bg-[#848484] transition-colors uppercase text-lg"
                onClick={onClose}
              >
                присоединиться
              </Link>
            </li>
          </ul>
        </div>
      </div>
    </>
  );
};

export default MobileMenu;