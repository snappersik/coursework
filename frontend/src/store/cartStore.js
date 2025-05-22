import { makeAutoObservable, runInAction } from 'mobx';
import apiClient, { addToCart, removeFromCart, createOrder as apiCreateOrder, clearUserCart, getUserInfo } from "../api/apiClient"; // Добавлен clearUserCart
import { authStore } from './store.js';

class CartStore {
  items = [];
  isLoading = false; // Добавим состояние загрузки

  constructor() {
    makeAutoObservable(this);
    // Загружаем корзину с сервера при инициализации, если пользователь авторизован
    // this.loadCartFromServer(); // Вызовем при необходимости или при логине
    // Пока оставим загрузку из localStorage, но с пониманием ее ограничений
    const savedCart = localStorage.getItem(`cartItems_${authStore.userId}`);
    if (savedCart) {
      this.items = JSON.parse(savedCart);
    }
  }

  async loadCartFromServer() {
    if (!authStore.isAuthorized || !authStore.userId) {
      // Если не авторизован, или нет userId, используем/очищаем локальную корзину
      const localCart = localStorage.getItem(`cartItems_guest`); // или общую
      this.items = localCart ? JSON.parse(localCart) : [];
      this.saveToLocalStorage();
      return;
    }
    this.isLoading = true;
    try {
      const cartData = await apiClient.get('/carts/my-cart');
      runInAction(() => {
        this.items = cartData.data.products.map(p => ({
          id: p.id,
          name: p.name,
          price: p.price,
          coverImageUrl: p.coverImageUrl || p.coverImageFilename, // Учитываем оба поля
          category: p.category,
          description: p.description,
          quantity: cartData.data.products.filter(prod => prod.id === p.id).length // Примерный подсчет количества, если бэк не дает quantity
          // В идеале, DTO корзины с бэкенда должно включать quantity для каждого ProductInCart
        })) || [];
        this.saveToLocalStorage(); // Синхронизируем localStorage с серверной версией
        this.isLoading = false;
      });
    } catch (error) {
      console.error("Ошибка при загрузке корзины с сервера:", error);
      runInAction(() => {
        this.isLoading = false;
        // Можно оставить локальную корзину или очистить ее в случае ошибки
        // this.items = []; 
        this.saveToLocalStorage();
      });
    }
  }


  async addItem(product) {
    if (!authStore.isAuthorized) {
      throw new Error('Необходимо авторизоваться');
    }
    // userId больше не нужен явно, сервер его знает
    this.isLoading = true;
    try {
      // Отправляем запрос на сервер
      const cartData = await addToCart(product.id); // Передаем только productId

      // Обновляем локальное состояние с полными данными о товаре
      // Лучше перезапросить корзину с сервера для актуальности
      await this.loadCartFromServer(); 
      // Или, если хотим оптимистичное обновление:
      // runInAction(() => {
      //   const existingItem = this.items.find(item => item.id === product.id);
      //   if (existingItem) {
      //     existingItem.quantity = cartData.products.find(p => p.id === product.id)?.quantity || existingItem.quantity + 1; // Пример
      //   } else {
      //     this.items.push({ ...product, quantity: 1 });
      //   }
      //   this.saveToLocalStorage();
      //   this.isLoading = false;
      // });
      return cartData;
    } catch (error) {
      console.error("Ошибка при добавлении товара в корзину:", error);
      runInAction(() => this.isLoading = false);
      throw error;
    }
  }

  async removeItem(productId) {
    if (!authStore.isAuthorized) {
      throw new Error('Необходимо авторизоваться');
    }
    this.isLoading = true;
    try {
      await removeFromCart(productId); // Передаем только productId
      // Обновляем локальное состояние
      // Лучше перезапросить корзину с сервера
      await this.loadCartFromServer();
      // Или оптимистичное обновление:
      // runInAction(() => {
      //   this.items = this.items.filter(item => item.id !== productId);
      //   this.saveToLocalStorage();
      //   this.isLoading = false;
      // });
    } catch (error) {
      runInAction(() => this.isLoading = false);
      throw error;
    }
  }

  async clearCart() { // Теперь очищает корзину текущего пользователя на сервере
    if (!authStore.isAuthorized) {
      this.items = [];
      this.saveToLocalStorage();
      return;
    }
    this.isLoading = true;
    try {
      await clearUserCart(); // Используем новый метод из apiClient
      runInAction(() => {
        this.items = [];
        this.saveToLocalStorage();
        this.isLoading = false;
      });
    } catch (error) {
      console.error("Ошибка при очистке корзины:", error);
      runInAction(() => this.isLoading = false);
      // Можно не очищать локально, если серверная операция не удалась
      throw error; 
    }
  }

  saveToLocalStorage() {
    // Сохраняем корзину для конкретного пользователя или как гостевую
    const key = authStore.userId ? `cartItems_${authStore.userId}` : 'cartItems_guest';
    localStorage.setItem(key, JSON.stringify(this.items));
  }

  // Вызывать при логине пользователя
  async handleLogin() {
    await this.loadCartFromServer();
  }

  // Вызывать при логауте пользователя
  handleLogout() {
    runInAction(() => {
      this.items = [];
      // Очищаем localStorage для всех пользователей или только для текущего
      // localStorage.removeItem(`cartItems_${authStore.userId}`); // Если userId еще доступен
      // Безопаснее пройтись по ключам или иметь специфический ключ
      // Простой вариант - не очищать localStorage при логауте, а при следующем входе грузить с сервера.
      // Либо очищать все `cartItems_*`
      this.saveToLocalStorage(); // Сохранит пустую гостевую корзину
    });
  }


  async createOrder() {
    this.isLoading = true;
    try {
      const result = await apiCreateOrder(); // apiCreateOrder уже определен и использует /orders/create
      await this.clearCart(); // Очистит корзину на сервере и локально
      runInAction(() => this.isLoading = false);
      return result;
    } catch (error) {
      runInAction(() => this.isLoading = false);
      throw error;
    }
  }

  get totalPrice() {
    return this.items.reduce((sum, item) => sum + (item.price * (item.quantity || 1)), 0);
  }

  get itemCount() {
    // Суммирует quantity всех товаров, если есть, иначе просто количество уникальных товаров
    return this.items.reduce((sum, item) => sum + (item.quantity || 1), 0);
    // return this.items.length; // если quantity не используется
  }
}

export const cartStore = new CartStore();

