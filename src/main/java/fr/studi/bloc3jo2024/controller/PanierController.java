package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.panier.AjouterOffrePanierDto;
import fr.studi.bloc3jo2024.dto.panier.ModifierContenuPanierDto;
import fr.studi.bloc3jo2024.dto.panier.PanierDto;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.service.PanierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/paniers")
@RequiredArgsConstructor
@Validated
public class PanierController {
    private final PanierService panierService;

    /**
     * Récupère le panier en cours de l'utilisateur.
     * @param userId L'ID de l'utilisateur.
     * @return ResponseEntity contenant le PanierDto et le statut HTTP 200 OK,
     * ou le statut HTTP 404 NOT_FOUND si l'utilisateur n'existe pas (potentiellement géré dans le service).
     */
    @GetMapping("/{userId}")
    public ResponseEntity<PanierDto> getPanier(@PathVariable UUID userId) {
        try {
            PanierDto dto = panierService.getPanierUtilisateur(userId.toString()); // Utilisez getPanierUtilisateur
            return ResponseEntity.ok(dto);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Ajoute une offre au panier de l'utilisateur.
     * @param userId L'ID de l'utilisateur.
     * @param request DTO contenant l'ID de l'offre et la quantité à ajouter.
     * @return ResponseEntity contenant le PanierDto mis à jour et le statut HTTP 201 CREATED,
     * ou le statut HTTP 400 BAD_REQUEST en cas de données invalides.
     */
    @PostMapping("/{userId}/offres")
    public ResponseEntity<PanierDto> ajouterOffre(
            @PathVariable UUID userId,
            @Valid @RequestBody AjouterOffrePanierDto request) {
        try {
            PanierDto dto = panierService.ajouterOffreAuPanier(userId.toString(), request); // Convertir UUID en String
            return new ResponseEntity<>(dto, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()); // Conflit si stock insuffisant
        }
    }

    /**
     * Modifie la quantité d'une offre dans le panier de l'utilisateur.
     * @param userId L'ID de l'utilisateur.
     * @param request DTO contenant l'ID de l'offre et la nouvelle quantité.
     * @return ResponseEntity contenant le PanierDto mis à jour et le statut HTTP 200 OK,
     * ou le statut HTTP 400 BAD_REQUEST en cas de données invalides,
     * ou le statut HTTP 404 NOT_FOUND si l'offre n'est pas dans le panier.
     */
    @PatchMapping("/{userId}/offres")
    public ResponseEntity<PanierDto> modifierQuantite(
            @PathVariable UUID userId,
            @Valid @RequestBody ModifierContenuPanierDto request) {
        try {
            PanierDto dto = panierService.modifierQuantiteOffrePanier(userId.toString(), request); // Convertir UUID en String
            return ResponseEntity.ok(dto);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()); // Conflit si places insuffisantes
        }
    }

    /**
     * Supprime une offre du panier de l'utilisateur.
     * @param userId L'ID de l'utilisateur.
     * @param offreId L'ID de l'offre à supprimer.
     * @return ResponseEntity avec le statut HTTP 200 OK et le PanierDto mis à jour,
     * ou le statut HTTP 404 NOT_FOUND si l'offre n'est pas dans le panier.
     */
    @DeleteMapping("/{userId}/offres/{offreId}")
    public ResponseEntity<PanierDto> supprimerOffre(
            @PathVariable UUID userId,
            @PathVariable Long offreId) {
        try {
            PanierDto dto = panierService.supprimerOffreDuPanier(userId.toString(), offreId); // Convertir UUID en String
            return ResponseEntity.ok(dto);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Valide (paie) le panier de l'utilisateur.
     * @param userId L'ID de l'utilisateur.
     * @return ResponseEntity contenant le PanierDto mis à jour (avec statut PAYE) et le statut HTTP 200 OK,
     * ou le statut HTTP 404 NOT_FOUND si le panier n'existe pas,
     * ou le statut HTTP 409 CONFLICT si le panier ne peut pas être payé.
     */
    @PostMapping("/{userId}/payer")
    public ResponseEntity<PanierDto> payerPanier(@PathVariable UUID userId) {
        try {
            PanierDto dto = panierService.finaliserAchat(userId.toString()); // Modifier la signature de finaliserAchat pour prendre l'ID utilisateur
            return ResponseEntity.ok(dto);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}