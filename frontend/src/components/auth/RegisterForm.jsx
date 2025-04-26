import React, { useState, useRef } from 'react';
import { useForm } from 'react-hook-form';
import DatePicker, { registerLocale } from 'react-datepicker';
import { ru } from 'date-fns/locale/ru';
import 'react-datepicker/dist/react-datepicker.css';
import { useNavigate } from 'react-router-dom';
import { registerUser } from '../../api/apiClient';
import { authStore } from '../../store/store.js';

registerLocale('ru', ru);

const RegisterForm = ({ onSwitchToLogin }) => {
  const navigate = useNavigate();
  const { register, handleSubmit, setValue, watch, formState: { errors } } = useForm({
    defaultValues: {
      email: '',
      firstName: '',
      lastName: '',
      patronymic: '',
      phone: '',
      address: '',
      birthDate: '',
      password: '',
      confirmPassword: '',
    },
  });
  const [startDate, setStartDate] = useState(null);
  const [birthDateValue, setBirthDateValue] = useState('');
  const phoneInputRef = useRef(null);

  const maxDate = new Date();
  maxDate.setFullYear(maxDate.getFullYear() - 14);

  const handleDateChange = (date) => {
    setStartDate(date);
    if (date) {
      const day = date.getDate().toString().padStart(2, '0');
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const year = date.getFullYear();
      const formattedDate = `${day}.${month}.${year}`;
      setValue('birthDate', formattedDate);
      setBirthDateValue(formattedDate);
    } else {
      setValue('birthDate', '');
      setBirthDateValue('');
    }
  };

  const handleBirthDateInputChange = (e) => {
    const value = e.target.value;
    setBirthDateValue(value);
    if (value && value.match(/^\d{2}\.\d{2}\.\d{4}$/)) {
      const [day, month, year] = value.split('.');
      const newDate = new Date(year, month - 1, day);
      if (!isNaN(newDate.getTime())) {
        setStartDate(newDate);
        setValue('birthDate', value);
      }
    } else {
      setValue('birthDate', value);
    }
  };

  const formatPhoneNumber = (value) => {
    if (!value) return '';
    const digitsOnly = value.replace(/\D/g, '');
    const limitedDigits = digitsOnly.substring(0, 11);
    let formatted = '';
    if (limitedDigits.length > 0) {
      formatted = '+' + limitedDigits[0];
      if (limitedDigits.length > 1) {
        formatted += ' (' + limitedDigits.substring(1, Math.min(4, limitedDigits.length));
        if (limitedDigits.length > 4) {
          formatted += ') ' + limitedDigits.substring(4, Math.min(7, limitedDigits.length));
          if (limitedDigits.length > 7) {
            formatted += ' ' + limitedDigits.substring(7, Math.min(9, limitedDigits.length));
            if (limitedDigits.length > 9) {
              formatted += '-' + limitedDigits.substring(9, 11);
            }
          }
        }
      }
    }
    return formatted;
  };

  const handlePhoneChange = (e) => {
    const value = e.target.value;
    const digitsOnly = value.replace(/\D/g, '').substring(0, 11);
    const formatted = formatPhoneNumber(digitsOnly);
    setValue('phone', digitsOnly);
    if (phoneInputRef.current) {
      phoneInputRef.current.value = formatted;
    }
  };

  const onFormSubmit = async (data) => {
    const userData = {
      email: data.email,
      firstName: data.firstName,
      lastName: data.lastName,
      patronymic: data.patronymic,
      phone: data.phone,
      address: data.address,
      birthDate: data.birthDate,
    };

    const registerData = {
      userData,
      password: data.password,
      confirmPassword: data.confirmPassword,
    };

    try {
      const result = await registerUser(registerData);

      if (result.success) {
        const role = 'USER';
        localStorage.setItem('userRole', role);
        authStore.setAuthorized(true, role);
        navigate('/');
      } else {
        alert(result.error || 'Ошибка регистрации');
      }
    } catch (err) {
      const errorMessage = err.response?.data || 'Произошла ошибка при регистрации';
      alert(errorMessage);
    }
  };

  return (
      <div className="space-y-4 md:space-y-6">
        <h1 className="text-xl md:text-2xl font-bold leading-tight tracking-tight text-white">
          Создать аккаунт
        </h1>
        <form onSubmit={handleSubmit(onFormSubmit)} className="space-y-4 md:space-y-6">
          <div>
            <label htmlFor="email" className="block mb-2 text-sm font-medium text-white">
              Ваша почта
            </label>
            <input
                id="email"
                {...register('email', { required: 'Email обязателен', pattern: { value: /^\S+@\S+$/i, message: 'Неверный формат email' } })}
                placeholder="name@gmail.com"
                className="w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-600 focus:border-yellow-600 placeholder-gray-400"
            />
            {errors.email && <span className="text-sm text-red-500">{errors.email.message}</span>}
          </div>
          <div>
            <label htmlFor="firstName" className="block mb-2 text-sm font-medium text-white">
              Имя
            </label>
            <input
                id="firstName"
                {...register('firstName', { required: 'Имя обязательно' })}
                placeholder="Имя *"
                className="w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-600 focus:border-yellow-600 placeholder-gray-400"
            />
            {errors.firstName && <span className="text-sm text-red-500">{errors.firstName.message}</span>}
          </div>
          <div>
            <label htmlFor="lastName" className="block mb-2 text-sm font-medium text-white">
              Фамилия
            </label>
            <input
                id="lastName"
                {...register('lastName', { required: 'Фамилия обязательна' })}
                placeholder="Фамилия *"
                className="w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-600 focus:border-yellow-600 placeholder-gray-400"
            />
            {errors.lastName && <span className="text-sm text-red-500">{errors.lastName.message}</span>}
          </div>
          <div>
            <label htmlFor="patronymic" className="block mb-2 text-sm font-medium text-white">
              Отчество
            </label>
            <input
                id="patronymic"
                {...register('patronymic')}
                placeholder="Отчество"
                className="w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-600 focus:border-yellow-600 placeholder-gray-400"
            />
          </div>
          <div>
            <label htmlFor="phone" className="block mb-2 text-sm font-medium text-white">
              Телефон
            </label>
            <input
                id="phone"
                type="text"
                ref={phoneInputRef}
                onChange={handlePhoneChange}
                placeholder="+7 (___) ___ __-__"
                className="w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-600 focus:border-yellow-600 placeholder-gray-400"
            />
            <input
                type="hidden"
                {...register('phone', { required: 'Телефон обязателен', minLength: { value: 11, message: 'Номер должен содержать 11 цифр' } })}
            />
            {errors.phone && <span className="text-sm text-red-500">{errors.phone.message}</span>}
          </div>
          <div>
            <label htmlFor="address" className="block mb-2 text-sm font-medium text-white">
              Адрес
            </label>
            <input
                id="address"
                {...register('address')}
                placeholder="Адрес"
                className="w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-600 focus:border-yellow-600 placeholder-gray-400"
            />
          </div>
          <div>
            <label htmlFor="birthDate" className="block mb-2 text-sm font-medium text-white">
              Дата рождения
            </label>
            <DatePicker
                selected={startDate}
                onChange={handleDateChange}
                maxDate={maxDate}
                dateFormat="dd.MM.yyyy"
                locale="ru"
                customInput={
                  <input
                      id="birthDate"
                      value={birthDateValue}
                      onChange={handleBirthDateInputChange}
                      placeholder="Дата рождения (дд.мм.гггг) *"
                      className="w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-600 focus:border-yellow-600 placeholder-gray-400"
                  />
                }
            />
            {errors.birthDate && <span className="text-sm text-red-500">{errors.birthDate.message}</span>}
          </div>
          <div>
            <label htmlFor="password" className="block mb-2 text-sm font-medium text-white">
              Пароль
            </label>
            <input
                id="password"
                type="password"
                {...register('password', { required: 'Пароль обязателен', minLength: { value: 6, message: 'Минимум 6 символов' } })}
                placeholder="••••••••"
                className="w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-600 focus:border-yellow-600 placeholder-gray-400"
            />
            {errors.password && <span className="text-sm text-red-500">{errors.password.message}</span>}
          </div>
          <div>
            <label htmlFor="confirmPassword" className="block mb-2 text-sm font-medium text-white">
              Подтвердите пароль
            </label>
            <input
                id="confirmPassword"
                type="password"
                {...register('confirmPassword', {
                  required: 'Подтверждение пароля обязательно',
                  validate: (value) => value === watch('password') || 'Пароли не совпадают',
                })}
                placeholder="••••••••"
                className="w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-600 focus:border-yellow-600 placeholder-gray-400"
            />
            {errors.confirmPassword && <span className="text-sm text-red-500">{errors.confirmPassword.message}</span>}
          </div>
          <div className="flex justify-center mt-2">
            <button
                type="submit"
                className="w-full sm:w-auto text-white bg-yellow-600 hover:bg-yellow-700 focus:ring-4 focus:outline-none focus:ring-yellow-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center"
            >
              Создать аккаунт
            </button>
          </div>
          <p className="text-sm font-light text-gray-400">
            Уже есть аккаунт?{' '}
            <button
                type="button"
                onClick={onSwitchToLogin}
                className="font-medium text-yellow-600 hover:underline"
            >
              Войти
            </button>
          </p>
        </form>
      </div>
  );
};

export default RegisterForm;