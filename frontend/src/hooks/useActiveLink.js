import { useLocation } from 'react-router-dom';

const useActiveLink = () => {
  const location = useLocation();
  
  const isActive = (path) => {
    // Для главной страницы
    if (path === '/' && location.pathname === '/') {
      return true;
    }
    
    // Для ссылок с якорями на главной странице
    if (path.startsWith('#') && location.pathname === '/') {
      return location.hash === path;
    }
    
    // Для обычных страниц
    return location.pathname === path;
  };
  
  return { isActive };
};

export default useActiveLink;
