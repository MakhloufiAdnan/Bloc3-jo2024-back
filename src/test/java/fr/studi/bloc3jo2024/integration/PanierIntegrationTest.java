package fr.studi.bloc3jo2024.integration;

import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.StatutPanier;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeRole;
import fr.studi.bloc3jo2024.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Tests d'intégration pour la création et la récupération d'un Panier avec ses contenus.
 * Utilise Testcontainers pour une base de données PostgreSQL.
 * Chaque méthode de test est transactionnelle pour assurer l'isolation.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class PanierIntegrationTest {

    @Container
    @SuppressWarnings("resource") // Testcontainers gère le cycle de vie
    static PostgreSQLContainer<?> postgresDBContainer = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("test_db_panier_integ_" + UUID.randomUUID().toString().substring(0, 8))
            .withUsername("testuser_panier")
            .withPassword("testpass_panier");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresDBContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresDBContainer::getUsername);
        registry.add("spring.datasource.password", postgresDBContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop"); // Schéma recréé à chaque exécution de la suite de tests
    }

    // Injection des repositories nécessaires
    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private AdresseRepository adresseRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PaysRepository paysRepository;
    @Autowired private PanierRepository panierRepository;
    @Autowired private OffreRepository offreRepository;
    @Autowired private ContenuPanierRepository contenuPanierRepository;
    @Autowired private DisciplineRepository disciplineRepository;
    @Autowired private EntityManager entityManager; // Pour un contrôle fin du contexte de persistance

    // Entités de test réutilisables
    private Utilisateur utilisateurTest;
    private Discipline disciplineTest;
    private Offre offreSoloTest;
    private Offre offreDuoTest;
    private Panier panierTest;

    @BeforeEach
    void setUpDatabase() {
        // 1. Pays
        Pays france = paysRepository.findByNomPays("France_PanierTest_Setup").orElseGet(() ->
                paysRepository.saveAndFlush(Pays.builder().nomPays("France_PanierTest_Setup").build())
        );

        // 2. Adresse
        Adresse adresse = Adresse.builder()
                .numeroRue(15)
                .nomRue("Rue du Setup Panier")
                .ville("Setupville")
                .codePostal("12345")
                .pays(france)
                .build();
        adresse = adresseRepository.saveAndFlush(adresse);

        // 3. Rôle
        Role roleUser = roleRepository.findByTypeRole(TypeRole.USER).orElseGet(() -> {
            Role newRole = Role.builder().typeRole(TypeRole.USER).build();
            return roleRepository.saveAndFlush(newRole);
        });

        // 4. Utilisateur
        String uniqueEmail = "setup_user_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        Authentification auth = Authentification.builder().motPasseHache(UUID.randomUUID().toString()).build(); // Simuler un mot de passe haché

        utilisateurTest = Utilisateur.builder()
                .email(uniqueEmail)
                .nom("Utilisateur")
                .prenom("DeTest")
                .dateNaissance(LocalDate.of(1985, 5, 20))
                .adresse(adresse)
                .role(roleUser)
                .isVerified(true)
                .authentification(auth) // Associer l'authentification
                .build();
        auth.setUtilisateur(utilisateurTest); // Lier l'utilisateur à l'authentification (relation bidirectionnelle)
        utilisateurTest = utilisateurRepository.saveAndFlush(utilisateurTest); // Sauvegarder l'utilisateur (et l'auth par cascade si configuré)

        // 5. Discipline
        disciplineTest = Discipline.builder()
                .nomDiscipline("Test Discipline Panier Setup " + UUID.randomUUID().toString().substring(0, 6))
                .dateDiscipline(LocalDateTime.now().plusDays(20))
                .nbPlaceDispo(500)
                .adresse(adresse)
                .build();
        disciplineTest = disciplineRepository.saveAndFlush(disciplineTest);

        // 6. Offres
        offreSoloTest = Offre.builder()
                .prix(new BigDecimal("150.00"))
                .quantite(200)
                .capacite(1)
                .statutOffre(StatutOffre.DISPONIBLE)
                .typeOffre(TypeOffre.SOLO)
                .discipline(disciplineTest)
                .dateExpiration(LocalDateTime.now().plusDays(45))
                .build();
        offreSoloTest = offreRepository.saveAndFlush(offreSoloTest);

        offreDuoTest = Offre.builder()
                .prix(new BigDecimal("250.00"))
                .quantite(100)
                .capacite(2)
                .statutOffre(StatutOffre.DISPONIBLE)
                .typeOffre(TypeOffre.DUO)
                .discipline(disciplineTest)
                .dateExpiration(LocalDateTime.now().plusDays(45))
                .build();
        offreDuoTest = offreRepository.saveAndFlush(offreDuoTest);

        // 7. Panier
        panierTest = Panier.builder()
                .statut(StatutPanier.EN_ATTENTE)
                .montantTotal(BigDecimal.ZERO)
                .utilisateur(utilisateurTest)
                .contenuPaniers(new HashSet<>()) // Important pour éviter NullPointerException
                .build();
        panierTest = panierRepository.saveAndFlush(panierTest);
    }

    @Test
    void testPanierAvecOffresEtContenuPanier_creationCalculEtRecuperation() {
        // Arrange: Ajouter des contenus au panier de test
        ContenuPanier cpSolo = ContenuPanier.builder()
                .offre(offreSoloTest)
                .quantiteCommandee(1) // 1 offre SOLO
                .panier(panierTest)
                .build();
        contenuPanierRepository.saveAndFlush(cpSolo);

        ContenuPanier cpDuo = ContenuPanier.builder()
                .offre(offreDuoTest)
                .quantiteCommandee(2) // 2 offres DUO
                .panier(panierTest)
                .build();
        contenuPanierRepository.saveAndFlush(cpDuo);

        // Rafraîchir l'entité panierTest pour que sa collection contenuPaniers soit à jour
        // avec les ContenuPanier persistés ci-dessus.
        entityManager.refresh(panierTest);

        // Calculer manuellement le montant total attendu pour la vérification
        BigDecimal montantAttendu = BigDecimal.ZERO;
        montantAttendu = montantAttendu.add(offreSoloTest.getPrix().multiply(BigDecimal.valueOf(cpSolo.getQuantiteCommandee())));
        montantAttendu = montantAttendu.add(offreDuoTest.getPrix().multiply(BigDecimal.valueOf(cpDuo.getQuantiteCommandee())));

        // Simuler la mise à jour du montant total du panier (ce que ferait un service)
        panierTest.setMontantTotal(montantAttendu);
        panierRepository.saveAndFlush(panierTest);

        // Act: Récupérer le panier depuis la base de données.
        // Utiliser la méthode du repository qui charge les détails peut être une bonne option.
        // Ici, on va chercher par ID et s'appuyer sur @Transactional pour les collections.
        Panier panierFromDb = panierRepository.findById(panierTest.getIdPanier())
                .orElseThrow(() -> new AssertionError("Le panier avec l'ID " + panierTest.getIdPanier() + " n'a pas été trouvé."));

        // Assert: Vérifier les propriétés du panier récupéré
        assertThat(panierFromDb.getMontantTotal()).isEqualByComparingTo(montantAttendu);
        assertThat(panierFromDb.getUtilisateur().getIdUtilisateur()).isEqualTo(utilisateurTest.getIdUtilisateur());

        // Vérifier les contenus du panier.
        // La collection panierFromDb.getContenuPaniers() devrait être chargée grâce à @Transactional
        // et au refresh précédent.
        assertThat(panierFromDb.getContenuPaniers())
                .hasSize(2)
                .extracting(
                        item -> item.getOffre().getIdOffre(), // Extrait l'ID de l'offre
                        ContenuPanier::getQuantiteCommandee  // Extrait la quantité commandée
                )
                .containsExactlyInAnyOrder(
                        tuple(offreSoloTest.getIdOffre(), cpSolo.getQuantiteCommandee()),
                        tuple(offreDuoTest.getIdOffre(), cpDuo.getQuantiteCommandee())
                );
    }
}