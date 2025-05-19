import React from "react";
import { useNavigate } from "react-router-dom";

const NotFoundPage = () => {
  const navigate = useNavigate();

  return (
    <div
      className="relative flex flex-col items-center justify-center min-h-screen bg-[#424242] overflow-hidden"
    >
      <img
        src="https://static.tildacdn.com/tild3065-6533-4937-b161-343562396665/svg.svg"
        alt="background"
        className="absolute inset-0 w-full h-full object-cover opacity-20 pointer-events-none"
      />
      <div className="relative z-10 flex flex-col items-center">
        <h1 className="title-font text-9xl font-bold text-yellow-400 mb-6 select-none">404</h1>
        <p className="text-2xl md:text-3xl text-white mb-10 text-center">Страница не найдена</p>
        <div className="flex gap-4">
          <button
            className="px-6 py-3 bg-yellow-400 text-[#424242] font-semibold rounded shadow hover:bg-yellow-300 transition"
            onClick={() => navigate("/")}
          >
            На главную
          </button>
          <button
            className="px-6 py-3 bg-[#505050] border border-white text-white font-semibold rounded hover:bg-[#606060] transition"
            onClick={() => navigate(-1)}
          >
            Вернуться назад
          </button>
        </div>
      </div>
    </div>
  );
};

export default NotFoundPage;
