package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.entity.Billet;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.repository.BilletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BilletService {

    private final BilletRepository billetRepository;
    private final QrCodeService qrCodeService;
    private final EmailService emailService;

    @Value("${app.email.sujet.billet}")
    private String sujetEmailBillet;

    @Value("${app.email.contenu.billet}")
    private String contenuEmailBillet;

    public String genererCleAchat() {
        return UUID.randomUUID().toString();
    }

    public String genererCleFinaleBillet(String cleUtilisateur, String cleAchat) {
        return cleUtilisateur + "-" + cleAchat;
    }

    @Transactional
    public Billet creerEtEnregistrerBillet(Utilisateur utilisateur, List<Offre> offres, String cleFinaleBillet) {
        Billet billet = Billet.builder()
                .utilisateur(utilisateur)
                .offres(offres)
                .cleFinaleBillet(cleFinaleBillet)
                .build();
        return billetRepository.save(billet);
    }

    @Transactional
    public Billet finaliserBilletAvecQrCode(Billet billet) {
        byte[] qrCodeImage = qrCodeService.generateQRCode(billet.getCleFinaleBillet());
        billet.setQrCodeImage(qrCodeImage);
        Billet billetSauvegarde = billetRepository.save(billet);

        // Envoyer l'e-mail après la sauvegarde du billet et la génération du QR code
        envoyerBilletParEmail(billetSauvegarde);
        return billetSauvegarde;
    }

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

    @Transactional(readOnly = true)
    public Optional<Billet> recupererBilletParId(Long id) {
        return billetRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Billet recupererBilletParCleFinale(String cleFinaleBillet) {
        return billetRepository.findByCleFinaleBillet(cleFinaleBillet)
                .orElseThrow(() -> new IllegalArgumentException("Billet non trouvé avec la clé : " + cleFinaleBillet));
    }
}