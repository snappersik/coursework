import React from 'react';
import { Link } from 'react-router-dom';

const Sidebar = ({ sidebarOpen, toggleSidebar, activeSection, setActiveSection, hasAccess, userRole }) => {
    return (
        <div className={`${sidebarOpen ? 'w-64' : 'w-20'} bg-indigo-800 text-white transition-all duration-300 ease-in-out`}>
            <div className="p-4 flex items-center justify-between">
                <h2 className={`text-xl font-semibold ${!sidebarOpen && 'hidden'}`}>Админ-панель</h2>
                <button onClick={toggleSidebar} className="p-1 rounded-full hover:bg-indigo-700">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16m-7 6h7" />
                    </svg>
                </button>
            </div>
            <nav className="mt-8">
                <ul>
                    {hasAccess('dashboard') && (
                        <li>
                            <Link
                                to="/admin/dashboard"
                                className={`flex items-center p-4 hover:bg-indigo-700 ${activeSection === 'dashboard' ? 'bg-indigo-700' : ''}`}
                                onClick={() => setActiveSection('dashboard')}
                            >
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
                                </svg>
                                {sidebarOpen && <span>Дашборд</span>}
                            </Link>
                        </li>
                    )}
                    {hasAccess('books') && (
                        <li>
                            <Link
                                to="/admin/books"
                                className={`flex items-center p-4 hover:bg-indigo-700 ${activeSection === 'books' ? 'bg-indigo-700' : ''}`}
                                onClick={() => setActiveSection('books')}
                            >
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                                </svg>
                                {sidebarOpen && <span>Книги</span>}
                            </Link>
                        </li>
                    )}
                    {hasAccess('users') && (
                        <li>
                            <Link
                                to="/admin/users"
                                className={`flex items-center p-4 hover:bg-indigo-700 ${activeSection === 'users' ? 'bg-indigo-700' : ''}`}
                                onClick={() => setActiveSection('users')}
                            >
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                                </svg>
                                {sidebarOpen && <span>Пользователи</span>}
                            </Link>
                        </li>
                    )}
                    {hasAccess('orders') && (
                        <li>
                            <Link
                                to="/admin/orders"
                                className={`flex items-center p-4 hover:bg-indigo-700 ${activeSection === 'orders' ? 'bg-indigo-700' : ''}`}
                                onClick={() => setActiveSection('orders')}
                            >
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
                                </svg>
                                {sidebarOpen && <span>Заказы</span>}
                            </Link>
                        </li>
                    )}
                    {hasAccess('events') && (
                        <li>
                            <Link
                                to="/admin/events"
                                className={`flex items-center p-4 hover:bg-indigo-700 ${activeSection === 'events' ? 'bg-indigo-700' : ''}`}
                                onClick={() => setActiveSection('events')}
                            >
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                </svg>
                                {sidebarOpen && <span>Мероприятия</span>}
                            </Link>
                        </li>
                    )}
                    {hasAccess('products') && (
                        <li>
                            <Link
                                to="/admin/products"
                                className={`flex items-center p-4 hover:bg-indigo-700 ${activeSection === 'products' ? 'bg-indigo-700' : ''}`}
                                onClick={() => setActiveSection('products')}
                            >
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                                </svg>
                                {sidebarOpen && <span>Продукты</span>}
                            </Link>
                        </li>
                    )}
                    {hasAccess('slider') && (
                        <li>
                            <Link
                                to="/admin/slider"
                                className={`flex items-center p-4 hover:bg-indigo-700 ${activeSection === 'slider' ? 'bg-indigo-700' : ''}`}
                                onClick={() => setActiveSection('slider')}
                            >
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                </svg>
                                {sidebarOpen && <span>Слайдер</span>}
                            </Link>
                        </li>
                    )}
                    {hasAccess('activity') && (
                        <li>
                            <Link
                                to="/admin/activity"
                                className={`flex items-center p-4 hover:bg-indigo-700 ${activeSection === 'activity' ? 'bg-indigo-700' : ''}`}
                                onClick={() => setActiveSection('activity')}
                            >
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
                                </svg>
                                {sidebarOpen && <span>Отслеживание активности</span>}
                            </Link>
                        </li>
                    )}
                </ul>
            </nav>
        </div>
    );
};

export default Sidebar;