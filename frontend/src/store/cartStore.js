// src/store/cartStore.js
import { makeAutoObservable } from 'mobx';
import { createOrder as apiCreateOrder } from "../api/apiClient";

class CartStore {
  items = [];
  
  constructor() {
    makeAutoObservable(this);
    
    // Initialize from localStorage if available
    const savedCart = localStorage.getItem('cartItems');
    if (savedCart) {
      this.items = JSON.parse(savedCart);
    }
  }
  
  addItem(product) {
    const existingItem = this.items.find(item => item.id === product.id);
    
    if (existingItem) {
      // If item already exists, increase quantity
      existingItem.quantity += 1;
    } else {
      // Otherwise add new item
      this.items.push({
        ...product,
        quantity: 1
      });
    }
    
    this.saveToLocalStorage();
  }
  
  removeItem(productId) {
    this.items = this.items.filter(item => item.id !== productId);
    this.saveToLocalStorage();
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
