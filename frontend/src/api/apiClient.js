import axios from 'axios'
import { authStore } from '../store/store.js'

// Базовый URL API
export const API_URL = 'http://localhost:8080/api/rest'

// Создаём инстанс axios
const apiClient = axios.create({
  baseURL: API_URL,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true
})

// Лог запросов
apiClient.interceptors.request.use(
  config => {
    console.log('🔄 Запрос:', config.method?.toUpperCase(), config.url)
    return config
  },
  error => {
    console.error('❌ Ошибка при подготовке запроса:', error)
    return Promise.reject(error)
  }
)

// Лог ответов и ошибок
apiClient.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      authStore.logout();
      if (!window.location.pathname.includes('/auth')) {
        window.location.href = '/auth';
      }
    }
    return Promise.reject(error);
  }
);

// === AUTH ===

// POST /auth/login
export const login = async (email, password) => {
  try {
    const res = await apiClient.post('/auth/login', { email, password });

    if (res.status === 200) {
      try {
        const userInfo = await getUserInfo();
        return { success: true, userInfo };
      } catch (error) {
        console.error('Ошибка получения информации о пользователе:', error);
        return { success: false, error: 'Не удалось получить данные пользователя' };
      }
    }

    return { success: false, error: 'Ошибка входа' };
  } catch (error) {
    console.error('Ошибка при входе:', error);

    // Обработка конкретных ошибок
    if (error.response) {
      const status = error.response.status;
      const errorMessage = error.response.data?.message;

      if (status === 401) {
        return { success: false, error: 'Неверный логин или пароль' };
      } else if (status === 404) {
        return { success: false, error: 'Пользователь не найден' };
      } else if (errorMessage) {
        return { success: false, error: errorMessage };
      }
    }

    return { success: false, error: 'Произошла ошибка при входе' };
  }
};


// POST /auth/logout
export const logout = async () => {
  try {
    const res = await apiClient.post('/auth/logout');
    return { success: res.status === 200 };
  } catch (error) {
    console.error('Ошибка при выходе:', error);
    return { success: false };
  } finally {
    authStore.logout(); // Гарантированная очистка состояния
  }
};

// GET /users/profile
export const checkAuth = async () => {
  try {
    const res = await apiClient.get('/users/profile')
    return res.status === 200
      ? { success: true, userInfo: res.data }
      : { success: false }
  } catch (error) {
    return { success: false, error: error.response?.data || error.message }
  }
}

// GET /users/profile
export const getUserInfo = async () => {
  try {
    const response = await apiClient.get('/users/profile');
    if (response.status === 200) {
      return response.data;
    }
    throw new Error('Не удалось получить данные профиля');
  } catch (error) {
    console.error('Get user info error:', error);
    throw error;
  }
};

export const updateUserProfile = async (userData) => {
  try {
    const response = await apiClient.put('/users/profile', userData);
    if (response.status === 200 && response.data) {
      return response.data;
    }
    throw new Error('Не удалось обновить профиль');
  } catch (error) {
    console.error('Update profile error:', error);
    throw error;
  }
};

// Добавьте функцию для загрузки аватара
export const uploadUserAvatar = async (formData) => {
  try {
    const response = await apiClient.post('/users/profile/avatar-upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  } catch (error) {
    console.error('Upload avatar error:', error);
    throw error;
  }
};

export const registerUser = async (registerData) => {
  try {
    const response = await apiClient.post('/auth/register', registerData);
    if (response.status === 200) {
      return { success: true };
    }
    return { success: false, error: 'Ошибка регистрации' };
  } catch (error) {
    console.error('Register error:', error);
    return {
      success: false,
      error: error.response?.data || 'Произошла ошибка при регистрации',
    };
  }
};

// === PASSWORD MANAGEMENT ===

export const requestPasswordReset = async (email) => {
  try {
    const response = await apiClient.post('/auth/forgot-password', { email });
    return { success: true, message: response.data };
  } catch (error) {
    if (error.response && error.response.data && error.response.data.message) {
      return { success: false, error: error.response.data.message };
    }
    return { success: true, message: "Если указанный email зарегистрирован, на него будет отправлено письмо с инструкциями по сбросу пароля." };
  }
};

// For resetting the password using a token
export const resetPasswordWithToken = async (token, newPassword, confirmPassword) => {
  try {
    const response = await apiClient.post('/auth/reset-password', { token, newPassword, confirmPassword });
    return { success: true, message: response.data }; // Assuming backend returns a success message
  } catch (error) {
    return {
      success: false,
      error: error.response?.data?.message || error.response?.data || 'Ошибка сброса пароля'
    };
  }
};

// For authenticated users changing their password
export const changeUserPassword = async (oldPassword, newPassword, confirmPassword) => {
  try {
    // This endpoint needs to be created on the backend for authenticated users
    // It should verify oldPassword and then update to newPassword
    // Let's assume PUT /users/profile/password
    // It should also check if newPassword and confirmPassword match server-side, but client-side check is good too.
    if (newPassword !== confirmPassword) {
      return { success: false, error: "Новый пароль и подтверждение не совпадают" };
    }
    const response = await apiClient.put('/users/profile/password', { oldPassword, newPassword });
    return { success: true, message: response.data.message || "Пароль успешно изменен" };
  } catch (error) {
    return {
      success: false,
      error: error.response?.data?.message || error.response?.data || 'Ошибка смены пароля'
    };
  }
};

// GET    /auth/validate-reset-token  (already in your backend AuthController)
export const validateResetToken = async (token) => {
  try {
    const response = await apiClient.get(`/auth/validate-reset-token?token=${token}`);
    return { success: true, message: response.data };
  } catch (error) {
    return {
      success: false,
      error: error.response?.data?.message || error.response?.data || 'Ошибка валидации токена',
    };
  }
};

export const testAuthentication = async () => {
  console.group('🔐 Тестирование методов аутентификации');
  try {
    console.log('1️⃣ Тест с withCredentials');
    try {
      const cookieResponse = await axios.get(`${API_URL}/users/profile`, {
        withCredentials: true,
      });
      console.log('✅ Аутентификация через cookies работает:', cookieResponse.data);
    } catch (error) {
      console.error('❌ Аутентификация через cookies не работает:', error.response?.status);
    }
    console.log('2️⃣ Тест с использованием apiClient');
    try {
      const clientResponse = await apiClient.get('/users/profile');
      console.log('✅ Аутентификация через apiClient работает:', clientResponse.data);
    } catch (error) {
      console.error('❌ Аутентификация через apiClient не работает:', error.response?.status);
    }
  } catch (error) {
    console.error('❌ Ошибка при тестировании аутентификации:', error);
  }
  console.groupEnd();
};

export const addToCart = async (userId, productId) => {
  try {
    const response = await apiClient.post(`/carts/${userId}/products/${productId}`);
    return response.data;
  } catch (error) {
    console.error('Add to cart error:', error);
    throw new Error(error.response?.data || 'Не удалось добавить товар в корзину');
  }
};

export const removeFromCart = async (userId, productId) => {
  try {
    const response = await apiClient.delete(`/carts/${userId}/products/${productId}`);
    return response.data;
  } catch (error) {
    console.error('Remove from cart error:', error);
    throw new Error(error.response?.data || 'Не удалось удалить товар из корзины');
  }
};

export const createOrder = async () => {
  try {
    const response = await apiClient.post('/orders/create');
    return response.data;
  } catch (error) {
    console.error('Create order error:', error);
    throw new Error(error.response?.data || 'Не удалось создать заказ');
  }
};

export const getUserOrders = async () => {
  try {
    const response = await apiClient.get('/orders/my');
    return response.data;
  } catch (error) {
    console.error('Get user orders error:', error);
    throw new Error(error.response?.data || 'Не удалось получить заказы');
  }
};

export const getUserEventApplications = async () => {
  try {
    const response = await apiClient.get('/event-applications/my');
    return response.data;
  } catch (error) {
    console.error('Get user event applications error:', error);
    throw new Error(error.response?.data || 'Не удалось получить заявки на мероприятия');
  }
};

export const cancelEventApplication = async (applicationId) => {
  try {
    const response = await apiClient.put(`/event-applications/${applicationId}/cancel`);
    return response.data;
  } catch (error) {
    console.error('Cancel event application error:', error);
    throw new Error(error.response?.data || 'Не удалось отменить заявку');
  }
};

export const restoreEventApplication = async (applicationId) => {
  try {
    const response = await apiClient.put(`/event-applications/${applicationId}/restore`);
    return response.data;
  } catch (error) {
    console.error('Restore event application error:', error);
    throw new Error(error.response?.data || 'Не удалось восстановить заявку');
  }
};

export const getEventById = async (eventId) => {
  try {
    const response = await apiClient.get(`/events/${eventId}`);
    return response.data;
  } catch (error) {
    console.error('Get event details error:', error);
    throw new Error(error.response?.data || 'Не удалось получить данные о мероприятии');
  }
};

export const getAuditEntries = async () => {
  try {
    const response = await apiClient.get('/audit');
    return response.data;
  } catch (error) {
    console.error('Get audit entries error:', error);
    throw new Error(error.response?.data || 'Не удалось получить данные аудита');
  }
};

export default apiClient;