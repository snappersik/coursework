import React, { useState, useEffect, useRef } from 'react';
import './Slider.css';
const Slider = () => {
    // Данные слайдера с добавленным z-index для контроля порядка отображения
    const [slides, setSlides] = useState([
        {
            id: 1,
            image: '/src/assets/img/book_1.webp', // книга Набокова (черная)
            backgroundUrl: '/src/assets/img/bg_1.webp', // фоновое изображение
            title: 'Осенний',
            subtitle: 'Эксклюзив',
            description: 'Антиутопия, Философская проза',
            status: 'current',
            direction: 0,
            zIndex: 3 // Центральная книга имеет наивысший z-index
        },
        {
            id: 2,
            image: '/src/assets/img/book_6.webp', // книга Хан Ган (розовая)
            backgroundUrl: '/src/assets/img/bg_2.webp',
            title: 'Новая',
            subtitle: 'Звезда',
            description: 'Психологический роман, Социальная проза',
            status: 'next',
            direction: 1,
            zIndex: 2 // Книга справа имеет средний z-index
        },
        {
            id: 3,
            image: '/src/assets/img/book_4.webp', // книга Стейнбека (светлая)
            backgroundUrl: '/src/assets/img/bg_3.webp',
            title: 'Бессмертная',
            subtitle: 'Классика',
            description: 'Классическая проза, Семейная сага',
            status: 'previous',
            direction: -1,
            zIndex: 1 // Книга слева имеет низший z-index
        }
    ]);
    const [loading, setLoading] = useState(true);
    const [loadingProgress, setLoadingProgress] = useState(0);
    // Refs для доступа к DOM-элементам
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
                // Улучшенная функция эффекта наклона
                const handleMouseMove = (e) => {
                    const { left, top, width, height } = slideElement.getBoundingClientRect();
                    const x = (e.clientX - left) / width - 0.5;
                    const y = (e.clientY - top) / height - 0.5;
                    // Увеличиваем угол наклона до 20 градусов и делаем более плавным
                    innerElement.style.transition = 'transform 0.2s cubic-bezier(0.215, 0.61, 0.355, 1)';
                    innerElement.style.transform = `perspective(1000px) rotateY(${x * 20}deg) rotateX(${-y *
                        20}deg) scale(1.05)`;
                    // Эффект параллакса для содержимого
                    if (infoInnerElement) {
                        infoInnerElement.style.transition = 'transform 0.2s cubic-bezier(0.215, 0.61, 0.355, 1)';
                        infoInnerElement.style.transform = `perspective(1000px) rotateY(${x * 10}deg)
rotateX(${-y * 10}deg) translateZ(20px)`;
                    }
                };
                // Более плавное возвращение в исходное положение
                const handleMouseLeave = () => {
                    innerElement.style.transition = 'transform 0.5s cubic-bezier(0.215, 0.61, 0.355, 1)';
                    innerElement.style.transform = 'perspective(1000px) rotateY(0deg) rotateX(0deg) scale(1)';
                    if (infoInnerElement) {
                        infoInnerElement.style.transition = 'transform 0.5s cubic-bezier(0.215, 0.61, 0.355, 1)';
                        infoInnerElement.style.transform = 'perspective(1000px) rotateY(0deg) rotateX(0deg) translateZ(0)';
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
    // Функция изменения слайдов с исправленным z-index
    const changeSlide = (direction) => {
        setSlides(prevSlides => {
            const newSlides = [...prevSlides];
            // Находим индексы слайдов
            const currentIndex = newSlides.findIndex(slide => slide.status === 'current');
            const nextIndex = newSlides.findIndex(slide => slide.status === 'next');
            const previousIndex = newSlides.findIndex(slide => slide.status === 'previous');
            if (direction === 1) {
                // Вперед: правая книга становится центральной
                // Временно устанавливаем высокий z-index для книги, которая будет центральной
                newSlides[nextIndex].zIndex = 10;
                // Обновляем статусы
                newSlides[currentIndex].status = 'previous';
                newSlides[nextIndex].status = 'current';
                newSlides[previousIndex].status = 'next';
                // После анимации устанавливаем правильные z-index'ы
                setTimeout(() => {
                    setSlides(current => {
                        const updated = [...current];
                        updated[currentIndex].zIndex = 1;
                        updated[nextIndex].zIndex = 3;
                        updated[previousIndex].zIndex = 2;
                        return updated;
                    });
                }, 500); // Подождем, пока закончится анимация
            } else {
                // Назад: левая книга становится центральной
                // Временно устанавливаем высокий z-index для книги, которая будет центральной
                newSlides[previousIndex].zIndex = 10;
                // Обновляем статусы
                newSlides[currentIndex].status = 'next';
                newSlides[nextIndex].status = 'previous';
                newSlides[previousIndex].status = 'current';
                // После анимации устанавливаем правильные z-index'ы
                setTimeout(() => {
                    setSlides(current => {
                        const updated = [...current];
                        updated[currentIndex].zIndex = 2;
                        updated[nextIndex].zIndex = 1;
                        updated[previousIndex].zIndex = 3;
                        return updated;
                    });
                }, 500); // Подождем, пока закончится анимация
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
                                    style={{
                                        '--z-index': slide.zIndex,
                                        zIndex: slide.zIndex
                                    }}
                                    ref={el => slidesRef.current[index] = el}
                                >
                                    <div
                                        className="slide__inner"
                                        ref={el => slideInnersRef.current[index] = el}
                                        style={{
                                            imageRendering: 'high-quality', // Улучшает качество изображения
                                            backfaceVisibility: 'hidden',
                                            transformStyle: 'preserve-3d',
                                            willChange: 'transform',
                                            transition: 'transform 0.4s cubic-bezier(0.215, 0.61, 0.355, 1)'
                                        }}
                                    >
                                        <div className="slide--image__wrapper">
                                            <img
                                                className="slide--image"
                                                src={slide.image}
                                                alt={`Slide ${slide.id}`}
                                                style={{
                                                    backfaceVisibility: 'hidden',
                                                    transformStyle: 'preserve-3d',
                                                    willChange: 'transform'
                                                }}
                                            />
                                        </div>
                                    </div>
                                </div>
                                <div
                                    className="slide__bg"
                                    style={{
                                        '--bg': `url(${slide.backgroundUrl})`,
                                        '--dir': slide.direction,
                                        backgroundColor: '#252525', // Фоновый цвет на случай, если изображение не загрузится
                                        backgroundImage: `url(${slide.backgroundUrl})`,
                                        backgroundSize: 'cover',
                                        backgroundPosition: 'center',
                                        backgroundRepeat: 'no-repeat',
                                        filter: 'blur(8px)',
                                        zIndex: 0
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
            {/* SVG фильтр для улучшения поддержки в Firefox */}
            <svg style={{ position: 'absolute', width: 0, height: 0 }}>
                <defs>
                    <filter id="image_overlay">
                        <feColorMatrix type="matrix" values="1 0 0 0 0 0 1 0 0 0 0 0 1 0 0 0 0 0 1 0" />
                    </filter>
                </defs>
            </svg>
        </div>
    );
};
export default Slider;
