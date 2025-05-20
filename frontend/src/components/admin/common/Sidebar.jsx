import React from 'react';
import { Link } from 'react-router-dom';
import { 
  FaChartBar, 
  FaBook, 
  FaUsers, 
  FaShoppingCart, 
  FaCalendarAlt, 
  FaImages, 
  FaBoxOpen,
  FaHistory,
  FaClipboardList
} from 'react-icons/fa';

// Компонент для элемента боковой панели для уменьшения дублирования
const SidebarItem = ({ to, icon, text, active, onClick, sidebarOpen }) => {
  return (
    <li className="mb-1">
      <Link
        to={to}
        onClick={onClick}
        className={`flex items-center py-2.5 px-4 rounded-md text-sm font-medium transition-colors duration-150 ease-in-out
                    ${active ? 'bg-yellow-500 text-white' : 'text-gray-300 hover:bg-indigo-700 hover:text-white'}
                    ${sidebarOpen ? 'justify-start' : 'justify-center'}`}
        title={sidebarOpen ? '' : text} // Показываем полный текст во всплывающей подсказке, когда сайдбар свернут
      >
        <span className="text-lg">{icon}</span>
        {sidebarOpen && <span className="ml-3">{text}</span>}
      </Link>
    </li>
  );
};

const Sidebar = ({ sidebarOpen, toggleSidebar, activeSection, setActiveSection, hasAccess, userRole }) => {
  return (
    <div className={`${sidebarOpen ? 'w-64' : 'w-20'} bg-indigo-800 text-white transition-all duration-300 ease-in-out h-screen`}>
      <div className="p-4 flex items-center justify-between border-b border-indigo-700"> {/* Небольшая коррекция границы */}
        <h2 className={`text-xl font-semibold ${!sidebarOpen && 'hidden'}`}>Админ-панель</h2>
        <button onClick={toggleSidebar} className="p-1 rounded-full hover:bg-indigo-700">
          <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16m-7 6h7" />
          </svg>
        </button>
      </div>
      
      <nav className="mt-6 px-2"> {/* Добавлены отступы для списка */}
        <ul>
          {hasAccess('dashboard') && (
            <SidebarItem
              to="/admin/dashboard"
              icon={<FaChartBar />}
              text="Дашборд"
              active={activeSection === 'dashboard'}
              onClick={() => setActiveSection('dashboard')}
              sidebarOpen={sidebarOpen}
            />
          )}
          
          {hasAccess('users') && (
            <SidebarItem
              to="/admin/users"
              icon={<FaUsers />}
              text="Пользователи"
              active={activeSection === 'users'}
              onClick={() => setActiveSection('users')}
              sidebarOpen={sidebarOpen}
            />
          )}
          
          {hasAccess('books') && (
            <SidebarItem
              to="/admin/books"
              icon={<FaBook />}
              text="Книги"
              active={activeSection === 'books'}
              onClick={() => setActiveSection('books')}
              sidebarOpen={sidebarOpen}
            />
          )}
          
          {hasAccess('orders') && (
            <SidebarItem
              to="/admin/orders"
              icon={<FaShoppingCart />}
              text="Заказы"
              active={activeSection === 'orders'}
              onClick={() => setActiveSection('orders')}
              sidebarOpen={sidebarOpen}
            />
          )}
          
          {hasAccess('events') && (
            <SidebarItem
              to="/admin/events"
              icon={<FaCalendarAlt />}
              text="Мероприятия"
              active={activeSection === 'events'}
              onClick={() => setActiveSection('events')}
              sidebarOpen={sidebarOpen}
            />
          )}

          {/* Добавляем пункт для EventApplicationManager */}
          {hasAccess('event-applications') && (
            <SidebarItem
              to="/admin/event-applications"
              icon={<FaClipboardList />} // Новая иконка для заявок
              text="Заявки на мероприятия"
              active={activeSection === 'event-applications'}
              onClick={() => setActiveSection('event-applications')}
              sidebarOpen={sidebarOpen}
            />
          )}
          
          {/* {hasAccess('slider') && (
            <SidebarItem
              to="/admin/slider"
              icon={<FaImages />}
              text="Слайдер"
              active={activeSection === 'slider'}
              onClick={() => setActiveSection('slider')}
              sidebarOpen={sidebarOpen}
            />
          )} */}
          
          {hasAccess('products') && (
            <SidebarItem
              to="/admin/products"
              icon={<FaBoxOpen />}
              text="Продукты"
              active={activeSection === 'products'}
              onClick={() => setActiveSection('products')}
              sidebarOpen={sidebarOpen}
            />
          )}
          
          {hasAccess('activity') && (
            <SidebarItem
              to="/admin/activity"
              icon={<FaHistory />}
              text="Отслеживание активности"
              active={activeSection === 'activity'}
              onClick={() => setActiveSection('activity')}
              sidebarOpen={sidebarOpen}
            />
          )}
        </ul>
      </nav>
      
      {sidebarOpen && (
        <div className="absolute bottom-4 left-4 text-sm">
          Вы вошли как: <span className="font-bold text-yellow-400">{userRole}</span>
        </div>
      )}
    </div>
  );
};

export default Sidebar;
