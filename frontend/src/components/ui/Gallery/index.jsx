import React from 'react';
import Carousel from 'react-multi-carousel';
import 'react-multi-carousel/lib/styles.css';
import './Gallery.css';

const Gallery = () => {
  // Динамический импорт всех изображений из директории
  const imageModules = import.meta.glob('/src/assets/img/gallery/rus/*.{jpg,png,jpeg,webp}', { eager: true });
  
  // Преобразуем объект модулей в массив URL изображений
  const bookImages = Object.values(imageModules).map(module => module.default);
  
  // Разделяем изображения на три ряда для карусели
  const firstRowImages = bookImages.slice(0, 7);
  const secondRowImages = bookImages.slice(7, 14);
  const thirdRowImages = bookImages.slice(14);
  
  // Настройки адаптивности для карусели
  const responsive = {
    superLargeDesktop: {
      breakpoint: { max: 4000, min: 1200 },
      items: 5
    },
    desktop: {
      breakpoint: { max: 1200, min: 992 },
      items: 4
    },
    tablet: {
      breakpoint: { max: 992, min: 576 },
      items: 3
    },
    mobile: {
      breakpoint: { max: 576, min: 0 },
      items: 2
    }
  };

  return (
    <section className="py-16" id="gallery">
      <div className="container mx-auto px-4">
        <h2 className="title-font text-4xl md:text-5xl text-white text-center mb-12 font-bold">
          Мы уже прочитали
        </h2>

        {/* Первая строка - движется влево */}
        <div className="mb-12">
          <Carousel
            responsive={responsive}
            infinite={true}
            autoPlay={true}
            autoPlaySpeed={3000}
            keyBoardControl={true}
            customTransition="transform 500ms ease-in-out"
            transitionDuration={500}
            containerClass="carousel-container"
            removeArrowOnDeviceType={["tablet", "mobile"]}
            itemClass="carousel-item"
            rtl={false}
            className="book-carousel"
          >
            {firstRowImages.map((image, index) => (
              <div key={`row1-${index}`} className="book-item">
                <img 
                  src={image} 
                  alt={`Книга ${index + 1}`} 
                  className="rounded-lg h-[350px] mx-auto"
                />
              </div>
            ))}
          </Carousel>
        </div>

        {/* Вторая строка - движется вправо */}
        <div className="mb-12">
          <Carousel
            responsive={responsive}
            infinite={true}
            autoPlay={true}
            autoPlaySpeed={4000}
            keyBoardControl={true}
            customTransition="transform 500ms ease-in-out"
            transitionDuration={500}
            containerClass="carousel-container"
            removeArrowOnDeviceType={["tablet", "mobile"]}
            itemClass="carousel-item"
            rtl={true}
            className="book-carousel"
          >
            {secondRowImages.map((image, index) => (
              <div key={`row2-${index}`} className="book-item">
                <img 
                  src={image} 
                  alt={`Книга ${index + 8}`} 
                  className="rounded-lg h-[350px] mx-auto"
                />
              </div>
            ))}
          </Carousel>
        </div>

        {/* Третья строка - движется влево, но медленнее */}
        <div>
          <Carousel
            responsive={responsive}
            infinite={true}
            autoPlay={true}
            autoPlaySpeed={5000}
            keyBoardControl={true}
            customTransition="transform 500ms ease-in-out"
            transitionDuration={500}
            containerClass="carousel-container"
            removeArrowOnDeviceType={["tablet", "mobile"]}
            itemClass="carousel-item"
            rtl={false}
            className="book-carousel"
          >
            {thirdRowImages.map((image, index) => (
              <div key={`row3-${index}`} className="book-item">
                <img 
                  src={image} 
                  alt={`Книга ${index + 15}`} 
                  className="rounded-lg h-[350px] mx-auto"
                />
              </div>
            ))}
          </Carousel>
        </div>
      </div>
    </section>
  );
};

export default Gallery;
