package fr.studi.bloc3jo2024.controller.offres;

import fr.studi.bloc3jo2024.dto.offres.CreerOffreDto;
import fr.studi.bloc3jo2024.dto.offres.MettreAJourOffreDto;
import fr.studi.bloc3jo2024.dto.offres.OffreAdminDto;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.service.offres.AdminOffreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page; // Pour la pagination
import org.springframework.data.domain.Pageable; // Pour la pagination


// import java.util.List; // Remplacé par Page pour obtenirToutesLesOffres
import java.util.Map;

@RestController
@RequestMapping("/admin/offres")
@RequiredArgsConstructor // Injection de dépendances
public class AdminOffreController {

    private final AdminOffreService adminOffreService;

    /**
     * Ajoute une nouvelle offre.
     * @param creerOffreDto DTO contenant les informations de la nouvelle offre.
     * @return ResponseEntity contenant l'OffreAdminDto de l'offre créée et le statut HTTP 201 (CREATED),
     * ou statut HTTP 400 (BAD_REQUEST) en cas d'erreur de validation ou de données (ex: discipline non trouvée).
     */
    @PostMapping
    public ResponseEntity<OffreAdminDto> ajouterOffre(@Valid @RequestBody CreerOffreDto creerOffreDto) {
        try {
            OffreAdminDto nouvelleOffre = adminOffreService.ajouterOffre(creerOffreDto);
            return new ResponseEntity<>(nouvelleOffre, HttpStatus.CREATED);
        } catch (IllegalArgumentException | ResourceNotFoundException e) { // Capturer ResourceNotFound pour discipline
            // Idéalement, créer une exception personnalisée ou utiliser un @ControllerAdvice pour gérer cela globalement
            return ResponseEntity.badRequest().build(); // Simple retour pour l'exemple
        }
    }

    /**
     * Récupère une offre par son ID.
     * @param id L'ID de l'offre à récupérer.
     * @return ResponseEntity contenant l'OffreAdminDto de l'offre trouvée et statut HTTP 200 (OK),
     * ou statut HTTP 404 (NOT_FOUND) si l'offre n'existe pas.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OffreAdminDto> getOffreParId(@PathVariable Long id) {
        try {
            OffreAdminDto offre = adminOffreService.obtenirOffreParId(id);
            return ResponseEntity.ok(offre);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Met à jour une offre existante.
     * @param id L'ID de l'offre à mettre à jour.
     * @param mettreAJourOffreDto DTO contenant les nouvelles informations de l'offre.
     * @return ResponseEntity contenant l'OffreAdminDto de l'offre mise à jour et statut HTTP 200 (OK),
     * ou statut HTTP 404 (NOT_FOUND) si l'offre n'existe pas,
     * ou statut HTTP 400 (BAD_REQUEST) en cas d'erreur de validation ou de données.
     */
    @PutMapping("/{id}")
    public ResponseEntity<OffreAdminDto> mettreAJourOffre(
            @PathVariable Long id,
            @Valid @RequestBody MettreAJourOffreDto mettreAJourOffreDto) {
        try {
            OffreAdminDto offreMiseAJour = adminOffreService.mettreAJourOffre(id, mettreAJourOffreDto);
            return ResponseEntity.ok(offreMiseAJour);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Supprime une offre.
     * @param id L'ID de l'offre à supprimer.
     * @return ResponseEntity avec statut HTTP 204 (NO_CONTENT) en cas de succès,
     * ou statut HTTP 404 (NOT_FOUND) si l'offre n'existe pas.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerOffre(@PathVariable Long id) {
        try {
            adminOffreService.supprimerOffre(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Récupère toutes les offres existantes, avec pagination.
     * @param pageable L'objet de pagination.
     * @return ResponseEntity contenant une page d'OffreAdminDto et statut HTTP 200 (OK).
     */
    @GetMapping
    public ResponseEntity<Page<OffreAdminDto>> obtenirToutesLesOffres(Pageable pageable) {
        Page<OffreAdminDto> toutesLesOffres = adminOffreService.obtenirToutesLesOffres(pageable);
        return ResponseEntity.ok(toutesLesOffres);
    }

    /**
     * Récupère le nombre de ventes par offre.
     * @return ResponseEntity contenant une map avec l'ID de l'offre et le nombre de ventes, statut HTTP 200 (OK).
     */
    @GetMapping("/ventes/par-offre")
    public ResponseEntity<Map<Long, Long>> getVentesParOffre() {
        return ResponseEntity.ok(adminOffreService.getNombreDeVentesParOffre());
    }

    /**
     * Récupère le nombre de ventes par type d'offre.
     * @return ResponseEntity contenant une map avec le type d'offre et le nombre de ventes, statut HTTP 200 (OK).
     */
    @GetMapping("/ventes/par-type")
    public ResponseEntity<Map<String, Long>> getVentesParType() {
        return ResponseEntity.ok(adminOffreService.getVentesParTypeOffre());
    }
}