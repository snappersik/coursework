export const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
export const DEFAULT_BOOK_COVER = '../assets/img/book_placeholder.webp';
export const DEFAULT_PRODUCT_IMAGE = '../assets/img/product_placeholder.webp'; // Добавлен экспорт для продуктов

// Настройки для запросов
export const API_CONFIG = {
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: 30000
};