package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Paiement;
import fr.studi.bloc3jo2024.entity.Panier;
import fr.studi.bloc3jo2024.entity.Pays;
import fr.studi.bloc3jo2024.entity.Role;
import fr.studi.bloc3jo2024.entity.Transaction;
import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.entity.enums.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Paiement paiement;
    private Transaction transaction;

    @BeforeEach
    @Transactional
    void setUp() {
        // Création et persistance d'un pays
        Pays pays = Pays.builder()
                .nomPays("France")
                .build();
        entityManager.persist(pays);
        entityManager.flush();

        // Création et persistance d'une adresse
        Adresse adresse = new Adresse();
        adresse.setNumeroRue(1);
        adresse.setNomRue("Rue Test");
        adresse.setCodePostal("75000");
        adresse.setVille("Paris");
        adresse.setPays(pays);
        entityManager.persist(adresse);

        // Création et persistance d'un rôle
        Role role = new Role();
        role.setTypeRole(TypeRole.USER);
        entityManager.persist(role);

        // Création et persistance d'un utilisateur
        Utilisateur utilisateur = Utilisateur.builder()
                .email("test@example.com")
                .nom("NomTest")
                .prenom("PrenomTest")
                .dateNaissance(LocalDate.now().minusYears(30))
                .adresse(adresse)
                .role(role)
                .build();
        entityManager.persist(utilisateur);
        entityManager.flush();

        Panier panier;
        panier = new Panier();
        panier.setMontantTotal(BigDecimal.valueOf(150.00));
        panier.setStatut(StatutPanier.EN_ATTENTE);
        panier.setUtilisateur(utilisateur); // Associez l'utilisateur au panier
        entityManager.persist(panier);
        entityManager.flush();

        paiement = new Paiement();
        paiement.setStatutPaiement(StatutPaiement.ACCEPTE);
        paiement.setMethodePaiement(MethodePaiementEnum.CARTE_BANCAIRE);
        paiement.setDatePaiement(LocalDateTime.now().minusMinutes(5));
        paiement.setMontant(BigDecimal.valueOf(150.00));
        paiement.setPanier(panier);
        paiement.setUtilisateur(utilisateur); // Also set User here if paiement needs it
        entityManager.persist(paiement);
        entityManager.flush();

        transaction = new Transaction();
        transaction.setMontant(BigDecimal.valueOf(150.00));
        transaction.setStatutTransaction(StatutTransaction.REUSSI);
        transaction.setDateValidation(LocalDateTime.now());
        transaction.setDetails("{\"payment_intent\": \"pi_123\", \"status\": \"succeeded\"}");
        transaction.setTest(false);
        transaction.setPaiement(paiement);
        entityManager.persist(transaction); // Persist the transaction here
        entityManager.flush();
    }

    @Test
    @Transactional
    void testSaveTransaction() {
        Transaction savedTransaction = transactionRepository.save(transaction);
        entityManager.flush();
        entityManager.clear();

        Transaction retrievedTransaction = entityManager.find(Transaction.class, savedTransaction.getIdTransaction());

        assertNotNull(retrievedTransaction);
        assertNotNull(retrievedTransaction.getIdTransaction());
        assertEquals(BigDecimal.valueOf(150.00).stripTrailingZeros(), retrievedTransaction.getMontant().stripTrailingZeros());
        assertEquals(StatutTransaction.REUSSI, retrievedTransaction.getStatutTransaction());
        assertNotNull(retrievedTransaction.getDateValidation());
        assertEquals("{\"payment_intent\": \"pi_123\", \"status\": \"succeeded\"}", retrievedTransaction.getDetails());
        assertFalse(retrievedTransaction.isTest());
        assertNotNull(retrievedTransaction.getPaiement());
        assertEquals(paiement.getIdPaiement(), retrievedTransaction.getPaiement().getIdPaiement());
    }

    @Test
    @Transactional
    void testFindTransactionByPaiement() {
        transactionRepository.save(transaction);
        entityManager.flush();
        entityManager.clear();

        Transaction foundTransaction = transactionRepository.findByPaiement(paiement).orElse(null);

        assertNotNull(foundTransaction);
        assertEquals(transaction.getIdTransaction(), foundTransaction.getIdTransaction());
    }
}
