package fr.bloc_jo2024.controller;

import fr.bloc_jo2024.entity.Offre;
import fr.bloc_jo2024.entity.TypeOffre;
import fr.bloc_jo2024.service.AdminOffreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/offres")
public class AdminOffreController {

    @Autowired
    private AdminOffreService adminOffreService;

    // Ajouter une nouvelle offre
    @PostMapping
    public Offre ajouterOffre(@RequestBody Offre nouvelleOffre) {
        return adminOffreService.ajouterOffre(nouvelleOffre);
    }

    // Modifier le prix d'une offre
    @PutMapping("/{idOffre}/prix")
    public Offre modifierPrixOffre(@PathVariable Long idOffre, @RequestParam double prix) {
        return adminOffreService.modifierPrixOffre(idOffre, prix);
    }

    // Supprimer une offre
    @DeleteMapping("/{idOffre}")
    public boolean supprimerOffre(@PathVariable Long idOffre) {
        return adminOffreService.supprimerOffre(idOffre);
    }

    // Obtenir les offres par type
    @GetMapping("/type/{typeOffre}")
    public List<Offre> obtenirOffresParType(@PathVariable TypeOffre typeOffre) {
        return adminOffreService.obtenirOffresParType(typeOffre);
    }

    // Obtenir toutes les offres
    @GetMapping
    public List<Offre> obtenirToutesLesOffres() {
        return adminOffreService.obtenirToutesLesOffres();
    }
}
