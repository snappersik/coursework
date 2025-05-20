import { makeAutoObservable } from 'mobx';
import Cookies from 'js-cookie';

class AuthStore {
  isAuthorized = false;
  userRole = null; // Будет хранить 'ADMIN', 'USER', 'ORGANIZER'
  userId = null;

  constructor() {
    makeAutoObservable(this);
    const storedRoleName = localStorage.getItem('userRole'); // Ожидаем имя роли
    const storedUserId = localStorage.getItem('userId');

    if (storedRoleName && storedRoleName !== 'null' && storedUserId && storedUserId !== 'null') {
      this.isAuthorized = true;
      this.userRole = storedRoleName; // Сохраняем имя роли
      this.userId = storedUserId;
      console.log('[AuthStore] Конструктор: Инициализация из localStorage:', {
        isAuthorized: this.isAuthorized,
        userRole: this.userRole,
        userId: this.userId
      });
    } else {
      console.log('[AuthStore] Конструктор: Нет валидных данных в localStorage для инициализации.');
    }
  } // Конец конструктора

  setAuthorized(isAuthorized, roleName, userId) { // Принимаем roleName
    console.log('[AuthStore] setAuthorized вызван с:', { isAuthorized, roleName, userId });
    this.isAuthorized = !!isAuthorized;
    this.userRole = roleName || null; // Сохраняем имя роли
    this.userId = userId || null;

    if (this.isAuthorized && this.userRole && this.userId) {
      localStorage.setItem('userRole', this.userRole); // Сохраняем имя роли
      localStorage.setItem('userId', String(this.userId));
      console.log('[AuthStore] setAuthorized: Сохранено в localStorage:', { userRole: this.userRole, userId: this.userId });
    } else {
      if (this.isAuthorized || this.userRole || this.userId) {
        console.log('[AuthStore] setAuthorized: Неполные данные для авторизации или isAuthorized=false, вызываю logout.');
        this.logout();
      } else {
        console.log('[AuthStore] setAuthorized: Уже в разлогиненном состоянии, logout не вызывается повторно.');
      }
    }
  }

  logout() {
    if (!this.isAuthorized && this.userRole === null && this.userId === null) {
      console.log('[AuthStore] logout: Уже разлогинен, выход.');
      return;
    }
    console.log('[AuthStore] logout: Полная очистка состояния и localStorage');
    this.isAuthorized = false;
    this.userRole = null;
    this.userId = null;

    localStorage.removeItem('userRole');
    localStorage.removeItem('userId');
    Cookies.remove('jwt');
    console.log('[AuthStore] logout: Состояние и localStorage очищены.');
  }
}

export const authStore = new AuthStore();
