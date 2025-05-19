import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import { toast } from 'react-toastify';
import { API_URL } from '../../../config';
import { FaSort, FaSortAlphaDown, FaSortAlphaUp, FaTrashRestore, FaTrash, FaPen, FaUserPlus } from 'react-icons/fa';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';

const UserManager = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [currentUser, setCurrentUser] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [sortConfig, setSortConfig] = useState({ key: null, direction: 'asc' });
  const [softDeletedUsers, setSoftDeletedUsers] = useState({});
  const [formData, setFormData] = useState({
    email: '',
    firstName: '',
    lastName: '',
    patronymic: '',
    phone: '',
    address: '',
    birthDate: null,
    role: 'USER',
    password: '',
    confirmPassword: ''
  });

  const getRoleIdByName = (roleName) => {
    switch (roleName) {
      case 'USER': return 1;
      case 'ORGANIZER': return 2;
      case 'ADMIN': return 3;
      default: return 1;
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${API_URL}/users`, {
        withCredentials: true,
        params: { populate: 'role' }
      });
      setUsers(response.data);
      setLoading(false);
    } catch (error) {
      console.error('Ошибка загрузки пользователей:', error);
      toast.error('Не удалось загрузить пользователей');
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleDateChange = (date) => {
    setFormData({
      ...formData,
      birthDate: date ? formatDate(date) : null
    });
  };

  const formatDate = (date) => {
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return `${day}.${month}.${year}`;
  };

  const parseDate = (dateString) => {
    if (!dateString) return null;
    const [day, month, year] = dateString.split('.');
    return new Date(year, month - 1, day);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (currentUser) {
        // При обновлении не отправляем пароль
        const roleIdForUpdate = getRoleIdByName(formData.role);
        const updateData = {
          id: currentUser.id,
          email: formData.email,
          firstName: formData.firstName,
          lastName: formData.lastName,
          patronymic: formData.patronymic || null,
          phone: formData.phone || null,
          address: formData.address || null,
          birthDate: formData.birthDate,
          role: { id: roleIdForUpdate }
        };

        await axios.put(`${API_URL}/users/${currentUser.id}`, updateData, {
          withCredentials: true,
        });
        toast.success('Пользователь успешно обновлен');
      } else {
        // Проверки для нового пользователя
        if (!formData.email || !formData.password || !formData.firstName || !formData.lastName || !formData.birthDate) {
          toast.error("Email, пароль, имя, фамилия и дата рождения обязательны.");
          return;
        }

        if (formData.password !== formData.confirmPassword) {
          toast.error('Пароли не совпадают!');
          return;
        }

        const roleId = getRoleIdByName(formData.role);
        const registerData = {
          userData: {
            email: formData.email,
            firstName: formData.firstName,
            lastName: formData.lastName,
            patronymic: formData.patronymic || null,
            phone: formData.phone || null,
            address: formData.address || null,
            birthDate: formData.birthDate,
            role: { id: roleId }
          },
          password: formData.password
        };

        await axios.post(`${API_URL}/users/create`, registerData, {
          withCredentials: true,
        });
        toast.success('Пользователь успешно создан');
      }

      setShowModal(false);
      fetchUsers();
    } catch (error) {
      console.error('Ошибка сохранения пользователя:', error);
      if (error.response && error.response.data) {
        const message = typeof error.response.data.message === 'string'
          ? error.response.data.message
          : (error.response.data.error || 'Не удалось сохранить пользователя');

        if (error.response.data.details && typeof error.response.data.details === 'object') {
          const details = Object.entries(error.response.data.details)
            .map(([key, value]) => `${key}: ${value}`)
            .join('; ');
          toast.error(`${message}. Детали: ${details}`);
        } else {
          toast.error(message);
        }
      } else {
        toast.error('Не удалось сохранить пользователя');
      }
    }
  };


  const handleEdit = (user) => {
    setCurrentUser(user);
    setFormData({
      email: user.email,
      firstName: user.firstName || '',
      lastName: user.lastName || '',
      patronymic: user.patronymic || '',
      phone: user.phone || '',
      address: user.address || '',
      birthDate: user.birthDate || null,
      role: user.role?.name || 'USER',
      password: '',
      confirmPassword: ''
    });
    setShowModal(true);
  };

  const handleSoftDelete = (id) => {
    setSoftDeletedUsers(prev => ({
      ...prev,
      [id]: true
    }));
    toast.warning('Пользователь помечен на удаление');
  };

  const handleRestore = (id) => {
    setSoftDeletedUsers(prev => {
      const newState = { ...prev };
      delete newState[id];
      return newState;
    });
    toast.success('Пользователь восстановлен');
  };

  const handleDelete = async (id) => {
    if (window.confirm('Вы уверены, что хотите удалить этого пользователя? Это действие нельзя отменить.')) {
      try {
        await axios.delete(`${API_URL}/users/${id}`, { withCredentials: true });
        toast.success('Пользователь успешно удален');
        fetchUsers();
        setSoftDeletedUsers(prev => {
          const newState = { ...prev };
          delete newState[id];
          return newState;
        });
      } catch (error) {
        console.error('Ошибка удаления пользователя:', error);
        toast.error('Не удалось удалить пользователя');
      }
    }
  };

  const handleAddNew = () => {
    setCurrentUser(null);
    setFormData({
      email: '',
      firstName: '',
      lastName: '',
      patronymic: '',
      phone: '',
      address: '',
      birthDate: null,
      role: 'USER',
      password: '',
      confirmPassword: ''
    });
    setShowModal(true);
  };

  // Функция сортировки
  const requestSort = (key) => {
    let direction = 'asc';
    if (sortConfig.key === key && sortConfig.direction === 'asc') {
      direction = 'desc';
    }
    setSortConfig({ key, direction });
  };

  // Получение иконки сортировки
  const getSortIcon = (key) => {
    if (sortConfig.key !== key) {
      return <FaSort className="ml-1 inline" />;
    }
    return sortConfig.direction === 'asc'
      ? <FaSortAlphaDown className="ml-1 inline text-yellow-400" />
      : <FaSortAlphaUp className="ml-1 inline text-yellow-400" />;
  };

  // Фильтрация и сортировка пользователей
  const filteredAndSortedUsers = React.useMemo(() => {
    // Сначала фильтруем по поисковому запросу
    let filteredUsers = users.filter(user =>
      user.email?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.firstName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.lastName?.toLowerCase().includes(searchQuery.toLowerCase())
    );

    // Затем сортируем
    if (sortConfig.key) {
      filteredUsers.sort((a, b) => {
        if (a[sortConfig.key] < b[sortConfig.key]) {
          return sortConfig.direction === 'asc' ? -1 : 1;
        }
        if (a[sortConfig.key] > b[sortConfig.key]) {
          return sortConfig.direction === 'asc' ? 1 : -1;
        }
        return 0;
      });
    }

    return filteredUsers;
  }, [users, searchQuery, sortConfig]);

  if (loading) {
    return (
      <div className="p-4 flex justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-yellow-500"></div>
      </div>
    );
  }

  return (
    <div className="p-4">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold text-white">Управление пользователями</h2>
        <button
          onClick={handleAddNew}
          className="px-4 py-2 bg-yellow-500 text-white rounded hover:bg-yellow-600 flex items-center"
        >
          <FaUserPlus className="mr-2" /> Добавить пользователя
        </button>
      </div>

      {/* Поиск */}
      <div className="mb-4">
        <input
          type="text"
          placeholder="Поиск по email, имени или фамилии..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-gray-200 placeholder-gray-400"
        />
      </div>

      {filteredAndSortedUsers.length > 0 ? (
        <div className="overflow-x-auto">
          <table className="min-w-full text-white [&_:is(th,td):not(:first-child)]:text-center">
            <thead className="bg-[#626262]">
              <tr>
                <th
                  className="px-4 py-2 cursor-pointer"
                  onClick={() => requestSort('email')}
                >
                  Email {getSortIcon('email')}
                </th>
                <th
                  className="px-4 py-2 cursor-pointer"
                  onClick={() => requestSort('firstName')}
                >
                  Имя {getSortIcon('firstName')}
                </th>
                <th
                  className="px-4 py-2 cursor-pointer"
                  onClick={() => requestSort('lastName')}
                >
                  Фамилия {getSortIcon('lastName')}
                </th>
                <th className="px-4 py-2">Роль</th>
                <th className="px-4 py-2">Действия</th>
              </tr>
            </thead>
            <tbody>
              {filteredAndSortedUsers.map(user => (
                <tr
                  key={user.id}
                  className={`bg-[#585858] border-t border-gray-700 ${softDeletedUsers[user.id] ? 'opacity-60' : ''}`}
                >
                  <td className="px-4 py-2">{user.email}</td>
                  <td className="px-4 py-2">{user.firstName || '-'}</td>
                  <td className="px-4 py-2">{user.lastName || '-'}</td>
                  <td className="px-4 py-2">
                    <span className={`px-2 py-1 rounded ${user.role?.id === 3 ? 'bg-red-600' :
                      user.role?.id === 2 ? 'bg-yellow-600' :
                        'bg-green-600'
                      }`}>
                      {user.role?.id === 3 ? 'ADMIN' :
                        user.role?.id === 2 ? 'ORGANIZER' :
                          user.role?.name || user.userRole || 'USER'}
                    </span>
                  </td>
                  <td className="px-4 py-2 flex space-x-2">
                    {!softDeletedUsers[user.id] ? (
                      <>
                        <button
                          onClick={() => handleEdit(user)}
                          className="p-2 bg-gray-600 text-white rounded hover:bg-gray-500"
                          title="Редактировать"
                        >
                          <FaPen />
                        </button>
                        <button
                          onClick={() => handleSoftDelete(user.id)}
                          className="p-2 bg-yellow-500 text-white rounded hover:bg-yellow-600"
                          title="Удалить"
                        >
                          <FaTrash />
                        </button>
                      </>
                    ) : (
                      <>
                        <button
                          onClick={() => handleRestore(user.id)}
                          className="p-2 bg-transparent border border-yellow-400 text-yellow-400 rounded hover:bg-yellow-400 hover:bg-opacity-10"
                          title="Восстановить"
                        >
                          <FaTrashRestore />
                        </button>
                        <button
                          onClick={() => handleDelete(user.id)}
                          className="p-2 bg-red-600 text-white rounded hover:bg-red-700"
                          title="Удалить навсегда"
                        >
                          <FaTrash />
                        </button>
                      </>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="text-center py-10 bg-[#585858] rounded-lg">
          <p className="text-xl text-gray-300">Пользователи не найдены</p>
          <p className="text-gray-400 mt-2">Добавьте нового пользователя или измените параметры поиска</p>
        </div>
      )}

      {/* Модальное окно для добавления/редактирования пользователя */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-[#585858] p-6 rounded-lg w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <h3 className="text-xl font-bold mb-4 text-white">
              {currentUser ? 'Редактировать пользователя' : 'Добавить пользователя'}
            </h3>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-1">Email</label>
                  <input
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleInputChange}
                    className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-1">Роль</label>
                  <select
                    name="role"
                    value={formData.role}
                    onChange={handleInputChange}
                    className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                  >
                    <option value="USER">Пользователь</option>
                    <option value="ORGANIZER">Организатор</option>
                    <option value="ADMIN">Администратор</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-1">Имя</label>
                  <input
                    type="text"
                    name="firstName"
                    value={formData.firstName}
                    onChange={handleInputChange}
                    className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-1">Фамилия</label>
                  <input
                    type="text"
                    name="lastName"
                    value={formData.lastName}
                    onChange={handleInputChange}
                    className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-1">Отчество</label>
                  <input
                    type="text"
                    name="patronymic"
                    value={formData.patronymic}
                    onChange={handleInputChange}
                    className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-1">Телефон</label>
                  <input
                    type="text"
                    name="phone"
                    value={formData.phone}
                    onChange={handleInputChange}
                    className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-1">Дата рождения</label>
                  <DatePicker
                    selected={formData.birthDate ? parseDate(formData.birthDate) : null}
                    onChange={handleDateChange}
                    dateFormat="dd.MM.yyyy"
                    className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                    placeholderText="дд.мм.гггг"
                    required
                  />
                </div>
                <div className="md:col-span-2">
                  <label className="block text-sm font-medium text-gray-300 mb-1">Адрес</label>
                  <input
                    type="text"
                    name="address"
                    value={formData.address}
                    onChange={handleInputChange}
                    className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                  />
                </div>

                {/* Показываем поля пароля только при создании нового пользователя */}
                {!currentUser && (
                  <>
                    <div>
                      <label className="block text-sm font-medium text-gray-300 mb-1">Пароль</label>
                      <input
                        type="password"
                        name="password"
                        value={formData.password}
                        onChange={handleInputChange}
                        className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-300 mb-1">Подтвердите пароль</label>
                      <input
                        type="password"
                        name="confirmPassword"
                        value={formData.confirmPassword}
                        onChange={handleInputChange}
                        className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                        required
                      />
                    </div>
                  </>
                )}
              </div>
              <div className="flex justify-end space-x-2 pt-4">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-500"
                >
                  Отмена
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-yellow-500 text-white rounded hover:bg-yellow-600"
                >
                  {currentUser ? 'Сохранить' : 'Добавить'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
};

export default UserManager;
