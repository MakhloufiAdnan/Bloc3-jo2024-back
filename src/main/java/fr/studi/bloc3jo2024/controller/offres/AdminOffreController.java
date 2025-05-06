package fr.studi.bloc3jo2024.controller.offres;

import fr.studi.bloc3jo2024.dto.offres.CreerOffreDto;
import fr.studi.bloc3jo2024.dto.offres.OffreAdminDto;
import fr.studi.bloc3jo2024.dto.offres.MettreAJourOffreDto;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.service.offres.AdminOffreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/offres")
public class AdminOffreController {

    private final AdminOffreService adminOffreService;

    public AdminOffreController(AdminOffreService adminOffreService) {
        this.adminOffreService = adminOffreService;
    }

    /**
     * Ajoute une nouvelle offre.
     * @param creerOffreDto DTO contenant les informations de la nouvelle offre.
     * @return ResponseEntity contenant l'OffreAdminDto de l'offre créée et le statut HTTP 201 CREATED,
     * ou le statut HTTP 400 BAD_REQUEST en cas d'erreur de validation ou de données.
     */
    @PostMapping
    public ResponseEntity<OffreAdminDto> ajouterOffre(@Valid @RequestBody CreerOffreDto creerOffreDto) {
        try {
            OffreAdminDto nouvelleOffre = adminOffreService.ajouterOffre(creerOffreDto);
            return new ResponseEntity<>(nouvelleOffre, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Récupère une offre par son ID.
     * @param id L'ID de l'offre à récupérer.
     * @return ResponseEntity contenant l'OffreAdminDto de l'offre trouvée et le statut HTTP 200 OK,
     * ou le statut HTTP 404 NOT_FOUND si l'offre n'existe pas.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OffreAdminDto> obtenirOffreParId(@PathVariable Long id) {
        try {
            OffreAdminDto offre = adminOffreService.obtenirOffreParId(id);
            return ResponseEntity.ok(offre);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Met à jour une offre existante.
     * @param id L'ID de l'offre à mettre à jour.
     * @param mettreAJourOffreDto DTO contenant les nouvelles informations de l'offre.
     * @return ResponseEntity contenant l'OffreAdminDto de l'offre mise à jour et le statut HTTP 200 OK,
     * ou le statut HTTP 404 NOT_FOUND si l'offre n'existe pas,
     * ou le statut HTTP 400 BAD_REQUEST en cas d'erreur de validation ou de données.
     */
    @PutMapping("/{id}")
    public ResponseEntity<OffreAdminDto> mettreAJourOffre(@PathVariable Long id, @Valid @RequestBody MettreAJourOffreDto mettreAJourOffreDto) {
        try {
            OffreAdminDto offreMiseAJour = adminOffreService.mettreAJourOffre(id, mettreAJourOffreDto);
            return ResponseEntity.ok(offreMiseAJour);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Supprime une offre.
     * @param id L'ID de l'offre à supprimer.
     * @return ResponseEntity avec le statut HTTP 204 NO_CONTENT en cas de succès,
     * ou le statut HTTP 404 NOT_FOUND si l'offre n'existe pas.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerOffre(@PathVariable Long id) {
        try {
            adminOffreService.supprimerOffre(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Récupère toutes les offres existantes.
     * @return ResponseEntity contenant une liste d'OffreAdminDto et le statut HTTP 200 OK.
     */
    @GetMapping
    public ResponseEntity<List<OffreAdminDto>> obtenirToutesLesOffres() {
        List<OffreAdminDto> toutesLesOffres = adminOffreService.obtenirToutesLesOffres();
        return ResponseEntity.ok(toutesLesOffres);
    }

    /**
     * Récupère le nombre de ventes par offre.
     * @return ResponseEntity contenant une map avec l'ID de l'offre comme clé et le nombre de ventes comme valeur,
     * et le statut HTTP 200 OK.
     */
    @GetMapping("/ventes")
    public ResponseEntity<Map<Long, Long>> getVentesParOffre() {
        return ResponseEntity.ok(adminOffreService.getNombreDeVentesParOffre());
    }

    /**
     * Récupère le nombre de ventes par type d'offre.
     * @return ResponseEntity contenant une map avec le type d'offre comme clé et le nombre de ventes comme valeur,
     * et le statut HTTP 200 OK.
     */
    @GetMapping("/ventes/par-type")
    public ResponseEntity<Map<String, Long>> getVentesParType() {
        return ResponseEntity.ok(adminOffreService.getVentesParTypeOffre());
    }
}