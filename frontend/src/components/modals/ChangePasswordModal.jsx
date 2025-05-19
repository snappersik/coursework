import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { toast } from 'react-toastify';
import { changeUserPassword, requestPasswordReset } from '../../api/apiClient';


const ChangePasswordModal = ({ isOpen, onClose, userEmail }) => {
  const [step, setStep] = useState(1); // 1: ввод старого пароля, 2: ввод нового пароля
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmNewPassword, setConfirmNewPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  
  const [attempts, setAttempts] = useState(0);
  const MAX_ATTEMPTS_OLD_PASSWORD = 5;
  const SUGGEST_FORGOT_ATTEMPTS = 3;
  const LOCKOUT_DURATION_MS = 3 * 60 * 60 * 1000; // 3 часа
  const [lockoutEndTime, setLockoutEndTime] = useState(0);

  useEffect(() => {
    // Сбрасываем состояние при открытии/закрытии модального окна или изменении шага
    if (!isOpen) {
      setTimeout(() => { // Задержка сброса для анимации выхода
        setStep(1);
        setOldPassword('');
        setNewPassword('');
        setConfirmNewPassword('');
        setError('');
        setAttempts(0);
        // Не сбрасываем lockoutEndTime здесь, он должен сохраняться, если модальное окно быстро переоткрывается
      }, 300);
    } else {
         // Проверяем, не заблокирован ли пользователь
        const storedLockoutTime = localStorage.getItem(`lockout_${userEmail}`);
        if (storedLockoutTime && Date.now() < parseInt(storedLockoutTime, 10)) {
            setLockoutEndTime(parseInt(storedLockoutTime, 10));
        } else {
            localStorage.removeItem(`lockout_${userEmail}`); // Очищаем истекшую блокировку
            setLockoutEndTime(0);
        }
    }
  }, [isOpen, userEmail]);

  useEffect(() => {
    let timer;
    if (lockoutEndTime > Date.now()) {
      timer = setTimeout(() => {
        setLockoutEndTime(0); // Разблокировка
        localStorage.removeItem(`lockout_${userEmail}`);
        setAttempts(0); // Сбрасываем попытки после блокировки
        setError('');
        toast.info("Блокировка на смену пароля снята.");
      }, lockoutEndTime - Date.now());
    }
    return () => clearTimeout(timer);
  }, [lockoutEndTime, userEmail]);


  const handleOldPasswordSubmit = async (e) => {
    e.preventDefault();
    if (Date.now() < lockoutEndTime) {
      const remainingTime = Math.ceil((lockoutEndTime - Date.now()) / (60 * 1000));
      setError(`Слишком много попыток. Попробуйте снова через ${remainingTime} мин.`);
      return;
    }
    setError('');
    setIsLoading(true);

    // Для этого примера, если пользователь вводит "incorrect", это считается неправильным паролем
    if (oldPassword === "incorrect") { // Симуляция неправильного пароля
        const newAttempts = attempts + 1;
        setAttempts(newAttempts);
        let message = "Неверный пароль.";
        if (newAttempts >= SUGGEST_FORGOT_ATTEMPTS && newAttempts < MAX_ATTEMPTS_OLD_PASSWORD) {
            message += " Попробуйте 'Забыли пароль?'.";
        }
        setError(message);

        if (newAttempts >= MAX_ATTEMPTS_OLD_PASSWORD) {
            const newLockoutTime = Date.now() + LOCKOUT_DURATION_MS;
            setLockoutEndTime(newLockoutTime);
            localStorage.setItem(`lockout_${userEmail}`, newLockoutTime.toString());
            setError(`Слишком много попыток. Попробуйте снова через ${LOCKOUT_DURATION_MS / (60 * 1000)} мин или используйте 'Забыли пароль?'.`);
            toast.error("Вы временно заблокированы от смены пароля из-за множества неудачных попыток.");
        }
    } else {
        // Предполагаем, что пароль верный, чтобы продолжить.
        // В реальном приложении: await api.verifyOldPassword(oldPassword)
        setStep(2);
        setError('');
        setAttempts(0); // Сбрасываем попытки при успешном вводе старого пароля
    }
    setIsLoading(false);
  };

  const handleNewPasswordSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (newPassword !== confirmNewPassword) {
      setError('Новый пароль и подтверждение не совпадают.');
      return;
    }
    if (newPassword.length < 6) { // Базовая валидация
        setError('Пароль должен содержать не менее 6 символов.');
        return;
    }

    setIsLoading(true);
    // `oldPassword` - это пароль, введенный и "проверенный" на шаге 1
    const result = await changeUserPassword(oldPassword, newPassword, confirmNewPassword);
    setIsLoading(false);

    if (result.success) {
      toast.success(result.message || 'Пароль успешно изменен!');
      onClose();
    } else {
      // Если ошибка от changeUserPassword указывает, что старый пароль был неверным,
      // мы можем вернуться к шагу 1 и увеличить счетчик попыток.
      // Это зависит от сообщений об ошибках с бэкенда.
      if (result.error && result.error.toLowerCase().includes("неверный старый пароль")) {
          setStep(1);
          const newAttempts = attempts + 1;
          setAttempts(newAttempts);
          let message = "Неверный старый пароль.";
          if (newAttempts >= SUGGEST_FORGOT_ATTEMPTS && newAttempts < MAX_ATTEMPTS_OLD_PASSWORD) {
              message += " Попробуйте 'Забыли пароль?'.";
          }
          setError(message);
          if (newAttempts >= MAX_ATTEMPTS_OLD_PASSWORD) {
              const newLockoutTime = Date.now() + LOCKOUT_DURATION_MS;
              setLockoutEndTime(newLockoutTime);
              localStorage.setItem(`lockout_${userEmail}`, newLockoutTime.toString());
              setError(`Слишком много попыток. Попробуйте снова через ${LOCKOUT_DURATION_MS / (60 * 1000)} мин или используйте 'Забыли пароль?'.`);
          }
      } else {
          setError(result.error || 'Не удалось изменить пароль.');
      }
    }
  };

  const handleForgotPasswordLink = async () => {
    setIsLoading(true);
    const result = await requestPasswordReset(userEmail);
    setIsLoading(false);
    if (result.success && result.message) {
         toast.info(typeof result.message === 'string' ? result.message : "Если ваш email зарегистрирован, на него будет отправлено письмо.");
    } else if(result.error) {
         toast.error(result.error);
    } else {
        toast.info("Если ваш email зарегистрирован, на него будет отправлено письмо с инструкциями по сбросу пароля.");
    }
    onClose(); // Закрываем модальное окно смены пароля
  };
  
  const isLocked = Date.now() < lockoutEndTime;
  const remainingLockoutTime = isLocked ? Math.ceil((lockoutEndTime - Date.now()) / (60 * 1000)) : 0;


  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <motion.div
            className="fixed inset-0 bg-black bg-opacity-60"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
          />
          <motion.div
            className="bg-[#404040] text-white rounded-lg shadow-xl overflow-hidden w-full max-w-md relative z-10 p-6 sm:p-8"
            initial={{ scale: 0.9, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            exit={{ scale: 0.9, opacity: 0 }}
            transition={{ type: 'spring', damping: 20 }}
          >
            {step === 1 && (
              <>
                <h3 className="text-xl font-bold text-yellow-400 mb-6 text-center">Проверка старого пароля</h3>
                <form onSubmit={handleOldPasswordSubmit} className="space-y-5">
                  <div>
                    <label htmlFor="oldPassword" className="block mb-1.5 text-sm font-medium text-gray-300">
                      Старый пароль
                    </label>
                    <input
                      type="password"
                      id="oldPassword"
                      value={oldPassword}
                      onChange={(e) => setOldPassword(e.target.value)}
                      required
                      disabled={isLocked || isLoading}
                      className="w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-500 focus:border-yellow-500 placeholder-gray-400 disabled:opacity-50"
                    />
                  </div>
                  {error && <p className="text-sm text-red-400 text-center">{error}</p>}
                  {isLocked && <p className="text-sm text-orange-400 text-center">Повторная попытка будет доступна через {remainingLockoutTime} мин.</p>}

                  <button
                    type="submit"
                    disabled={isLoading || isLocked}
                    className="w-full text-white bg-yellow-600 hover:bg-yellow-700 focus:ring-4 focus:outline-none focus:ring-yellow-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center disabled:opacity-60"
                  >
                    {isLoading ? 'Проверка...' : 'Проверить'}
                  </button>
                  <div className="text-center mt-3">
                    <button 
                      type="button" 
                      onClick={handleForgotPasswordLink}
                      className="text-sm font-medium text-yellow-500 hover:underline disabled:text-gray-400"
                      disabled={isLoading}
                    >
                      Забыли пароль?
                    </button>
                  </div>
                </form>
              </>
            )}

            {step === 2 && (
              <>
                <h3 className="text-xl font-bold text-yellow-400 mb-6 text-center">Установка нового пароля</h3>
                <form onSubmit={handleNewPasswordSubmit} className="space-y-5">
                  <div>
                    <label htmlFor="newPasswordModal" className="block mb-1.5 text-sm font-medium text-gray-300">
                      Новый пароль
                    </label>
                    <input
                      type="password"
                      id="newPasswordModal"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      required
                      minLength="6"
                      disabled={isLoading}
                      className="w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-500 focus:border-yellow-500 placeholder-gray-400 disabled:opacity-50"
                    />
                  </div>
                  <div>
                    <label htmlFor="confirmNewPasswordModal" className="block mb-1.5 text-sm font-medium text-gray-300">
                      Подтвердите новый пароль
                    </label>
                    <input
                      type="password"
                      id="confirmNewPasswordModal"
                      value={confirmNewPassword}
                      onChange={(e) => setConfirmNewPassword(e.target.value)}
                      required
                      minLength="6"
                      disabled={isLoading}
                      className="w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-500 focus:border-yellow-500 placeholder-gray-400 disabled:opacity-50"
                    />
                  </div>
                  {error && <p className="text-sm text-red-400 text-center">{error}</p>}
                  <button
                    type="submit"
                    disabled={isLoading}
                    className="w-full text-white bg-green-600 hover:bg-green-700 focus:ring-4 focus:outline-none focus:ring-green-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center disabled:opacity-60"
                  >
                    {isLoading ? 'Изменение...' : 'Изменить пароль'}
                  </button>
                </form>
              </>
            )}
            <button
                onClick={onClose}
                className="mt-6 w-full text-gray-300 bg-gray-600 hover:bg-gray-500 focus:ring-4 focus:outline-none focus:ring-gray-400 font-medium rounded-lg text-sm px-5 py-2.5 text-center"
            >
                Отмена
            </button>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};

export default ChangePasswordModal;
