package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Paiement;
import fr.studi.bloc3jo2024.entity.Panier;
import fr.studi.bloc3jo2024.entity.Pays;
import fr.studi.bloc3jo2024.entity.Role;
import fr.studi.bloc3jo2024.entity.Transaction;
import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.entity.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PaiementRepositoryTest {

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private TestEntityManager entityManager;

    Panier panier;
    Paiement paiement;
    Transaction transaction;

    @BeforeEach
    void setUp() {
        // Création et persistance d'un pays
        Pays pays;
        pays = Pays.builder()
                .nomPays("France")
                .build();
        entityManager.persist(pays);
        entityManager.flush();

        // Création et persistance d'une adresse
        Adresse adresse;
        adresse = new Adresse();
        adresse.setNumeroRue(1);
        adresse.setNomRue("Rue Test");
        adresse.setCodePostal("75000");
        adresse.setVille("Paris");
        adresse.setPays(pays);
        entityManager.persist(adresse);

        // Création et persistance d'un rôle
        Role role;
        role = new Role();
        role.setTypeRole(TypeRole.USER);
        entityManager.persist(role);

        // Création et persistance d'un utilisateur
        Utilisateur utilisateur;
        utilisateur = Utilisateur.builder()
                .email("test@example.com")
                .nom("NomTest")
                .prenom("PrenomTest")
                .dateNaissance(LocalDate.now().minusYears(30))
                .adresse(adresse)
                .role(role)
                .build();
        entityManager.persist(utilisateur);
        entityManager.flush();

        panier = new Panier();
        panier.setMontantTotal(java.math.BigDecimal.valueOf(100.00));
        panier.setStatut(StatutPanier.EN_ATTENTE);
        panier.setUtilisateur(utilisateur);
        entityManager.persist(panier);
        entityManager.flush();

        paiement = new Paiement();
        paiement.setStatutPaiement(StatutPaiement.EN_ATTENTE);
        paiement.setMethodePaiement(MethodePaiementEnum.PAYPAL);
        paiement.setDatePaiement(java.time.LocalDateTime.now());
        paiement.setMontant(java.math.BigDecimal.valueOf(100.00));
        paiement.setPanier(panier);
        paiement.setUtilisateur(utilisateur); // Associer l'utilisateur au paiement !
        entityManager.persist(paiement);

        transaction = new Transaction();
        transaction.setMontant(java.math.BigDecimal.valueOf(100.00));
        transaction.setStatutTransaction(StatutTransaction.REUSSI);
        transaction.setDetails("{\"status\": \"COMPLETED\"}");
        transaction.setTest(true);
        transaction.setPaiement(paiement);

        entityManager.persist(transaction);
        entityManager.flush();
    }

    @Test
    void testSavePaiement() {
        Paiement savedPaiement = paiementRepository.findById(paiement.getIdPaiement()).orElse(null);
        assertNotNull(savedPaiement);
        assertNotNull(savedPaiement.getIdPaiement());
        assertEquals(java.math.BigDecimal.valueOf(100.0).stripTrailingZeros(), savedPaiement.getMontant().stripTrailingZeros());
        assertEquals(panier.getIdPanier(), savedPaiement.getPanier().getIdPanier());

        Transaction associatedTransaction = entityManager.find(Transaction.class, transaction.getIdTransaction());
        assertNotNull(associatedTransaction);
        if (transaction.getIdTransaction() != null) {
            assertEquals(transaction.getIdTransaction().toString(), associatedTransaction.getIdTransaction().toString());
        }
        assertEquals(StatutTransaction.REUSSI, associatedTransaction.getStatutTransaction());
        assertTrue(associatedTransaction.isTest());
    }
}
