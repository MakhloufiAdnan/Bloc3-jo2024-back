import { isAuthenticated, logout } from '/js/api/authUtils.js'; 
import { getFavorites } from '/js/pages/favoriteService.js';

document.addEventListener("DOMContentLoaded", () => {
  const popupMapping = {
    "openPopupConnection": "popup-Connection",
    "openPopupSignup": "popup-inscription",
    "openPopupAccessibility": "popup-Accessibility",
    "openPopupFavorites": "popup-My-favorit"
  };

  // Variable pour vérifier si une popup est ouverte
  let isPopupOpen = false;

  // Gestion des clics sur les boutons qui ouvrent une popup
  document.addEventListener("click", (e) => {
    if (e.target.classList.contains("text-icons") && !isPopupOpen) {
      e.preventDefault();
      const popupId = popupMapping[e.target.id];
      if (popupId) {
        openPopup(popupId);
        console.log(`Ouverture de la popup : ${popupId}`); // debug
      }
    }

    // Vérifier si c'est un bouton "Fermer" ou un clic sur la popup pour la fermer
    if (e.target.classList.contains("close-btn") || e.target.classList.contains("popup")) {
      closePopup(e.target.closest(".popup"));
    }
  });

  // Fonction pour ouvrir une popup
  function openPopup(popupId) {
    if (isPopupOpen) return; // Empêche d'ouvrir une nouvelle popup si une est déjà ouverte

    const popup = document.getElementById(popupId);
    if (popup) {
      popup.classList.add("show");
      isPopupOpen = true;
      updatePopupConnection(popupId); // Mise à jour en fonction de l'état de l'utilisateur
    }
  }

  // Fonction pour fermer une popup
  function closePopup(popup) {
    if (popup) {
      popup.classList.remove("show");
      isPopupOpen = false; // Permet d'ouvrir d'autres popups après la fermeture
    }
  }

  // Gestion des icônes mobiles
  const iconsMobile = [
    { id: "icon-user-mobile", popup: "popup-Connection" },
    { id: "icon-accessibility-mobile", popup: "popup-Accessibility" },
    { id: "icon-love-mobile", popup: "popup-My-favorit" }
  ];
  iconsMobile.forEach(({ id, popup }) => {
    document.getElementById(id)?.addEventListener("click", () => openPopup(popup));
  });

  // Gestion des liens de la sidebar
  const linksSideBar = [
    { id: "login-sidebar", popup: "popup-Connection" },
    { id: "inscription-sidebar", popup: "popup-inscription" }
  ];
  linksSideBar.forEach(({ id, popup }) => {
    document.getElementById(id)?.addEventListener("click", () => openPopup(popup));
  });

  // Fonction pour mettre à jour la popup de connexion selon l'état de l'utilisateur
  function updatePopupConnection(popupId) {
    if (popupId === 'popup-Connection') {
      const popupConnection = document.getElementById("popup-Connection");
      const connectedSection = popupConnection.querySelector(".connected");
      const disconnectedSection = popupConnection.querySelector(".disconnected");
      const logoutBtn = popupConnection.querySelector("#logout-btn");

      if (isAuthenticated()) {
        // Afficher la section connectée
        connectedSection.style.display = "block";
        disconnectedSection.style.display = "none";

        // Ajouter un événement pour la déconnexion
        logoutBtn.addEventListener("click", () => {
          logout(); // Déconnecte l'utilisateur
          updatePopupConnection('popup-Connection'); // Met à jour l'affichage de la popup
        });
      } else {
        // Afficher la section déconnectée
        connectedSection.style.display = "none";
        disconnectedSection.style.display = "block";
      }
    }

    if (popupId === 'popup-My-favorit') {
      const favoritesContainer = document.getElementById("favorites-list");

      if (favoritesContainer) {
        if (!isAuthenticated()) {
          // Si l'utilisateur n'est pas connecté, afficher le message
          favoritesContainer.innerHTML = "<p>Un compte est nécessaire pour accéder à vos favoris.</p>";
        } else {
          // Si l'utilisateur est connecté, afficher la liste des favoris
          const favoris = getFavorites();
          favoritesContainer.innerHTML = favoris.length > 0 ? `
            <ul>
              ${favoris.map(fav => `<li><a href="${fav.link}">${fav.title}</a></li>`).join("")}
            </ul>
          ` : "<p>Aucun favori enregistré.</p>";
        }
      }
    }
  }
});

console.log("Gestion des popups chargée.");