import { saveToken, isAuthenticated, logout } from '/js/api/authUtils.js';

document.addEventListener('DOMContentLoaded', function () {
  const loginForm = document.getElementById("auth-form");

  if (!loginForm) {
    console.warn("Le formulaire de connexion n'est pas présent sur cette page.");
    return;
  }

  loginForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;
    const rememberMe = document.getElementById("rememberMe")?.checked || false;

    if (!validateEmail(email)) {
      showPopup("Veuillez entrer un email valide.", "error");
      return;
    }

    try {
      const response = await fetch('/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        throw new Error("Échec de la connexion. Vérifiez vos identifiants.");
      }

      const result = await response.json();
      saveToken(result.token, rememberMe);
      showPopup("Connexion réussie !", "success");

      setTimeout(() => {
        window.location.href = "/index.html";
      }, 1000);
    } catch (error) {
      console.error(error);
      showPopup(error.message, "error");
    }
  });

  if (isAuthenticated()) {
    window.location.href = "/index.html";
  }

  function validateEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }

  function showPopup(message, type) {
    const popup = document.createElement("div");
    popup.classList.add(type === "success" ? "popup-success" : "popup-error");
    popup.innerText = message;
    document.body.appendChild(popup);
    setTimeout(() => popup.remove(), 5000);
  }
});
