package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

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

        Adresse adresse = entityManager.persist(Adresse.builder().numeroRue(1).nomRue("Rue Transac").codePostal("75000").ville("Paris").pays(pays).build());

        Role role = entityManager.getEntityManager()
                .createQuery("SELECT r FROM Role r WHERE r.typeRole = :type", Role.class)
                .setParameter("type", TypeRole.USER)
                .getResultStream().findFirst().orElseGet(() -> entityManager.persist(Role.builder().typeRole(TypeRole.USER).build()));

        Utilisateur utilisateur = entityManager.persist(Utilisateur.builder()
                .email("transac_" + UUID.randomUUID().toString().substring(0,8) + "@example.com")
                .nom("NomTransac").prenom("PrenomTransac").dateNaissance(LocalDate.now().minusYears(30))
                .adresse(adresse).role(role).dateCreation(LocalDateTime.now()).isVerified(true).build());

        Panier panier = new Panier();
        panier.setMontantTotal(BigDecimal.valueOf(150.00));
        panier.setStatut(StatutPanier.EN_ATTENTE);
        panier.setUtilisateur(utilisateur);
        panier.setDateAjout(LocalDateTime.now());
        entityManager.persist(panier);

        paiementEntity = new Paiement();
        paiementEntity.setStatutPaiement(StatutPaiement.ACCEPTE);
        paiementEntity.setMethodePaiement(MethodePaiementEnum.CARTE_BANCAIRE);
        paiementEntity.setDatePaiement(LocalDateTime.now().minusMinutes(5));
        paiementEntity.setMontant(BigDecimal.valueOf(150.00));
        paiementEntity.setPanier(panier);
        paiementEntity.setUtilisateur(utilisateur);
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
        assertTrue(retrievedOpt.isPresent());
        Transaction retrievedTransaction = retrievedOpt.get();

        assertNotNull(retrievedTransaction.getIdTransaction());
        assertEquals(0, BigDecimal.valueOf(150.00).compareTo(retrievedTransaction.getMontant()));
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
        assertTrue(foundOptional.isPresent());
        Transaction foundTransaction = foundOptional.get();
        assertEquals(transaction.getIdTransaction(), foundTransaction.getIdTransaction());
        assertEquals(transaction.getDetails(), foundTransaction.getDetails());
    }
}