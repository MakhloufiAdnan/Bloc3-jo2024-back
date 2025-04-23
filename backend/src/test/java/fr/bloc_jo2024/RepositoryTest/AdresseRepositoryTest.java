package fr.bloc_jo2024.RepositoryTest;

import fr.bloc_jo2024.entity.Adresse;
import fr.bloc_jo2024.entity.Pays;
import fr.bloc_jo2024.repository.AdresseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager; // Import TestEntityManager
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
        pays.setNom("France");
        entityManager.persist(pays); // Use entityManager to persist

        adresse = new Adresse();
        adresse.setNomRue("123 Rue Test");
        adresse.setVille("Paris");
        adresse.setCodePostal("75000");
        adresse.setPays(pays);
    }

    @Test
    void testSaveAdresse() {
        Adresse savedAdresse = adresseRepository.save(adresse);
        assertNotNull(savedAdresse);
        assertNotNull(savedAdresse.getIdAdresse());
        assertEquals("123 Rue Test", savedAdresse.getNomRue());
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