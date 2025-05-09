package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.billets.BilletVerificationDto;
import fr.studi.bloc3jo2024.entity.Billet;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.service.BilletService;
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

@RestController
@RequestMapping("/api/billets")
@RequiredArgsConstructor
public class BilletController {

    private final BilletService billetService;
    private static final Logger logger = LoggerFactory.getLogger(BilletController.class);
    // Récupération du QR Code associé à un billet
    @GetMapping("/{id}/qr-code")
    public ResponseEntity<byte[]> getQRCodeForBillet(@PathVariable Long id) {
        Optional<Billet> billetOptional = billetService.recupererBilletParId(id);

        return billetOptional
                .filter(b -> b.getQrCodeImage() != null)
                .map(b -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.IMAGE_PNG);
                    return new ResponseEntity<>(b.getQrCodeImage(), headers, HttpStatus.OK);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Vérification du billet par sa clé finale
    @GetMapping("/verifier/{cleFinaleBillet}")
    public ResponseEntity<BilletVerificationDto> verifierBillet(@PathVariable String cleFinaleBillet) {
        try {
            // Le service lève une IllegalArgumentException si non trouvé
            Billet billet = billetService.recupererBilletParCleFinale(cleFinaleBillet);

            // Mapper l'entité Billet vers le DTO de vérification
            BilletVerificationDto verificationDto = new BilletVerificationDto();
            verificationDto.setIdBillet(billet.getIdBillet());
            verificationDto.setCleFinaleBillet(billet.getCleFinaleBillet());

            // Utilisateur mapping
            Utilisateur utilisateur = billet.getUtilisateur();
            if (utilisateur != null) {
                verificationDto.setIdUtilisateur(utilisateur.getIdUtilisateur());
                verificationDto.setNomUtilisateur(utilisateur.getPrenom() + " " + utilisateur.getNom());
            }

            // Offre mapping
            List<Offre> offres = billet.getOffres();
            if (offres != null) {
                verificationDto.setOffres(offres.stream()
                        .map(offre -> offre.getTypeOffre().name())
                        .toList());
            } else {
                verificationDto.setOffres(List.of());
            }

            return ResponseEntity.ok(verificationDto); // Retourne 200 OK avec le DTO
        } catch (IllegalArgumentException e) {
            // Billet non trouvé avec cette clé finale
            logger.warn("Billet non trouvé avec la clé : {}", cleFinaleBillet);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification du billet avec la clé : {}", cleFinaleBillet, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne lors de la vérification du billet.");
        }
    }
}
