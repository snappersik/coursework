import { useLocation } from 'react-router-dom';

const useActiveLink = () => {
  const location = useLocation();
  
  const isActive = (path) => {
    // Для главной страницы и якорей
    if (path === '#about' && location.pathname === '/') {
      return true;
    }
    
    // Для ссылок с якорями на главной странице
    if (path.startsWith('#') && location.pathname === '/') {
      return location.hash === path;
    }
    
    // Для страниц из навигации
    if (path.startsWith('./assets/pages/')) {
      const cleanPath = path.replace('./assets/pages/', '/').replace('.html', '');
      return location.pathname === cleanPath;
    }
    
    return location.pathname === path;
  };
  
  return { isActive };
};

export default useActiveLink;