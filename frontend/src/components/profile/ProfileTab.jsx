import React, { useState, useEffect, useCallback } from 'react';
import { observer } from 'mobx-react-lite';
import { toast } from 'react-toastify';
import defaultAvatar from '../../assets/img/default-avatar.png';
import ChangePasswordModal from '../modals/ChangePasswordModal';
import { uploadUserAvatar } from '../../api/apiClient';

const ProfileTab = observer(({ user, onUpdateProfile }) => {
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    patronymic: '',
    phone: '',
    address: '',
  });
  const [avatarPreview, setAvatarPreview] = useState(defaultAvatar);
  const [avatarFile, setAvatarFile] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [showChangePasswordModal, setShowChangePasswordModal] = useState(false);
  const [isDragging, setIsDragging] = useState(false);

  useEffect(() => {
    if (user) {
      setFormData({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        patronymic: user.patronymic || '',
        phone: user.phone || '',
        address: user.address || '',
      });
      // Формируем полный URL для аватара, если он приходит как относительный путь с бэкенда
      // и не является data URL (например, после выбора нового файла)
      const initialAvatar = user.avatarUrl
        ? (user.avatarUrl.startsWith('http') || user.avatarUrl.startsWith('data:')
          ? user.avatarUrl
          : `http://localhost:8080/api${user.avatarUrl}`)
        : defaultAvatar;

      setAvatarPreview(initialAvatar);
    }
  }, [user]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const processFile = (file) => {
    if (file) {
      // Проверка типа файла
      if (!file.type.startsWith('image/')) {
        toast.error('Можно загружать только изображения');
        return;
      }

      // Проверка размера файла (например, макс. 5MB)
      if (file.size > 5 * 1024 * 1024) {
        toast.error('Размер файла не должен превышать 5MB');
        return;
      }

      setAvatarFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setAvatarPreview(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleAvatarChange = (e) => {
    const file = e.target.files[0];
    processFile(file);
  };

  // Обработчики для drag-and-drop
  const handleDragEnter = useCallback((e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(true);
  }, []);

  const handleDragLeave = useCallback((e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
  }, []);

  const handleDragOver = useCallback((e) => {
    e.preventDefault();
    e.stopPropagation();
  }, []);

  const handleDrop = useCallback((e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);

    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      processFile(e.dataTransfer.files[0]);
    }
  }, []);

  const handleSave = async () => {
    setIsLoading(true);
    let finalAvatarUrl = user?.avatarUrl; // Начинаем с текущего URL аватара

    if (avatarFile) { // Если был выбран новый файл
      try {
        const uploadData = new FormData();
        uploadData.append('file', avatarFile);
        const uploadResponse = await uploadUserAvatar(uploadData); // Вызов API для загрузки аватара
        if (uploadResponse && uploadResponse.avatarUrl) {
          finalAvatarUrl = uploadResponse.avatarUrl; // Используем путь, возвращенный бэкендом
          toast.success('Аватар успешно загружен.');
        } else {
          throw new Error('Ответ сервера не содержит URL аватара.');
        }
      } catch (error) {
        console.error('Ошибка загрузки аватара:', error);
        toast.error(`Не удалось загрузить аватар: ${error.message || 'Произошла ошибка'}`);
        setIsLoading(false);
        return; // Прерываем сохранение, если загрузка аватара не удалась
      }
    }

    const profileDataToUpdate = {
      ...user, // Отправляем оригинальные ID, email и т.д.
      ...formData, // Отправляем обновленные поля формы
      avatarUrl: finalAvatarUrl, // Отправляем новый или существующий URL аватара (путь)
    };

    const success = await onUpdateProfile(profileDataToUpdate);

    if (success) {
      toast.success('Профиль успешно обновлен');
      setIsEditing(false);
      setAvatarFile(null); // Сбрасываем файл аватара после успешного сохранения
      if (finalAvatarUrl) { // Обновляем превью, если URL изменился (например, после новой загрузки)
        setAvatarPreview(finalAvatarUrl.startsWith('http') || finalAvatarUrl.startsWith('data:') ? finalAvatarUrl : `http://localhost:8080/api${finalAvatarUrl}`);
      }
    } else {
      toast.error('Не удалось обновить профиль');
    }
    setIsLoading(false);
  };

  const handleCancel = () => {
    if (user) {
      setFormData({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        patronymic: user.patronymic || '',
        phone: user.phone || '',
        address: user.address || '',
      });
      const initialAvatar = user.avatarUrl
        ? (user.avatarUrl.startsWith('http') || user.avatarUrl.startsWith('data:')
          ? user.avatarUrl
          : `http://localhost:8080/api${user.avatarUrl}`)
        : defaultAvatar;
      setAvatarPreview(initialAvatar);
    }
    setAvatarFile(null); // Очищаем выбранный файл
    setIsEditing(false);
  };

  if (!user) {
    return <div className="text-center p-8 text-white">Загрузка данных пользователя...</div>;
  }

  const displayAvatarUrl = avatarPreview;

  return (
    <div className="bg-gray-800 p-6 rounded-lg shadow-xl">
      <div className="flex flex-col items-center md:flex-row md:items-start">
        <div
          className={`relative mb-6 md:mb-0 md:mr-8 ${isDragging ? 'border-yellow-500 bg-gray-700' : ''}`}
          onDragEnter={isEditing ? handleDragEnter : undefined}
          onDragLeave={isEditing ? handleDragLeave : undefined}
          onDragOver={isEditing ? handleDragOver : undefined}
          onDrop={isEditing ? handleDrop : undefined}
        >
          <img
            src={displayAvatarUrl}
            alt="Аватар"
            className="w-40 h-40 rounded-full object-cover border-4 border-yellow-500"
            onError={(e) => { e.target.onerror = null; e.target.src = defaultAvatar; }} // Запасной вариант для битых ссылок
          />
          {isEditing && (
            <>
              <label htmlFor="avatarUpload" className="absolute bottom-0 right-0 bg-gray-700 p-2 rounded-full cursor-pointer hover:bg-gray-600">
                ✏️
                <input type="file" id="avatarUpload" className="hidden" accept="image/*" onChange={handleAvatarChange} />
              </label>
              {isDragging && (
                <div className="absolute inset-0 flex items-center justify-center bg-black bg-opacity-50 text-white text-xs text-center p-2 rounded-full">
                  Перетащите изображение сюда
                </div>
              )}
            </>
          )}
        </div>

        <div className="flex-1 w-full">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-5">
            <div>
              <label className="block text-sm font-medium text-gray-400">Email</label>
              <p className="mt-1 text-lg p-2.5 bg-gray-700 rounded-md break-all">{user.email}</p>
            </div>
            <div>
              <label htmlFor="firstName" className="block text-sm font-medium text-gray-400">Имя</label>
              {isEditing ? (
                <input type="text" name="firstName" id="firstName" value={formData.firstName} onChange={handleInputChange} className="mt-1 w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-500 focus:border-yellow-500" />
              ) : (
                <p className="mt-1 text-lg p-2.5 bg-gray-700 rounded-md min-h-[46px]">{formData.firstName || 'Не указано'}</p>
              )}
            </div>
            <div>
              <label htmlFor="lastName" className="block text-sm font-medium text-gray-400">Фамилия</label>
              {isEditing ? (
                <input type="text" name="lastName" id="lastName" value={formData.lastName} onChange={handleInputChange} className="mt-1 w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-500 focus:border-yellow-500" />
              ) : (
                <p className="mt-1 text-lg p-2.5 bg-gray-700 rounded-md min-h-[46px]">{formData.lastName || 'Не указано'}</p>
              )}
            </div>
            <div>
              <label htmlFor="patronymic" className="block text-sm font-medium text-gray-400">Отчество</label>
              {isEditing ? (
                <input type="text" name="patronymic" id="patronymic" value={formData.patronymic} onChange={handleInputChange} className="mt-1 w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-500 focus:border-yellow-500" />
              ) : (
                <p className="mt-1 text-lg p-2.5 bg-gray-700 rounded-md min-h-[46px]">{formData.patronymic || 'Не указано'}</p>
              )}
            </div>
            <div className="md:col-span-2">
              <label htmlFor="phone" className="block text-sm font-medium text-gray-400">Телефон</label>
              {isEditing ? (
                <input type="tel" name="phone" id="phone" value={formData.phone} onChange={handleInputChange} className="mt-1 w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-500 focus:border-yellow-500" />
              ) : (
                <p className="mt-1 text-lg p-2.5 bg-gray-700 rounded-md min-h-[46px]">{formData.phone || 'Не указано'}</p>
              )}
            </div>
            <div className="md:col-span-2">
              <label htmlFor="address" className="block text-sm font-medium text-gray-400">Адрес</label>
              {isEditing ? (
                <textarea name="address" id="address" value={formData.address} onChange={handleInputChange} rows="3" className="mt-1 w-full p-2.5 text-sm text-white bg-gray-700 border border-gray-600 rounded-lg focus:ring-yellow-500 focus:border-yellow-500"></textarea>
              ) : (
                <p className="mt-1 text-lg p-2.5 bg-gray-700 rounded-md min-h-[80px] break-words">{formData.address || 'Не указан'}</p>
              )}
            </div>
          </div>

          <div className="mt-8 flex flex-col sm:flex-row sm:flex-wrap gap-3">
            {isEditing ? (
              <>
                <button
                  onClick={handleSave}
                  disabled={isLoading}
                  className="px-6 py-2.5 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors disabled:opacity-50"
                >
                  {isLoading ? 'Сохранение...' : 'Сохранить'}
                </button>
                <button
                  onClick={() => setShowChangePasswordModal(true)}
                  className="px-6 py-2.5 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
                >
                  Сменить пароль
                </button>
                <button
                  onClick={handleCancel}
                  className="px-6 py-2.5 bg-gray-600 text-white rounded-lg hover:bg-gray-500 transition-colors"
                >
                  Отмена
                </button>
              </>
            ) : (
              <button
                onClick={() => setIsEditing(true)}
                className="px-6 py-2.5 bg-yellow-500 text-white rounded-lg hover:bg-yellow-600 transition-colors"
              >
                Редактировать профиль
              </button>
            )}
          </div>
        </div>
      </div>
      {showChangePasswordModal && user && (
        <ChangePasswordModal
          isOpen={showChangePasswordModal}
          onClose={() => setShowChangePasswordModal(false)}
          userEmail={user.email}
        />
      )}
    </div>
  );
});

export default ProfileTab;
