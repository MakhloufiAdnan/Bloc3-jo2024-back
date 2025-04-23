package fr.bloc_jo2024.RepositoryTest;

import fr.bloc_jo2024.entity.Telephone;
import fr.bloc_jo2024.entity.Utilisateur;
import fr.bloc_jo2024.entity.enums.TypeTel;
import fr.bloc_jo2024.repository.TelephoneRepository;
import fr.bloc_jo2024.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TelephoneRepositoryTest {

    @Autowired
    private TelephoneRepository telephoneRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    private Utilisateur utilisateur;
    private Telephone telephone;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur();
        utilisateur.setNom("John");
        utilisateur.setPrenom("Doe");
        utilisateurRepository.save(utilisateur);

        telephone = new Telephone();
        telephone.setTypeTel(TypeTel.MOBILE);
        telephone.setNumeroTelephone("+33612345678");
        telephone.setUtilisateur(utilisateur);
    }

    @Test
    void testSaveTelephone() {
        Telephone savedTelephone = telephoneRepository.save(telephone);
        assertNotNull(savedTelephone);
        assertNotNull(savedTelephone.getIdTelephone());
        assertEquals("+33612345678", savedTelephone.getNumeroTelephone());
        assertEquals(TypeTel.MOBILE, savedTelephone.getTypeTel()); // Add verification for enum
        assertEquals(utilisateur.getIdUtilisateur(), savedTelephone.getUtilisateur().getIdUtilisateur()); // Verify the relation
    }

    @Test
    void testFindByNumeroTelephone() {
        telephoneRepository.save(telephone);
        Telephone foundTelephone = telephoneRepository.findByNumeroTelephone("+33612345678");
        assertNotNull(foundTelephone);
        assertEquals("+33612345678", foundTelephone.getNumeroTelephone());
        assertEquals(TypeTel.MOBILE, foundTelephone.getTypeTel()); // Add verification for enum
        assertEquals(utilisateur.getIdUtilisateur(), foundTelephone.getUtilisateur().getIdUtilisateur()); // Verify the relation
    }
}