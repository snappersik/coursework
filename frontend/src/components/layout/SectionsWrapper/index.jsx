import React from 'react';

const SectionsWrapper = ({ children }) => {
  return (
    <div className="relative">
      <div 
        className="absolute inset-0 z-0" 
        style={{
          backgroundImage: `url('https://static.tildacdn.com/tild3065-6533-4937-b161-343562396665/svg.svg')`,
          backgroundSize: 'contain',
          backgroundPosition: 'center center',
          backgroundRepeat: 'no-repeat',
          opacity: 0.8
        }}
      ></div>
      <div className="relative z-10">
        {children}
      </div>
    </div>
  );
};

export default SectionsWrapper;
