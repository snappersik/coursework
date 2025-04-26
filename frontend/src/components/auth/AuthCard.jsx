import React, { useState } from 'react';
import LoginForm from './LoginForm';
import RegisterForm from './RegisterForm';

const AuthCard = () => {
    const [isLogin, setIsLogin] = useState(true); // По умолчанию показываем форму входа

    return (
        <div className="flex flex-col items-center justify-center ">
            {/* Форма входа или регистрации */}
            <div className="w-full sm:max-w-md mx-auto bg-[#494949] shadow-[0_25px_50px_-12px_rgba(0,0,0,0.25)] p-6 rounded-3xl">
                {isLogin ? (
                    <LoginForm onSwitchToRegister={() => setIsLogin(false)} />
                ) : (
                    <RegisterForm onSwitchToLogin={() => setIsLogin(true)} />
                )}
            </div>
        </div>
    );
};

export default AuthCard;