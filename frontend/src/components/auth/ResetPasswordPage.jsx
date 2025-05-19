import React, { useState, useEffect } from 'react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import { resetPasswordWithToken, validateResetToken } from '../../api/apiClient';
import { toast } from 'react-toastify';

const ResetPasswordPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token');

  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isTokenValid, setIsTokenValid] = useState(null); // null: validating, true: valid, false: invalid
  const [validationMessage, setValidationMessage] = useState('');

  useEffect(() => {
    if (!token) {
      toast.error('Токен для сброса пароля отсутствует.');
      setIsTokenValid(false);
      setValidationMessage('Ошибка: Токен не найден. Пожалуйста, запросите сброс пароля снова.');
      return;
    }

    const checkTokenValidity = async () => {
      const result = await validateResetToken(token);
      if (result.success) {
        setIsTokenValid(true);
        setValidationMessage(result.message || 'Токен действителен. Введите новый пароль.');
      } else {
        setIsTokenValid(false);
        setValidationMessage(result.error || 'Токен недействителен или срок его действия истек. Пожалуйста, запросите сброс пароля снова.');
        toast.error(result.error || 'Недействительный токен.');
      }
    };
    checkTokenValidity();
  }, [token]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (password !== confirmPassword) {
      toast.error('Пароли не совпадают.');
      return;
    }
    if (!token || !isTokenValid) {
      toast.error('Невозможно сбросить пароль. Токен недействителен.');
      return;
    }

    setIsLoading(true);
    const result = await resetPasswordWithToken(token, password, confirmPassword);
    setIsLoading(false);

    if (result.success) {
      toast.success(result.message || 'Пароль успешно изменен! Теперь вы можете войти с новым паролем.');
      navigate('/auth');
    } else {
      toast.error(result.error || 'Не удалось сбросить пароль.');
    }
  };
  
  if (isTokenValid === null) {
    return (
        <div className="flex items-center justify-center min-h-screen py-10 bg-[url('https://static.tildacdn.com/tild3264-3362-4861-b161-363561643235/svg_1.svg')] bg-top text-white">
            Проверка токена...
        </div>
    );
  }

  return (
    <div className="flex items-center justify-center min-h-screen py-10 bg-[url('https://static.tildacdn.com/tild3264-3362-4861-b161-363561643235/svg_1.svg')] bg-top">
      <div className="w-full max-w-md px-4">
        <div className="text-center mb-8">
          <Link to="/" className="mb-4 text-5xl font-semibold text-white title-font">
            Книжная гавань
          </Link>
        </div>
        <div className="bg-[#494949] shadow-[0_25px_50px_-12px_rgba(0,0,0,0.25)] p-6 sm:p-8 rounded-3xl">
          <h1 className="text-xl md:text-2xl font-bold leading-tight tracking-tight text-white mb-4 text-center">
            Установка нового пароля
          </h1>
          
          {validationMessage && (
            <p className={`mb-4 text-sm text-center ${isTokenValid ? 'text-green-400' : 'text-red-400'}`}>
              {validationMessage}
            </p>
          )}

          {isTokenValid && (
            <form onSubmit={handleSubmit} className="space-y-4 md:space-y-6">
              <div>
                <label htmlFor="newPassword" className="block mb-2 text-sm font-medium text-white">
                  Новый пароль
                </label>
                <input
                  type="password"
                  name="newPassword"
                  id="newPassword"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  required
                  className="w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-600 focus:border-yellow-600 placeholder-gray-400"
                />
              </div>
              <div>
                <label htmlFor="confirmPassword" className="block mb-2 text-sm font-medium text-white">
                  Подтвердите новый пароль
                </label>
                <input
                  type="password"
                  name="confirmPassword"
                  id="confirmPassword"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="••••••••"
                  required
                  className="w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-600 focus:border-yellow-600 placeholder-gray-400"
                />
              </div>
              <button
                type="submit"
                disabled={isLoading || !isTokenValid}
                className="w-full text-white bg-yellow-600 hover:bg-yellow-700 focus:ring-4 focus:outline-none focus:ring-yellow-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center disabled:opacity-50"
              >
                {isLoading ? 'Сохранение...' : 'Сохранить новый пароль'}
              </button>
            </form>
          )}
           <p className="text-sm font-light text-gray-400 text-center mt-4">
              <Link to="/auth" className="font-medium text-yellow-600 hover:underline">
                Вернуться ко входу
              </Link>
            </p>
        </div>
      </div>
    </div>
  );
};

export default ResetPasswordPage;
