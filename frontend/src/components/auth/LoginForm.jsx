import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login, testAuthentication } from '../../api/apiClient';
import { authStore } from '../../store/store.js';

// Функция для преобразования ID роли в её строковое название


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
      case 1:
        return 'USER';
      case 2:
        return 'ORGANIZER';
      case 3:
        return 'ADMIN';
      default:
        return 'USER'; // По умолчанию, если ID не распознан
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const result = await login(formData.login, formData.password);

      if (result.success && result.userInfo) {
        // Логирование для отладки
        console.log('Данные пользователя:', result.userInfo);
        console.log('Роль пользователя:', result.userInfo.role);

        // Извлечение ID роли
        let roleId;
        if (typeof result.userInfo.role === 'number') {
          roleId = result.userInfo.role; // Если role — просто число (ID)
        } else if (result.userInfo.role && typeof result.userInfo.role === 'object') {
          roleId = result.userInfo.role.id; // Если role — объект с полем id
        } else {
          roleId = 1; // По умолчанию ID для USER
        }

        // Преобразование ID в строковое название роли
        const userRole = getRoleNameById(roleId);
        console.log('Устанавливаемая роль:', userRole);

        // Сохранение роли и установка состояния авторизации
        localStorage.setItem('userRole', userRole);
        authStore.setAuthorized(true, userRole);

        await testAuthentication();
        navigate('/');
      } else {
        setError(result.error || 'Ошибка входа');
      }
    } catch (err) {
      console.error('Исключение при входе:', err);
      setError('Произошла ошибка при входе');
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
            <label htmlFor="login" className="block mb-2 text-sm font-medium text-white">
              Почта
            </label>
            <input
                type="text"
                name="login"
                id="login"
                value={formData.login}
                onChange={handleChange}
                placeholder="name@gmail.com"
                required
                className="w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-600 focus:border-yellow-600 placeholder-gray-400"
            />
          </div>
          <div>
            <label htmlFor="password" className="block mb-2 text-sm font-medium text-white">
              Пароль
            </label>
            <input
                type="password"
                name="password"
                id="password"
                value={formData.password}
                onChange={handleChange}
                placeholder="••••••••"
                required
                className="w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-600 focus:border-yellow-600 placeholder-gray-400"
            />
          </div>
          {error && <p className="text-sm text-red-500">{error}</p>}
          <div className="flex justify-center mt-2">
            <button
                type="submit"
                disabled={isLoading}
                className="w-full sm:w-auto text-white bg-yellow-600 hover:bg-yellow-700 focus:ring-4 focus:outline-none focus:ring-yellow-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center"
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