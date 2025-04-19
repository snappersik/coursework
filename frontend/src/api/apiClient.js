const API_URL = process.env.REACT_APP_API_URL || "http://localhost:8080/api";

// Получить список книг
export async function fetchBooks(params = {}) {
  let query = Object.entries(params)
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
    .join("&");
  let url = `${API_URL}/books${query ? "?" + query : ""}`;
  const resp = await fetch(url);
  if (!resp.ok) throw new Error("Не удалось получить список книг");
  return await resp.json();
}

// Получить одну книгу полностью
export async function fetchBook(id) {
  const resp = await fetch(`${API_URL}/books/${id}`);
  if (!resp.ok) throw new Error("Книга не найдена");
  return await resp.json();
}

// Получить url для обложки или фона
export function getBookCoverUrl(book) {
  if (!book) return "";
  // если у книги есть свойство coverImageUrl (extern) — воспользуемся им
  if (book.coverImageUrl) return book.coverImageUrl;
  // если у книги есть локальная картинка на сервере (выдаст backend)
  if (book.id) return `${API_URL}/books/${book.id}/cover`;
  // иначе плейсхолдер
  return "/src/assets/img/book_placeholder.webp";
}

// Загрузить обложку (file или url)
export async function uploadBookImage({bookId, file, url, type = "cover"}) {
  const formData = new FormData();
  if (file) formData.append("file", file);
  if (url) formData.append("url", url);
  let endpoint = `${API_URL}/books/${bookId}/upload-${type}`;
  const resp = await fetch(endpoint, {
    method: "POST",
    body: formData,
  });
  if (!resp.ok) throw new Error("Ошибка загрузки изображения");
  return await resp.json();
}

// То же — для фонового изображения (по желанию)
export function getBookBackgroundUrl(book) {
  if (!book) return "";
  if (book.backgroundImageUrl) return book.backgroundImageUrl;
  if (book.id) return `${API_URL}/books/${book.id}/background`;
  return "";
}

export async function uploadBookBackground({bookId, file, url}) {
  const formData = new FormData();
  if (file) formData.append("file", file);
  if (url) formData.append("url", url);
  let endpoint = `${API_URL}/books/${bookId}/upload-background`;
  const resp = await fetch(endpoint, {
    method: "POST",
    body: formData,
  });
  if (!resp.ok) throw new Error("Ошибка загрузки фонового изображения");
  return await resp.json();
}