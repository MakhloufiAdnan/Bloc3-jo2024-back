package fr.bloc_jo2024.controller;

import fr.bloc_jo2024.dto.CreerOffreDTO;
import fr.bloc_jo2024.dto.MettreAJourOffreDTO;
import fr.bloc_jo2024.entity.Offre;
import fr.bloc_jo2024.entity.enums.TypeOffre;
import fr.bloc_jo2024.service.AdminOffreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/offres")
public class AdminOffreController {

    private final AdminOffreService adminOffreService;

    public AdminOffreController(AdminOffreService adminOffreService) {
        this.adminOffreService = adminOffreService;
    }

    // Ajouter une nouvelle offre
    @PostMapping
    public ResponseEntity<Offre> ajouterOffre(@Valid @RequestBody CreerOffreDTO creerOffreDTO) {
        Offre nouvelleOffre = adminOffreService.ajouterOffre(creerOffreDTO);
        return new ResponseEntity<>(nouvelleOffre, HttpStatus.CREATED);
    }

    // Obtenir une offre par ID
    @GetMapping("/{idOffre}")
    public ResponseEntity<Offre> obtenirOffreParId(@PathVariable Long idOffre) {
        Offre offre = adminOffreService.obtenirOffreParId(idOffre);
        if (offre != null) {
            return new ResponseEntity<>(offre, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Mettre à jour une offre
    @PutMapping("/{idOffre}")
    public ResponseEntity<Offre> mettreAJourOffre(@PathVariable Long idOffre, @Valid @RequestBody MettreAJourOffreDTO mettreAJourOffreDTO) {
        Offre offreMiseAJour = adminOffreService.mettreAJourOffre(idOffre, mettreAJourOffreDTO);
        if (offreMiseAJour != null) {
            return new ResponseEntity<>(offreMiseAJour, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Modifier le prix d'une offre (peut être conservé ou remplacé par la mise à jour complète)
    @PutMapping("/{idOffre}/prix")
    public ResponseEntity<Offre> modifierPrixOffre(@PathVariable Long idOffre, @RequestParam double prix) {
        Offre offreModifiee = adminOffreService.modifierPrixOffre(idOffre, prix);
        if (offreModifiee != null) {
            return new ResponseEntity<>(offreModifiee, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Supprimer une offre
    @DeleteMapping("/{idOffre}")
    public ResponseEntity<Void> supprimerOffre(@PathVariable Long idOffre) {
        boolean supprime = adminOffreService.supprimerOffre(idOffre);
        if (supprime) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Obtenir les offres par type
    @GetMapping("/type/{typeOffre}")
    public ResponseEntity<List<Offre>> obtenirOffresParType(@PathVariable TypeOffre typeOffre) {
        List<Offre> offres = adminOffreService.obtenirOffresParType(typeOffre);
        return new ResponseEntity<>(offres, HttpStatus.OK);
    }

    // Obtenir toutes les offres
    @GetMapping
    public ResponseEntity<List<Offre>> obtenirToutesLesOffres() {
        List<Offre> offres = adminOffreService.obtenirToutesLesOffres();
        return new ResponseEntity<>(offres, HttpStatus.OK);
    }
}