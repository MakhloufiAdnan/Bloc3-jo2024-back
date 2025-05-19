package fr.studi.bloc3jo2024.controller.offres;

import fr.studi.bloc3jo2024.dto.offres.OffreDto;
import fr.studi.bloc3jo2024.service.offres.UtilisateurOffreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/offres") // Pour les utilisateurs (non-admin)
@RequiredArgsConstructor
public class UtilisateurOffreController {

    private final UtilisateurOffreService utilisateurOffreService;

    /**
     * Récupère toutes les offres disponibles pour les utilisateurs, avec pagination.
     * @param pageable Objet de pagination.
     * @return Une page d'OffreDto.
     */
    @GetMapping
    public ResponseEntity<Page<OffreDto>> obtenirToutesLesOffresDisponibles(Pageable pageable) {
        Page<OffreDto> offresDisponibles = utilisateurOffreService.obtenirToutesLesOffresDisponibles(pageable);
        return ResponseEntity.ok(offresDisponibles);
    }

    /**
     * Récupère une offre spécifique disponible par son ID.
     * @param id L'ID de l'offre.
     * @return ResponseEntity avec l'OffreDto ou 404 NOT_FOUND.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OffreDto> obtenirOffreDisponibleParId(@PathVariable Long id) {
        try {
            OffreDto offreDisponible = utilisateurOffreService.obtenirOffreDisponibleParId(id);
            return ResponseEntity.ok(offreDisponible);
        } catch (ResourceNotFoundException e) { // Attraper l'exception spécifique
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}