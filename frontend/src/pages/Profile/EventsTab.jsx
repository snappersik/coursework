import React, { useState } from 'react';
import { observer } from 'mobx-react-lite';
import EventDetailsModal from '../../components/modals/EventDetailsModal';
import ConfirmationModal from '../../components/modals/ConfirmationModal';
import { toast } from 'react-toastify';

const EventsTab = observer(({ applications, onCancelApplication, onRestoreApplication }) => {
  const [selectedApplication, setSelectedApplication] = useState(null);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [confirmAction, setConfirmAction] = useState(null);
  const [actionText, setActionText] = useState('');

  const handleViewDetails = (application) => {
    setSelectedApplication(application);
    setShowDetailsModal(true);
  };

  const handleCancelClick = (application) => {
    setSelectedApplication(application);
    setConfirmAction('cancel');
    setActionText('отменить');
    setShowConfirmModal(true);
  };

  const handleRestoreClick = (application) => {
    setSelectedApplication(application);
    setConfirmAction('restore');
    setActionText('восстановить');
    setShowConfirmModal(true);
  };

  const handleConfirmAction = async () => {
    setShowConfirmModal(false);
    
    if (confirmAction === 'cancel') {
      const success = await onCancelApplication(selectedApplication.id);
      if (success) {
        toast.success('Заявка успешно отменена');
      } else {
        toast.error('Не удалось отменить заявку');
      }
    } else if (confirmAction === 'restore') {
      const success = await onRestoreApplication(selectedApplication.id);
      if (success) {
        toast.success('Заявка успешно восстановлена');
      } else {
        toast.error('Не удалось восстановить заявку');
      }
    }
  };

  // Разделяем заявки на активные и удаленные
  const activeApplications = applications.filter(app => !app.isDeleted);
  const deletedApplications = applications.filter(app => app.isDeleted);

  if (applications.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center p-12 bg-gray-700 rounded-lg">
        <div className="text-5xl mb-5">📅</div>
        <h3 className="text-xl font-semibold text-yellow-500 mb-2">У вас пока нет заявок на мероприятия</h3>
        <p className="text-gray-400">Посетите раздел мероприятий, чтобы найти интересные события</p>
      </div>
    );
  }

  return (
    <div className="text-white">
      <h2 className="text-2xl font-bold text-yellow-500 mb-5">Ваши заявки на мероприятия</h2>
      
      {activeApplications.length > 0 && (
        <div className="mb-8">
          <h3 className="text-xl text-gray-300 mb-4">Активные заявки</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {activeApplications.map(application => (
              <div key={application.id} className="bg-gray-700 rounded-lg overflow-hidden shadow-md">
                <div className="bg-gray-800 p-4 flex justify-between items-center">
                  <div className="flex flex-col">
                    <span className="font-bold">Заявка #{application.id}</span>
                    <span className="text-sm text-gray-400">{application.createdWhen}</span>
                  </div>
                  <div className={`px-3 py-1 rounded-full text-sm font-semibold
                    ${application.applicationStatus === 'APPROVED' ? 'bg-green-500 text-white' : 
                      application.applicationStatus === 'REJECTED' ? 'bg-red-500 text-white' : 
                      'bg-yellow-500 text-gray-900'}`}>
                    {application.applicationStatus === 'APPROVED' ? 'Одобрена' : 
                      application.applicationStatus === 'REJECTED' ? 'Отклонена' : 
                      'На рассмотрении'}
                  </div>
                </div>
                
                <div className="p-4 flex justify-between">
                  <button 
                    className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors"
                    onClick={() => handleViewDetails(application)}
                  >
                    Просмотр
                  </button>
                  <button 
                    className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600 transition-colors"
                    onClick={() => handleCancelClick(application)}
                  >
                    Отменить
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
      
      {deletedApplications.length > 0 && (
        <div>
          <h3 className="text-xl text-gray-300 mb-4">Отмененные заявки</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {deletedApplications.map(application => (
              <div key={application.id} className="bg-gray-700 rounded-lg overflow-hidden shadow-md opacity-70">
                <div className="bg-gray-800 p-4 flex justify-between items-center">
                  <div className="flex flex-col">
                    <span className="font-bold">Заявка #{application.id}</span>
                    <span className="text-sm text-gray-400">{application.createdWhen}</span>
                  </div>
                  <div className="bg-red-500 text-white px-3 py-1 rounded-full text-sm font-semibold">
                    Отменена
                  </div>
                </div>
                
                <div className="p-4 flex justify-between">
                  <button 
                    className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors"
                    onClick={() => handleViewDetails(application)}
                  >
                    Просмотр
                  </button>
                  <button 
                    className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600 transition-colors"
                    onClick={() => handleRestoreClick(application)}
                  >
                    Восстановить
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
      
      <EventDetailsModal 
        isOpen={showDetailsModal} 
        onClose={() => setShowDetailsModal(false)} 
        application={selectedApplication} 
      />
      
      <ConfirmationModal 
        isOpen={showConfirmModal} 
        onClose={() => setShowConfirmModal(false)} 
        onConfirm={handleConfirmAction}
        title={`Подтверждение действия`}
        message={`Вы уверены, что хотите ${actionText} заявку #${selectedApplication?.id}?`}
      />
    </div>
  );
});

export default EventsTab;
