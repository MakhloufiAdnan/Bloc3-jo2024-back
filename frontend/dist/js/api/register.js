import { saveToken, isAuthenticated } from '/js/api/authUtils.js';

document.addEventListener('DOMContentLoaded', function () {
  const signupForm = document.getElementById("signup-form");

  if (!signupForm) {
    console.warn("Le formulaire d'inscription n'est pas présent sur cette page.");
    return;
  }

  signupForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    const data = {
      username: document.getElementById("username").value.trim(),
      firstname: document.getElementById("firstname").value.trim(),
      date: document.getElementById("date").value,
      email: document.getElementById("email").value.trim(),
      phonenumber: document.getElementById("phonenumber").value.trim(),
      address: document.getElementById("address").value.trim(),
      postalcode: document.getElementById("postalcode").value.trim(),
      city: document.getElementById("city").value.trim(),
      password: document.getElementById("password").value,
    };

    if (data.password !== document.getElementById("confirmPassword").value) {
      showPopup("Les mots de passe ne correspondent pas.", "error");
      return;
    }
    // Récupère le token JWT (si disponible)
      const tokenJWT = isAuthenticated();

      if (!tokenJWT) {
          showPopup("Token JWT manquant ou non valide.", "error");
          return;
      }

    try {
      const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + tokenJWT
        },
        body: JSON.stringify(data),
      });

      if (!response.ok) {
        const errorText = await response.text();  // Récupère le message d'erreur du backend
        throw new Error(errorText || "Erreur lors de l'inscription.");
      }

      showPopup("Inscription réussie ! Vous pouvez maintenant vous connecter.", "success");
      setTimeout(() => {
        window.location.href = "/index.html";
      }, 1000);
    } catch (error) {
        console.error("Erreur lors de l'inscription :", error);
        showPopup(error.message || "Erreur lors de l'inscription.", "error");
    }
  });

  function showPopup(message, type) {
    const popup = document.createElement("div");
    popup.classList.add(type === "success" ? "popup-success" : "popup-error");
    popup.innerText = message;
    document.body.appendChild(popup);
    setTimeout(() => popup.remove(), 5000);
  }
});