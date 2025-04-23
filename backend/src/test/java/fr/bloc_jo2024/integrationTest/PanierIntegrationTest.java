package fr.bloc_jo2024.integrationTest;

import fr.bloc_jo2024.entity.*;
import fr.bloc_jo2024.entity.enums.StatutOffre;
import fr.bloc_jo2024.entity.enums.StatutPanier;
import fr.bloc_jo2024.entity.enums.TypeOffre;
import fr.bloc_jo2024.entity.enums.TypeRole;
import fr.bloc_jo2024.repository.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PanierIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private AdresseRepository adresseRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PaysRepository paysRepository;

    @Autowired
    private PanierRepository panierRepository;

    @Autowired
    private OffreRepository offreRepository;

    @Autowired
    private ContenuPanierRepository contenuPanierRepository;

    @Autowired
    private EvenementRepository evenementRepository;

    @Autowired
    private EpreuveRepository epreuveRepository;

    @Autowired
    private ComporterRepository comporterRepository;

    @Test
    @Transactional
    void testPanierAvecOffresEtContenuPanier() {
        // Création Pays et Adresse (inchangé)
        Pays france = Pays.builder().nom("France").build();
        paysRepository.save(france);

        Adresse adresse = Adresse.builder()
                .numeroRue(2)
                .nomRue("Rue de Lyon")
                .ville("Lyon")
                .codePostal("69000")
                .pays(france)
                .build();
        adresseRepository.save(adresse);

        // Création du rôle et de l'utilisateur (inchangé)
        Role role = Role.builder().typeRole(TypeRole.USER).build();
        roleRepository.save(role);

        Utilisateur user = Utilisateur.builder()
                .email("client@jo.fr")
                .nom("Client")
                .prenom("Pierre")
                .dateNaissance(LocalDate.of(1995, 5, 10))
                .adresse(adresse)
                .role(role)
                .build();
        utilisateurRepository.save(user);

        // Création de l'épreuve
        Epreuve epreuve = Epreuve.builder().nomEpreuve("100m").build();
        epreuveRepository.save(epreuve);

        // Création de l'événement (instance spécifique de l'épreuve)
        Evenement evenement = Evenement.builder()
                .nomEvenement("Athletisme")
                .dateEvenement(LocalDateTime.of(2024, 8, 4, 15, 0))
                .nbPlaceDispo(80000)
                .adresse(adresse)
                .build();
        evenementRepository.save(evenement);

        // Création de la relation Comporter (lie l'épreuve à l'événement)
        Comporter comporter = Comporter.builder()
                .epreuve(epreuve)
                .evenement(evenement)
                .build();
        comporterRepository.save(comporter);

        // Création des offres (associées à l'événement)
        Offre offre1 = Offre.builder()
                .prix(50.0)
                .quantite(1000)
                .capacite(1)
                .statutOffre(StatutOffre.DISPONIBLE)
                .typeOffre(TypeOffre.SOLO)
                .evenement(evenement)
                .build();
        offreRepository.save(offre1);

        Offre offre2 = Offre.builder()
                .prix(90.0)
                .quantite(500)
                .capacite(2)
                .statutOffre(StatutOffre.DISPONIBLE)
                .typeOffre(TypeOffre.DUO)
                .evenement(evenement)
                .build();
        offreRepository.save(offre2);

        // Création du panier et ajout du contenu (inchangé)
        Panier panier = Panier.builder()
                .statut(StatutPanier.EN_ATTENTE)
                .montantTotal(140.0) // Mise à jour du montant total
                .utilisateur(user)
                .build();
        panierRepository.save(panier);

        ContenuPanier cp1 = ContenuPanier.builder()
                .offre(offre1)
                .quantiteCommandee(1)
                .panier(panier)
                .build();
        contenuPanierRepository.save(cp1);

        ContenuPanier cp2 = ContenuPanier.builder()
                .offre(offre2)
                .quantiteCommandee(1)
                .panier(panier)
                .build();
        contenuPanierRepository.save(cp2);

        panier.setContenuPaniers(Set.of(cp1, cp2));

        // Vérification (inchangé)
        Panier fromDb = panierRepository.findById(panier.getIdPanier()).orElseThrow();
        assertThat(fromDb.getMontantTotal()).isEqualTo(140.0);
        assertThat(fromDb.getContenuPaniers()).hasSize(2);
        assertThat(fromDb.getUtilisateur().getEmail()).isEqualTo("client@jo.fr");
    }
}