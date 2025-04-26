import { makeAutoObservable } from 'mobx';
import apiClient from '../api/apiClient';

class EventStore {
  events = new Map();
  loading = false;
  error = null;

  constructor() {
    makeAutoObservable(this);
  }

  setLoading(status) {
    this.loading = status;
  }

  setError(error) {
    this.error = error;
  }

  getEvent(eventId) {
    return this.events.get(eventId);
  }

  setEvent(eventId, eventData) {
    this.events.set(eventId, eventData);
  }

  async fetchEvent(eventId) {
    if (this.events.has(eventId)) {
      return this.events.get(eventId);
    }

    this.setLoading(true);
    this.setError(null);

    try {
      const response = await apiClient.get(`/events/${eventId}`);
      const eventData = response.data;
      this.setEvent(eventId, eventData);
      return eventData;
    } catch (error) {
      this.setError(error.message || 'Не удалось загрузить данные о мероприятии');
      console.error('Ошибка при загрузке мероприятия:', error);
      return null;
    } finally {
      this.setLoading(false);
    }
  }
}

export const eventStore = new EventStore();
