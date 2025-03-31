import React, { useState, useEffect, useRef } from 'react';
import './Slider.css';

const sliderData = [
  {
    id: 1,
    image: '/src/assets/img/book_1.webp',
    backgroundUrl: 'https://i.pinimg.com/564x/74/2f/3c/742f3c2b7cd7f528598658ae11e31e62.jpg',
    title: 'Осенний',
    subtitle: 'Эксклюзив',
    description: 'Антиутопия, Филосовская проза',
    status: 'current',
    direction: 0
  },
  {
    id: 2,
    image: '/src/assets/img/book_6.webp',
    backgroundUrl: 'https://e.snmc.io/i/600/s/cb4f84ea2b08f494e3f52eb46dd55e29/10833951/east-of-eden-east-of-eden-cover-art.jpg',
    title: 'Бессмертная',
    subtitle: 'Классика',
    description: 'Классическая проза, Семейная сага',
    status: 'next',
    direction: 1
  },
  {
    id: 3,
    image: '/src/assets/img/book_4.webp',
    backgroundUrl: 'https://i.pinimg.com/736x/22/15/bd/2215bd3b90b2ea4d5ad6e2f94aa03011.jpg',
    title: 'Новая',
    subtitle: 'Звезда',
    description: 'Психологический роман, Социальная проза',
    status: 'previous',
    direction: -1
  }
];

const Slider = () => {
  const [slides, setSlides] = useState(sliderData);
  const [loading, setLoading] = useState(true);
  const [loadingProgress, setLoadingProgress] = useState(0);
  
  const slidesRef = useRef([]);
  const slideInnersRef = useRef([]);
  const slideInfosRef = useRef([]);
  const slideInfoInnersRef = useRef([]);
  
  // Настройка refs для каждого слайда
  useEffect(() => {
    slidesRef.current = slidesRef.current.slice(0, slides.length);
    slideInnersRef.current = slideInnersRef.current.slice(0, slides.length);
    slideInfosRef.current = slideInfosRef.current.slice(0, slides.length);
    slideInfoInnersRef.current = slideInfoInnersRef.current.slice(0, slides.length);
  }, [slides]);
  
  // Эффект tilt для активного слайда
  useEffect(() => {
    slides.forEach((slide, index) => {
      if (slide.status === 'current' && 
          slidesRef.current[index] && 
          slideInnersRef.current[index]) {
        
        const slideElement = slidesRef.current[index];
        const innerElement = slideInnersRef.current[index];
        const infoInnerElement = slideInfoInnersRef.current[index];
        
        // Применяем эффект tilt к активному слайду
        const handleMouseMove = (e) => {
          const { left, top, width, height } = slideElement.getBoundingClientRect();
          const x = (e.clientX - left) / width - 0.5;
          const y = (e.clientY - top) / height - 0.5;
          
          innerElement.style.setProperty('--rotY', `${x * 10}deg`);
          innerElement.style.setProperty('--rotX', `${-y * 10}deg`);
          innerElement.style.setProperty('--bgPosX', `${-x * 5}%`);
          innerElement.style.setProperty('--bgPosY', `${y * 5}%`);
          
          if (infoInnerElement) {
            infoInnerElement.style.setProperty('--rotY', `${x * 10}deg`);
            infoInnerElement.style.setProperty('--rotX', `${-y * 10}deg`);
          }
        };
        
        const handleMouseLeave = () => {
          innerElement.style.setProperty('--rotY', '0deg');
          innerElement.style.setProperty('--rotX', '0deg');
          innerElement.style.setProperty('--bgPosX', '0%');
          innerElement.style.setProperty('--bgPosY', '0%');
          
          if (infoInnerElement) {
            infoInnerElement.style.setProperty('--rotY', '0deg');
            infoInnerElement.style.setProperty('--rotX', '0deg');
          }
        };
        
        slideElement.addEventListener('mousemove', handleMouseMove);
        slideElement.addEventListener('mouseleave', handleMouseLeave);
        
        return () => {
          slideElement.removeEventListener('mousemove', handleMouseMove);
          slideElement.removeEventListener('mouseleave', handleMouseLeave);
        };
      }
    });
  }, [slides]);

  // Имитация загрузки изображений
  useEffect(() => {
    let progress = 0;
    const interval = setInterval(() => {
      progress += 5;
      setLoadingProgress(progress);
      if (progress >= 100) {
        clearInterval(interval);
        setTimeout(() => setLoading(false), 500);
      }
    }, 100);

    return () => clearInterval(interval);
  }, []);

  // Функция изменения слайдов
  const changeSlide = (direction) => {
    setSlides(prevSlides => {
      const newSlides = [...prevSlides];
      
      // Находим индексы слайдов
      const currentIndex = newSlides.findIndex(slide => slide.status === 'current');
      const nextIndex = newSlides.findIndex(slide => slide.status === 'next');
      const previousIndex = newSlides.findIndex(slide => slide.status === 'previous');
      
      if (direction === 1) {
        // Вперед
        newSlides[currentIndex].status = 'previous';
        newSlides[nextIndex].status = 'current';
        newSlides[previousIndex].status = 'next';
      } else {
        // Назад
        newSlides[currentIndex].status = 'next';
        newSlides[nextIndex].status = 'previous';
        newSlides[previousIndex].status = 'current';
      }
      
      return newSlides;
    });
  };
  
  return (
    <div className="hero">
      <div className="slider">
        <button 
          className="slider--btn slider--btn__prev"
          onClick={() => changeSlide(-1)}
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <path d="m15 18-6-6 6-6" />
          </svg>
        </button>

        <div className="slides__wrapper">
          <div className="slides">
            {slides.map((slide, index) => (
              <React.Fragment key={slide.id}>
                <div 
                  className="slide" 
                  {...{
                    [`data-${slide.status}`]: ""
                  }}
                  ref={el => slidesRef.current[index] = el}
                >
                  <div 
                    className="slide__inner"
                    ref={el => slideInnersRef.current[index] = el}
                  >
                    <div className="slide--image__wrapper">
                      <img
                        className="slide--image"
                        src={slide.image}
                        alt={`Slide ${slide.id}`}
                      />
                    </div>
                  </div>
                </div>
                <div
                  className="slide__bg"
                  style={{
                    '--bg': `url(${slide.backgroundUrl})`,
                    '--dir': slide.direction,
                  }}
                  {...{
                    [`data-${slide.status}`]: ""
                  }}
                ></div>
              </React.Fragment>
            ))}
          </div>

          <div className="slides--infos">
            {slides.map((slide, index) => (
              <div 
                className="slide-info" 
                {...{
                  [`data-${slide.status}`]: ""
                }}
                key={`info-${slide.id}`}
                ref={el => slideInfosRef.current[index] = el}
              >
                <div 
                  className="slide-info__inner"
                  ref={el => slideInfoInnersRef.current[index] = el}
                >
                  <div className="slide-info--text__wrapper">
                    <div data-title="" className="slide-info--text">
                      <span>{slide.title}</span>
                    </div>
                    <div data-subtitle="" className="slide-info--text">
                      <span>{slide.subtitle}</span>
                    </div>
                    <div data-description="" className="slide-info--text">
                      <span>{slide.description}</span>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        <button 
          className="slider--btn slider--btn__next"
          onClick={() => changeSlide(1)}
        >
            <svg
            xmlns="http://www.w3.org/2000/svg"
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <path d="m9 18 6-6-6-6" />
          </svg>
        </button>
      </div>

      {loading && (
        <div className="loader" style={{ opacity: loadingProgress < 100 ? 1 : 0 }}>
          <span className="loader__text">{loadingProgress}%</span>
        </div>
      )}
    </div>
  );
};

export default Slider;