document.addEventListener("DOMContentLoaded", function () {
  const countries = [
    "Allemagne", "Belgique", "Canada", "Etats-Unis", "Ghana",
    "Hongrie", "Italie", "Kenya"
  ];
  const tests = [
    "Athlétisme", "Basketball", "Boxe", "Breaking Dance", "Cyclisme",
    "Judo"
  ];

  document.querySelectorAll(".btn-filter").forEach(dropdownContainer => {
    const dropdownButton = document.createElement("button");
    dropdownButton.className = "dropdown-button";
    const dropdownIcon = document.createElement("img");
    dropdownIcon.className = "dropdown-icon";
    dropdownIcon.src = "icons/btn-up-white.svg";
    dropdownIcon.alt = "Chevron";

    if (dropdownContainer.classList.contains("btn-filter-countries")) {
      dropdownButton.innerHTML = `Toutes les équipes `;
    } else if (dropdownContainer.classList.contains("btn-filter-test")) {
      dropdownButton.innerHTML = `Toutes les épreuves `;
    }

    dropdownButton.appendChild(dropdownIcon); 
    dropdownContainer.appendChild(dropdownButton);

    const dropdownMenu = document.createElement("ul");
    dropdownMenu.className = "dropdown-menu hidden";

    if (dropdownContainer.classList.contains("btn-filter-test")) {
      tests.forEach(test => {
        const li = document.createElement("li");
        li.className = "dropdown-item";
        li.textContent = test;
        dropdownMenu.appendChild(li);
      });
    } else if (dropdownContainer.classList.contains("btn-filter-countries")) {
      countries.forEach(country => {
        const li = document.createElement("li");
        li.className = "dropdown-item";
        li.textContent = country;
        dropdownMenu.appendChild(li);
      });
    }

    dropdownContainer.appendChild(dropdownMenu);

    // Ajoute l'écouteur de clic sur le bouton
    dropdownButton.addEventListener("click", function (event) {
      event.stopPropagation();// Empêcher la propagation du clic à l'événement document
      dropdownMenu.classList.toggle("hidden");
      dropdownIcon.style.transform = dropdownMenu.classList.contains("hidden") ? "rotate(0deg)" : "rotate(180deg)";
    });

    // Ferme le menu quand on clique à l'extérieur
    document.addEventListener("click", function (event) {
      if (!dropdownContainer.contains(event.target)) {
        dropdownMenu.classList.add("hidden");
        dropdownIcon.style.transform = "rotate(0deg)";
      }
    });

    dropdownMenu.addEventListener("click", function (event) {
      if (event.target.classList.contains("dropdown-item")) {
        dropdownButton.firstChild.textContent = event.target.textContent;
        dropdownMenu.classList.add("hidden");
        dropdownIcon.style.transform = "rotate(0deg)";
      }
    });
  });
});

console.log("script componentBtnDropdown chargé");