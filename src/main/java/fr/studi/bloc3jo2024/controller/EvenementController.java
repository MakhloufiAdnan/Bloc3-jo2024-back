package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.entity.Evenement;
import fr.studi.bloc3jo2024.service.EvenementService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class EvenementController {
    private final EvenementService evenementService;

    public EvenementController(EvenementService evenementService) {
        this.evenementService = evenementService;
    }

    // Endpoint pour obtenir les événements à venir
    @GetMapping("/evenements/avenir")
    public List<Evenement> getEvenementsAvenir() {
        return evenementService.getEvenementsAvenir();
    }

    // Endpoint pour obtenir les événements par ville
    @GetMapping("/evenements/ville")
    public List<Evenement> getEvenementsByVille(@RequestParam("ville") String ville) {
        return evenementService.getEvenementsByVille(ville);
    }

    // Endpoint pour obtenir les événements par épreuve
    @GetMapping("/evenements/epreuve")
    public List<Evenement> getEvenementsByEpreuve(@RequestParam("idEpreuve") Long idEpreuve) {
        return evenementService.getEvenementsByEpreuve(idEpreuve);
    }

    // Endpoint pour obtenir les événements à une date précise
    @GetMapping("/evenements")
    public List<Evenement> getEvenementsByDate(@RequestParam("date") LocalDateTime date) {
        return evenementService.getEvenementsByDate(date);
    }
}
