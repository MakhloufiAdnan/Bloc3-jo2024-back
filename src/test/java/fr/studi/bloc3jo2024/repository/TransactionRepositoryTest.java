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
class TransactionRepositoryTest {

    @SuppressWarnings("resource")
    @Container
    static PostgreSQLContainer<?> postgresDBContainer = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("test_transac_db_" + UUID.randomUUID().toString().substring(0,8))
            .withUsername("test_user_transac")
            .withPassword("test_pass_transac");

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
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Paiement paiementEntity;

    @BeforeEach
    void setUp() {
        Pays pays = entityManager.getEntityManager()
                .createQuery("SELECT p FROM Pays p WHERE p.nomPays = :nom", Pays.class)
                .setParameter("nom", "France")
                .getResultStream().findFirst().orElseGet(() -> entityManager.persist(Pays.builder().nomPays("France").build()));
        entityManager.flush();

        Adresse userAdresse;
        userAdresse = entityManager.persist(Adresse.builder().numeroRue(1).nomRue("Rue Transac").codePostal("75000").ville("Paris").pays(pays).build());
        entityManager.flush();

        Role userRole;
        userRole = entityManager.getEntityManager()
                .createQuery("SELECT r FROM Role r WHERE r.typeRole = :type", Role.class)
                .setParameter("type", TypeRole.USER)
                .getResultStream().findFirst().orElseGet(() -> entityManager.persist(Role.builder().typeRole(TypeRole.USER).build()));
        entityManager.flush();

        Utilisateur testUtilisateur;
        testUtilisateur = entityManager.persist(Utilisateur.builder()
                .email("transac_" + UUID.randomUUID().toString().substring(0,8) + "@example.com")
                .nom("NomTransac").prenom("PrenomTransac").dateNaissance(LocalDate.now().minusYears(30))
                .adresse(userAdresse).role(userRole).dateCreation(LocalDateTime.now()).isVerified(true).build());
        entityManager.flush();

        Panier testPanier;
        testPanier = new Panier();
        testPanier.setMontantTotal(BigDecimal.valueOf(150.00));
        testPanier.setStatut(StatutPanier.EN_ATTENTE);
        testPanier.setUtilisateur(testUtilisateur);
        testPanier.setDateAjout(LocalDateTime.now());
        entityManager.persist(testPanier);
        entityManager.flush();

        paiementEntity = new Paiement();
        paiementEntity.setStatutPaiement(StatutPaiement.ACCEPTE);
        paiementEntity.setMethodePaiement(MethodePaiementEnum.CARTE_BANCAIRE);
        paiementEntity.setDatePaiement(LocalDateTime.now().minusMinutes(5));
        paiementEntity.setMontant(BigDecimal.valueOf(150.00));
        paiementEntity.setPanier(testPanier);
        paiementEntity.setUtilisateur(testUtilisateur);
        entityManager.persist(paiementEntity);
        entityManager.flush();
    }

    @Test
    void testSaveAndRetrieveTransaction() {
        Transaction transaction = new Transaction();
        transaction.setMontant(BigDecimal.valueOf(150.00));
        transaction.setStatutTransaction(StatutTransaction.REUSSI);
        transaction.setDateTransaction(LocalDateTime.now().minusMinutes(1));
        transaction.setDateValidation(LocalDateTime.now());
        transaction.setDetails("{\"payment_intent\": \"pi_save_test\", \"status\": \"succeeded\"}");
        transaction.setTest(false);
        transaction.setPaiement(paiementEntity);

        Transaction savedTransaction = transactionRepository.save(transaction);
        entityManager.flush();

        Optional<Transaction> retrievedOpt = transactionRepository.findById(savedTransaction.getIdTransaction());
        assertTrue(retrievedOpt.isPresent(), "La transaction sauvegardée devrait être récupérable.");
        Transaction retrievedTransaction = retrievedOpt.get();

        assertNotNull(retrievedTransaction.getIdTransaction());
        assertEquals(0, BigDecimal.valueOf(150.00).compareTo(retrievedTransaction.getMontant()), "Les montants devraient correspondre.");
        assertEquals(StatutTransaction.REUSSI, retrievedTransaction.getStatutTransaction());
        assertNotNull(retrievedTransaction.getDateValidation());
        assertEquals("{\"payment_intent\": \"pi_save_test\", \"status\": \"succeeded\"}", retrievedTransaction.getDetails());
        assertFalse(retrievedTransaction.isTest());
        assertNotNull(retrievedTransaction.getPaiement());
        assertEquals(paiementEntity.getIdPaiement(), retrievedTransaction.getPaiement().getIdPaiement());
    }

    @Test
    void testFindTransactionByPaiement() {
        Transaction transaction = new Transaction();
        transaction.setMontant(BigDecimal.valueOf(150.00));
        transaction.setStatutTransaction(StatutTransaction.REUSSI);
        transaction.setDateTransaction(LocalDateTime.now().minusMinutes(1));
        transaction.setDateValidation(LocalDateTime.now());
        transaction.setDetails("{\"payment_intent\": \"pi_find_test\", \"status\": \"succeeded\"}");
        transaction.setTest(false);
        transaction.setPaiement(paiementEntity);
        entityManager.persist(transaction);
        entityManager.flush();

        Optional<Transaction> foundOptional = transactionRepository.findByPaiement(paiementEntity);

        assertTrue(foundOptional.isPresent(), "Une transaction devrait être trouvée pour le paiement donné.");
        Transaction foundTransaction = foundOptional.get();
        assertEquals(transaction.getIdTransaction(), foundTransaction.getIdTransaction());
        assertEquals(transaction.getDetails(), foundTransaction.getDetails());
    }
}
