package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.*;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaiementRepositoryTest {

    @SuppressWarnings("resource")
    @Container
    static PostgreSQLContainer<?> postgresDBContainer = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("test_paiement_db_" + UUID.randomUUID().toString().substring(0,8))
            .withUsername("test_user_paiement")
            .withPassword("test_pass_paiement");

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
    private PaiementRepository paiementRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Panier panierEntity;
    private Utilisateur utilisateurEntity;

    @BeforeEach
    void setUp() {
        Pays pays = entityManager.getEntityManager()
                .createQuery("SELECT p FROM Pays p WHERE p.nomPays = :nom", Pays.class)
                .setParameter("nom", "France")
                .getResultStream().findFirst().orElseGet(() -> entityManager.persist(Pays.builder().nomPays("France").build()));

        Adresse adresse = entityManager.persist(Adresse.builder().numeroRue(1).nomRue("Rue Paiement").codePostal("75000").ville("Paris").pays(pays).build());

        Role role = entityManager.getEntityManager()
                .createQuery("SELECT r FROM Role r WHERE r.typeRole = :type", Role.class)
                .setParameter("type", TypeRole.USER)
                .getResultStream().findFirst().orElseGet(() -> entityManager.persist(Role.builder().typeRole(TypeRole.USER).build()));

        utilisateurEntity = entityManager.persist(Utilisateur.builder()
                .email("paiement_" + UUID.randomUUID().toString().substring(0,8) + "@example.com")
                .nom("NomPaiement").prenom("PrenomPaiement").dateNaissance(LocalDate.now().minusYears(30))
                .adresse(adresse).role(role).dateCreation(LocalDateTime.now()).isVerified(true).build());

        panierEntity = new Panier();
        panierEntity.setMontantTotal(BigDecimal.valueOf(100.00));
        panierEntity.setStatut(StatutPanier.EN_ATTENTE);
        panierEntity.setUtilisateur(utilisateurEntity);
        panierEntity.setDateAjout(LocalDateTime.now());
        entityManager.persist(panierEntity);
        entityManager.flush();
    }

    @Test
    void testSaveAndRetrievePaiement() {
        Paiement paiement = new Paiement();
        paiement.setStatutPaiement(StatutPaiement.EN_ATTENTE);
        paiement.setMethodePaiement(MethodePaiementEnum.PAYPAL);
        paiement.setDatePaiement(LocalDateTime.now());
        paiement.setMontant(BigDecimal.valueOf(100.00));
        paiement.setPanier(panierEntity);
        paiement.setUtilisateur(utilisateurEntity);

        Paiement savedPaiement = paiementRepository.save(paiement);
        entityManager.flush();

        Optional<Paiement> retrievedOpt = paiementRepository.findById(savedPaiement.getIdPaiement());
        assertTrue(retrievedOpt.isPresent(), "Le paiement sauvegardé devrait être récupérable.");
        Paiement retrievedPaiement = retrievedOpt.get();

        assertNotNull(retrievedPaiement.getIdPaiement());
        assertEquals(0, BigDecimal.valueOf(100.00).compareTo(retrievedPaiement.getMontant()), "Les montants devraient correspondre.");
        assertNotNull(retrievedPaiement.getPanier(), "Le panier associé ne devrait pas être null.");
        assertEquals(panierEntity.getIdPanier(), retrievedPaiement.getPanier().getIdPanier());
        assertNotNull(retrievedPaiement.getUtilisateur(), "L'utilisateur associé ne devrait pas être null.");
        assertEquals(utilisateurEntity.getIdUtilisateur(), retrievedPaiement.getUtilisateur().getIdUtilisateur());
        assertEquals(StatutPaiement.EN_ATTENTE, retrievedPaiement.getStatutPaiement());
        assertEquals(MethodePaiementEnum.PAYPAL, retrievedPaiement.getMethodePaiement());
    }
}