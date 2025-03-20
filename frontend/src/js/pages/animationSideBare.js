// Sélectionner les éléments de la sidebar
const sidebar = document.querySelector('.nav-sidebars');
const sidebarOn = document.querySelector('.nav-sideBar-on');
const sidebarOff = document.querySelector('.nav-sideBar-off');

// Fonction pour ouvrir la sidebar
function openSidebar() {
  sidebarOff.style.display = 'none';
  sidebarOn.style.display = 'flex';
  sidebar.style.display = 'block';
}

// Fonction pour fermer la sidebar
function closeSidebar() {
  sidebarOff.style.display = 'flex';
  sidebarOn.style.display = 'none';
}

// Ajouter les écouteurs d'événements pour ouvrir et fermer au passage de la souris

sidebarOff.addEventListener('mouseenter', openSidebar); 
sidebarOn.addEventListener('mouseleave', closeSidebar); 

console.log('script animation sidebare chargé');