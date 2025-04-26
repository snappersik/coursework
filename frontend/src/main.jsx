// main.jsx
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App.jsx';
import { authStore } from './store/store.js';
import Cookies from "js-cookie"; // Import authStore
const authToken = Cookies.get('authToken');
const userRole = localStorage.getItem('userRole');
if (authToken && userRole && !authStore.isAuthorized) {
    authStore.setAuthorized(true, userRole);
}

createRoot(document.getElementById('root')).render(
    <StrictMode>
        <App />
    </StrictMode>
);
