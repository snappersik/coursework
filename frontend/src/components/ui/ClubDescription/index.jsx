import { useState, useEffect, useRef } from 'react';
import './ClubDescription.css';

const ClubDescription = () => {
  const words = ["о литературе", "о кино", "об искусстве"];
  const [displayText, setDisplayText] = useState("");
  const [wordIndex, setWordIndex] = useState(0);
  const [isDeleting, setIsDeleting] = useState(false);
  const [typingSpeed, setTypingSpeed] = useState(100);
  const currentWordRef = useRef(words[0]);
  
  useEffect(() => {
    const typeEffect = () => {
      currentWordRef.current = words[wordIndex];
      
      if (isDeleting) {
        setDisplayText(prev => prev.substring(0, prev.length - 1));
        setTypingSpeed(50);
      } else {
        setDisplayText(prev => currentWordRef.current.substring(0, prev.length + 1));
        setTypingSpeed(100);
      }
      
      if (!isDeleting && displayText === currentWordRef.current) {
        setTypingSpeed(1000);
        setIsDeleting(true);
      } else if (isDeleting && displayText === '') {
        setIsDeleting(false);
        setWordIndex((prev) => (prev + 1) % words.length);
        setTypingSpeed(200);
      }
    };
    
    const timer = setTimeout(typeEffect, typingSpeed);
    return () => clearTimeout(timer);
  }, [displayText, isDeleting, wordIndex, words, typingSpeed]);
  
  return (
    <section 
      className="text-white py-16 relative"    >
      <div className="container mx-auto px-4">
        <div>
          <h2 className="leading-relaxed">
            <span className="title-font font-bold whitespace-nowrap text-base sm:text-lg md:text-4xl lg:text-5xl xl:text-6xl 2xl:text-7xl text-white inline">
              Клуб — это уникальное сообщество, <br />
              в котором каждый участник расширяет <br />
              свои представления&nbsp;
            </span>
            <span className="title-font font-bold whitespace-nowrap text-base sm:text-lg md:text-4xl lg:text-5xl xl:text-6xl 2xl:text-7xl text-yellow-400 inline">
              {displayText}
            </span>
            <span className="cursor text-yellow-400 text-xs sm:text-base md:text-2xl lg:text-3xl xl:text-4xl 2xl:text-5xl">|</span>
            </h2>
          
          <p className="mt-8 text-xl">
            <i>Главное в клубе — это люди и наше взаимодействие.</i> В нашем сообществе участники развивают 
            навыки чтения, критического мышления, анализа искусства и кино.
          </p>
          
        </div>
      </div>
    </section>
  );
};

export default ClubDescription;
