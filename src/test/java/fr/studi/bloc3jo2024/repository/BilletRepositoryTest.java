package fr.studi.bloc3jo2024.repository;

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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BilletRepositoryTest {

    // L'annotation @Container avec un champ static assure que Testcontainers gère
    // le cycle de vie (start/stop) du conteneur pour toutes les méthodes de test de cette classe.
    // L'avertissement IDE sur "try-with-resources" peut être ignoré ici.
    @SuppressWarnings("resource") // Optionnel, pour supprimer l'avertissement IDE si persistant
    @Container
    static PostgreSQLContainer<?> postgresDBContainer = new PostgreSQLContainer<>("postgres:17-alpine3.21")
            .withDatabaseName("test_billet_db_" + UUID.randomUUID().toString().substring(0,8))
            .withUsername("test_user_billet")
            .withPassword("test_pass_billet");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresDBContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresDBContainer::getUsername);
        registry.add("spring.datasource.password", postgresDBContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.defer-datasource-initialization", () -> "true");
        registry.add("spring.sql.init.mode", () -> "always");
    }

    @Autowired
    private BilletRepository billetRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Utilisateur testUser;
    private Offre testOffre;


    @BeforeEach
    void setUpTestData() {

        Role userRoleEntity;
        userRoleEntity = entityManager.getEntityManager()
                .createQuery("SELECT r FROM Role r WHERE r.typeRole = :type", Role.class)
                .setParameter("type", TypeRole.USER)
                .getResultStream().findFirst()
                .orElseGet(() -> entityManager.persist(Role.builder().typeRole(TypeRole.USER).build()));

        Pays francePays;
        francePays = entityManager.getEntityManager()
                .createQuery("SELECT p FROM Pays p WHERE p.nomPays = :nom", Pays.class)
                .setParameter("nom", "France")
                .getResultStream().findFirst().orElseGet(() -> {
                    Pays newPays = new Pays();
                    newPays.setNomPays("France");
                    return entityManager.persist(newPays);
                });

        Adresse userAdresse;
        userAdresse = Adresse.builder()
                .numeroRue(10)
                .nomRue("Rue Ticket")
                .ville("Nice")
                .codePostal("06000")
                .pays(francePays)
                .build();
        entityManager.persist(userAdresse);

        testUser = Utilisateur.builder()
                .email("billet_" + UUID.randomUUID().toString().substring(0,8) + "@jo.fr")
                .nom("Toto")
                .prenom("Ticket")
                .dateNaissance(LocalDate.of(1990, 2, 1))
                .role(userRoleEntity)
                .adresse(userAdresse)
                .dateCreation(LocalDateTime.now())
                .isVerified(true)
                .build();
        entityManager.persist(testUser);

        Adresse disciplineAdresseEntity;
        disciplineAdresseEntity = Adresse.builder()
                .numeroRue(20)
                .nomRue("Rue des Athlètes")
                .ville("Paris")
                .codePostal("75001")
                .pays(francePays)
                .build();
        entityManager.persist(disciplineAdresseEntity);

        Discipline disciplineEntity;
        disciplineEntity = Discipline.builder()
                .nomDiscipline("Natation Test Billet")
                .dateDiscipline(LocalDateTime.now().plusDays(7))
                .nbPlaceDispo(100)
                .adresse(disciplineAdresseEntity)
                .build();
        entityManager.persist(disciplineEntity);

        // Créer Offre - Supposant que votre entité Offre a un constructeur ou des setters
        testOffre = new Offre(); // Si @Builder n'est pas disponible
        testOffre.setTypeOffre(TypeOffre.SOLO);
        testOffre.setPrix(java.math.BigDecimal.valueOf(60.00));
        testOffre.setQuantite(10);
        testOffre.setCapacite(1);
        testOffre.setStatutOffre(StatutOffre.DISPONIBLE);
        testOffre.setDiscipline(disciplineEntity);
        entityManager.persist(testOffre);
        entityManager.flush();
    }

    @Test
    void testBilletCreationAvecQRCode() {
        // Créer Billet - Supposant que votre entité Billet a un constructeur ou des setters
        Billet billet = new Billet(); // Si @Builder n'est pas disponible
        billet.setCleFinaleBillet("BILLET-UNIQUE-" + UUID.randomUUID());
        billet.setQrCodeImage("fake-qrcode-image-billet-test".getBytes());
        billet.setUtilisateur(testUser);
        billet.setOffres(List.of(testOffre));

        Billet savedBillet = billetRepository.save(billet);
        entityManager.flush();

        assertThat(billetRepository.findById(savedBillet.getIdBillet()))
                .isPresent()
                .hasValueSatisfying(retrievedBillet -> {
                    assertThat(retrievedBillet.getUtilisateur().getEmail()).isEqualTo(testUser.getEmail());
                    assertThat(retrievedBillet.getOffres())
                            .hasSize(1)
                            .anySatisfy(offreAssociee -> assertThat(offreAssociee.getIdOffre()).isEqualTo(testOffre.getIdOffre()));
                    assertThat(retrievedBillet.getCleFinaleBillet()).isEqualTo(billet.getCleFinaleBillet());
                    assertThat(retrievedBillet.getQrCodeImage()).isEqualTo("fake-qrcode-image-billet-test".getBytes());
                });
    }
}