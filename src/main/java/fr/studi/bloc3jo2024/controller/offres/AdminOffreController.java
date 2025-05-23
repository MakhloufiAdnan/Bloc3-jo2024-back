package fr.studi.bloc3jo2024.controller.offres;

import fr.studi.bloc3jo2024.dto.offres.CreerOffreDto;
import fr.studi.bloc3jo2024.dto.offres.MettreAJourOffreDto;
import fr.studi.bloc3jo2024.dto.offres.OffreAdminDto;
import fr.studi.bloc3jo2024.service.offres.AdminOffreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/offres")
@RequiredArgsConstructor
public class AdminOffreController {

    private static final Logger log = LoggerFactory.getLogger(AdminOffreController.class);
    private final AdminOffreService adminOffreService;

    /**
     * Ajoute une nouvelle offre. Les données de l'offre sont validées.
     * En cas de succès, retourne l'offre créée avec un statut HTTP 201 (Created).
     *
     * @param creerOffreDto DTO contenant les informations de la nouvelle offre.
     * @return ResponseEntity contenant l'{@link OffreAdminDto} de l'offre créée.
     */
    @PostMapping
    public ResponseEntity<OffreAdminDto> ajouterOffre(@Valid @RequestBody CreerOffreDto creerOffreDto) {
        log.info("Tentative d'ajout d'une nouvelle offre : {}", creerOffreDto.getTypeOffre());
        OffreAdminDto nouvelleOffre = adminOffreService.ajouterOffre(creerOffreDto);
        log.info("Nouvelle offre ajoutée avec ID : {}", nouvelleOffre.getId());
        return new ResponseEntity<>(nouvelleOffre, HttpStatus.CREATED);
    }

    /**
     * Récupère une offre spécifique par son ID pour l'administration.
     *
     * @param id L'ID de l'offre à récupérer.
     * @return ResponseEntity contenant l'{@link OffreAdminDto} de l'offre.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OffreAdminDto> obtenirOffreParId(@PathVariable Long id) {
        log.info("Tentative de récupération de l'offre avec ID : {}", id);
        OffreAdminDto offre = adminOffreService.obtenirOffreParId(id);
        return ResponseEntity.ok(offre);
    }

    /**
     * Met à jour une offre existante. Les données de mise à jour sont validées.
     *
     * @param id L'ID de l'offre à mettre à jour.
     * @param mettreAJourOffreDto DTO contenant les nouvelles informations de l'offre.
     * @return ResponseEntity contenant l'{@link OffreAdminDto} de l'offre mise à jour.
     */
    @PutMapping("/{id}")
    public ResponseEntity<OffreAdminDto> mettreAJourOffre(
            @PathVariable Long id,
            @Valid @RequestBody MettreAJourOffreDto mettreAJourOffreDto) {
        log.info("Tentative de mise à jour de l'offre avec ID : {}", id);
        OffreAdminDto offreMiseAJour = adminOffreService.mettreAJourOffre(id, mettreAJourOffreDto);
        log.info("Offre ID : {} mise à jour avec succès.", id);
        return ResponseEntity.ok(offreMiseAJour);
    }

    /**
     * Supprime une offre par son ID.
     *
     * @param id L'ID de l'offre à supprimer.
     * @return ResponseEntity avec un statut HTTP 204 (No Content) si la suppression réussit.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerOffre(@PathVariable Long id) {
        log.info("Tentative de suppression de l'offre avec ID : {}", id);
        adminOffreService.supprimerOffre(id);
        log.info("Offre ID : {} supprimée avec succès.", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupère toutes les offres (pour l'administration) avec pagination.
     * Les paramètres de pagination (page, size, sort) peuvent être passés dans l'URL.
     * Exemple: /admin/offres?page=0&size=5&sort=prix,desc
     *
     * @param pageable Objet de pagination injecté par Spring.
     * @return ResponseEntity contenant une {@link Page} d'{@link OffreAdminDto}.
     */
    @GetMapping
    public ResponseEntity<Page<OffreAdminDto>> obtenirToutesLesOffres(Pageable pageable) {
        log.info("Récupération de toutes les offres (admin) avec pagination : {}", pageable);
        Page<OffreAdminDto> toutesLesOffres = adminOffreService.obtenirToutesLesOffres(pageable);
        return ResponseEntity.ok(toutesLesOffres);
    }

    /**
     * Récupère des statistiques sur le nombre de ventes par offre.
     *
     * @return ResponseEntity contenant une Map avec l'ID de l'offre en clé et le nombre de ventes en valeur.
     */
    @GetMapping("/ventes/par-offre")
    public ResponseEntity<Map<Long, Long>> getVentesParOffre() {
        log.info("Récupération des statistiques de ventes par offre.");
        Map<Long, Long> ventes = adminOffreService.getNombreDeVentesParOffre();
        return ResponseEntity.ok(ventes);
    }

    /**
     * Récupère des statistiques sur le nombre de ventes groupées par type d'offre.
     *
     * @return ResponseEntity contenant une Map avec le type d'offre (String) en clé et le nombre de ventes en valeur.
     */
    @GetMapping("/ventes/par-type")
    public ResponseEntity<Map<String, Long>> getVentesParType() {
        log.info("Récupération des statistiques de ventes par type d'offre.");
        Map<String, Long> ventesParType = adminOffreService.getVentesParTypeOffre();
        return ResponseEntity.ok(ventesParType);
    }
}
