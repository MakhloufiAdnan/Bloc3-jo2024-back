package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.billets.BilletVerificationDto;
import fr.studi.bloc3jo2024.entity.Billet;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.exception.BilletAlreadyScannedException;
import fr.studi.bloc3jo2024.exception.BilletNotFoundException;
import fr.studi.bloc3jo2024.service.BilletService;
import fr.studi.bloc3jo2024.service.BilletQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

/**
 * Contrôleur REST pour la gestion des billets.
 * Fournit des endpoints pour récupérer les QR codes et vérifier les billets.
 */
@RestController
@RequestMapping("/api/billets")
@RequiredArgsConstructor
public class BilletController {

    private final BilletService billetService; // Pour les opérations de modification/transactionnelles
    private final BilletQueryService billetQueryService; // Pour les opérations de lecture seule

    private static final Logger logger = LoggerFactory.getLogger(BilletController.class);

    /**
     * Récupère le QR Code associé à un billet spécifique par son identifiant.
     * @param id L'identifiant du billet.
     * @return Une ResponseEntity contenant l'image du QR code (MediaType.IMAGE_PNG) si trouvée,
     * ou un statut 404 NOT_FOUND si le billet ou son QR code n'existe pas.
     */
    @GetMapping("/{id}/qr-code")
    public ResponseEntity<byte[]> getQRCodeForBillet(@PathVariable Long id) {
        // Utilisation de BilletQueryService pour une opération de lecture
        Optional<Billet> billetOptional = billetQueryService.recupererBilletParId(id);

        return billetOptional
                .filter(b -> b.getQrCodeImage() != null) // Assure que le QR code existe
                .map(b -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.IMAGE_PNG); // Type de contenu pour l'image PNG
                    return new ResponseEntity<>(b.getQrCodeImage(), headers, HttpStatus.OK);
                })
                .orElse(ResponseEntity.notFound().build()); // Retourne 404 si le billet ou le QR code est introuvable
    }

    /**
     * Vérifie un billet par sa clé finale, le marque comme scanné si valide.
     * @param cleFinaleBillet La clé finale du billet à vérifier.
     * @return Une ResponseEntity contenant un DTO de vérification du billet si succès (200 OK).
     * @throws ResponseStatusException avec un statut 404 NOT_FOUND si le billet n'existe pas,
     * un statut 409 CONFLICT si le billet a déjà été scanné,
     * ou un statut 500 INTERNAL_SERVER_ERROR pour toute autre erreur.
     */
    @GetMapping("/verifier/{cleFinaleBillet}")
    public ResponseEntity<BilletVerificationDto> verifierBillet(@PathVariable String cleFinaleBillet) {
        try {
            // Utilisation de BilletService pour l'opération transactionnelle de vérification/scan
            Billet billet = billetService.verifierEtMarquerCommeScanne(cleFinaleBillet);

            // Mapper l'entité Billet vers le DTO de vérification
            BilletVerificationDto verificationDto = new BilletVerificationDto();
            verificationDto.setIdBillet(billet.getIdBillet());
            verificationDto.setCleFinaleBillet(billet.getCleFinaleBillet());
            verificationDto.setDateAchat(billet.getPurchaseDate());

            // Mapping des informations utilisateur si présentes
            Utilisateur utilisateur = billet.getUtilisateur();
            if (utilisateur != null) {
                verificationDto.setIdUtilisateur(utilisateur.getIdUtilisateur());
                verificationDto.setNomUtilisateur(utilisateur.getPrenom() + " " + utilisateur.getNom());
            }

            // Mapping des types d'offre si présentes
            List<Offre> offres = billet.getOffres();
            if (offres != null) {
                verificationDto.setOffres(offres.stream()
                        .map(offre -> offre.getTypeOffre().name())
                        .toList());
            } else {
                verificationDto.setOffres(List.of()); // Assure une liste vide si pas d'offres
            }

            return ResponseEntity.ok(verificationDto);

        } catch (BilletNotFoundException e) {
            logger.warn("Tentative de scan d'un billet non trouvé avec la clé : {}", cleFinaleBillet);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BilletAlreadyScannedException e) {
            logger.warn("Tentative de scan d'un billet déjà utilisé. Clé : {}", cleFinaleBillet);
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur interne lors de la vérification du billet avec la clé : {}", cleFinaleBillet, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne lors de la vérification du billet.");
        }
    }

    /**
     * Endpoint de synchronisation pour le mode hors-ligne.
     * Récupère une liste de toutes les clés de billets valides (non scannées).
     * @return Une ResponseEntity contenant la liste des clés valides (200 OK) ou un statut
     * 500 INTERNAL_SERVER_ERROR en cas d'erreur.
     */
    @GetMapping("/sync/valid-keys")
    public ResponseEntity<List<String>> getValidTicketKeys() {
        try {
            // Utilisation de BilletQueryService pour une opération de lecture
            List<String> keys = billetQueryService.getClesBilletsValides();
            return ResponseEntity.ok(keys);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des clés de billets valides pour la synchronisation.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}