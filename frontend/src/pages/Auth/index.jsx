// src/pages/Auth/index.jsx
import React from 'react';
import AuthCard from '../../components/auth/AuthCard';

const AuthPage = () => {
  return (
    <div className="flex items-center justify-center py-10 bg-[url('https://static.tildacdn.com/tild3264-3362-4861-b161-363561643235/svg_1.svg')] bg-top">
      <div className="w-full max-w-xl px-4">
        <div className="text-center mb-8">
          {/* Title */}
          <a href="/"
            className="mb-4 text-5xl font-semibold text-white title-font" >
            Книжная гавань
          </a>
        </div>
        <AuthCard />
      </div>
    </div>
  );
};

export default AuthPage;
