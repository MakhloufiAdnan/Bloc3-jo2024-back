package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.integration.AbstractPostgresIntegrationTest;
import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BilletRepositoryTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private BilletRepository billetRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Utilisateur testUser;
    private Offre testOffre;
    private Discipline testDiscipline;
    private Role testRole;
    private Adresse testAdresse;
    private Pays testPays;

    /**
     * Prépare les données de test avant chaque exécution.
     * Le nettoyage est effectué au début pour garantir un environnement propre.
     */
    @BeforeEach
    void setUpTestData() {
        // --- Nettoyage des données dans l'ordre inverse des dépendances pour éviter les erreurs de contraintes ---
        // 1. Nettoyer la table de jointure en premier
        entityManager.getEntityManager().createNativeQuery("DELETE FROM billet_offre").executeUpdate();
        // 2. Nettoyer les entités principales
        entityManager.getEntityManager().createQuery("DELETE FROM Billet").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Offre").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Discipline").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Utilisateur").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Adresse").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Pays").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Role").executeUpdate();
        entityManager.flush();

        // --- Création des données de test ---
        this.createBaseEntities();

        testUser = Utilisateur.builder()
                .email("billet_" + UUID.randomUUID().toString().substring(0, 8) + "@jo.fr")
                .nom("Toto")
                .prenom("Ticket")
                .dateNaissance(LocalDate.of(1990, 2, 1))
                .role(testRole)
                .adresse(testAdresse)
                .dateCreation(LocalDateTime.now())
                .isVerified(true)
                .build();
        entityManager.persist(testUser);

        testOffre = Offre.builder()
                .typeOffre(TypeOffre.SOLO)
                .prix(java.math.BigDecimal.valueOf(60.00))
                .quantite(10)
                .capacite(1)
                .statutOffre(StatutOffre.DISPONIBLE)
                .discipline(testDiscipline)
                .billets(new ArrayList<>()) // Initialisation de la liste
                .build();
        entityManager.persist(testOffre);

        entityManager.flush();
    }

    /**
     * Teste la création d'un billet, son enregistrement en base de données,
     * et la vérification de ses propriétés et relations.
     */
    @Test
    void testBilletCreationAvecQRCode() {
        // Arrange
        // On n'utilise PAS le builder pour être 100% sûr que Lombok n'est pas en cause.
        Billet billet = new Billet();
        billet.setCleFinaleBillet("BILLET-UNIQUE-" + UUID.randomUUID());
        billet.setQrCodeImage("fake-qrcode-image-billet-test".getBytes());
        billet.setUtilisateur(testUser);
        billet.setOffres(List.of(testOffre));

        // On assure la cohérence de la relation bidirectionnelle
        testOffre.getBillets().add(billet);

        // --- LIGNE DE DÉBOGAGE CRUCIALE ---
        // Cette ligne va nous dire le type exact de l'objet dans le champ qrCodeImage.
        // On s'attend à voir "[B" dans la console, ce qui signifie "tableau de bytes".
        System.out.println(">>>> Type du champ qrCodeImage avant la sauvegarde : " + billet.getQrCodeImage().getClass().getName());

        // Act
        Billet savedBillet = billetRepository.save(billet);
        entityManager.flush();

        // Assert
        assertThat(billetRepository.findById(savedBillet.getIdBillet()))
                .isPresent()
                .hasValueSatisfying(retrievedBillet -> {
                    assertThat(retrievedBillet.getUtilisateur().getEmail()).isEqualTo(testUser.getEmail());
                    assertThat(retrievedBillet.getOffres())
                            .hasSize(1)
                            .first()
                            .usingRecursiveComparison()
                            .isEqualTo(testOffre);
                    assertThat(retrievedBillet.getCleFinaleBillet()).isEqualTo(billet.getCleFinaleBillet());
                    assertThat(retrievedBillet.getQrCodeImage()).isEqualTo("fake-qrcode-image-billet-test".getBytes());
                });
    }

    /**
     * Méthode utilitaire pour créer et persister les entités de base
     * nécessaires pour les tests (Pays, Role, Adresse, Discipline).
     */
    private void createBaseEntities() {
        testPays = entityManager.persist(Pays.builder().nomPays("France").build());
        testRole = entityManager.persist(Role.builder().typeRole(TypeRole.USER).build());

        testAdresse = entityManager.persist(Adresse.builder()
                .numeroRue(10)
                .nomRue("Rue Ticket")
                .ville("Nice")
                .codePostal("06000")
                .pays(testPays)
                .build());

        Adresse disciplineAdresse = entityManager.persist(Adresse.builder()
                .numeroRue(20)
                .nomRue("Rue des Athlètes")
                .ville("Paris")
                .codePostal("75001")
                .pays(testPays)
                .build());

        testDiscipline = entityManager.persist(Discipline.builder()
                .nomDiscipline("Natation Test Billet")
                .dateDiscipline(LocalDateTime.now().plusDays(7))
                .nbPlaceDispo(100)
                .adresse(disciplineAdresse)
                .build());
    }
}