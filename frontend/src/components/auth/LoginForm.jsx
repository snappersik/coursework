import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { login, testAuthentication } from '../../api/apiClient';
import { authStore } from '../../store/store.js';

const LoginForm = ({ onSwitchToRegister }) => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({ login: '', password: '' });
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setError('');
  };

  const getRoleNameById = (roleId) => {
    switch (roleId) {
      case 1: return 'USER';
      case 2: return 'ORGANIZER';
      case 3: return 'ADMIN';
      default: return 'USER';
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    // Пропускаем проверку формата email, если введен "admin"
    if (formData.login !== 'admin') {
      // Проверка формата email только для не-admin пользователей
      const emailRegex = /\S+@\S+\.\S+/;
      if (!emailRegex.test(formData.login)) {
        setError('Пожалуйста, введите корректный email');
        setIsLoading(false); // Убедись, что isLoading сбрасывается здесь
        return;
      }
    }
    
    setIsLoading(true); // Устанавливаем isLoading перед запросом

    try {
      const result = await login(formData.login, formData.password);

      if (result.success && result.userInfo) {
        console.log('Данные пользователя:', result.userInfo);

        let roleId = result.userInfo.role?.id || result.userInfo.role || 1; // Обработка случая, когда role может быть числом или объектом
        const userRole = getRoleNameById(roleId);
        const userId = result.userInfo.id;

        if (!userId) {
          // Это не должно произойти, если result.userInfo есть и содержит id
          throw new Error('Не удалось получить ID пользователя с сервера');
        }

        authStore.setAuthorized(true, userRole, userId);
        // await testAuthentication(); // Раскомментируй, если testAuthentication актуальна
        navigate('/');
      } else {
        setError(result.error || 'Ошибка входа');
      }
    } catch (err) {
      console.error('Исключение при входе:', err);
      setError(err.message || 'Произошла ошибка при входе');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="space-y-4 md:space-y-6">
      <h1 className="text-xl md:text-2xl font-bold leading-tight tracking-tight text-white">
        Войти в аккаунт
      </h1>
      <form onSubmit={handleSubmit} className="space-y-4 md:space-y-6">
        <div>
          <label htmlFor="loginInput" className="block mb-2 text-sm font-medium text-white">
            Логин или Email
          </label>
          <input
            type="text" // ИЗМЕНЕНО с "email" на "text"
            name="login"
            id="loginInput"
            value={formData.login}
            onChange={handleChange}
            placeholder="name@gmail.com"
            required
            className="w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-600 focus:border-yellow-600 placeholder-gray-400"
            autoComplete="username" // Можно изменить на "username" для лучшей семантики
          />
        </div>
        <div>
          <label htmlFor="passwordInput" className="block mb-2 text-sm font-medium text-white">
            Пароль
          </label>
          <input
            type="password"
            name="password"
            id="passwordInput"
            value={formData.password}
            onChange={handleChange}
            placeholder="••••••••"
            required
            className="w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-600 focus:border-yellow-600 placeholder-gray-400"
            autoComplete="current-password"
          />
        </div>
        {error && <p className="text-sm text-red-500">{error}</p>}
        <div className="flex items-center justify-between">
          <Link to="/forgot-password" className="text-sm font-medium text-yellow-500 hover:underline">
            Забыли пароль?
          </Link>
        </div>
        <div className="flex justify-center mt-2">
          <button
            type="submit"
            disabled={isLoading}
            className="w-full sm:w-auto text-white bg-yellow-600 hover:bg-yellow-700 focus:ring-4 focus:outline-none focus:ring-yellow-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center disabled:opacity-50"
          >
            {isLoading ? 'Вход...' : 'Войти'}
          </button>
        </div>
        <p className="text-sm font-light text-gray-400">
          Нет аккаунта?{' '}
          <button
            type="button"
            onClick={onSwitchToRegister}
            className="font-medium text-yellow-600 hover:underline"
          >
            Зарегистрироваться
          </button>
        </p>
      </form>
    </div>
  );
};

export default LoginForm;
