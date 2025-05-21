import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { Helmet } from 'react-helmet';
import { format, parseISO } from 'date-fns';
import { ru } from 'date-fns/locale';
import { motion } from 'framer-motion';
import { toast } from 'react-toastify';
import { FaCalendarAlt, FaUsers, FaBook, FaArrowLeft, FaUserCheck, FaTimesCircle } from 'react-icons/fa';
import axios from 'axios';
import { API_URL } from '../../config';
import { authStore } from '../../store/store';
import EventApplicationForm from '../../components/events/EventApplicationForm';

const EventDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [event, setEvent] = useState(null);
  const [book, setBook] = useState(null);
  const [loading, setLoading] = useState(true);
  const [userApplication, setUserApplication] = useState(null);
  const [showApplicationForm, setShowApplicationForm] = useState(false);
  const [imageUrl, setImageUrl] = useState('');
  const { isAuthorized, userRole, userId } = authStore;

  useEffect(() => {
    fetchEventDetails();
  }, [id]);

  const fetchEventDetails = async () => {
    try {
      setLoading(true);
      const eventResponse = await axios.get(`${API_URL}/events/${id}`);
      setEvent(eventResponse.data);

      // Если у мероприятия есть связанная книга, загружаем информацию о ней
      if (eventResponse.data.bookId) {
        try {
          const bookResponse = await axios.get(`${API_URL}/books/${eventResponse.data.bookId}`);
          setBook(bookResponse.data);

          // Если у книги есть обложка, используем ее для мероприятия
          if (bookResponse.data.coverImageUrl) {
            setImageUrl(bookResponse.data.coverImageUrl);
          } else if (bookResponse.data.id) {
            // Иначе пробуем загрузить обложку через эндпоинт cover
            setImageUrl(`${API_URL}/books/${bookResponse.data.id}/cover`);
          }
        } catch (error) {
          console.error('Ошибка при загрузке информации о книге:', error);
        }
      }

      // Остальной код остается без изменений
    } catch (error) {
      console.error('Ошибка при загрузке деталей мероприятия:', error);
      toast.error('Не удалось загрузить информацию о мероприятии');
      navigate('/events');
    } finally {
      setLoading(false);
    }
  };

  const handleApplicationSubmit = async () => {
    try {
      // Исправленный вызов API
      const response = await createEventApplication(parseInt(id));
      setUserApplication(response);
      setShowApplicationForm(false);
      toast.success('Заявка на участие успешно отправлена');
      fetchEventDetails(); // Обновляем данные
    } catch (error) {
      console.error('Ошибка при отправке заявки:', error);
      toast.error(error.message || 'Не удалось отправить заявку');
    }
  };

  const handleCancelApplication = async () => {
    if (!userApplication) return;

    try {
      await axios.put(`${API_URL}/event-applications/${userApplication.id}/cancel`, {}, {
        withCredentials: true
      });

      toast.success('Заявка успешно отменена');
      setUserApplication(null);
      fetchEventDetails(); // Обновляем данные
    } catch (error) {
      console.error('Ошибка при отмене заявки:', error);
      toast.error('Не удалось отменить заявку');
    }
  };

  // Форматирование даты
  const formatEventDate = (dateString) => {
    try {
      // Преобразуем строку в формате "dd.MM.yyyy HH:mm" в объект Date
      const [datePart, timePart] = dateString.split(' ');
      const [day, month, year] = datePart.split('.');
      const [hours, minutes] = timePart.split(':');

      const date = new Date(year, month - 1, day, hours, minutes);

      // Форматируем дату с использованием date-fns
      return format(date, "d MMMM yyyy 'в' HH:mm", { locale: ru });
    } catch (error) {
      console.error('Ошибка при форматировании даты:', error);
      return dateString;
    }
  };

  // Перевод типа мероприятия
  const translateEventType = (type) => {
    const translations = {
      'MEETING': 'Встреча',
      'PRESENTATION': 'Презентация',
      'WORKSHOP': 'Мастер-класс',
      'OTHER': 'Другое'
    };
    return translations[type] || type;
  };

  // Перевод статуса заявки
  const translateApplicationStatus = (status) => {
    const translations = {
      'PENDING': 'На рассмотрении',
      'APPROVED': 'Одобрена',
      'REJECTED': 'Отклонена',
      'CANCELLED': 'Отменена'
    };
    return translations[status] || status;
  };

  // Определение стиля для статуса заявки
  const getApplicationStatusStyle = (status) => {
    switch (status) {
      case 'APPROVED':
        return 'bg-green-600';
      case 'PENDING':
        return 'bg-yellow-600';
      case 'REJECTED':
        return 'bg-red-600';
      case 'CANCELLED':
        return 'bg-gray-600';
      default:
        return 'bg-gray-600';
    }
  };

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8 flex justify-center items-center min-h-[50vh]">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-yellow-500"></div>
      </div>
    );
  }

  if (!event) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="bg-gray-800 rounded-lg p-8 text-center">
          <h2 className="text-2xl font-bold text-white mb-4">Мероприятие не найдено</h2>
          <p className="text-gray-300 mb-6">Запрашиваемое мероприятие не существует или было удалено.</p>
          <Link
            to="/events"
            className="inline-flex items-center bg-yellow-500 hover:bg-yellow-600 text-white font-bold py-2 px-4 rounded transition-colors duration-300"
          >
            <FaArrowLeft className="mr-2" />
            Вернуться к списку мероприятий
          </Link>
        </div>
      </div>
    );
  }

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="container mx-auto px-4 py-8"
    >
      <Helmet>
        <title>{event.title} | Книжная Гавань</title>
        <meta name="description" content={event.description} />
      </Helmet>

      <div className="mb-6">
        <Link
          to="/events"
          className="inline-flex items-center text-yellow-500 hover:text-yellow-400 transition-colors duration-300"
        >
          <FaArrowLeft className="mr-2" />
          Назад к списку мероприятий
        </Link>
      </div>

      <div className="bg-gray-800 rounded-lg overflow-hidden shadow-xl">
        <div className="relative h-64 md:h-80">
          <div className="absolute inset-0 bg-gradient-to-t from-black to-transparent"></div>

          {/* Тип мероприятия */}
          <div className="absolute top-4 right-4">
            <span className="bg-yellow-500 text-white text-sm font-bold px-3 py-1 rounded">
              {translateEventType(event.eventType)}
            </span>
          </div>

          {/* Отменено */}
          {event.isCancelled && (
            <div className="absolute inset-0 bg-black bg-opacity-70 flex items-center justify-center">
              <div className="text-center">
                <span className="bg-red-600 text-white text-2xl font-bold px-6 py-3 rounded transform -rotate-12 inline-block mb-4">
                  ОТМЕНЕНО
                </span>
                {event.cancellationReason && (
                  <p className="text-white bg-black bg-opacity-70 p-4 rounded max-w-lg mx-auto">
                    Причина: {event.cancellationReason}
                  </p>
                )}
              </div>
            </div>
          )}

          {/* Заголовок */}
          <div className="absolute bottom-0 left-0 right-0 p-6">
            <h1 className="text-3xl md:text-4xl font-bold text-white">{event.title}</h1>
          </div>
        </div>

        {/* Основная информация */}
        <div className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="md:col-span-2">
              <h2 className="text-xl font-bold text-white mb-4">Описание</h2>
              <div className="text-gray-300 mb-6 whitespace-pre-line">
                {event.description || 'Описание отсутствует'}
              </div>

              {book && (
                <div className="mb-6">
                  <h2 className="text-xl font-bold text-white mb-4">Связанная книга</h2>
                  <Link
                    to={`/books/${book.id}`}
                    className="flex items-start p-4 bg-gray-700 rounded-lg hover:bg-gray-600 transition-colors duration-300"
                  >
                    {book.coverImageUrl && (
                      <img
                        src={book.coverImageUrl || `${API_URL}/books/${book.id}/cover`}
                        alt={book.title}
                        className="w-16 h-24 object-cover rounded mr-4"
                        onError={(e) => {
                          e.target.src = placeholderImage;
                        }}
                      />
                    )}
                    <div>
                      <h3 className="text-lg font-semibold text-white">{book.title}</h3>
                      <p className="text-gray-400">{book.author}</p>
                    </div>
                  </Link>
                </div>
              )}
            </div>

            <div>
              <div className="bg-gray-700 rounded-lg p-4 mb-6">
                <h2 className="text-xl font-bold text-white mb-4">Информация</h2>
                <ul className="space-y-4">
                  <li className="flex items-start">
                    <FaCalendarAlt className="text-yellow-500 mt-1 mr-3" />
                    <div>
                      <p className="text-gray-400 text-sm">Дата и время</p>
                      <p className="text-white">{formatEventDate(event.date)}</p>
                    </div>
                  </li>
                  <li className="flex items-start">
                    <FaUsers className="text-yellow-500 mt-1 mr-3" />
                    <div>
                      <p className="text-gray-400 text-sm">Количество мест</p>
                      <p className="text-white">{event.maxParticipants}</p>
                    </div>
                  </li>
                  {book && (
                    <li className="flex items-start">
                      <FaBook className="text-yellow-500 mt-1 mr-3" />
                      <div>
                        <p className="text-gray-400 text-sm">Книга</p>
                        <p className="text-white">{book.title}</p>
                      </div>
                    </li>
                  )}
                </ul>
              </div>

              {/* Секция для авторизованных пользователей */}
              {isAuthorized ? (
                <div className="bg-gray-700 rounded-lg p-4">
                  <h2 className="text-xl font-bold text-white mb-4">Участие</h2>

                  {userApplication ? (
                    <div>
                      <div className="flex items-center mb-4">
                        <div className={`${getApplicationStatusStyle(userApplication.applicationStatus)} rounded-full w-3 h-3 mr-2`}></div>
                        <span className="text-white">
                          Статус заявки: <strong>{translateApplicationStatus(userApplication.applicationStatus)}</strong>
                        </span>
                      </div>

                      {userApplication.applicationStatus === 'APPROVED' && (
                        <div className="bg-green-600 bg-opacity-20 border border-green-600 rounded-lg p-4 mb-4">
                          <div className="flex items-center text-green-400 mb-2">
                            <FaUserCheck className="mr-2" />
                            <span className="font-semibold">Ваша заявка одобрена!</span>
                          </div>
                          <p className="text-gray-300 text-sm">
                            Вы зарегистрированы на это мероприятие. Не забудьте прийти вовремя!
                          </p>
                        </div>
                      )}

                      {(userApplication.applicationStatus === 'APPROVED' || userApplication.applicationStatus === 'PENDING') && (
                        <button
                          onClick={handleCancelApplication}
                          className="w-full flex justify-center items-center bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded transition-colors duration-300"
                          disabled={event.isCancelled}
                        >
                          <FaTimesCircle className="mr-2" />
                          Отменить заявку
                        </button>
                      )}
                    </div>
                  ) : (
                    <div>
                      {event.isCancelled ? (
                        <div className="bg-red-600 bg-opacity-20 border border-red-600 rounded-lg p-4">
                          <p className="text-gray-300 text-center">
                            Мероприятие отменено. Регистрация невозможна.
                          </p>
                        </div>
                      ) : (
                        <button
                          onClick={() => setShowApplicationForm(true)}
                          className="w-full bg-yellow-500 hover:bg-yellow-600 text-white font-bold py-2 px-4 rounded transition-colors duration-300"
                        >
                          Подать заявку на участие
                        </button>
                      )}
                    </div>
                  )}
                </div>
              ) : (
                <div className="bg-gray-700 rounded-lg p-4 text-center">
                  <p className="text-gray-300 mb-4">
                    Чтобы подать заявку на участие, необходимо авторизоваться
                  </p>
                  <Link
                    to="/auth"
                    className="inline-block bg-yellow-500 hover:bg-yellow-600 text-white font-bold py-2 px-4 rounded transition-colors duration-300"
                  >
                    Войти
                  </Link>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Модальное окно подачи заявки */}
      {showApplicationForm && (
        <EventApplicationForm
          event={event}
          onSubmit={handleApplicationSubmit}
          onCancel={() => setShowApplicationForm(false)}
        />
      )}
    </motion.div>
  );
};

export default EventDetailPage;
