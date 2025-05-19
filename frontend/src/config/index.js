export const SERVER_BASE_URL = import.meta.env.VITE_SERVER_BASE_URL || 'http://localhost:8080';
export const API_URL = `${SERVER_BASE_URL}/api/rest`;
export const DEFAULT_BOOK_COVER = '../assets/img/book_placeholder.webp';

export const DEFAULT_PRODUCT_IMAGE = '../assets/img/product_placeholder.webp';

// Настройки для запросов
export const API_CONFIG = {
  headers: {  
    'Content-Type': 'application/json'
  },
  timeout: 30000
};