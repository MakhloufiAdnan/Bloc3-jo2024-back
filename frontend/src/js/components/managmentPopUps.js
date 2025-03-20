document.addEventListener("click", (e) => {
  if (e.target.classList.contains("text-icons")) {
    e.preventDefault();

    let popups = {
      "openPopupConnection": "Connexion",
      "openPopupSignup": "Formulaire d’inscription",
      "openPopupAccessibility": "Accessibilité",
      "openPopupFavorites": "Mes favoris"
    };

    if (popups[e.target.id]) {
      openPopup(popups[e.target.id]);
    }
  }
});

// Gestion des icônes mobiles
const iconsmobile = [
  { id: "icon-user-mobile", popup: "Connexion" },
  { id: "icon-accessibility-mobile", popup: "Accessibilité" },
  { id: "icon-love-mobile", popup: "Mes favoris" }
];

iconsmobile.forEach(({ id, popup }) => {
  const icon = document.getElementById(id);
  if (icon) {
    icon.addEventListener("click", () => openPopup(popup));
  }
});

// Gestion des liens sidebar
const linksSideBar = [
  { id: "login-sidebar", popup: "Connexion" },
  { id: "incription-sidebar", popup: "Formulaire d’inscription" },
];

linksSideBar.forEach(({ id, popup }) => {
  const linkSideBar = document.getElementById(id);
  if (linkSideBar) {
    linkSideBar.addEventListener("click", () => openPopup(popup));
  }
});

function openPopup(title) {
  const popup = document.createElement("div");
  popup.classList.add("login-container");

  let content = "";

  if (title === "Connexion") {
    content = `
      <form class="form" id="auth-link" method="post">
        <label for="email">Email</label>
        <input type="text" id="email" name="email" required>
        <label for="password">Mot de passe</label>
        <input type="password" id="password" name="password" required>
        <p class="mdp-forget"><a href="#">Mot de passe oublié ?</a></p> 
        <button type="submit" class="submit-btn">Se connecter</button>
      </form>
      <div class="checkbox">
        <div class="remember-me">
          <input class="input-checkbox" type="checkbox" id="scales" name="scales"/>
          <label for="scales">Se souvenir de moi ?</label>
        </div>
        <div class="subscribe">
          <input class="input-checkbox" type="checkbox" id="subscribeNews" name="subscribe" value="newsletter"/>
          <label for="subscribeNews">Souhaitez-vous vous abonner à la newsletter ?</label>
        </div>
      </div>
      <div class="new-account">
        <p class="link-new-account"><a href="#signup-form">Créer un compte</a></p>
      </div>
      <div class="social-login">
        <div class="google-btn social-btn" onclick="handleGoogleLogin()">
          <img src="/icons/btn-connection-google.png" alt="Connexion Google">
        </div>
        <div class="facebook-btn social-btn" onclick="handleFacebookLogin()">
          <img src="/icons/btn-connection-facebook.png" alt="Connexion Facebook">
        </div>
      </div>`;
  } else if (title === "Formulaire d’inscription") {
    content = `
      <div class="inscription-container">
        <form class="form" id="signup-form" method="post">
          <div class="form-div form-identity">
            <input type="text" id="username" name="username" placeholder="Nom" required>
            <input type="text" id="firstname" name="firstname" placeholder="Prénom" required>
          </div>
          <input type="date" id="date" name="date" placeholder="date de naissance" required>
          <div class="form-div form-contact">
            <input type="text" id="email" name="email" placeholder="Email" required>
            <input type="number" id="phonenumber" name="phonenumber" placeholder="N° de téléphone" required>
          </div>
          <div class="form-div form-adress">
            <input type="text" id="adress" name="adress" placeholder="Adresse" required>
          </div>
          <div class="form-div form-adress2">
            <input type="number" id="postalcode" name="postalcode" placeholder="Code postale" required>
            <input type="text" id="city" name="city" placeholder="Ville" required>
          </div>
          <div class="form-password">
            <input type="password" id="password" name="password" placeholder="Mot de passe" required>
            <input type="password" id="confirmPassword" name="ConfirmPassword" placeholder="Confirmation du mot de passe" required>
          </div>
          <div class="form-checkbox">
            <input class="input-form-checkbox" type="checkbox" id="terms" name="terms" required />
            <label for="terms">J’accepte la Politique de confidentialité et confirme avoir plus de 16 ans.</label>
          </div>
          <button type="submit" class="submit-btn">S'inscrire</button>
        </form>
      </div>
      `;
  } else if (title === "Accessibilité") {
    content = `
      <div class="accessibility-container"   id="accessibility">
        <h3>Options d’accessibilité</h3>
        <ul>
          <li><button onclick="increaseFontSize()">🔍 Augmenter la taille du texte</button></li>
          <li><button onclick="decreaseFontSize()">🔎 Diminuer la taille du texte</button></li>
          <li><button onclick="toggleContrast()">🌗 Activer/Désactiver le mode contraste élevé</button></li>
          <li><button onclick="toggleDyslexiaMode()">🧠 Mode Dyslexie</button></li>
          <li><button onclick="toggleTextToSpeech()">🔊 Activer/Désactiver la lecture vocale</button></li>
          <li><button onclick="disableAnimations()">🌀 Désactiver les animations</button></li>
        </ul>
      </div>
    `;
  }else if (title === "Mes favoris") {
    content = `<p>Un compte est nécessaire pour cette fonction.</p>`;
  } 

  popup.innerHTML = `
    <div class="form-group">
      <h2>${title}</h2>
      <button id="closePopup" class="close-btn">&times;</button>
      ${content}
    </div>
    `;

  document.body.appendChild(popup);

  // Fermer la popup au clic sur le bouton
  const closeBtn = document.getElementById("closePopup");
  if (closeBtn) {
    closeBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      popup.remove();
    });
  }
}

console.log("managmentPopUps chargé");