import React, { useState, useEffect, useMemo } from 'react'; 
import axios from 'axios';
import { toast } from 'react-toastify';
import { API_URL } from '../../../config'; // Убедитесь, что путь правильный
import { FaSort, FaSortAlphaDown, FaSortAlphaUp, FaTrashRestore, FaTrash, FaPen, FaUserPlus } from 'react-icons/fa';
import 'react-datepicker/dist/react-datepicker.css';

const UserManager = () => {
  const [users, setUsers] = useState([]); // Инициализируем как пустой массив
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [currentUser, setCurrentUser] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [sortConfig, setSortConfig] = useState({ key: null, direction: 'asc' });
  const [softDeletedUsers, setSoftDeletedUsers] = useState({});

  // Начальное состояние для формы, совпадающее с тем, что было у вас
  const [formData, setFormData] = useState({
    email: '',
    firstName: '',
    lastName: '',
    patronymic: '',
    phone: '',
    address: '',
    birthDate: null, // Используем null для дат, если используется DatePicker, или строку для input type="date"
    role: 'USER', // Изначально строка, как было у вас
    password: '',
    confirmPassword: ''
  });

  const getRoleIdByName = (roleName) => {
    switch (roleName) {
      case 'USER': return 1;
      case 'ORGANIZER': return 2;
      case 'ADMIN': return 3;
      default: return 1; // По умолчанию USER
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${API_URL}/users`, { // Ваш URL был /users
        withCredentials: true,
        params: { populate: 'role' } // Этот параметр может быть специфичен для вашего бэкенда
      });
      // Убеждаемся, что устанавливаем массив
      setUsers(response.data || []); // Если API напрямую возвращает массив
    } catch (error) {
      console.error('Ошибка загрузки пользователей:', error);
      toast.error('Не удалось загрузить пользователей');
      setUsers([]); // В случае ошибки устанавливаем пустой массив
    } finally {
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

  // Если вы используете input type="date", эта функция не нужна
  // const handleDateChange = (date) => {
  //   setFormData({
  //     ...formData,
  //     birthDate: date ? formatDate(date) : null // Используйте formatDate если дата - объект Date
  //   });
  // };

  // Преобразование даты из объекта Date в строку "dd.MM.yyyy"
  const formatDate = (date) => {
    if (!date) return null;
    const d = new Date(date); // Убедимся, что это объект Date
    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const year = d.getFullYear();
    return `${day}.${month}.${year}`;
  };

  // Преобразование строки "dd.MM.yyyy" в объект Date для DatePicker или для работы
  const parseDate = (dateString) => {
    if (!dateString || typeof dateString !== 'string') return null;
    const parts = dateString.split('.');
    if (parts.length === 3) {
      return new Date(parts[2], parts[1] - 1, parts[0]); // год, месяц (0-11), день
    }
    return null;
  };

  // Преобразование из "yyyy-MM-dd" (от input type="date") в "dd.MM.yyyy" для отправки
  const formatDateForBackend = (dateString_yyyyMMdd) => {
    if (!dateString_yyyyMMdd || typeof dateString_yyyyMMdd !== 'string') return null;
    const parts = dateString_yyyyMMdd.split('-');
    if (parts.length === 3) {
        return `${parts[2]}.${parts[1]}.${parts[0]}`; // dd.MM.yyyy
    }
    return null;
  };


  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      let roleObjectToSend;
      // Проверяем, является ли formData.role уже объектом {id: ..., name: ...} или строкой
      if (typeof formData.role === 'string') {
        roleObjectToSend = { id: getRoleIdByName(formData.role), name: formData.role };
      } else if (formData.role && typeof formData.role === 'object' && formData.role.id !== undefined) {
        roleObjectToSend = formData.role; // Уже правильный объект
      } else {
          // Если роль не установлена корректно, можно установить по умолчанию или выдать ошибку
          console.warn("Роль не определена корректно, устанавливается USER по умолчанию");
          roleObjectToSend = { id: getRoleIdByName('USER'), name: 'USER' };
      }


      const birthDateForSubmit = formData.birthDate ? formatDateForBackend(formData.birthDate) : null;

      if (currentUser) {
        // При обновлении не отправляем пароль, если он не изменен
        const updateData = {
          id: currentUser.id,
          email: formData.email,
          firstName: formData.firstName,
          lastName: formData.lastName,
          patronymic: formData.patronymic || null,
          phone: formData.phone || null,
          address: formData.address || null,
          birthDate: birthDateForSubmit,
          role: roleObjectToSend // Отправляем объект роли
        };
        // Добавляем пароль, только если он введен
        if (formData.password) {
            if (formData.password !== formData.confirmPassword) {
                toast.error('Пароли не совпадают!');
                return;
            }
            updateData.newPassword = formData.password; // Или просто password, в зависимости от вашего API
        }

        await axios.put(`${API_URL}/users/${currentUser.id}`, updateData, {
          withCredentials: true,
        });
        toast.success('Пользователь успешно обновлен');
      } else {
        // Проверки для нового пользователя
        if (!formData.email || !formData.password || !formData.firstName || !formData.lastName || !birthDateForSubmit) {
          toast.error("Email, пароль, имя, фамилия и дата рождения обязательны.");
          return;
        }
        if (formData.password !== formData.confirmPassword) {
          toast.error('Пароли не совпадают!');
          return;
        }

        const registerData = {
          userData: { // Ваш бэкенд ожидает userData объект
            email: formData.email,
            firstName: formData.firstName,
            lastName: formData.lastName,
            patronymic: formData.patronymic || null,
            phone: formData.phone || null,
            address: formData.address || null,
            birthDate: birthDateForSubmit,
            role: roleObjectToSend // Отправляем объект роли
          },
          password: formData.password
        };

        await axios.post(`${API_URL}/users/create`, registerData, { // Ваш URL был /users/create
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
      email: user.email || '',
      firstName: user.firstName || '',
      lastName: user.lastName || '',
      patronymic: user.patronymic || '',
      phone: user.phone || '',
      address: user.address || '',
      // Преобразуем dd.MM.yyyy в yyyy-MM-dd для input type="date"
      birthDate: user.birthDate ? formatDateForInput(user.birthDate) : '', 
      role: user.role?.name || 'USER', // Используем имя роли, как было у вас
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
    // Здесь должна быть логика восстановления пользователя на бэкенде, если она есть
    // Например, снятие флага isDeleted или что-то подобное
    // Пока просто убираем из softDeletedUsers для UI
    setSoftDeletedUsers(prev => {
      const newState = { ...prev };
      delete newState[id];
      return newState;
    });
    toast.success('Пользователь восстановлен (в UI)');
    // Если есть API для восстановления:
    /*
    try {
      await axios.put(`${API_URL}/users/${id}/restore`, {}, { withCredentials: true });
      toast.success('Пользователь восстановлен');
      fetchUsers();
    } catch (error) {
      toast.error('Ошибка восстановления пользователя');
    }
    */
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
      birthDate: '', // Пустая строка для input type="date"
      role: 'USER',
      password: '',
      confirmPassword: ''
    });
    setShowModal(true);
  };

  const requestSort = (key) => {
    let direction = 'asc';
    if (sortConfig.key === key && sortConfig.direction === 'asc') {
      direction = 'desc';
    }
    setSortConfig({ key, direction });
  };

  const getSortIcon = (key) => {
    if (sortConfig.key !== key) return <FaSort className="ml-1 inline" />;
    return sortConfig.direction === 'asc' ? <FaSortAlphaDown className="ml-1 inline text-yellow-400" /> : <FaSortAlphaUp className="ml-1 inline text-yellow-400" />;
  };

  const filteredAndSortedUsers = useMemo(() => {
    if (!Array.isArray(users)) {
      console.warn('users не является массивом в filteredAndSortedUsers, возвращен пустой массив. users:', users);
      return [];
    }

    let filtered = users.filter(user =>
      (user.firstName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
       user.lastName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
       user.email?.toLowerCase().includes(searchQuery.toLowerCase()) ||
       (user.role?.name || user.userRole || '')?.toLowerCase().includes(searchQuery.toLowerCase())) // Проверка на существование user.role
    );

    if (sortConfig.key) {
      filtered.sort((a, b) => {
        let valA = a[sortConfig.key];
        let valB = b[sortConfig.key];

        if (sortConfig.key === 'role') { // Сортировка по имени роли
            valA = a.role?.name || a.userRole || ''; // Учитываем оба варианта
            valB = b.role?.name || b.userRole || '';
        }

        if (typeof valA === 'string') valA = valA.toLowerCase();
        if (typeof valB === 'string') valB = valB.toLowerCase();
        
        if (valA < valB) return sortConfig.direction === 'asc' ? -1 : 1;
        if (valA > valB) return sortConfig.direction === 'asc' ? 1 : -1;
        return 0;
      });
    }
    return filtered;
  }, [users, searchQuery, sortConfig]);


  if (loading && !users.length) {
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

      <div className="mb-4">
        <input
          type="text"
          placeholder="Поиск по имени, фамилии, email или роли..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-gray-200 placeholder-gray-400"
        />
      </div>

      {filteredAndSortedUsers.length > 0 ? (
        <div className="overflow-x-auto">
          <table className="min-w-full text-white [&_th]:text-center [&_td:not(:first-child)]:text-center">
            <thead className="bg-[#626262]">
              <tr>
                <th className="px-4 py-2 cursor-pointer" onClick={() => requestSort('email')}>Email {getSortIcon('email')}</th>
                <th className="px-4 py-2 cursor-pointer" onClick={() => requestSort('firstName')}>Имя {getSortIcon('firstName')}</th>
                <th className="px-4 py-2 cursor-pointer" onClick={() => requestSort('lastName')}>Фамилия {getSortIcon('lastName')}</th>
                <th className="px-4 py-2 cursor-pointer" onClick={() => requestSort('role')}>Роль {getSortIcon('role')}</th>
                <th className="px-4 py-2">Действия</th>
              </tr>
            </thead>
            <tbody>
              {filteredAndSortedUsers.map(user => (
                <tr key={user.id} className={`bg-[#585858] border-t border-gray-700 ${softDeletedUsers[user.id] ? 'opacity-60' : ''}`}>
                  <td className="px-4 py-2">{user.email}</td>
                  <td className="px-4 py-2">{user.firstName || '-'}</td>
                  <td className="px-4 py-2">{user.lastName || '-'}</td>
                  <td className="px-4 py-2">
                  <span className={`px-2 py-1 rounded ${
                      (user.role?.name || user.userRole) === 'ADMIN' ? 'bg-red-600' :
                      (user.role?.name || user.userRole) === 'ORGANIZER' ? 'bg-blue-600' :
                      (user.role?.name || user.userRole) === 'USER' ? 'bg-green-600' : 'bg-gray-600'
                    }`}>
                      {user.role?.name || user.userRole || 'USER'} {/* Отображаем роль */}
                    </span>
                  </td>
                  <td className="px-4 py-2 flex justify-center space-x-2">
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
                          title="Пометить на удаление"
                        >
                          <FaTrash />
                        </button>
                      </>
                    ) : (
                      <>
                        <button
                          onClick={() => handleRestore(user.id)}
                          className="p-2 bg-transparent border border-yellow-400 text-yellow-400 rounded hover:bg-yellow-400 hover:bg-opacity-10"
                          title="Отменить удаление"
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

      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-[#585858] p-6 rounded-lg w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <h3 className="text-xl font-bold mb-4 text-white">{currentUser ? 'Редактировать пользователя' : 'Добавить пользователя'}</h3>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">Email</label>
                <input type="email" name="email" value={formData.email} onChange={handleInputChange} className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white" required disabled={!!currentUser} />
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-1">Имя</label>
                  <input type="text" name="firstName" value={formData.firstName} onChange={handleInputChange} className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white" required />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-1">Фамилия</label>
                  <input type="text" name="lastName" value={formData.lastName} onChange={handleInputChange} className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white" required />
                </div>
              </div>
              <div>
                  <label className="block text-sm font-medium text-gray-300 mb-1">Отчество</label>
                  <input type="text" name="patronymic" value={formData.patronymic} onChange={handleInputChange} className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white" />
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                    <label className="block text-sm font-medium text-gray-300 mb-1">Телефон</label>
                    <input type="tel" name="phone" value={formData.phone} onChange={handleInputChange} className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white" />
                </div>
                <div>
                    <label className="block text-sm font-medium text-gray-300 mb-1">Дата рождения</label>
                    {/* Используем input type="date", который ожидает формат yyyy-MM-dd */}
                    <input type="date" name="birthDate" value={formData.birthDate} onChange={handleInputChange} className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white" />
                </div>
              </div>
              <div>
                  <label className="block text-sm font-medium text-gray-300 mb-1">Адрес</label>
                  <input type="text" name="address" value={formData.address} onChange={handleInputChange} className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">Роль</label>
                <select name="role" value={formData.role} onChange={handleInputChange} className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white">
                  <option value="USER">USER</option>
                  <option value="ORGANIZER">ORGANIZER</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </div>
              {!currentUser && (
                <>
                  <div>
                    <label className="block text-sm font-medium text-gray-300 mb-1">Пароль</label>
                    <input type="password" name="password" value={formData.password} onChange={handleInputChange} className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white" required />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-300 mb-1">Подтвердите пароль</label>
                    <input type="password" name="confirmPassword" value={formData.confirmPassword} onChange={handleInputChange} className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white" required />
                  </div>
                </>
              )}
              {currentUser && (
                <>
                  <div>
                    <label className="block text-sm font-medium text-gray-300 mb-1">Новый пароль (оставьте пустым, если не хотите менять)</label>
                    <input type="password" name="password" value={formData.password} onChange={handleInputChange} className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-300 mb-1">Подтвердите новый пароль</label>
                    <input type="password" name="confirmPassword" value={formData.confirmPassword} onChange={handleInputChange} className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-white" />
                  </div>
                </>
              )}
              <div className="flex justify-end space-x-2 pt-4">
                <button type="button" onClick={() => setShowModal(false)} className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-500">Отмена</button>
                <button type="submit" className="px-4 py-2 bg-yellow-500 text-white rounded hover:bg-yellow-600">{currentUser ? 'Сохранить изменения' : 'Добавить пользователя'}</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserManager;
