import React from 'react';

const ClubHarbor = () => {
  return (
    <section className="bg-[#424242] text-white py-12">
      <div className="container mx-auto px-4">
        <div className="text-center">
          <h2 className="title-font text-white font-bold text-6xl mb-4">
            Книжная гавань
          </h2>
          <p className="mb-8 leading-relaxed text-lg md:max-w-sm mx-auto">
            Книжный клуб для тех, кто хочет найти единомышленников и привнести в
            свою жизнь энергию творчества.
          </p>
          
          <button className="mt-4 bg-yellow-400 hover:bg-yellow-500 text-white font-medium py-3 px-8 rounded-full">
            ВСТУПИТЬ В КЛУБ
          </button>
        </div>
      </div>
    </section>
  );
};

export default ClubHarbor;
