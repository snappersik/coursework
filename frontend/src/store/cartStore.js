import { makeAutoObservable } from 'mobx';
import { addToCart, removeFromCart, createOrder as apiCreateOrder } from "../api/apiClient";
import { authStore } from './store.js'; // Импортируем authStore для получения userId

class CartStore {
  items = [];

  constructor() {
    makeAutoObservable(this);
    // Загружаем корзину из localStorage при инициализации
    const savedCart = localStorage.getItem('cartItems');
    if (savedCart) {
      this.items = JSON.parse(savedCart);
    }
  }

  async addItem(product) {
    if (!authStore.isAuthorized) {
      throw new Error('Необходимо авторизоваться');
    }

    const userId = authStore.userId; // Предполагаем, что userId будет добавлен в authStore
    if (!userId) {
      throw new Error('Не удалось определить пользователя');
    }

    try {
      // Отправляем запрос на сервер
      await addToCart(userId, product.id);

      // Обновляем локальное состояние
      const existingItem = this.items.find(item => item.id === product.id);
      if (existingItem) {
        existingItem.quantity += 1;
      } else {
        this.items.push({ ...product, quantity: 1 });
      }
      this.saveToLocalStorage();
    } catch (error) {
      throw error;
    }
  }

  async removeItem(productId) {
    if (!authStore.isAuthorized) {
      throw new Error('Необходимо авторизоваться');
    }

    const userId = authStore.userId;
    if (!userId) {
      throw new Error('Не удалось определить пользователя');
    }

    try {
      await removeFromCart(userId, productId);
      this.items = this.items.filter(item => item.id !== productId);
      this.saveToLocalStorage();
    } catch (error) {
      throw error;
    }
  }

  clearCart() {
    this.items = [];
    this.saveToLocalStorage();
  }

  saveToLocalStorage() {
    localStorage.setItem('cartItems', JSON.stringify(this.items));
  }

  async createOrder() {
    try {
      const result = await apiCreateOrder();
      this.clearCart();
      return result;
    } catch (error) {
      throw error;
    }
  }

  get totalPrice() {
    return this.items.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  }

  get itemCount() {
    return this.items.length;
  }
}

export const cartStore = new CartStore();