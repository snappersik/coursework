import React from 'react';
import './ClubBenefits.css';

// Импортируем изображения
import dostoevskyImage from '/src/assets/img/dostoevsky.png';
import mayakovskyImage from '/src/assets/img/mayakovsky.png';
import ostapImage from '/src/assets/img/ostap.png';
import artImage from '/src/assets/img/art.png';
import marcusImage from '/src/assets/img/marcus.png';
import bradburyImage from '/src/assets/img/bradbury.png';

const ClubBenefits = () => {
  return (
    <section className="py-16 relative">
      <div className="container mx-auto px-4 relative z-10">
        <h2 className="title-font text-4xl md:text-5xl text-white text-center mb-12 font-bold">
          Зачем вам вступать в клуб?
        </h2>

        <div className="cards-container">
          {/* Карточка 1 */}
          <div className="card">
            <div className="card-content">
              <div className="dialogue-box" style={{ top: '20px', left: '20px' }}>
                <p className="dialogue-text">Как это вы меня<br />не любите?</p>
              </div>
              <img src={dostoevskyImage} alt="Достоевский" className="justify-self-end" />
            </div>
            <h3 className="text-base sm:text-lg md:text-lg lg:text-2xl text-white font-bold mb-2 text-center">
              Заново влюбиться в литературу
            </h3>
            <p className="text-xs sm:text-sm md:text-base lg:text-lg leading-relaxed text-center italic font-light">
              Обсуждение прочитанного в кругу<br />
              заинтересованных людей придаёт<br />
              чтению совершенно иной статус
            </p>
          </div>

          {/* Карточка 2 */}
          <div className="card">
            <div className="card-content">
              <div className="dialogue-box" style={{ top: '20px', right: '20px' }}>
                <p className="dialogue-text">Я так чувствую</p>
              </div>
              <img src={mayakovskyImage} alt="Маяковский" />
            </div>
            <h3 className="text-base sm:text-lg md:text-lg lg:text-2xl text-white font-bold mb-2 text-center">
              Научиться чувствовать «свои» книги
            </h3>
            <p className="text-xs sm:text-sm md:text-base lg:text-lg leading-relaxed text-center italic font-light">
              Вы сможете сделать чтение частью<br />
              своей повседневности
            </p>
          </div>

          {/* Карточка 3 */}
          <div className="card">
            <div className="card-content">
              <div className="dialogue-box" style={{ top: '20px', right: '20px' }}>
                <p className="dialogue-text">Бензин ваш —<br />идеи наши!</p>
              </div>
              <img src={ostapImage} alt="Остап" className="justify-self-end" />
            </div>
            <h3 className="text-base sm:text-lg md:text-lg lg:text-2xl text-white font-bold mb-2 text-center">
              Наполнять свою жизнь новыми мыслями и идеями
            </h3>
            <p className="text-xs sm:text-sm md:text-base lg:text-lg leading-relaxed text-center italic font-light">
              Вы сможете легко видеть сложное<br />
              в простом, а простое в сложном
            </p>
          </div>

          {/* Карточка 4 */}
          <div className="card">
            <div className="card-content">
              <div className="dialogue-box" style={{ top: '20px', left: '20px' }}>
                <p className="dialogue-text">Я просто с выставки<br />вернулась</p>
              </div>
              <img src={artImage} alt="Искусство" className="justify-self-start" />
            </div>
            <h3 className="text-base sm:text-lg md:text-lg lg:text-2xl text-white font-bold mb-2 text-center">
              Открыть мир искусства и кино
            </h3>
            <p className="text-xs sm:text-sm md:text-base lg:text-lg leading-relaxed text-center italic font-light">
              Искусство — это про любовь. Про то, насколько вы готовы услышать и познать мир вокруг и самих себя.
            </p>
          </div>

          {/* Карточка 5 */}
          <div className="card">
            <div className="card-content">
              <div className="dialogue-box" style={{ top: '20px', right: '20px' }}>
                <p className="dialogue-text">Поразмышляем<br />не наедине?</p>
              </div>
              <img src={marcusImage} alt="Марк Аврелий" />
            </div>
            <h3 className="text-base sm:text-lg md:text-lg lg:text-2xl text-white font-bold mb-2 text-center">
              Общаться с близкими по духу и находить контакт с собой
            </h3>
            <p className="text-xs sm:text-sm md:text-base lg:text-lg leading-relaxed text-center italic font-light">
              Некоторые подарки, как собственные изменения, надо себе позволить
            </p>
          </div>

          {/* Карточка 6 */}
          <div className="card">
            <div className="card-content">
              <div className="dialogue-box" style={{ top: '20px', right: '20px' }}>
                <p className="dialogue-text">Чур, с меня спички!</p>
              </div>
              <img src={bradburyImage} alt="Брэдбери" />
            </div>
            <h3 className="text-base sm:text-lg md:text-lg lg:text-2xl text-white font-bold mb-2 text-center">
              Узнать себя и найти то, что зажигает!
            </h3>
            <p className="text-xs sm:text-sm md:text-base lg:text-lg leading-relaxed text-center italic font-light">
              Нам важны не только знакомства, но и живые дискуссии на каждой встрече
            </p>
          </div>
        </div>
      </div>
    </section>
  );
};

export default ClubBenefits;
