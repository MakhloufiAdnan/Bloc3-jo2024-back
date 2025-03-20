document.addEventListener("DOMContentLoaded", async function () {
    const ctx = document.getElementById("sellChart").getContext("2d");

    async function fetchSalesData() {
        try {
            const response = await fetch("http://localhost:8080/api/sales");
            const salesData = await response.json();

            const labels = salesData.map(sale => sale.day);
            const soloSales = salesData.map(sale => sale.soloSales / 1000); // Convertir en milliers
            const duoSales = salesData.map(sale => sale.duoSales / 1000);
            const familySales = salesData.map(sale => sale.familySales / 1000);

            new Chart(ctx, {
                type: "bar",
                data: {
                    labels: labels,
                    datasets: [
                        {
                            label: "Offre Solo (en milliers)",
                            data: soloSales,
                            backgroundColor: "#FF6384",
                            borderColor: "#FF6384",
                            borderWidth: 1
                        },
                        {
                            label: "Offre Duo (en milliers)",
                            data: duoSales,
                            backgroundColor: "#36A2EB",
                            borderColor: "#36A2EB",
                            borderWidth: 1
                        },
                        {
                            label: "Offre Famille (en milliers)",
                            data: familySales,
                            backgroundColor: "#FFCE56",
                            borderColor: "#FFCE56",
                            borderWidth: 1
                        }
                    ]
                },
                options: {
                    responsive: true,
                    scales: {
                        y: {
                            beginAtZero: true,
                            title: {
                                display: true,
                                text: "Nombre de ventes (en milliers)"
                            }
                        }
                    }
                }
            });
        } catch (error) {
            console.error("Erreur lors du chargement des données :", error);
        }
    }

    fetchSalesData();
});