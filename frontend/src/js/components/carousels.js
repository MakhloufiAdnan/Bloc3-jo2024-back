// initialiser un carousel
function initCarousel(carouselSelector, groupSelector, navLeftSelector, navRightSelector) {
  let compteur = 0;
  const carousel = document.querySelector(carouselSelector);
  const elements = document.querySelector(groupSelector);
  const speed = carousel.dataset.speed;
  const transition = carousel.dataset.transition;
  let slides = Array.from(elements.children);
  let timer;

  // Clonage de la première image et ajout à la fin
  let firstSlide = elements.firstElementChild.cloneNode(true);
  elements.appendChild(firstSlide);
  slides = Array.from(elements.children); // Mise à jour des slides après clonage

  function updateSlideWidth() {
      return carousel.getBoundingClientRect().width;
  }

  // Récupération des boutons de navigation
  const next = document.querySelector(navRightSelector);
  const prev = document.querySelector(navLeftSelector);

  function slideNext() {
    compteur++;
    let slideWidth = updateSlideWidth();
    elements.style.transition = `${transition}ms linear`;
    elements.style.transform = `translateX(${-slideWidth * compteur}px)`;

    setTimeout(() => {
      if (compteur >= slides.length - 1) {
        compteur = 0;
        elements.style.transition = 'unset';
        elements.style.transform = 'translateX(0)';
      }
    }, transition);
  }

  function slidePrev() {
    compteur--;
    let slideWidth = updateSlideWidth();
    elements.style.transition = `${transition}ms linear`;
    if (compteur < 0) {
      compteur = slides.length - 1;
      elements.style.transition = 'unset';
      elements.style.transform = `translateX(${-slideWidth * compteur}px)`;
      setTimeout(slidePrev, 1);
    } else {
      elements.style.transform = `translateX(${-slideWidth * compteur}px)`;
    }
  }

  function stopTimer() {
    clearInterval(timer);
  }

  function startTimer() {
    timer = setInterval(slideNext, speed);
  }

  next.addEventListener('click', slideNext);
  prev.addEventListener('click', slidePrev);
  carousel.addEventListener('mouseover', stopTimer);
  carousel.addEventListener('mouseout', startTimer);
  
  // Démarrage du carrousel
  startTimer();
}

// Initialisation des carousels
window.onload = () => {
  initCarousel('.carousel1', '.carousel-group1', '.nav-left', '.nav-right');
  initCarousel('.carousel2', '.carousel-group2', '.nav-left2', '.nav-right2');
};

console.log('script carousels chargé');