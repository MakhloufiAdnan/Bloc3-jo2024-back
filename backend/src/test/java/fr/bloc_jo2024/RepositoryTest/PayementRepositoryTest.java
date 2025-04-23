package fr.bloc_jo2024.RepositoryTest;

import fr.bloc_jo2024.entity.Payement;
import fr.bloc_jo2024.entity.Panier;
import fr.bloc_jo2024.repository.PayementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager; // Import
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PayementRepositoryTest {

    @Autowired
    private PayementRepository payementRepository;

    @Autowired
    private TestEntityManager entityManager; // Inject

    private Panier panier;
    private Payement payement;

    @BeforeEach
    void setUp() {
        panier = new Panier();
        panier.setMontantTotal(100.0);
        entityManager.persist(panier); // Use entityManager

        payement = new Payement();
        payement.setMontantPaye(100.0);
        payement.setPanier(panier);
        payement.setTransactionId("TRN123");
        payement.setPaiementReussi(true);
    }

    @Test
    void testSavePayement() {
        Payement savedPayement = payementRepository.save(payement);
        assertNotNull(savedPayement);
        assertNotNull(savedPayement.getIdPayement());
        assertEquals(100.0, savedPayement.getMontantPaye());
        assertEquals(panier.getIdPanier(), savedPayement.getPanier().getIdPanier());
        assertEquals("TRN123", savedPayement.getTransactionId());
        assertTrue(savedPayement.isPaiementReussi());
    }
}