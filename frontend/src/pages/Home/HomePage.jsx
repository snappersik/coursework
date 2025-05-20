import React from 'react';
import Slider from '../../components/ui/Slider';
import ClubHarbor from '../../components/ui/ClubHarbor';
import ClubDescription from '../../components/ui/ClubDescription';
import ClubBenefits from '../../components/ui/ClubBenefits';
import Gallery from '../../components/ui/Gallery';
import SectionsWrapper from '../../components/layout/SectionsWrapper';

const HomePage = () => {
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
