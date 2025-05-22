package fr.studi.bloc3jo2024.integration;

import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.StatutPanier;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeRole;
import fr.studi.bloc3jo2024.repository.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PanierIntegrationTest {

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
    private EpreuveRepository epreuveRepository;

    @Autowired
    private ContenuPanierRepository contenuPanierRepository;

    @Autowired
    private DisciplineRepository disciplineRepository;

    @Test
    @Transactional
    void testPanierAvecOffresEtContenuPanier() {

        Pays france = Pays.builder().nomPays("France").build();
        paysRepository.save(france);

        Adresse adresse = Adresse.builder()
                .numeroRue(2)
                .nomRue("Rue de Lyon")
                .ville("Lyon")
                .codePostal("69000")
                .pays(france)
                .build();
        adresseRepository.save(adresse);

        Role role = roleRepository.findByTypeRole(TypeRole.USER)
                .orElseGet(() -> {
                    Role newUserRole = Role.builder().typeRole(TypeRole.USER).build();
                    return roleRepository.save(newUserRole);
                });

        Utilisateur user = Utilisateur.builder()
                .email("client@jo.fr")
                .nom("Client")
                .prenom("Pierre")
                .dateNaissance(LocalDate.of(1995, 5, 10))
                .adresse(adresse)
                .role(role)
                .build();
        user = utilisateurRepository.save(user);

        Epreuve epreuve = Epreuve.builder().nomEpreuve("100m").build();
        epreuveRepository.save(epreuve);

        // Correction : La date doit être dans le futur
        Discipline discipline = Discipline.builder()
                .nomDiscipline("Athlétisme")
                .dateDiscipline(LocalDateTime.now().plusDays(10)) // Date dans 10 jours
                .nbPlaceDispo(80000)
                .adresse(adresse)
                .build();
        discipline = disciplineRepository.save(discipline);

        Offre offre1 = Offre.builder()
                .prix(BigDecimal.valueOf(100.00))
                .quantite(1000)
                .capacite(1)
                .statutOffre(StatutOffre.DISPONIBLE)
                .typeOffre(TypeOffre.SOLO)
                .discipline(discipline)
                .build();
        offre1 = offreRepository.save(offre1);

        Offre offre2 = Offre.builder()
                .prix(BigDecimal.valueOf(70.00))
                .quantite(500)
                .capacite(2)
                .statutOffre(StatutOffre.DISPONIBLE)
                .typeOffre(TypeOffre.DUO)
                .discipline(discipline)
                .build();
        offre2 = offreRepository.save(offre2);

        Panier panier = Panier.builder()
                .statut(StatutPanier.EN_ATTENTE)
                .montantTotal(BigDecimal.valueOf(140.00))
                .utilisateur(user)
                .build();
        panier = panierRepository.save(panier);

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

        Panier fromDb = panierRepository.findById(panier.getIdPanier()).orElseThrow();
        assertThat(fromDb.getMontantTotal()).isEqualTo(BigDecimal.valueOf(140.00));
        assertThat(fromDb.getUtilisateur().getEmail()).isEqualTo("client@jo.fr");

        List<ContenuPanier> contenuPaniers = contenuPanierRepository.findAll();
        assertThat(contenuPaniers)
                .hasSize(2)
                .extracting(
                        c -> c.getOffre().getIdOffre(),
                        ContenuPanier::getQuantiteCommandee,
                        c -> c.getPanier().getIdPanier()
                )
                .containsExactlyInAnyOrder(
                        tuple(offre1.getIdOffre(), 1, panier.getIdPanier()),
                        tuple(offre2.getIdOffre(), 1, panier.getIdPanier())
                );
    }
}
