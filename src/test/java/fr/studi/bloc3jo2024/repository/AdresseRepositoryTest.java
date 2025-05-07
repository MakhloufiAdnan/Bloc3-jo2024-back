package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Pays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AdresseRepositoryTest {

    @Autowired
    private AdresseRepository adresseRepository;

    @Autowired
    private TestEntityManager entityManager; // Inject TestEntityManager

    private Pays pays;
    private Adresse adresse;

    @BeforeEach
    void setUp() {
        pays = new Pays();
        pays.setNomPays("France");
        entityManager.persist(pays); // Use entityManager to persist

        adresse = new Adresse();
        adresse.setNumeroRue(123);
        adresse.setNomRue("Rue Test");
        adresse.setVille("Paris");
        adresse.setCodePostal("75000");
        adresse.setPays(pays);
    }

    @Test
    void testSaveAdresse() {
        Adresse savedAdresse = adresseRepository.save(adresse);
        assertNotNull(savedAdresse);
        assertNotNull(savedAdresse.getIdAdresse());
        assertEquals(123, savedAdresse.getNumeroRue());
        assertEquals("Rue Test", savedAdresse.getNomRue());
        assertEquals("Paris", savedAdresse.getVille());
        assertEquals("75000", savedAdresse.getCodePostal());
        assertEquals(pays.getIdPays(), savedAdresse.getPays().getIdPays());
    }

    @Test
    void testFindById() {
        Adresse savedAdresse = adresseRepository.save(adresse);
        Adresse foundAdresse = adresseRepository.findById(savedAdresse.getIdAdresse()).orElse(null);
        assertNotNull(foundAdresse);
        assertEquals("Paris", foundAdresse.getVille());
        assertEquals(pays.getIdPays(), foundAdresse.getPays().getIdPays());
    }
}