document.addEventListener("DOMContentLoaded", function () {
  const ContainerOffers = document.querySelector(".container-offers");
  const ContainerPurchaseSummary = document.querySelector(".container-purchase-summary");


  // Données fictives pour les offres test ****************************
  const typeOffer = ["Offre Solo", "Offre Duo", "Offre Famille"];
  const priceOffer = ["35€", "60€", "100€"];
  const nameTest = ["Athlétisme", "Basketball", "Cyclisme", "Football", "Golf", "Handball", "Judo", "Natation", "Tennis", "Volleyball"];
  const country = ["Allemagne", "Belgique", "Canada", "Etats-Unis", "Ghana", "Hongrie", "Italie", "Kenya"];
  const placeOfTest = ["Paris", "Lyon", "Marseille", "Bordeaux", "Toulouse", "Nantes", "Lille", "Strasbourg", "Nice", "Rennes"];

  let offerCount = 0; // Compteur pour générer des ID uniques

  // ******************************************************************

  function createOfferCard() {
    let offerIndex = offerCount++; // ID unique pour chaque carte

    let randomOffer = Math.floor(Math.random() * typeOffer.length);
    let randomSport = Math.floor(Math.random() * nameTest.length);
    let randomCountry = Math.floor(Math.random() * country.length);
    let randomPlace = Math.floor(Math.random() * placeOfTest.length);

    const offerCard = document.createElement("div");
    offerCard.classList.add("offer-cards");
    offerCard.innerHTML = `
      <div class="container-description box-offers">
        <div class="first-container box-offer">
          <div class="container-type">
            <p>Type d'offre :</p>
            <p class="offre" id="var-offer-${offerIndex}">${typeOffer[randomOffer]}</p>
          </div>
          <div class="container-type">
            <p>Prix :</p>
            <p class="prix" id="var-price-${offerIndex}">${priceOffer[randomOffer]}</p>
          </div>
        </div>
        <div class="second-container box-offer">
          <div class="container-type">
            <p id="name-test-${offerIndex}">${nameTest[randomSport]}</p>
          </div>
          <div class="container-type">
            <div class="country-name">
              <p>Pays :</p>
              <p id="country-${offerIndex}">${country[randomCountry]}</p>
            </div>
            <div class="test-medal">
              <div id="medal">
                <img src="icons/medal-gold.svg" alt="icon-medal">
              </div>
            </div>
          </div>
        </div>
        <div class="third-container box-offer">
          <div class="container-type">
            <p>Date de l'épreuve :</p>
            <p id="date-test-${offerIndex}">12/06/2024</p>
          </div>
          <div class="container-type">
            <p>Lieu de l’épreuve :</p>
            <p id="place-test-${offerIndex}">${placeOfTest[randomPlace]}</p>
          </div>
        </div> 
      </div>
      <div class="container-choice-offers box-offers">
        <div class="offers-icons">
          <p>Choix de l'offre</p>
          <div class="offer-icons">
            <img class="offer-icon" src="icons/icon-1ticket.svg" alt="Icone de l'offre solo">
            <img class="offer-icon" src="icons/icon-2tickets.svg" alt="Icone de l'offre duo">
            <img class="offer-icon" src="icons/icon-4tickets.svg" alt="Icone de l'offre famille">
          </div>
        </div>
        <div class="box-quantity">
          <button class="less-btn btn-quantity" data-id="${offerIndex}">-</button>
          <input class="quantity" type="text" id="quantity-${offerIndex}" value="1">
          <button class="more-btn btn-quantity" data-id="${offerIndex}">+</button>
        </div>
        <div class="box-add">
          <button class="btn-shop add-btn" data-id="${offerIndex}">Ajouter au panier</button>
        </div>
      </div>
    `;

    ContainerOffers.appendChild(offerCard);

    // Gestion de la quantité dynamique
    offerCard.querySelector(".less-btn").addEventListener("click", function () {
      let quantityInput = document.getElementById(`quantity-${offerIndex}`);
      let quantity = parseInt(quantityInput.value);
      if (quantity > 1) {
        quantityInput.value = quantity - 1;
      }
    });

    offerCard.querySelector(".more-btn").addEventListener("click", function () {
      let quantityInput = document.getElementById(`quantity-${offerIndex}`);
      quantityInput.value = parseInt(quantityInput.value) + 1;
    });

    // Gestion de l'ajout au panier
    offerCard.querySelector(".add-btn").addEventListener("click", function () {
      createPurchaseSummary(offerIndex);
    });
  }

  function createPurchaseSummary(offerIndex) {
    const purchaseSummary = document.createElement("div");
    purchaseSummary.classList.add("purchase-summary");
    purchaseSummary.innerHTML = `
      <div class="purchase-summary-title">
        <span class="recap">${document.getElementById(`name-test-${offerIndex}`).textContent}</span>
        <span class="recap">${document.getElementById(`date-test-${offerIndex}`).textContent}</span>
        <span class="recap place-test">${document.getElementById(`place-test-${offerIndex}`).textContent}</span>
      </div>
      <div class="purchase-summary-content">
        <p class="offre">${document.getElementById(`var-offer-${offerIndex}`).textContent}</p>
        <p class="prix">${document.getElementById(`var-price-${offerIndex}`).textContent}</p>
        <p>Quantité : ${document.getElementById(`quantity-${offerIndex}`).value}</p>
        <div class="total-price">
          <p>Total : </p>
          <p>${parseInt(document.getElementById(`quantity-${offerIndex}`).value) * parseInt(document.getElementById(`var-price-${offerIndex}`).textContent)}</p>
        </div>
      </div>
    `;

    ContainerPurchaseSummary.appendChild(purchaseSummary);
  }

  // Générer une carte au chargement
  createOfferCard();
  createPurchaseSummary(0);
});