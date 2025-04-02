// Clé de stockage dans le localStorage
const FAVORITES_KEY = "userFavorites";

/**
 * Récupère la liste des billets favoris de l'utilisateur.
 * @returns {Array} Un tableau d'objets représentant les billets favoris.
 */
export function getFavorites() {
  const favs = localStorage.getItem(FAVORITES_KEY);
  if (!favs) return [];
  try {
    return JSON.parse(favs);
  } catch (error) {
    console.error("Erreur lors du parsing des favoris :", error);
    return [];
  }
}

/**
 * Ajoute un billet aux favoris.
 * @param {Object} favorite - L'objet représentant le billet à ajouter.
 * Par exemple : { id: "billet123", title: "Concert", link: "/billets/123" }
 */
export function addFavorite(favorite) {
  const favorites = getFavorites();
  // Vérifier que le billet n'existe pas déjà dans les favoris (en fonction d'un identifiant unique)
  const exists = favorites.some(fav => fav.id === favorite.id);
  if (!exists) {
    favorites.push(favorite);
    localStorage.setItem(FAVORITES_KEY, JSON.stringify(favorites));
  }
}

/**
 * Supprime un billet des favoris.
 * @param {string} favoriteId - L'identifiant unique du billet à supprimer.
 */
export function removeFavorite(favoriteId) {
  let favorites = getFavorites();
  favorites = favorites.filter(fav => fav.id !== favoriteId);
  localStorage.setItem(FAVORITES_KEY, JSON.stringify(favorites));
}

/**
 * Vérifie si un billet est déjà ajouté aux favoris.
 * @param {string} favoriteId - L'identifiant unique du billet.
 * @returns {boolean} True si le billet est dans les favoris, sinon false.
 */
export function isFavorite(favoriteId) {
  const favorites = getFavorites();
  return favorites.some(fav => fav.id === favoriteId);
}