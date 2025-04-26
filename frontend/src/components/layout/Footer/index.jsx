import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import './Footer.css';

const Footer = () => {
  const [isMobile, setIsMobile] = useState(false);

  // Определяем, является ли устройство мобильным
  useEffect(() => {
    const handleResize = () => {
      setIsMobile(window.innerWidth < 768);
    };
    
    handleResize(); // Проверяем при первой загрузке
    window.addEventListener('resize', handleResize);
    
    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, []);

  return (
    <footer className="bg-[#424242] py-8 border-t border-[#848484]">
      <div className="container mx-auto px-4">
        <div className={`footer-container ${isMobile ? 'flex-col' : 'flex-row'}`}>
          <h2 className={`title-font text-3xl md:text-4xl text-white ${isMobile ? 'mb-6 text-center' : 'mb-0'}`}>
            Книжная гавань
          </h2>
          
          <nav className={`footer-nav ${isMobile ? 'flex-col items-center' : 'flex-row'}`}>
            <Link to="/about" className="text-white hover:text-yellow-400 transition-colors">
              О клубе
            </Link>
            <Link to="/books" className="text-white hover:text-yellow-400 transition-colors">
              Прочитанное
            </Link>
            <Link to="/shop" className="text-white hover:text-yellow-400 transition-colors">
              Магазин
            </Link>
            <Link to="/cart" className="text-white hover:text-yellow-400 transition-colors">
              Корзина
            </Link>
            <Link to="/contacts" className="text-white hover:text-yellow-400 transition-colors">
              Контакты
            </Link>
          </nav>
        </div>
        
        <p className="text-gray-400 text-sm text-center mt-8">
          © {new Date().getFullYear()} Волкова Ольга. Все права защищены. Наверное.
        </p>
      </div>
    </footer>
  );
};

export default Footer;
