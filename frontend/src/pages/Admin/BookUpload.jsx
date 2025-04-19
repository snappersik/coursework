import React, { useState } from "react";
import { uploadBookImage, uploadBookBackground } from "../../api/apiClient";

const BookUpload = ({ bookId }) => {
  // Для обложки
  const [file, setFile] = useState();
  const [url, setUrl] = useState("");
  // Для фона
  const [bgFile, setBgFile] = useState();
  const [bgUrl, setBgUrl] = useState("");
  const [message, setMessage] = useState("");

  async function handleCover(e) {
    e.preventDefault();
    setMessage("");
    try {
      await uploadBookImage({ bookId, file, url });
      setMessage("Обложка успешно загружена");
    } catch {
      setMessage("Ошибка загрузки обложки");
    }
  }
  async function handleBg(e) {
    e.preventDefault();
    setMessage("");
    try {
      await uploadBookBackground({ bookId, file: bgFile, url: bgUrl });
      setMessage("Фон успешно загружен");
    } catch {
      setMessage("Ошибка загрузки фона");
    }
  }

  return (
    <div className="max-w-lg mx-auto mt-10 p-6 bg-white shadow-lg rounded-lg space-y-7">
      <h2 className="text-xl font-bold mb-2">Загрузка обложки книги</h2>
      <form className="flex flex-col gap-4" onSubmit={handleCover}>
        <label className="block">
          <span className="text-sm">Обложка файл:</span>
          <input
            type="file"
            className="block w-full mt-1"
            accept="image/*"
            onChange={e => setFile(e.target.files[0])}
          />
        </label>
        <label className="block">
          <span className="text-sm">Обложка по URL:</span>
          <input
            type="text"
            placeholder="https://..."
            value={url}
            onChange={e => setUrl(e.target.value)}
            className="mt-1 px-3 py-2 border rounded w-full"
          />
        </label>
        <button
          type="submit"
          className="bg-blue-600 text-white rounded px-4 py-2 hover:bg-blue-700"
        >Загрузить обложку</button>
      </form>

      <h2 className="text-xl font-bold mb-2">Загрузка фонового изображения</h2>
      <form className="flex flex-col gap-4" onSubmit={handleBg}>
        <label className="block">
          <span className="text-sm">Фон файл:</span>
          <input
            type="file"
            className="block w-full mt-1"
            accept="image/*"
            onChange={e => setBgFile(e.target.files[0])}
          />
        </label>
        <label className="block">
          <span className="text-sm">Фон по URL:</span>
          <input
            type="text"
            placeholder="https://..."
            value={bgUrl}
            onChange={e => setBgUrl(e.target.value)}
            className="mt-1 px-3 py-2 border rounded w-full"
          />
        </label>
        <button
          type="submit"
          className="bg-green-600 text-white rounded px-4 py-2 hover:bg-green-700"
        >Загрузить фон</button>
      </form>

      {message && (
        <div className="mt-4 text-center text-sm font-bold text-gray-700">
          {message}
        </div>
      )}
    </div>
  );
};

export default BookUpload;
