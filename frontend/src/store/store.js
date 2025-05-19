import { makeAutoObservable } from 'mobx';

class AuthStore {
  isAuthorized = false;
  userRole = null;
  userId = null; // Добавляем поле для userId

  constructor() {
    makeAutoObservable(this);

    // Инициализация из localStorage
    const storedRole = localStorage.getItem('userRole');
    const storedUserId = localStorage.getItem('userId');
    if (storedRole && storedUserId) {
      this.isAuthorized = true;
      this.userRole = storedRole;
      this.userId = storedUserId;
      console.log('Инициализация AuthStore из localStorage:', {
        isAuthorized: this.isAuthorized,
        userRole: this.userRole,
        userId: this.userId
      });
    }
  }

  setAuthorized(isAuthorized, userRole, userId) {
    console.log('Обновление состояния авторизации:', { isAuthorized, userRole, userId });
    this.isAuthorized = isAuthorized;
    this.userRole = userRole;
    this.userId = userId;

    // Сохраняем в localStorage
    if (isAuthorized && userRole && userId) {
      localStorage.setItem('userRole', userRole);
      localStorage.setItem('userId', userId);
    }
  }

  logout() {
    console.log('[AuthStore] Полная очистка состояния');
    this.isAuthorized = false;
    this.userRole = null;
    this.userId = null;

    // Атомарная очистка хранилища
    ['userRole', 'userId'].forEach(key =>
      localStorage.removeItem(key)
    );
  }

}

export const authStore = new AuthStore();