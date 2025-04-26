import { makeAutoObservable } from 'mobx';

class AuthStore {
  isAuthorized = false;
  userRole = null;

  constructor() {
    makeAutoObservable(this);
    
    // Инициализация состояния из localStorage при создании хранилища
    const storedRole = localStorage.getItem('userRole');
    if (storedRole) {
      this.isAuthorized = true;
      this.userRole = storedRole;
      console.log('Инициализация AuthStore из localStorage:', { isAuthorized: this.isAuthorized, userRole: this.userRole });
    }
  }

  setAuthorized(isAuthorized, userRole) {
    console.log('Обновление состояния авторизации:', { isAuthorized, userRole });
    this.isAuthorized = isAuthorized;
    this.userRole = userRole;
    
    // Сохраняем роль в localStorage при авторизации
    if (isAuthorized && userRole) {
      localStorage.setItem('userRole', userRole);
    }
  }

  logout() {
    console.log('Выход из системы');
    this.isAuthorized = false;
    this.userRole = null;
    localStorage.removeItem('userRole');
  }
}

export const authStore = new AuthStore();
