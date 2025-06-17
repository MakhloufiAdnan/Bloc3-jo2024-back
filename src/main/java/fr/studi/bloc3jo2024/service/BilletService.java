package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.entity.Billet;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.exception.BilletAlreadyScannedException;
import fr.studi.bloc3jo2024.exception.BilletNotFoundException;
import fr.studi.bloc3jo2024.repository.BilletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service principal pour la gestion des opérations transactionnelles
 * liées aux billets (création, finalisation, vérification et scan).
 */
@Service
@RequiredArgsConstructor
public class BilletService {

    private final BilletRepository billetRepository;
    private final QrCodeService qrCodeService;
    private final EmailService emailService;
    private final BilletQueryService billetQueryService; // Injection du nouveau service de lecture

    @Value("${app.email.sujet.billet}")
    private String sujetEmailBillet;

    @Value("${app.email.contenu.billet}")
    private String contenuEmailBillet;

    /**
     * Génère une clé d'achat unique sous forme d'UUID.
     * @return La clé d'achat générée.
     */
    public String genererCleAchat() {
        return UUID.randomUUID().toString();
    }

    /**
     * Génère la clé finale d'un billet en combinant la clé utilisateur et la clé d'achat.
     * @param cleUtilisateur La clé de l'utilisateur.
     * @param cleAchat La clé d'achat générée.
     * @return La clé finale du billet.
     */
    public String genererCleFinaleBillet(String cleUtilisateur, String cleAchat) {
        return cleUtilisateur + "-" + cleAchat;
    }

    /**
     * Crée et enregistre un billet en base de données.
     * L'opération est transactionnelle.
     * @param utilisateur L'utilisateur associé au billet.
     * @param offres La liste des offres incluses dans le billet.
     * @param cleFinaleBillet La clé finale unique du billet.
     * @param purchaseDate La date d'achat du billet.
     * @return Le billet sauvegardé.
     */
    @Transactional
    public Billet creerEtEnregistrerBillet(Utilisateur utilisateur, List<Offre> offres, String cleFinaleBillet, LocalDateTime purchaseDate) {
        Billet billet = Billet.builder()
                .utilisateur(utilisateur)
                .offres(offres)
                .cleFinaleBillet(cleFinaleBillet)
                .purchaseDate(purchaseDate)
                .build();
        return billetRepository.save(billet);
    }

    /**
     * Finalise la création d'un billet en générant son QR code et en l'associant,
     * puis envoie le billet par e-mail à l'utilisateur.
     * L'opération est transactionnelle.
     * @param billet Le billet à finaliser.
     * @return Le billet finalisé et sauvegardé.
     */
    @Transactional
    public Billet finaliserBilletAvecQrCode(Billet billet) {
        byte[] qrCodeImage = qrCodeService.generateQRCode(billet.getCleFinaleBillet());
        billet.setQrCodeImage(qrCodeImage);
        Billet billetSauvegarde = billetRepository.save(billet);

        // Envoyer l'e-mail après la sauvegarde du billet et la génération du QR code
        envoyerBilletParEmail(billetSauvegarde);
        return billetSauvegarde;
    }

    /**
     * Envoie le billet (avec QR code intégré) par e-mail à l'utilisateur.
     * Cette méthode est privée car elle est une étape interne de la finalisation du billet.
     * @param billet Le billet à envoyer.
     */
    private void envoyerBilletParEmail(Billet billet) {
        Utilisateur utilisateur = billet.getUtilisateur();
        byte[] qrCodeImage = billet.getQrCodeImage();
        String nomFichierQrCode = "billet_" + billet.getCleFinaleBillet() + ".png";

        // Remplacer les placeholders dans le contenu de l'e-mail
        String contenuPersonnalise = String.format(contenuEmailBillet, utilisateur.getPrenom(), billet.getCleFinaleBillet());

        emailService.envoyerEmailAvecQrCode(
                utilisateur.getEmail(),
                sujetEmailBillet,
                contenuPersonnalise,
                qrCodeImage,
                nomFichierQrCode
        );
    }

    /**
     * Vérifie un billet. S'il est valide, le marque comme "scanné" pour empêcher sa réutilisation.
     * Cette méthode est l'interface principale pour le scan de billets.
     * L'annotation @Transactional garantit que la lecture et la mise à jour sont une opération unique et sécurisée.
     * @param cleFinaleBillet La clé unique du billet à vérifier.
     * @return Le Billet s'il est valide et vient d'être scanné.
     * @throws BilletNotFoundException si le billet n'existe pas.
     * @throws BilletAlreadyScannedException si le billet a déjà été utilisé.
     */
    @Transactional
    public Billet verifierEtMarquerCommeScanne(String cleFinaleBillet) {
        Billet billet = billetQueryService.recupererBilletParCleFinale(cleFinaleBillet);

        if (billet.isScanned()) {
            throw new BilletAlreadyScannedException("Ce billet a déjà été scanné le " + billet.getScannedAt());
        }

        billet.setScanned(true);
        billet.setScannedAt(LocalDateTime.now());

        return billetRepository.save(billet);
    }
}