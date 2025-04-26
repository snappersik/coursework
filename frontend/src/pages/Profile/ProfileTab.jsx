// src/pages/Profile/ProfileTab.jsx
import React, { useState } from 'react';
import { observer } from 'mobx-react-lite';
import { toast } from 'react-toastify';
import defaultAvatar from '../../assets/img/default-avatar.png';

const ProfileTab = observer(({ user, onUpdateProfile }) => {
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    firstName: user.firstName || '',
    lastName: user.lastName || '',
    patronymic: user.patronymic || '',
    phone: user.phone || '',
    address: user.address || '',
  });
  const [avatar, setAvatar] = useState(user.avatarUrl || defaultAvatar);
  const [isLoading, setIsLoading] = useState(false);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value
    }));
  };

  const handleAvatarChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        setAvatar(e.target.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSave = async () => {
    setIsLoading(true);
    const success = await onUpdateProfile({
      ...user,
      ...formData,
      avatarUrl: avatar
    });
    
    if (success) {
      toast.success('Профиль успешно обновлен');
      setIsEditing(false);
    } else {
      toast.error('Не удалось обновить профиль');
    }
    setIsLoading(false);
  };

  const handleCancel = () => {
    setFormData({
      firstName: user.firstName || '',
      lastName: user.lastName || '',
      patronymic: user.patronymic || '',
      phone: user.phone || '',
      address: user.address || '',
    });
    setAvatar(user.avatarUrl || defaultAvatar);
    setIsEditing(false);
  };

  return (
    <div className="flex flex-col md:flex-row gap-8">
      <div className="md:w-64 flex-shrink-0">
        <div className="relative w-48 h-48 mx-auto md:w-64 md:h-64 rounded-full overflow-hidden bg-gray-600 shadow-lg">
          <img src={avatar} alt="Аватар пользователя" className="w-full h-full object-cover" />
          {isEditing && (
            <label className="absolute inset-x-0 bottom-0 py-2 bg-black bg-opacity-70 text-white text-center cursor-pointer hover:bg-opacity-80 transition-colors">
              <input 
                type="file" 
                accept="image/*" 
                onChange={handleAvatarChange} 
                className="hidden" 
              />
              Изменить
            </label>
          )}
        </div>
      </div>
      
      <div className="flex-1">
        {isEditing ? (
          <div className="space-y-4">
            <div>
              <label className="block text-yellow-500 font-bold mb-1">Имя:</label>
              <input
                type="text"
                name="firstName"
                value={formData.firstName}
                onChange={handleInputChange}
                className="w-full p-3 bg-gray-800 border border-gray-600 rounded text-white focus:outline-none focus:ring-2 focus:ring-yellow-500"
              />
            </div>
            <div>
              <label className="block text-yellow-500 font-bold mb-1">Фамилия:</label>
              <input
                type="text"
                name="lastName"
                value={formData.lastName}
                onChange={handleInputChange}
                className="w-full p-3 bg-gray-800 border border-gray-600 rounded text-white focus:outline-none focus:ring-2 focus:ring-yellow-500"
              />
            </div>
            <div>
              <label className="block text-yellow-500 font-bold mb-1">Отчество:</label>
              <input
                type="text"
                name="patronymic"
                value={formData.patronymic}
                onChange={handleInputChange}
                className="w-full p-3 bg-gray-800 border border-gray-600 rounded text-white focus:outline-none focus:ring-2 focus:ring-yellow-500"
              />
            </div>
            <div>
              <label className="block text-yellow-500 font-bold mb-1">Телефон:</label>
              <input
                type="text"
                name="phone"
                value={formData.phone}
                onChange={handleInputChange}
                className="w-full p-3 bg-gray-800 border border-gray-600 rounded text-white focus:outline-none focus:ring-2 focus:ring-yellow-500"
              />
            </div>
            <div>
              <label className="block text-yellow-500 font-bold mb-1">Адрес:</label>
              <input
                type="text"
                name="address"
                value={formData.address}
                onChange={handleInputChange}
                className="w-full p-3 bg-gray-800 border border-gray-600 rounded text-white focus:outline-none focus:ring-2 focus:ring-yellow-500"
              />
            </div>
            <div>
              <label className="block text-yellow-500 font-bold mb-1">Почта:</label>
              <p className="p-3 bg-gray-800 border border-gray-600 rounded text-gray-400 italic">{user.email} (нельзя изменить)</p>
            </div>
            <div>
              <label className="block text-yellow-500 font-bold mb-1">Дата рождения:</label>
              <p className="p-3 bg-gray-800 border border-gray-600 rounded text-gray-400 italic">{user.birthDate || 'Не указано'}</p>
            </div>
            <div className="flex space-x-4 mt-6">
              <button 
                onClick={handleSave}
                disabled={isLoading}
                className="px-6 py-3 bg-green-600 text-white font-bold rounded hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isLoading ? 'Сохранение...' : 'Сохранить'}
              </button>
              <button 
                onClick={handleCancel}
                className="px-6 py-3 bg-red-600 text-white font-bold rounded hover:bg-red-700 transition-colors"
              >
                Отмена
              </button>
            </div>
          </div>
        ) : (
          <div className="space-y-4">
            <div className="flex border-b border-gray-600 pb-3">
              <span className="w-32 text-yellow-500 font-bold">Имя:</span>
              <span className="flex-1 text-white">{user.firstName || 'Не указано'}</span>
            </div>
            <div className="flex border-b border-gray-600 pb-3">
              <span className="w-32 text-yellow-500 font-bold">Фамилия:</span>
              <span className="flex-1 text-white">{user.lastName || 'Не указано'}</span>
            </div>
            <div className="flex border-b border-gray-600 pb-3">
              <span className="w-32 text-yellow-500 font-bold">Отчество:</span>
              <span className="flex-1 text-white">{user.patronymic || 'Не указано'}</span>
            </div>
            <div className="flex border-b border-gray-600 pb-3">
              <span className="w-32 text-yellow-500 font-bold">Телефон:</span>
              <span className="flex-1 text-white">{user.phone || 'Не указано'}</span>
            </div>
            <div className="flex border-b border-gray-600 pb-3">
              <span className="w-32 text-yellow-500 font-bold">Адрес:</span>
              <span className="flex-1 text-white">{user.address || 'Не указано'}</span>
            </div>
            <div className="flex border-b border-gray-600 pb-3">
              <span className="w-32 text-yellow-500 font-bold">Почта:</span>
              <span className="flex-1 text-white">{user.email}</span>
            </div>
            <div className="flex border-b border-gray-600 pb-3">
              <span className="w-32 text-yellow-500 font-bold">Дата рождения:</span>
              <span className="flex-1 text-white">{user.birthDate || 'Не указано'}</span>
            </div>
            <button 
              onClick={() => setIsEditing(true)}
              className="mt-6 px-6 py-3 bg-yellow-500 text-gray-900 font-bold rounded hover:bg-yellow-600 transition-colors"
            >
              Редактировать
            </button>
          </div>
        )}
      </div>
    </div>
  );
});

export default ProfileTab;
