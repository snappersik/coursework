import React, { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import Slider from '../../components/ui/Slider';
import ClubHarbor from '../../components/ui/ClubHarbor';
import ClubDescription from '../../components/ui/ClubDescription';
import ClubBenefits from '../../components/ui/ClubBenefits';
import Gallery from '../../components/ui/Gallery';
import SectionsWrapper from '../../components/layout/SectionsWrapper';

const HomePage = () => {
  const location = useLocation();
  
  useEffect(() => {
    if (location.hash) {
      const id = location.hash.slice(1);
      const element = document.getElementById(id);
      if (element) {
        element.scrollIntoView({ behavior: 'smooth' });
      }
    }
  }, [location.hash]);

  return (
    <>
      <Slider />
      <SectionsWrapper>
        <ClubHarbor />
        <ClubDescription />
        <ClubBenefits />
        <Gallery />
      </SectionsWrapper>
    </>
  );
};

export default HomePage;
