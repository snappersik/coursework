import React, { useState, useEffect } from 'react';
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import axios from 'axios';
import { API_URL } from '../../../config';

const SliderManager = () => {
  const [sliderBooks, setSliderBooks] = useState([]);
  const [allBooks, setAllBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [customDescriptions, setCustomDescriptions] = useState({});

  // Загрузка данных
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);

        // Запрос книг слайдера
        const sliderResponse = await axios.get(`${API_URL}/rest/slider/books`);

        // Запрос всех доступных книг
        const booksResponse = await axios.get(`${API_URL}/rest/books`);

        setSliderBooks(sliderResponse.data);
        setAllBooks(booksResponse.data);

        // Инициализация кастомных описаний
        const descriptions = {};
        sliderResponse.data.forEach(book => {
          descriptions[book.id] = book.description || '';
        });

        setCustomDescriptions(descriptions);
        setLoading(false);
      } catch (error) {
        console.error('Ошибка загрузки данных:', error);
        toast.error('Не удалось загрузить данные');
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  // Обработчик перетаскивания
  const handleDragEnd = (result) => {
    if (!result.destination) return;

    const items = Array.from(sliderBooks);
    const [reorderedItem] = items.splice(result.source.index, 1);
    items.splice(result.destination.index, 0, reorderedItem);

    setSliderBooks(items);

    // Сохраняем новый порядок на сервере
    saveSliderOrder(items);
  };

  // Сохранение порядка на сервере
  const saveSliderOrder = async (books) => {
    try {
      // Формируем массив ID в новом порядке
      const bookIds = books.map(book => book.id);

      await axios.post(`${API_URL}/rest/slider/reorder`, bookIds);
      toast.success('Порядок слайдера обновлен');
    } catch (error) {
      console.error('Ошибка сохранения порядка:', error);
      toast.error('Не удалось сохранить порядок слайдера');
    }
  };

  // Добавление книги в слайдер
  const addBookToSlider = async (bookId) => {
    try {
      // Проверяем, что книга еще не в слайдере
      if (sliderBooks.some(book => book.id === bookId)) {
        toast.info('Эта книга уже добавлена в слайдер');
        return;
      }

      const response = await axios.post(`${API_URL}/rest/slider/add/${bookId}`);

      // Добавляем книгу в начало списка
      const newBook = response.data;
      setSliderBooks([newBook, ...sliderBooks]);

      // Инициализируем кастомное описание
      setCustomDescriptions({
        ...customDescriptions,
        [newBook.id]: newBook.description || ''
      });

      toast.success('Книга добавлена в слайдер');
    } catch (error) {
      console.error('Ошибка добавления книги:', error);
      toast.error('Не удалось добавить книгу в слайдер');
    }
  };

  // Удаление книги из слайдера
  const removeBookFromSlider = async (bookId) => {
    try {
      await axios.delete(`${API_URL}/rest/slider/remove/${bookId}`);

      // Удаляем книгу из списка
      setSliderBooks(sliderBooks.filter(book => book.id !== bookId));

      toast.success('Книга удалена из слайдера');
    } catch (error) {
      console.error('Ошибка удаления книги:', error);
      toast.error('Не удалось удалить книгу из слайдера');
    }
  };

  // Обновление кастомного описания
  const updateDescription = (bookId, description) => {
    setCustomDescriptions({
      ...customDescriptions,
      [bookId]: description
    });
  };

  // Сохранение кастомного описания
  const saveDescription = async (bookId) => {
    try {
      await axios.post(`${API_URL}/rest/slider/${bookId}/description`, {
        description: customDescriptions[bookId]
      });

      toast.success('Описание обновлено');
    } catch (error) {
      console.error('Ошибка обновления описания:', error);
      toast.error('Не удалось обновить описание');
    }
  };

  // Обработчик для загрузки фонового изображения
  const uploadBackground = async (bookId, file) => {
    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await axios.post(
        `${API_URL}/rest/slider/books/${bookId}/background`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }
      );

      if (response.status === 200) {
        toast.success('Фоновое изображение успешно загружено');

        // Обновляем данные в состоянии
        setSliderBooks(prevBooks =>
          prevBooks.map(book =>
            book.id === bookId
              ? { ...book, backgroundUrl: response.data.backgroundUrl }
              : book
          )
        );
      }
    } catch (error) {
      toast.error('Ошибка при загрузке фонового изображения');
      console.error('Ошибка загрузки фона:', error);
    }
  };

  // Добавить поле для ввода URL изображения
  const handleBackgroundUrlChange = async (bookId, url) => {
    try {
      const response = await axios.post(
        `${API_URL}/rest/slider/books/${bookId}/background-url`,
        { url },
        {
          headers: {
            'Content-Type': 'application/json'
          }
        }
      );

      if (response.status === 200) {
        toast.success('URL фонового изображения успешно обновлен');

        // Обновляем данные в состоянии
        setSliderBooks(prevBooks =>
          prevBooks.map(book =>
            book.id === bookId
              ? { ...book, backgroundUrl: url }
              : book
          )
        );
      }
    } catch (error) {
      toast.error('Ошибка при обновлении URL фонового изображения');
      console.error('Ошибка обновления URL:', error);
    }
  };

  if (loading) {
    return <div className="text-center py-10">Загрузка...</div>;
  }

  return (
    <div className="container mx-auto px-4 py-8 text-black">
      <h1 className="text-3xl font-bold mb-6">Управление слайдером</h1>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {/* Секция с книгами в слайдере */}
        <div className="bg-[#424242] p-6 rounded-lg shadow-lg">
          <h2 className="text-xl font-semibold mb-4">Книги в слайдере</h2>

          <DragDropContext onDragEnd={handleDragEnd}>
            <Droppable droppableId="slider-books">
              {(provided) => (
                <div
                  {...provided.droppableProps}
                  ref={provided.innerRef}
                  className="space-y-4"
                >
                  {sliderBooks.length === 0 ? (
                    <p className="text-gray-500 italic">Нет книг в слайдере</p>
                  ) : (
                    sliderBooks.map((book, index) => (
                      <Draggable
                        key={book.id.toString()}
                        draggableId={book.id.toString()}
                        index={index}
                      >
                        {(provided) => (
                          <div
                            ref={provided.innerRef}
                            {...provided.draggableProps}
                            {...provided.dragHandleProps}
                            className="bg-gray-50 p-4 rounded border border-gray-200 hover:shadow-md transition-shadow"
                          >
                            <div className="flex items-start justify-between">
                              <div className="flex items-center">
                                <div className="w-16 h-24 overflow-hidden mr-4 rounded">
                                  <img
                                    src={`${API_URL}/rest/books/${book.id}/cover`}
                                    alt={book.title}
                                    className="w-full h-full object-cover"
                                    onError={(e) => {
                                      e.target.src = '/assets/img/book_placeholder.webp';
                                    }}
                                  />
                                </div>
                                <div>
                                  <h3 className="font-semibold">{book.title}</h3>
                                  <p className="text-sm text-gray-600">{book.author}</p>
                                </div>
                              </div>
                              <button
                                onClick={() => removeBookFromSlider(book.id)}
                                className="text-red-500 hover:text-red-700"
                              >
                                Удалить
                              </button>
                            </div>

                            <div className="mt-4">
                              <label className="block text-sm font-medium text-gray-700 mb-1">
                                Кастомное описание:
                              </label>
                              <textarea
                                value={customDescriptions[book.id] || ''}
                                onChange={(e) => updateDescription(book.id, e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                rows="3"
                              />
                              <button
                                onClick={() => saveDescription(book.id)}
                                className="mt-2 px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition-colors"
                              >
                                Сохранить описание
                              </button>
                            </div>

                            <div className="mt-4">
                              <label className="block text-sm font-medium text-gray-700 mb-1">
                                Загрузить фон:
                              </label>
                              <input
                                type="file"
                                accept="image/*"
                                onChange={(e) => {
                                  if (e.target.files && e.target.files[0]) {
                                    uploadBackground(book.id, e.target.files[0]);
                                  }
                                }}
                                className="w-full"
                              />
                            </div>
                          </div>
                        )}
                      </Draggable>
                    ))
                  )}
                  {provided.placeholder}
                </div>
              )}
            </Droppable>
          </DragDropContext>
        </div>

        {/* Секция с доступными книгами */}
        <div className="bg-[#424242] p-6 rounded-lg shadow-lg">
          <h2 className="text-xl font-semibold mb-4">Доступные книги</h2>

          <div className="mb-4">
            <input
              type="text"
              placeholder="Поиск книг..."
              className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            // Здесь можно добавить функцию поиска
            />
          </div>

          <div className="space-y-4 max-h-[600px] overflow-y-auto">
            {allBooks
              .filter(book => !sliderBooks.some(sliderBook => sliderBook.id === book.id))
              .map(book => (
                <div
                  key={book.id}
                  className="flex items-center justify-between p-3 border border-gray-200 rounded hover:bg-gray-50"
                >
                  <div className="flex items-center">
                    <div className="w-12 h-16 overflow-hidden mr-3 rounded">
                      <img
                        src={`${API_URL}/rest/books/${book.id}/cover`}
                        alt={book.title}
                        className="w-full h-full object-cover"
                        onError={(e) => {
                          e.target.src = '/assets/img/book_placeholder.webp';
                        }}
                      />
                    </div>
                    <div>
                      <h3 className="font-medium">{book.title}</h3>
                      <p className="text-xs text-gray-600">{book.author}</p>
                    </div>
                  </div>
                  <button
                    onClick={() => addBookToSlider(book.id)}
                    className="px-3 py-1 bg-green-500 text-white text-sm rounded hover:bg-green-600 transition-colors"
                  >
                    Добавить
                  </button>
                </div>
              ))}
          </div>
        </div>
      </div>

      <ToastContainer position="bottom-right" />
    </div>
  );
};

export default SliderManager;
