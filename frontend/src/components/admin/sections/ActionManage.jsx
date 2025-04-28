import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { getAuditEntries } from '../../../api/apiClient'; // Импортируем метод из apiClient

const ActionManage = () => {
    const [auditEntries, setAuditEntries] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchAuditEntries();
    }, []);

    const fetchAuditEntries = async () => {
        try {
            setLoading(true);
            const data = await getAuditEntries(); // Используем getAuditEntries
            setAuditEntries(data);
            setLoading(false);
        } catch (error) {
            console.error('Ошибка загрузки данных аудита:', error);
            toast.error('Не удалось загрузить данные аудита');
            setLoading(false);
        }
    };

    if (loading) {
        return <div className="text-center py-10">Загрузка...</div>;
    }

    return (
        <div>
            <h2 className="text-xl font-semibold mb-6">Отслеживание активности</h2>
            <div className="bg-white shadow-md rounded-lg overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                        <tr>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Тип действия</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Пользователь</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Время</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Детали</th>
                        </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                        {auditEntries.map(entry => (
                            <tr key={entry.id}>
                                <td className="px-6 py-4 whitespace-nowrap text-gray-500">{entry.id}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-gray-500">{entry.actionType}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-gray-500">{entry.userIdentifier}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-gray-500">{new Date(entry.timestamp).toLocaleString()}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-gray-500">{entry.details}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default ActionManage;