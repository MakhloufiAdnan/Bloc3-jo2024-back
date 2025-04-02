// Fonction pour sauvegarder le token dans le localStorage
export function saveToken(token, rememberMe) {
  if (rememberMe) {
    localStorage.setItem("jwt", token);
  } else {
    sessionStorage.setItem("jwt", token);
  }
}

// Fonction pour vérifier si l'utilisateur est connecté
export function isAuthenticated() {
  return localStorage.getItem("jwt") || sessionStorage.getItem("jwt");
}

// Fonction pour se déconnecter
export function logout() {
  localStorage.removeItem("jwt");
  sessionStorage.removeItem("jwt");
  window.location.href = "/index.html"; 
}
