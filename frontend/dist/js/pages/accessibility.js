// ⚡️ Fonctionnalités Accessibilité ⚡️

// 1️⃣ Augmenter la taille du texte
function increaseFontSize() {
  document.body.style.fontSize = "larger";
}

// 2️⃣ Diminuer la taille du texte
function decreaseFontSize() {
  document.body.style.fontSize = "smaller";
}

// 3️⃣ Mode contraste élevé (fond noir, texte blanc)
function toggleContrast() {
  document.body.classList.toggle("high-contrast");
}

// 4️⃣ Mode Dyslexie (utilisation d'une police adaptée)
function toggleDyslexiaMode() {
  document.body.classList.toggle("dyslexia-mode");
}

// 5️⃣ Lecture vocale (Text-to-Speech)
let speechEnabled = false;
function toggleTextToSpeech() {
  speechEnabled = !speechEnabled;
  if (speechEnabled) {
    speakText(document.body.innerText);
  } else {
    window.speechSynthesis.cancel();
  }
}

function speakText(text) {
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = "fr-FR";
  utterance.rate = 1;
  window.speechSynthesis.speak(utterance);
}

// 6️⃣ Désactiver les animations pour les personnes avec troubles vestibulaires
function disableAnimations() {
  document.body.classList.toggle("no-animation");
}

console.log("Script accessibilité chargé !");