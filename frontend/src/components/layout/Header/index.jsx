import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import useActiveLink from '../../../hooks/useActiveLink';
import MobileMenu from './MobileMenu';

const Header = () => {
  const { isActive } = useActiveLink();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  
  const toggleMobileMenu = () => {
    setIsMobileMenuOpen(prev => !prev);
  };
  
  useEffect(() => {
    if (isMobileMenuOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    
    return () => {
      document.body.style.overflow = '';
    };
  }, [isMobileMenuOpen]);
  
  return (
    <header className="text-[#848484] body-font">
      <div className="container mx-auto flex flex-wrap flex-col md:flex-row items-center w-full justify-between">
        <nav className="md:mr-auto flex flex-wrap items-center text-base justify-between w-full h-24 container relative">
          <div
            className="w-full flex-grow lg:flex lg:items-center lg:w-auto hidden lg:block pt-6 lg:pt-0"
            id="nav-content"
          >
            <ul className="list-reset lg:flex justify-between flex-1 items-center">
              <li className="mr-3">
                <Link
                  to="/#about"
                  className={`uppercase text-lg hover:text-[#ffb100] transition-colors ${
                    isActive('#about') ? 'text-[#ffb100] font-medium' : ''
                  }`}
                >
                  о клубе
                </Link>
              </li>
              <li>
                <Link
                  to="/#gallery"
                  className={`uppercase text-lg hover:text-[#ffb100] transition-colors ${
                    isActive('#gallery') ? 'text-[#ffb100] font-medium' : ''
                  }`}
                >
                  прочитанное
                </Link>
              </li>
              <li>
                <Link
                  to="/catalog"
                  className={`uppercase text-lg hover:text-[#ffb100] transition-colors ${
                    isActive('/catalog') ? 'text-[#ffb100] font-medium' : ''
                  }`}
                >
                  магазин
                </Link>
              </li>
              <li>
                <Link
                  to="/cart"
                  className={`uppercase text-lg hover:text-[#ffb100] transition-colors ${
                    isActive('/cart') ? 'text-[#ffb100] font-medium' : ''
                  }`}
                >
                  корзина
                </Link>
              </li>
              <li>
                <Link
                  to="/contact"
                  className={`uppercase text-lg hover:text-[#ffb100] transition-colors ${
                    isActive('/contact') ? 'text-[#ffb100] font-medium' : ''
                  }`}
                >
                  контакты
                </Link>
              </li>
              <li>
                <Link
                  to="/login"
                  className="inline-flex items-center bg-[#555555] text-white border-0 rounded-full py-5 px-9 focus:outline-none hover:bg-[#848484] transition-colors uppercase text-lg"
                >
                  присоединиться
                </Link>
              </li>
            </ul>
          </div>
          <div className="block lg:hidden absolute right-0 top-1/2 transform -translate-y-1/2">
            <button
              onClick={toggleMobileMenu}
              className="flex items-center px-3 py-2 border rounded text-gray-500 border-gray-600 hover:text-white hover:border-white transition-colors"
            >
              <svg 
                className="h-10 w-10" 
                viewBox="0 0 24 24"
                fill="currentColor"
              >
                <circle cx="4" cy="12" r="2" />
                <circle cx="12" cy="12" r="2" />
                <circle cx="20" cy="12" r="2" />
              </svg>
            </button>
          </div>
        </nav>
      </div>
      
      <MobileMenu isOpen={isMobileMenuOpen} onClose={() => setIsMobileMenuOpen(false)} isActive={isActive} />
    </header>
  );
};

export default Header;