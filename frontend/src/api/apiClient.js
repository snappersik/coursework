import axios from 'axios';

export const API_URL = 'http://localhost:8080/api/rest';

const apiClient = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

apiClient.interceptors.request.use(
    (config) => {
      console.log('🔄 Исходящий запрос:', {
        url: config.url,
        method: config.method,
        headers: config.headers,
        withCredentials: config.withCredentials,
        cookies: document.cookie,
      });
      return config;
    },
    (error) => {
      console.error('❌ Ошибка при подготовке запроса:', error);
      return Promise.reject(error);
    }
);

apiClient.interceptors.response.use(
    (response) => {
      console.log('✅ Успешный ответ:', {
        url: response.config.url,
        status: response.status,
      });
      return response;
    },
    (error) => {
      console.error('❌ Ошибка ответа:', {
        url: error.config?.url,
        status: error.response?.status,
        data: error.response?.data,
        message: error.message,
      });
      if (error.response?.status === 401) {
        console.error('🔒 Ошибка аутентификации. Необходимо войти в систему.');
      } else if (error.response?.status === 403) {
        console.error('🚫 Ошибка доступа. Недостаточно прав.');
      }
      return Promise.reject(error);
    }
);

export const login = async (email, password) => {
  try {
    const response = await apiClient.post('/auth/login', { email, password });
    if (response.status === 200) {
      const userInfo = await getUserInfo();
      return { success: true, userInfo };
    }
    return { success: false, error: 'Ошибка входа' };
  } catch (error) {
    console.error('Login error:', error);
    throw error;
  }
};

export const logout = async () => {
  try {
    const response = await apiClient.post('/auth/logout');
    return {
      success: response.status === 200,
    };
  } catch (error) {
    console.error('Logout error:', error);
    return {
      success: false,
      error: error.response?.data || 'Произошла ошибка при выходе',
    };
  }
};

export const checkAuth = async () => {
  try {
    const response = await apiClient.get('/users/profile');
    if (response.status === 200) {
      return {
        success: true,
        userInfo: response.data,
      };
    }
    return {
      success: false,
    };
  } catch (error) {
    console.error('Auth check error:', error);
    return {
      success: false,
      error: error.response?.data || 'Ошибка проверки аутентификации',
    };
  }
};

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
    if (response.status === 200) {
      return response.data;
    }
    throw new Error('Не удалось обновить профиль');
  } catch (error) {
    console.error('Update profile error:', error);
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
    const response = await apiClient.get('/audit'); // Исправлено с '/rest/audit' на '/audit'
    return response.data;
  } catch (error) {
    console.error('Get audit entries error:', error);
    throw new Error(error.response?.data || 'Не удалось получить данные аудита');
  }
};

export default apiClient;