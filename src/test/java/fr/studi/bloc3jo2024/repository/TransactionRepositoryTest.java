package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.integration.AbstractPostgresIntegrationTest;
import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TransactionRepositoryTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MethodePaiementRepository methodePaiementRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Paiement paiementEntity;

    /**
     * Nettoie les données et prépare les entités de base pour les tests.
     */
    @BeforeEach
    void setUp() {
        // --- Nettoyage des données ---
        entityManager.getEntityManager().createQuery("DELETE FROM Transaction").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Paiement").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Panier").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Utilisateur").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Adresse").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Pays").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Role").executeUpdate();
        entityManager.flush();

        // --- Préparation des données de base ---
        Pays pays = entityManager.getEntityManager()
                .createQuery("SELECT p FROM Pays p WHERE p.nomPays = :nom", Pays.class)
                .setParameter("nom", "France")
                .getResultStream().findFirst().orElseGet(() -> entityManager.persist(Pays.builder().nomPays("France").build()));

        Adresse adresse = entityManager.persist(Adresse.builder().numeroRue(1).nomRue("Rue Transac").codePostal("75000").ville("Paris").pays(pays).build());

        Role role = entityManager.getEntityManager()
                .createQuery("SELECT r FROM Role r WHERE r.typeRole = :type", Role.class)
                .setParameter("type", TypeRole.USER)
                .getResultStream().findFirst().orElseGet(() -> entityManager.persist(Role.builder().typeRole(TypeRole.USER).build()));

        Utilisateur utilisateurEntity;
        utilisateurEntity = entityManager.persist(Utilisateur.builder()
                .email("transac_" + UUID.randomUUID().toString().substring(0,8) + "@example.com")
                .nom("NomTransac").prenom("PrenomTransac").dateNaissance(LocalDate.now().minusYears(30))
                .adresse(adresse).role(role).dateCreation(LocalDateTime.now()).isVerified(true).build());

        Panier panierEntity;
        panierEntity = new Panier();
        panierEntity.setMontantTotal(BigDecimal.valueOf(150.00));
        panierEntity.setStatut(StatutPanier.EN_ATTENTE);
        panierEntity.setUtilisateur(utilisateurEntity);
        panierEntity.setDateAjout(LocalDateTime.now());
        entityManager.persist(panierEntity);

        MethodePaiement carteBancaireMethode;
        carteBancaireMethode = methodePaiementRepository.findByNomMethodePaiement(MethodePaiementEnum.CARTE_BANCAIRE)
                .orElseGet(() -> methodePaiementRepository.saveAndFlush(
                        new MethodePaiement(null, MethodePaiementEnum.CARTE_BANCAIRE)));

        paiementEntity = new Paiement();
        paiementEntity.setStatutPaiement(StatutPaiement.ACCEPTE);
        paiementEntity.setMethodePaiement(carteBancaireMethode);
        paiementEntity.setDatePaiement(LocalDateTime.now().minusMinutes(5));
        paiementEntity.setMontant(BigDecimal.valueOf(150.00));
        paiementEntity.setPanier(panierEntity);
        paiementEntity.setUtilisateur(utilisateurEntity);
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