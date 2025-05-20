import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import { useDropzone } from 'react-dropzone';
import { toast } from 'react-toastify';
import { API_URL } from '../../../config';
import { FaPlus, FaTimes, FaEye, FaSort, FaSortAlphaDown, FaSortAlphaUp, FaTrashRestore, FaTrash, FaPen, FaLink } from 'react-icons/fa';
import {
  FaBook, FaUserTie, FaLandmark, FaRocket, FaDragon, FaMagnifyingGlass,
  FaSkull, FaGhost, FaLandmarkDome, FaHeart, FaHorse, FaUser,
  FaUserPen, FaBookOpenReader, FaHandsHolding, FaBriefcase,
  FaEarthAmericas, FaChildren, FaFeather, FaMasksTheater, FaBookOpen
} from 'react-icons/fa6';

const BookManager = () => {
  const [books, setBooks] = useState([]);
  const [filter, setFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [showGenreModal, setShowGenreModal] = useState(false);
  const [showViewGenresModal, setShowViewGenresModal] = useState(false);
  const [selectedBookGenres, setSelectedBookGenres] = useState([]);
  const [current, setCurrent] = useState(null);
  const [genres, setGenres] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [sortConfig, setSortConfig] = useState({ key: null, direction: 'asc' });
  const [softDeletedBooks, setSoftDeletedBooks] = useState({});
  const [form, setForm] = useState({
    title: '',
    author: '',
    description: '',
    isbn: '',
    price: 0,
    publishYear: new Date().getFullYear(),
    genres: [],
    isBestseller: false,
    coverUrl: '',
    coverFile: null
  });

  // Иконки для жанров
  const genreIcons = {
    FICTION: <FaBook className="text-yellow-400" />,
    NON_FICTION: <FaUserTie className="text-yellow-400" />,
    SCIENCE_FICTION: <FaRocket className="text-yellow-400" />,
    FANTASY: <FaDragon className="text-yellow-400" />,
    MYSTERY: <FaMagnifyingGlass className="text-yellow-400" />,
    THRILLER: <FaSkull className="text-yellow-400" />,
    HORROR: <FaGhost className="text-yellow-400" />,
    HISTORICAL_FICTION: <FaLandmarkDome className="text-yellow-400" />,
    ROMANCE: <FaHeart className="text-yellow-400" />,
    WESTERN: <FaHorse className="text-yellow-400" />,
    BIOGRAPHY: <FaUser className="text-yellow-400" />,
    AUTOBIOGRAPHY: <FaUserPen className="text-yellow-400" />,
    MEMOIR: <FaBookOpenReader className="text-yellow-400" />,
    SELF_HELP: <FaHandsHolding className="text-yellow-400" />,
    BUSINESS: <FaBriefcase className="text-yellow-400" />,
    TRAVEL: <FaEarthAmericas className="text-yellow-400" />,
    CHILDREN: <FaChildren className="text-yellow-400" />,
    POETRY: <FaFeather className="text-yellow-400" />,
    DRAMA: <FaMasksTheater className="text-yellow-400" />,
    CLASSICS: <FaLandmark className="text-yellow-400" />
  };

  useEffect(() => {
    fetchBooks();
    fetchGenres();
  }, []);

  // Исправленный код в методе fetchGenres
  const fetchGenres = async () => {
    try {
      const response = await axios.get(`${API_URL}/books/genres`, {
        withCredentials: true
      });
      // Добавляем проверку на массив и резервный вариант
      setGenres(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error('Error fetching genres:', error);
      setGenres([]); // Гарантированно устанавливаем массив
      toast.error('Не удалось загрузить жанры');
    }
  };

  const fetchBooks = async () => {
    setLoading(true);
    try {
      const { data } = await axios.get(`${API_URL}/books/paginated?page=0&size=100`, { withCredentials: true });
      setBooks(data.content || []);
    } catch (error) {
      toast.error('Не удалось загрузить книги');
    } finally {
      setLoading(false);
    }
  };

  const onDrop = useCallback((acceptedFiles) => {
    const file = acceptedFiles[0];
    setForm(prevForm => ({
      ...prevForm,
      coverFile: file,
      coverUrl: URL.createObjectURL(file),
    }));
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'image/*': ['.jpg', '.jpeg', '.png', '.gif', '.webp']
    }
  });

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setForm({
      ...form,
      [name]: value
    });
  };

  const handleAddGenre = (genreName) => {
    // Проверяем, не добавлен ли уже этот жанр
    if (!form.genres.includes(genreName)) {
      setForm({
        ...form,
        genres: [...form.genres, genreName]
      });
    }
    setShowGenreModal(false);
  };

  const handleRemoveGenre = (genreName) => {
    setForm({
      ...form,
      genres: form.genres.filter(g => g !== genreName)
    });
  };

  const handleViewGenres = (bookGenres) => {
    setSelectedBookGenres(bookGenres || []);
    setShowViewGenresModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      // Явно формируем DTO для отправки в JSON-части
      const bookDto = {
        title: form.title,
        author: form.author,
        description: form.description,
        isbn: form.isbn,
        price: form.price,
        publishYear: form.publishYear,
        genres: form.genres, // Убедись, что это массив строк (имен жанров)
        isBestseller: form.isBestseller,
        coverUrl: form.coverUrl // URL из формы (существующий, новый внешний, blob-preview или пустой)
        // Не включаем form.coverFile сюда
      };

      const formDataPayload = new FormData();
      formDataPayload.append('book', new Blob([JSON.stringify(bookDto)], { type: 'application/json' }));

      if (form.coverFile) { // Если выбран новый файл для загрузки
        formDataPayload.append('file', form.coverFile);
      }

      if (current) {
        // Обновление книги
        await axios.put(`${API_URL}/books/${current.id}`, formDataPayload, { withCredentials: true });
        toast.success('Книга успешно обновлена');
      } else {
        // Создание новой книги
        await axios.post(`${API_URL}/books`, formDataPayload, { withCredentials: true });
        toast.success('Книга успешно создана');
      }
      setShowModal(false);
      fetchBooks(); // Обновляем список книг
    } catch (error) {
      console.error('Ошибка сохранения книги:', error);
      let errorMessage = 'Не удалось сохранить книгу';
      // Пытаемся извлечь сообщение об ошибке с бэкенда
      if (error.response && error.response.data && error.response.data.message) {
        errorMessage = error.response.data.message;
      } else if (error.message) {
        errorMessage = error.message;
      }
      toast.error(errorMessage);
    }
  };


  const handleEdit = (book) => {
    setCurrent(book);
    setForm({
      title: book.title,
      author: book.author,
      description: book.description || '',
      isbn: book.isbn || '',
      price: book.price || 0,
      publishYear: book.publishYear || new Date().getFullYear(),
      genres: book.genres || [],
      isBestseller: book.isBestseller || false,
      coverUrl: book.coverUrl || '',
      coverFile: null
    });
    setShowModal(true);
  };

  const handleSoftDelete = (id) => {
    setSoftDeletedBooks(prev => ({
      ...prev,
      [id]: true
    }));
    toast.warning('Книга помечена на удаление');
  };

  const handleRestore = (id) => {
    setSoftDeletedBooks(prev => {
      const newState = { ...prev };
      delete newState[id];
      return newState;
    });
    toast.success('Книга восстановлена');
  };

  const handleDelete = async (id) => {
    if (window.confirm('Вы уверены, что хотите удалить эту книгу? Это действие нельзя отменить.')) {
      try {
        await axios.delete(`${API_URL}/books/${id}`, { withCredentials: true });
        toast.success('Книга успешно удалена');
        fetchBooks();
        setSoftDeletedBooks(prev => {
          const newState = { ...prev };
          delete newState[id];
          return newState;
        });
      } catch (error) {
        console.error('Ошибка удаления книги:', error);
        toast.error('Не удалось удалить книгу');
      }
    }
  };

  const handleAddNew = () => {
    setCurrent(null);
    setForm({
      title: '',
      author: '',
      description: '',
      isbn: '',
      price: 0,
      publishYear: new Date().getFullYear(),
      genres: [],
      isBestseller: false,
      coverUrl: '',
      coverFile: null
    });
    setShowModal(true);
  };

  const handleCoverUrlChange = (e) => {
    const url = e.target.value;
    setForm({
      ...form,
      coverUrl: url,
      coverFile: null
    });
  };

  // Получение отображаемого имени жанра
  const getGenreDisplayName = (genreName) => {
    const genre = genres.find(g => g.name === genreName);
    return genre ? genre.displayName : genreName;
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

  // Фильтрация и сортировка книг
  const filteredAndSortedBooks = React.useMemo(() => {
    // Сначала фильтруем по поисковому запросу
    let filteredBooks = books.filter(book =>
      book.title?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      book.author?.toLowerCase().includes(searchQuery.toLowerCase())
    );

    // Затем сортируем
    if (sortConfig.key) {
      filteredBooks.sort((a, b) => {
        if (a[sortConfig.key] < b[sortConfig.key]) {
          return sortConfig.direction === 'asc' ? -1 : 1;
        }
        if (a[sortConfig.key] > b[sortConfig.key]) {
          return sortConfig.direction === 'asc' ? 1 : -1;
        }
        return 0;
      });
    }

    return filteredBooks;
  }, [books, searchQuery, sortConfig]);

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
        <h2 className="text-2xl font-bold text-white">Управление книгами</h2>
        <button
          onClick={handleAddNew}
          className="px-4 py-2 bg-yellow-500 text-white rounded hover:bg-yellow-600"
        >
          Добавить книгу
        </button>
      </div>

      {/* Поиск */}
      <div className="mb-4">
        <input
          type="text"
          placeholder="Поиск по названию или автору..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-gray-200 placeholder-gray-400"
        />
      </div>

      {filteredAndSortedBooks.length > 0 ? (
        <div className="overflow-x-auto">
          <table className="min-w-full text-white [&_th]:text-center [&_td:not(:first-child)]:text-center">
            <thead className="bg-[#626262]">
              <tr>
                <th
                  className="px-4 py-2 cursor-pointer"
                  onClick={() => requestSort('title')}
                >
                  Название {getSortIcon('title')}
                </th>
                <th
                  className="px-4 py-2 cursor-pointer"
                  onClick={() => requestSort('author')}
                >
                  Автор {getSortIcon('author')}
                </th>
                <th className="px-4 py-2">Жанры</th>
                <th className="px-4 py-2">Действия</th>
              </tr>
            </thead>
            <tbody>
              {filteredAndSortedBooks.map(book => (
                <tr
                  key={book.id}
                  className={`bg-[#585858] border-t border-gray-700 ${softDeletedBooks[book.id] ? 'opacity-60' : ''}`}
                >
                  <td className="px-4 py-2">{book.title}</td>
                  <td className="px-4 py-2">{book.author}</td>
                  <td className="px-4 py-2">
                    {book.genres && book.genres.length > 0 ? (
                      <button
                        onClick={() => handleViewGenres(book.genres)}
                        className="px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700 flex items-center"
                      >
                        <FaEye className="mr-1" /> Просмотр ({book.genres.length})
                      </button>
                    ) : (
                      'Не указаны'
                    )}
                  </td>
                  <td className="px-4 py-2 flex space-x-2">
                    {!softDeletedBooks[book.id] ? (
                      <>
                        <button
                          onClick={() => handleEdit(book)}
                          className="p-2 bg-gray-600 text-white rounded hover:bg-gray-500"
                          title="Редактировать"
                        >
                          <FaPen />
                        </button>
                        <button
                          onClick={() => handleSoftDelete(book.id)}
                          className="p-2 bg-yellow-500 text-white rounded hover:bg-yellow-600"
                          title="Удалить"
                        >
                          <FaTrash />
                        </button>
                      </>
                    ) : (
                      <>
                        <button
                          onClick={() => handleRestore(book.id)}
                          className="p-2 bg-transparent border border-yellow-400 text-yellow-400 rounded hover:bg-yellow-400 hover:bg-opacity-10"
                          title="Восстановить"
                        >
                          <FaTrashRestore />
                        </button>
                        <button
                          onClick={() => handleDelete(book.id)}
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
          <p className="text-xl text-gray-300">Книги не найдены</p>
          <p className="text-gray-400 mt-2">Добавьте новую книгу или измените параметры поиска</p>
        </div>
      )}

      {/* Модальное окно для добавления/редактирования книги */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-[#585858] p-6 rounded-lg w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <h3 className="text-xl font-bold mb-4 text-white">{current ? 'Редактировать книгу' : 'Добавить книгу'}</h3>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-300">Название</label>
                <input
                  type="text"
                  name="title"
                  value={form.title}
                  onChange={handleInputChange}
                  className="mt-1 block w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-300">Автор</label>
                <input
                  type="text"
                  name="author"
                  value={form.author}
                  onChange={handleInputChange}
                  className="mt-1 block w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                  required
                />
              </div>

              {/* Секция жанров */}
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-2">Жанры</label>
                <div className="flex flex-wrap gap-2 mb-2">
                  {form.genres.map(genreName => {
                    const genreInfo = genres.find(g => g.name === genreName);
                    return (
                      <div
                        key={genreName}
                        className="flex items-center bg-[#707070] hover:bg-[#808080] px-3 py-1 rounded-full cursor-pointer"
                        onClick={() => handleRemoveGenre(genreName)}
                      >
                        {genreIcons[genreName]}
                        <span className="mx-1 text-white">{genreInfo ? genreInfo.displayName : genreName}</span>
                        <FaTimes className="text-gray-400" />
                      </div>
                    );
                  })}
                  <div
                    className="flex items-center text-gray-400 hover:text-white px-3 py-1 rounded-full cursor-pointer"
                    onClick={() => setShowGenreModal(true)}
                  >
                    <FaPlus className="mr-1" />
                    <span>Добавить жанр</span>
                  </div>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-300">Описание</label>
                <textarea
                  name="description"
                  value={form.description}
                  onChange={handleInputChange}
                  rows="4"
                  className="mt-1 block w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                ></textarea>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-300">URL обложки</label>
                <div className="flex">
                  <input
                    type="text"
                    name="coverUrlInput"
                    value={form.coverUrl}
                    onChange={handleCoverUrlChange}
                    placeholder="https://example.com/image.jpg"
                    className="mt-1 flex-1 p-2 bg-[#707070] border border-gray-600 rounded-l text-white"
                  />
                  <button
                    type="button"
                    className="mt-1 p-2 bg-blue-600 text-white rounded-r hover:bg-blue-700"
                    onClick={() => {
                      if (!form.coverUrl) {
                        toast.error('Введите URL изображения');
                        return;
                      }
                      toast.info('URL обложки установлен');
                    }}
                  >
                    <FaLink />
                  </button>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-300">Загрузить обложку</label>
                <div
                  {...getRootProps()}
                  className={`mt-1 border-2 border-dashed p-4 rounded text-center cursor-pointer ${isDragActive ? 'border-yellow-500 bg-[#707070]' : 'border-gray-600'
                    }`}
                >
                  <input {...getInputProps()} />
                  {form.coverUrl && form.coverFile ? (
                    <div className="flex flex-col items-center">
                      <img src={form.coverUrl} alt="Preview" className="h-32 object-contain mb-2" />
                      <p className="text-white">Перетащите новый файл или кликните для замены</p>
                    </div>
                  ) : form.coverUrl ? (
                    <div className="flex flex-col items-center">
                      <img src={form.coverUrl} alt="Preview" className="h-32 object-contain mb-2" />
                      <p className="text-white">Используется URL изображения</p>
                    </div>
                  ) : (
                    <p className="text-white">Перетащите файл или кликните для выбора</p>
                  )}
                </div>
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
                  {current ? 'Сохранить' : 'Добавить'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Модальное окно для выбора жанров */}
      {showGenreModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-[#585858] p-6 rounded-lg w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <h3 className="text-xl font-bold mb-4 text-white">Выберите жанр</h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
              {genres.map(genre => (
                <div
                  key={genre.name}
                  className={`flex items-center p-3 rounded-lg cursor-pointer transition-colors ${form.genres.includes(genre.name)
                    ? 'bg-[#707070] opacity-50'
                    : 'bg-[#707070] hover:bg-[#808080]'
                    }`}
                  onClick={() => !form.genres.includes(genre.name) && handleAddGenre(genre.name)}
                >
                  <div className="mr-3">
                    {genreIcons[genre.name] || <FaBook className="text-yellow-400" />}
                  </div>
                  <span className="text-white">{genre.displayName}</span>
                </div>
              ))}
            </div>
            <div className="flex justify-end mt-4">
              <button
                onClick={() => setShowGenreModal(false)}
                className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-500"
              >
                Закрыть
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Модальное окно для просмотра жанров книги */}
      {showViewGenresModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-[#585858] p-6 rounded-lg w-full max-w-2xl">
            <h3 className="text-xl font-bold mb-4 text-white">Жанры книги</h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3 mb-4">
              {selectedBookGenres.map(genreName => {
                const genreInfo = genres.find(g => g.name === genreName);
                return (
                  <div
                    key={genreName}
                    className="flex items-center p-3 bg-[#707070] rounded-lg"
                  >
                    <div className="mr-3">
                      {genreIcons[genreName] || <FaBook className="text-yellow-400" />}
                    </div>
                    <span className="text-white">{genreInfo ? genreInfo.displayName : genreName}</span>
                  </div>
                );
              })}
              {selectedBookGenres.length === 0 && (
                <div className="col-span-3 text-center py-4 text-gray-400">
                  У этой книги нет жанров
                </div>
              )}
            </div>
            <div className="flex justify-end">
              <button
                onClick={() => setShowViewGenresModal(false)}
                className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
              >
                Закрыть
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default BookManager;
