import { useState, useEffect } from 'react';

export const useImagesLoaded = (images = []) => {
  const [loaded, setLoaded] = useState(false);
  const [progress, setProgress] = useState(0);
  
  useEffect(() => {
    if (!images.length) {
      setLoaded(true);
      setProgress(100);
      return;
    }
    
    let loadedCount = 0;
    const totalCount = images.length;
    
    const onLoad = () => {
      loadedCount++;
      setProgress(Math.floor((loadedCount / totalCount) * 100));
      
      if (loadedCount === totalCount) {
        setLoaded(true);
      }
    };
    
    const loadImage = (src) => {
      return new Promise((resolve, reject) => {
        const img = new Image();
        img.src = src;
        img.onload = () => {
          onLoad();
          resolve(img);
        };
        img.onerror = reject;
      });
    };
    
    // Загружаем все изображения
    Promise.all(images.map(src => loadImage(src)))
      .catch(err => console.error('Failed to load images', err));
    
  }, [images]);
  
  return { loaded, progress };
};