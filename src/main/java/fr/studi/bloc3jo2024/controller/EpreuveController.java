package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.epreuves.MettreAJourEpreuveVedetteDto;
import fr.studi.bloc3jo2024.entity.Epreuve;
import fr.studi.bloc3jo2024.service.EpreuveService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;

@RestController
@RequestMapping("/admin/epreuves")
@RequiredArgsConstructor
public class EpreuveController {

    private final EpreuveService epreuveService;

    /**
     * Récupère la liste de toutes les épreuves pour l'interface d'administration, avec pagination.
     * @param pageable Objet de pagination.
     * @return Une page d'Epreuves.
     */
    @GetMapping
    public ResponseEntity<Page<Epreuve>> getAllEpreuves(Pageable pageable) {
        Page<Epreuve> epreuves = epreuveService.getAllEpreuves(pageable);
        return ResponseEntity.ok(epreuves);
    }

    /**
     * Récupère la liste de toutes les épreuves marquées comme "en vedette".
     * @return Une liste d'Epreuves en vedette.
     */
    @GetMapping("/vedette")
    public ResponseEntity<List<Epreuve>> getEpreuvesEnVedette() {
        List<Epreuve> epreuvesEnVedette = epreuveService.getEpreuvesEnVedette();
        return ResponseEntity.ok(epreuvesEnVedette);
    }

    /**
     * Met à jour le statut "en vedette" d'une épreuve.
     * @param mettreAJourEpreuveVedetteDto DTO pour la mise à jour.
     * @return ResponseEntity avec l'Epreuve mise à jour ou 404 NOT_FOUND.
     */
    @PatchMapping("/vedette")
    public ResponseEntity<Epreuve> mettreAJourStatutVedette(
            @Valid @RequestBody MettreAJourEpreuveVedetteDto mettreAJourEpreuveVedetteDto) {
        try {
            Epreuve updatedEpreuve = epreuveService.mettreAJourStatutVedette(mettreAJourEpreuveVedetteDto);
            return ResponseEntity.ok(updatedEpreuve);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}