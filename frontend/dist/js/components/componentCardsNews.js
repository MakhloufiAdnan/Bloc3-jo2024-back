const imageCardsNews = ["image-seine-paris.jpg", "village-olympique.jpg", "nouveaux-sports.jpg", "police-jo.jpg"];
const textDataCardsNews = [
  { title: "la Seine sera le théâtre de la cérémonie d\’ouverture des Jeux devant 600 000 personnes.", 
    paragraph: "Les organisateurs ont dévoilé, lundi 13 décembre, leur concept de « Seine olympique » : les 10 500 athlètes défileront, le 26 juillet 2024, sur des bateaux, devant les spectateurs massés sur les quais du fleuve parisien. La cérémonie d\’ouverture des Jeux olympiques de Paris 2024 s\’annonce spectaculaire, grandiose, unique. C\’est en tout cas ce que promettent les images dévoilées lundi 13 décembre par le Comité d\’organisation (COJO) de Paris 2024. Le 26 juillet 2024, pour la première fois de l\’histoire olympique, une cérémonie d\’ouverture se déroulera hors d\’un stade. Ce n\’est pas une surprise : Tony Estanguet, le patron de Paris 2024, l\’avait dit au Monde début 2021, révélant alors que la Seine serait le fil conducteur de l\’événement." },

  { title: "Paris 2024 : le village des athlètes, grand test du bâtiment réversible.", 
    paragraph: "A la lisière de Saint-Denis, de L'Ile-Saint-Denis et de Saint-Ouen, une forêt de grues a investi le site qui accueillera, à l'été 2024, les 14.250 athlètes attendus pour les Jeux Olympiques. Si la plupart des bâtiments sortent à peine de terre, d'autres - après un an de travaux -, ont déjà plusieurs étages. Le site grouille d'ouvriers (ils sont environ 1.400) allant et venant sur les trois parcelles du futur village. L'intérêt de ce projet est d'avoir prévu des bâtiments réversibles, qui pourront changer d'usage et bénéficier d'une seconde vie une fois les médailles distribuées. En France, cela n'avait jamais été réalisé à une telle échelle." },

  { title: "Quatre nouvelles disciplines aux Jeux Olympiques 2024.", 
    paragraph: "Quatre disciplines ont été proposées par le Comité d\’organisation des Jeux Olympiques de Paris (COJO) pour être ajoutées au programme des JO en 2024. Il s\’agit du breakdance, de l\’escalade, du skateboard et du surf. Ces trois dernières disciplines figurent déjà au programme des Jeux de 2020 à Tokyo. Seul le breakdance, une danse acrobatique issue de la culture hip-hop, pourrait faire son entrée aux JO pour la première fois. Le breakdance est apparue aux Jeux Olympiques de la Jeunesse l\’année dernière à Buenos Aires, sous forme de duels départagés par des juges. Cette discipline est rattachée à la Fédération mondiale de la danse sportive (WDSF)." },

  { title: "JO : un dispositif de sécurité hors normes", 
    paragraph: "Entourés d\’un dispositif de sécurité aux dimensions inédites, qui a fait l\’objet de plusieurs années de préparation. Les autorités assurent que tout est prêt. Un immense périmètre de protection antiterroriste est déployé le long de la quasi-totalité de la Seine à Paris. Baptisé SILT (du nom de la loi sur la sécurité intérieure et la lutte contre le terrorisme), il vise à sanctuariser le fleuve et ses abords, mais aussi à installer les décors de la cérémonie d\’ouverture, et assurer les dernières répétitions." } 
];

// Sélectionne l'élément parent des cartes
const cardsNews = document.querySelector(".cards-news");

if (!cardsNews) {
  console.error("Erreur : Élément '.cards-news' introuvable.");
} else {
  // Crée un fragment pour améliorer les performances
  const fragmentCardsNews = document.createDocumentFragment();

  
  // Génère dynamiquement les cartes
  imageCardsNews.forEach((imageSrc, index) => {
    if (!textDataCardsNews[index]) {
      console.warn(`Données textuelles manquantes pour l'image: ${imageSrc}`);
      return;
    }

    // Création de la carte
    const card = document.createElement("div");
    card.classList.add("card-news");

    // Ajout de l'image
    const img = document.createElement("img");
    img.src = `image/${imageSrc}`;
    img.alt = textDataCardsNews[index].title;
    img.classList.add("img-card-news");
    card.appendChild(img);

    // Ajout du titre
    const title = document.createElement("h2");
    title.textContent = textDataCardsNews[index].title;
    title.classList.add("title-card-news");
    card.appendChild(title);

    // Ajout du paragraphe
    const paragraph = document.createElement("p");
    paragraph.textContent = textDataCardsNews[index].paragraph;
    paragraph.classList.add("paragraph-card-news");
    card.appendChild(paragraph);

    // Ajout du bouton
    const button = document.createElement("button");
    button.textContent = "En savoir plus";
    button.classList.add("btn-more"); 
    card.appendChild(button);

    // Ajout de la carte au fragment (et non au DOM directement)
    fragmentCardsNews.appendChild(card);
  });
  
  // Ajout final des cartes au DOM
  cardsNews.appendChild(fragmentCardsNews);

  // Ajoute les écouteurs d'événements APRÈS l'ajout au DOM
  document.querySelectorAll(".btn-more").forEach(button => {
    button.addEventListener("click", () => {
      const card = button.parentElement; 

      if (card.classList.contains("expanded")) {
        card.classList.remove("expanded");
        button.textContent = "En savoir plus";
      } else {
        card.classList.add("expanded");
        button.textContent = "Fermer";
      }
    });
  });
}

console.log("Script componentCardsNews chargé");