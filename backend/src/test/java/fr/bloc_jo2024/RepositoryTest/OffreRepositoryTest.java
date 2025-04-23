package fr.bloc_jo2024.RepositoryTest;

import fr.bloc_jo2024.entity.Offre;
import fr.bloc_jo2024.entity.enums.StatutOffre;
import fr.bloc_jo2024.entity.enums.TypeOffre;
import fr.bloc_jo2024.repository.OffreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class OffreRepositoryTest {

    @Autowired
    private OffreRepository offreRepository;

    private Offre offre;

    @BeforeEach
    void setUp() {
        offre = Offre.builder()
                .typeOffre(TypeOffre.SOLO)
                .quantite(3)
                .prix(100.0)
                .capacite(1)
                .statutOffre(StatutOffre.DISPONIBLE)
                .build();
    }

    @Test
    void testSaveOffre() {
        Offre savedOffre = offreRepository.save(offre);
        assertNotNull(savedOffre);
        assertNotNull(savedOffre.getIdOffre());
        assertEquals(TypeOffre.SOLO, savedOffre.getTypeOffre());
        assertEquals(3, savedOffre.getQuantite());
        assertEquals(100.0, savedOffre.getPrix());
        assertEquals(1, savedOffre.getCapacite());
        assertEquals(StatutOffre.DISPONIBLE, savedOffre.getStatutOffre());
    }
}