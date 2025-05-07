package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeRole;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BilletRepositoryTest {

    @Autowired
    private BilletRepository billetRepository;
    @Autowired
    private TestEntityManager entityManager;

    private Utilisateur createUser() {
        Pays pays = new Pays();
        pays.setNomPays("France");
        entityManager.persist(pays);

        Adresse adresse = Adresse.builder()
                .numeroRue(10)
                .nomRue("Rue Ticket")
                .ville("Nice")
                .codePostal("06000")
                .pays(pays)
                .build();
        entityManager.persist(adresse);

        Role role = Role.builder()
                .typeRole(TypeRole.USER)
                .build();
        entityManager.persist(role);

        Utilisateur user = Utilisateur.builder()
                .email("billet@jo.fr")
                .nom("Toto")
                .prenom("Ticket")
                .dateNaissance(LocalDate.of(1990, 2, 1))
                .role(role)
                .adresse(adresse)
                .build();
        entityManager.persist(user);
        return user;
    }

    private Discipline createDiscipline() {
        Pays pays = new Pays();
        pays.setNomPays("France");
        entityManager.persist(pays);

        Adresse adresse = Adresse.builder()
                .numeroRue(5)
                .nomRue("Rue des Athlètes")
                .ville("Paris")
                .codePostal("75001")
                .pays(pays)
                .build();
        entityManager.persist(adresse);

        Discipline discipline = Discipline.builder()
                .nomDiscipline("Natation")
                .dateDiscipline(LocalDateTime.now().plusDays(7))
                .nbPlaceDispo(100)
                .adresse(adresse)
                .build();
        entityManager.persist(discipline);
        return discipline;
    }

    private Offre createOffre() {
        Offre offre = new Offre();
        offre.setTypeOffre(TypeOffre.SOLO);
        offre.setPrix(java.math.BigDecimal.valueOf(60.00));
        offre.setDiscipline(createDiscipline());
        entityManager.persist(offre);
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