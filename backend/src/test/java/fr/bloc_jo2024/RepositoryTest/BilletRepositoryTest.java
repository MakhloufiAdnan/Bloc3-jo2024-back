package fr.bloc_jo2024.RepositoryTest;

import fr.bloc_jo2024.entity.*;
import fr.bloc_jo2024.entity.enums.TypeRole;
import fr.bloc_jo2024.repository.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager; // Import
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BilletRepositoryTest {

    @Autowired
    private BilletRepository billetRepository;
    @Autowired
    private TestEntityManager entityManager; // Inject

    private Utilisateur createUser() {
        Pays pays = new Pays();
        pays.setNom("France");
        entityManager.persist(pays); // Use entityManager

        Adresse adresse = Adresse.builder()
                .nomRue("Rue Ticket")
                .ville("Nice")
                .codePostal("06000")
                .pays(pays)
                .build();
        entityManager.persist(adresse); // Use entityManager

        Role role = Role.builder()
                .typeRole(TypeRole.USER)
                .build();
        entityManager.persist(role); // Use entityManager

        Utilisateur user = Utilisateur.builder()
                .email("billet@jo.fr")
                .nom("Toto")
                .prenom("Ticket")
                .dateNaissance(LocalDate.of(1990, 2, 1))
                .role(role)
                .adresse(adresse)
                .build();
        entityManager.persist(user); // Use entityManager
        return user;
    }

    private Offre createOffre() {
        Offre offre = new Offre();
        offre.setPrix(60.0);
        entityManager.persist(offre); // Use entityManager
        return offre;
    }

    @Test
    @Transactional
    void testBilletCreationAvecQRCode() {
        Utilisateur user = createUser();
        Offre offre = createOffre();

        Billet billet = new Billet();
        billet.setCleFinaleBillet("BILLET-UNIQUE-123456");
        billet.setQrCodeImage("fake-qrcode-image".getBytes());
        billet.setUtilisateur(user);
        billet.setOffre(offre);
        billetRepository.save(billet);

        assertThat(billetRepository.findById(billet.getIdBillet()))
                .isPresent()
                .hasValueSatisfying(savedBillet -> {
                    assertThat(savedBillet.getUtilisateur().getEmail()).isEqualTo("billet@jo.fr");
                    assertThat(savedBillet.getOffre().getIdOffre()).isEqualTo(offre.getIdOffre());
                    assertThat(savedBillet.getCleFinaleBillet()).isEqualTo("BILLET-UNIQUE-123456");
                    assertThat(savedBillet.getQrCodeImage()).isEqualTo("fake-qrcode-image".getBytes());
                });
    }
}