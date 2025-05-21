import React, { useState } from "react";

const ContactPage = () => {
  const [form, setForm] = useState({
    name: "",
    fromEmail: "",
    phone: "",
    body: "",
  });
  const [status, setStatus] = useState("");

  const handleChange = (e) => {
    setForm((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setStatus("loading");
    try {
      const response = await fetch("/api/rest/contact", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(form),
      });
      if (response.ok) {
        setStatus("success");
        setForm({ name: "", fromEmail: "", phone: "", body: "" });
      } else {
        setStatus("error");
      }
    } catch (error) {
      setStatus("error");
    }
  };

  return (
    <div className="max-w-5xl mx-auto p-6 bg-[#424242] rounded-xl shadow-lg mt-8 text-white">
      <h1 className="text-3xl font-bold mb-6 text-yellow-500">Контакты</h1>
      <div className="grid md:grid-cols-2 gap-8">
        {/* Левая колонка: основательницы и карта */}
        <div>
          <div className="mb-8">
            <h2 className="text-xl font-semibold mb-2 text-white">Основательницы клуба</h2>
            <div className="flex flex-col gap-4">
              <div className="flex items-center gap-4">
                <img
                  src="/founder1.jpg"
                  alt="Екатерина"
                  className="w-16 h-16 rounded-full object-cover border-2 border-yellow-500"
                />
                <div>
                  <p className="font-bold text-yellow-400">Екатерина</p>
                  <p className="text-gray-300 text-sm">Организатор и вдохновитель</p>
                </div>
              </div>
              <div className="flex items-center gap-4">
                <img
                  src="/founder2.jpg"
                  alt="Мария"
                  className="w-16 h-16 rounded-full object-cover border-2 border-yellow-500"
                />
                <div>
                  <p className="font-bold text-yellow-400">Мария</p>
                  <p className="text-gray-300 text-sm">Куратор мероприятий</p>
                </div>
              </div>
            </div>
          </div>
          <div>
            <h2 className="text-xl font-semibold mb-2 text-white">Мы на карте</h2>
            <div className="rounded-lg overflow-hidden border-2 border-yellow-500 shadow-lg">
              <iframe
                title="Карта"
                src="https://yandex.ru/map-widget/v1/?um=constructor%3A7e7b6e1a0f3c0d1c5b64e3f2b8d8b7e6b2c9e7c7e9c8e7c8e7c8e7c8e7c8e7c8&amp;source=constructor"
                width="100%"
                height="220"
                frameBorder="0"
                allowFullScreen
                className="w-full"
              ></iframe>
            </div>
          </div>
        </div>
        {/* Правая колонка: форма */}
        <div>
          <h2 className="text-xl font-semibold mb-4 text-white">Связаться с нами</h2>
          <form onSubmit={handleSubmit} className="space-y-4">
            <input
              name="name"
              type="text"
              placeholder="Ваше имя"
              value={form.name}
              onChange={handleChange}
              required
              className="w-full p-3 rounded bg-[#525252] border border-gray-700 text-white focus:ring-2 focus:ring-yellow-500"
            />
            <input
              name="fromEmail"
              type="email"
              placeholder="Ваш email"
              value={form.fromEmail}
              onChange={handleChange}
              required
              className="w-full p-3 rounded bg-[#525252] border border-gray-700 text-white focus:ring-2 focus:ring-yellow-500"
            />
            <input
              name="phone"
              type="text"
              placeholder="Телефон (необязательно)"
              value={form.phone}
              onChange={handleChange}
              className="w-full p-3 rounded bg-[#525252] border border-gray-700 text-white focus:ring-2 focus:ring-yellow-500"
            />
            <textarea
              name="body"
              placeholder="Ваше сообщение"
              value={form.body}
              onChange={handleChange}
              required
              rows={5}
              className="w-full p-3 rounded bg-[#525252] border border-gray-700 text-white focus:ring-2 focus:ring-yellow-500"
            />
            <button
              type="submit"
              disabled={status === "loading"}
              className="w-full py-3 bg-yellow-500 hover:bg-yellow-600 rounded text-white font-bold transition-colors"
            >
              {status === "loading" ? "Отправка..." : "Отправить"}
            </button>
            {status === "success" && (
              <div className="text-green-400 mt-2">Ваше сообщение отправлено!</div>
            )}
            {status === "error" && (
              <div className="text-red-400 mt-2">Ошибка при отправке сообщения</div>
            )}
          </form>
        </div>
      </div>
    </div>
  );
};

export default ContactPage;
