package fr.studi.bloc3jo2024.controller.offres;

import fr.studi.bloc3jo2024.dto.offres.OffreDto;
import fr.studi.bloc3jo2024.service.offres.UtilisateurOffreService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour les opérations liées aux offres accessibles par les utilisateurs.
 * Permet de consulter les offres disponibles avec pagination.
 */
@RestController
@RequestMapping("/offres")
@RequiredArgsConstructor
public class UtilisateurOffreController {

    private static final Logger log = LoggerFactory.getLogger(UtilisateurOffreController.class);
    private final UtilisateurOffreService utilisateurOffreService;

    /**
     * Récupère une page d'offres actuellement disponibles pour les utilisateurs.
     * La pagination est gérée par Spring Data via l'objet Pageable.
     * Les paramètres de pagination (page, size, sort) peuvent être passés dans l'URL.
     * Exemple: /offres?page=0&size=10&sort=prix,asc
     *
     * @param pageable L'objet de pagination injecté par Spring.
     * @return Une {@link ResponseEntity} contenant une {@link Page} de {@link OffreDto}.
     */
    @GetMapping
    public ResponseEntity<Page<OffreDto>> obtenirToutesLesOffresDisponibles(Pageable pageable) {
        log.info("Requête pour obtenir toutes les offres disponibles avec pagination : {}", pageable);
        Page<OffreDto> offresDisponibles = utilisateurOffreService.obtenirToutesLesOffresDisponibles(pageable);
        return ResponseEntity.ok(offresDisponibles);
    }

    /**
     * Récupère une offre spécifique disponible par son ID.
     *
     * @param id L'ID de l'offre à récupérer.
     * @return Une {@link ResponseEntity} contenant l'{@link OffreDto} si trouvée et disponible.
     * Lève ResourceNotFoundException (gérée par GlobalExceptionHandler pour un statut 404)
     * si l'offre n'est pas trouvée ou pas disponible.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OffreDto> obtenirOffreDisponibleParId(@PathVariable Long id) {
        log.info("Requête pour obtenir l'offre disponible avec ID : {}", id);
        OffreDto offreDisponible = utilisateurOffreService.obtenirOffreDisponibleParId(id);
        return ResponseEntity.ok(offreDisponible);
    }
}
