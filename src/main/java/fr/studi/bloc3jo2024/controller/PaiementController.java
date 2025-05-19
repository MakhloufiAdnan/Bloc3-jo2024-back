package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.paiement.PaiementDto;
import fr.studi.bloc3jo2024.dto.paiement.PaiementSimulationResultDto;
import fr.studi.bloc3jo2024.entity.enums.MethodePaiementEnum;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.service.PaiementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
@Validated
public class PaiementController {

    private final PaiementService paiementService;

    private UUID getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            try {
                return UUID.fromString(username);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("L'identifiant de l'utilisateur authentifié n'est pas un UUID valide.", e);
            }
        }
        throw new IllegalStateException("Impossible de récupérer l'ID de l'utilisateur authentifié.");
    }

    @PostMapping("/panier/{idPanier}")
    public ResponseEntity<PaiementDto> effectuerPaiement(
            @PathVariable Long idPanier,
            @RequestParam MethodePaiementEnum methodePaiement) {
        UUID utilisateurId = getAuthenticatedUserId();
        try {
            PaiementDto paiementDto = paiementService.effectuerPaiement(utilisateurId, idPanier, methodePaiement);
            return ResponseEntity.status(HttpStatus.CREATED).body(paiementDto);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{idPaiement}/simuler")
    public ResponseEntity<PaiementSimulationResultDto> simulerResultatPaiement(
            @PathVariable Long idPaiement,
            @RequestParam boolean reussi,
            @RequestParam(required = false) String details) {
        try {
            PaiementSimulationResultDto resultDto = paiementService.simulerResultatPaiement(idPaiement, reussi, details != null ? details : "");
            return ResponseEntity.ok(resultDto);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/panier/{idPanier}")
    public ResponseEntity<PaiementDto> getPaiementParPanier(
            @PathVariable Long idPanier) {
        UUID utilisateurId = getAuthenticatedUserId();
        try {
            return paiementService.getPaiementParPanier(utilisateurId, idPanier)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paiement non trouvé pour ce panier et cet utilisateur"));
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{idPaiement}")
    public ResponseEntity<PaiementDto> getPaiementParId(
            @PathVariable Long idPaiement) {
        return paiementService.getPaiementParId(idPaiement)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paiement non trouvé avec l'ID : " + idPaiement));
    }
}