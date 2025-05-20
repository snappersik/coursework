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
      // Дополнительно проверяем, что это не ошибка с /auth/login, чтобы избежать цикла редиректов
      // или если мы уже на странице /auth
      if (!error.config.url.endsWith('/auth/login') && !window.location.pathname.includes('/auth')) {
        authStore.logout();
        window.location.href = '/auth'; // Перенаправляем на страницу входа
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
      // После успешного логина, checkAuth() в App.jsx сам обновит store.
      // Но для немедленного получения userInfo здесь, можно сделать так:
      try {
        const userInfo = await getUserInfo(); // Эта функция уже есть ниже
        return { success: true, userInfo }; // Возвращаем userInfo для LoginForm
      } catch (error) {
        console.error('Ошибка получения информации о пользователе после логина:', error);
        // Логин был успешен, но userInfo не получили. Это странно.
        // Можно вернуть success: true, но без userInfo, App.jsx разберется
        return { success: true, userInfo: null, error: 'Не удалось получить данные пользователя после логина' };
      }
    }
    // Если статус не 200, это должно быть обработано как ошибка Axios и попасть в catch
    return { success: false, error: 'Ошибка входа (не 200)' };
  } catch (error) {
    console.error('Ошибка при входе:', error);
    if (error.response) {
      const status = error.response.status;
      const errorMessage = error.response.data?.message || error.response.data?.error;
      if (status === 401) {
        return { success: false, error: 'Неверный логин или пароль' };
      } else if (status === 404) { // Сервер может вернуть 404 если email не найден до проверки пароля
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
  console.log('[apiClient] logout: Попытка выхода пользователя.');
  try {
    // Сначала делаем запрос на бэкенд для инвалидации сессии и удаления HttpOnly кук
    const res = await apiClient.post('/auth/logout'); // Убедитесь, что ваш эндпоинт POST
    
    // Независимо от ответа сервера (успех или нет), очищаем состояние на фронте
    // Блок finally гарантирует, что authStore.logout() будет вызван
    
    if (res.status === 200) {
      console.log('[apiClient] logout: Запрос на сервер успешен (статус 200).');
      return { success: true };
    } else {
      // Если сервер вернул не 200, но не выбросил ошибку (что маловероятно для Axios)
      console.warn('[apiClient] logout: Запрос на сервер вернул статус', res.status);
      return { success: false, error: `Сервер вернул статус ${res.status}` };
    }
  } catch (error) {
    console.error('[apiClient] logout: Ошибка при запросе на сервер /auth/logout:', error.response?.status, error.message);
    // Даже если запрос на сервер не удался, все равно пытаемся очистить состояние на фронте
    return { success: false, error: error.response?.data?.message || error.message || 'Ошибка при выходе на сервере' };
  } finally {
    // Этот блок выполнится всегда, даже если была ошибка или успешный return
    console.log('[apiClient] logout: Вызов authStore.logout() в блоке finally.');
    authStore.logout(); // Гарантированная очистка состояния MobX и localStorage на фронте
  }
};

export const checkAuth = async () => {
  console.log('[apiClient] checkAuth: Попытка получить /users/profile');
  try {
    const res = await apiClient.get('/users/profile');
    if (res.status === 200 && res.data) {
      console.log('[apiClient] checkAuth: Профиль получен успешно:', res.data);
      return { success: true, userInfo: res.data };
    }
    console.warn('[apiClient] checkAuth: Профиль получен, но статус не 200 или нет данных:', res);
    return { success: false, userInfo: null };
  } catch (error) {
    console.warn('[apiClient] checkAuth: Ошибка при получении профиля:', error.response?.status, error.message);
    return { success: false, userInfo: null, error: error.response?.data || error.message };
  }
};

// GET /users/profile (используется login и checkAuth)
export const getUserInfo = async () => {
  try {
    const response = await apiClient.get('/users/profile');
    if (response.status === 200) {
      return response.data;
    }
    throw new Error('Не удалось получить данные профиля (статус не 200)');
  } catch (error) {
    console.error('Get user info error:', error);
    throw error; // Перебрасываем ошибку, чтобы ее обработали выше
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
    // Убедитесь, что RegisterDTO на бэкенде ожидает структуру:
    // { userData: { email, firstName, ... }, password, confirmPassword }
    const response = await apiClient.post('/auth/register', registerData);
    if (response.status === 200 || response.status === 201) { // 201 Created is also good
      return { success: true, data: response.data }; // Возвращаем данные, т.к. бэк может вернуть UserDTO
    }
    return { success: false, error: 'Ошибка регистрации' };
  } catch (error) {
    console.error('Register error:', error);
    return {
      success: false,
      error: error.response?.data?.message || error.response?.data || 'Произошла ошибка при регистрации',
    };
  }
};

// === PASSWORD MANAGEMENT ===
export const requestPasswordReset = async (email) => {
  try {
    const response = await apiClient.post('/auth/forgot-password', { email });
    // Бэкенд всегда возвращает общий ответ, не раскрывая, существует ли email.
    // Поэтому response.data здесь будет строкой.
    return { success: true, message: response.data };
  } catch (error) {
    // Этот catch может не сработать, если бэк всегда 200 OK для /forgot-password
    // Но на всякий случай, если сервер вернет ошибку
    if (error.response && error.response.data && (error.response.data.message || typeof error.response.data === 'string')) {
      return { success: false, error: error.response.data.message || error.response.data };
    }
    // Возвращаем стандартное сообщение, если ошибка не содержит полезной информации
    return { success: true, message: "Если указанный email зарегистрирован, на него будет отправлено письмо с инструкциями по сбросу пароля." };
  }
};

export const resetPasswordWithToken = async (token, newPassword, confirmPassword) => {
  try {
    const response = await apiClient.post('/auth/reset-password', { token, newPassword, confirmPassword });
    return { success: true, message: response.data };
  } catch (error) {
    return {
      success: false,
      error: error.response?.data?.message || error.response?.data || 'Ошибка сброса пароля'
    };
  }
};

export const changeUserPassword = async (oldPassword, newPassword, confirmPassword) => {
  try {
    if (newPassword !== confirmPassword) {
      return { success: false, error: "Новый пароль и подтверждение не совпадают" };
    }
    // Сервер ожидает { oldPassword, newPassword }
    const response = await apiClient.put('/users/profile/password', { oldPassword, newPassword });
    return { success: true, message: response.data.message || "Пароль успешно изменен" };
  } catch (error) {
    return {
      success: false,
      error: error.response?.data?.message || error.response?.data || 'Ошибка смены пароля'
    };
  }
};

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

export const restoreEventApplication = async (applicationId) => {
  try {
    const response = await apiClient.put(`/event-applications/${applicationId}/restore`);
    return response.data;
  } catch (error) {
    console.error('Restore event application error:', error);
    throw new Error(error.response?.data || 'Не удалось восстановить заявку');
  }
};

// Получение списка мероприятий с пагинацией
export const getEvents = async (page = 0, size = 9, filters = {}) => {
  try {
    const params = new URLSearchParams({
      page,
      size
    });

    if (filters.title) params.append('title', filters.title);
    if (filters.eventType) params.append('eventType', filters.eventType);
    if (filters.startDate) params.append('startDate', filters.startDate);
    if (filters.endDate) params.append('endDate', filters.endDate);

    const response = await apiClient.get(`/events/paginated?${params.toString()}`);
    return response.data;
  } catch (error) {
    console.error('Get events error:', error);
    throw new Error(error.response?.data || 'Не удалось загрузить мероприятия');
  }
};

// Получение информации о мероприятии по ID
export const getEventById = async (eventId) => {
  try {
    const response = await apiClient.get(`/events/${eventId}`);
    return response.data;
  } catch (error) {
    console.error('Get event details error:', error);
    throw new Error(error.response?.data || 'Не удалось получить данные о мероприятии');
  }
};

// Создание заявки на участие в мероприятии
export const createEventApplication = async (eventId) => {
  try {
    const response = await apiClient.post('/event-applications', {
      eventId,
      userId: authStore.userId
    });
    return response.data;
  } catch (error) {
    console.error('Create event application error:', error);
    throw new Error(error.response?.data?.message || error.response?.data || 'Не удалось создать заявку на участие');
  }
};

// Получение заявок текущего пользователя
export const getUserEventApplications = async () => {
  try {
    const response = await apiClient.get('/event-applications/my');
    return response.data;
  } catch (error) {
    console.error('Get user event applications error:', error);
    throw new Error(error.response?.data || 'Не удалось получить заявки на мероприятия');
  }
};

// Отмена заявки на участие
export const cancelEventApplication = async (applicationId) => {
  try {
    const response = await apiClient.put(`/event-applications/${applicationId}/cancel`);
    return response.data;
  } catch (error) {
    console.error('Cancel event application error:', error);
    throw new Error(error.response?.data || 'Не удалось отменить заявку');
  }
};

// Для администраторов и организаторов

// Создание нового мероприятия
export const createEvent = async (eventData) => {
  try {
    const response = await apiClient.post('/events', eventData);
    return response.data;
  } catch (error) {
    console.error('Create event error:', error);
    throw new Error(error.response?.data || 'Не удалось создать мероприятие');
  }
};

// Обновление мероприятия
export const updateEvent = async (eventId, eventData) => {
  try {
    const response = await apiClient.put(`/events/${eventId}`, eventData);
    return response.data;
  } catch (error) {
    console.error('Update event error:', error);
    throw new Error(error.response?.data || 'Не удалось обновить мероприятие');
  }
};

// Отмена мероприятия
export const cancelEvent = async (eventId, reason) => {
  try {
    const response = await apiClient.post(`/events/${eventId}/cancel`, null, {
      params: { reason }
    });
    return response.data;
  } catch (error) {
    console.error('Cancel event error:', error);
    throw new Error(error.response?.data || 'Не удалось отменить мероприятие');
  }
};

// Перенос мероприятия
export const rescheduleEvent = async (eventId, newDate, reason) => {
  try {
    const response = await apiClient.post(`/events/${eventId}/reschedule`, {
      newDate,
      reason
    });
    return response.data;
  } catch (error) {
    console.error('Reschedule event error:', error);
    throw new Error(error.response?.data || 'Не удалось перенести мероприятие');
  }
};

// Получение заявок на мероприятие
export const getEventApplications = async (eventId) => {
  try {
    const response = await apiClient.get(`/events/${eventId}/applications`);
    return response.data;
  } catch (error) {
    console.error('Get event applications error:', error);
    throw new Error(error.response?.data || 'Не удалось получить заявки на мероприятие');
  }
};

// Одобрение заявки
export const approveEventApplication = async (applicationId) => {
  try {
    const response = await apiClient.put(`/event-applications/${applicationId}/approve`);
    return response.data;
  } catch (error) {
    console.error('Approve event application error:', error);
    throw new Error(error.response?.data || 'Не удалось одобрить заявку');
  }
};

// Отклонение заявки
export const rejectEventApplication = async (applicationId) => {
  try {
    const response = await apiClient.put(`/event-applications/${applicationId}/reject`);
    return response.data;
  } catch (error) {
    console.error('Reject event application error:', error);
    throw new Error(error.response?.data || 'Не удалось отклонить заявку');
  }
};

// Отметка о посещении
export const markEventApplicationAttended = async (applicationId) => {
  try {
    const response = await apiClient.put(`/event-applications/${applicationId}/mark-attended`);
    return response.data;
  } catch (error) {
    console.error('Mark attended error:', error);
    throw new Error(error.response?.data || 'Не удалось отметить посещение');
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