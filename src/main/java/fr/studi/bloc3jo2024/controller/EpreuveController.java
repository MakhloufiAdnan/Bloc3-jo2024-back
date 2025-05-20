package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.epreuves.MettreAJourEpreuveVedetteDto;
import fr.studi.bloc3jo2024.entity.Epreuve;
import fr.studi.bloc3jo2024.service.EpreuveService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/epreuves")
public class EpreuveController {

    private final EpreuveService epreuveService;

    public EpreuveController(EpreuveService epreuveService) {
        this.epreuveService = epreuveService;
    }

    /**
     * Récupère la liste de toutes les épreuves pour l'interface d'administration.
     */
    @GetMapping
    public ResponseEntity<List<Epreuve>> getAllEpreuves() {
        return ResponseEntity.ok(epreuveService.getAllEpreuves());
    }

    /**
     * Récupère la liste de toutes les épreuves marquées comme "en vedette".
     * Correspond à la méthode getEpreuvesEnVedette() dans EpreuveService.
     */
    @GetMapping("/vedette")
    public ResponseEntity<List<Epreuve>> getEpreuvesEnVedette() {
        return ResponseEntity.ok(epreuveService.getEpreuvesEnVedette());
    }

    /**
     * Met à jour le statut "en vedette" d'une épreuve.
     */
    @PatchMapping("/vedette")
    public ResponseEntity<Epreuve> mettreAJourStatutVedette(
            @Valid @RequestBody MettreAJourEpreuveVedetteDto mettreAJourEpreuveVedetteDto
    ) {
        try {
            Epreuve updatedEpreuve = epreuveService.mettreAJourStatutVedette(mettreAJourEpreuveVedetteDto);
            return ResponseEntity.ok(updatedEpreuve);
        } catch (EntityNotFoundException e) {
            // Gérer le cas où l'épreuve n'est pas trouvée et retourner un 404 NOT FOUND
            return ResponseEntity.notFound().build();
        }
    }
}