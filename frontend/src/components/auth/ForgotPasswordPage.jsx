import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { requestPasswordReset } from '../../api/apiClient';
import { toast } from 'react-toastify';

const ForgotPasswordPage = () => {
  const [email, setEmail] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    const result = await requestPasswordReset(email);
    setIsLoading(false);

    if (result.success && result.message) {
      // The backend provides a generic success message for security
      toast.info(typeof result.message === 'string' ? result.message : "Если указанный email зарегистрирован, на него будет отправлено письмо с инструкциями по сбросу пароля.");
      setEmail(''); // Clear the form
    } else if (result.error) {
      toast.error(result.error);
    } else {
      // Fallback for unexpected response structure
      toast.error("Произошла ошибка. Пожалуйста, попробуйте позже.");
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen py-10 bg-[url('https://static.tildacdn.com/tild3264-3362-4861-b161-363561643235/svg_1.svg')] bg-top">
      <div className="w-full max-w-md px-4">
        <div className="text-center mb-8">
          <Link to="/" className="mb-4 text-5xl font-semibold text-white title-font">
            Книжная гавань
          </Link>
        </div>
        <div className="bg-[#494949] shadow-[0_25px_50px_-12px_rgba(0,0,0,0.25)] p-6 sm:p-8 rounded-3xl">
          <h1 className="text-xl md:text-2xl font-bold leading-tight tracking-tight text-white mb-6 text-center">
            Восстановление пароля
          </h1>
          <form onSubmit={handleSubmit} className="space-y-4 md:space-y-6">
            <div>
              <label htmlFor="email" className="block mb-2 text-sm font-medium text-white">
                Ваша почта
              </label>
              <input
                type="email"
                name="email"
                id="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="name@example.com"
                required
                className="w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-600 focus:border-yellow-600 placeholder-gray-400"
              />
            </div>
            <button
              type="submit"
              disabled={isLoading}
              className="w-full text-white bg-yellow-600 hover:bg-yellow-700 focus:ring-4 focus:outline-none focus:ring-yellow-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center disabled:opacity-50"
            >
              {isLoading ? 'Отправка...' : 'Отправить ссылку для сброса'}
            </button>
            <p className="text-sm font-light text-gray-400 text-center">
              Вспомнили пароль?{' '}
              <Link to="/auth" className="font-medium text-yellow-600 hover:underline">
                Войти
              </Link>
            </p>
          </form>
        </div>
      </div>
    </div>
  );
};

export default ForgotPasswordPage;
