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
     * @param userId L'ID de l'utilisateur (UUID).
     * @return ResponseEntity contenant le PanierDto.
     * @throws ResponseStatusException avec statut 404 si non trouvé.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<PanierDto> getPanier(@PathVariable UUID userId) {
        try {
            PanierDto dto = panierService.getPanierUtilisateur(userId.toString());
            return ResponseEntity.ok(dto);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    /**
     * Ajoute une offre au panier de l'utilisateur.
     * @param userId L'ID de l'utilisateur.
     * @param request DTO pour l'ajout d'offre.
     * @return ResponseEntity avec le PanierDto mis à jour et statut 201.
     * @throws ResponseStatusException pour les erreurs 400, 404, 409.
     */
    @PostMapping("/{userId}/offres")
    public ResponseEntity<PanierDto> ajouterOffre(
            @PathVariable UUID userId,
            @Valid @RequestBody AjouterOffrePanierDto request) {
        try {
            PanierDto dto = panierService.ajouterOffreAuPanier(userId.toString(), request);
            return new ResponseEntity<>(dto, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        }
    }

    /**
     * Modifie la quantité d'une offre dans le panier.
     * @param userId L'ID de l'utilisateur.
     * @param request DTO pour la modification.
     * @return ResponseEntity avec le PanierDto mis à jour.
     * @throws ResponseStatusException pour les erreurs.
     */
    @PatchMapping("/{userId}/offres")
    public ResponseEntity<PanierDto> modifierQuantite(
            @PathVariable UUID userId,
            @Valid @RequestBody ModifierContenuPanierDto request) {
        try {
            PanierDto dto = panierService.modifierQuantiteOffrePanier(userId.toString(), request);
            return ResponseEntity.ok(dto);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        }
    }

    /**
     * Supprime une offre du panier.
     * @param userId L'ID de l'utilisateur.
     * @param offreId L'ID de l'offre à supprimer.
     * @return ResponseEntity avec le PanierDto mis à jour.
     * @throws ResponseStatusException si l'offre n'est pas trouvée dans le panier.
     */
    @DeleteMapping("/{userId}/offres/{offreId}")
    public ResponseEntity<PanierDto> supprimerOffre(
            @PathVariable UUID userId,
            @PathVariable Long offreId) {
        try {
            PanierDto dto = panierService.supprimerOffreDuPanier(userId.toString(), offreId);
            return ResponseEntity.ok(dto);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    /**
     * Valide (paie) le panier de l'utilisateur.
     * @param userId L'ID de l'utilisateur.
     * @return ResponseEntity avec le PanierDto mis à jour (statut PAYE).
     * @throws ResponseStatusException pour les erreurs.
     */
    @PostMapping("/{userId}/payer")
    public ResponseEntity<PanierDto> payerPanier(@PathVariable UUID userId) {
        try {
            PanierDto dto = panierService.finaliserAchat(userId.toString());
            return ResponseEntity.ok(dto);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        }
    }
}