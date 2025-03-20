const burger = document.getElementById("burger");
const menu = document.getElementById("menu");

// Toggle "open" class on burger and menu
burger.addEventListener("click", () => {
  burger.classList.toggle("open");
  menu.classList.toggle("open");
});

console.log("burger, chargé");