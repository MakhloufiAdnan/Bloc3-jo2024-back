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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BilletRepositoryTest {

    @Autowired
    private BilletRepository billetRepository;
    @Autowired
    private TestEntityManager entityManager;

    private Pays createFrance() {
        Pays pays = new Pays();
        pays.setNomPays("France");
        return entityManager.persist(pays);
    }

    private Adresse createAdresse(String nomRue, String ville, String codePostal, Pays pays) {
        Adresse adresse = Adresse.builder()
                .numeroRue(10)
                .nomRue(nomRue)
                .ville(ville)
                .codePostal(codePostal)
                .pays(pays)
                .build();
        return entityManager.persist(adresse);
    }

    private Utilisateur createUser(Adresse adresse, Role role) {
        Utilisateur user = Utilisateur.builder()
                .email("billet@jo.fr")
                .nom("Toto")
                .prenom("Ticket")
                .dateNaissance(LocalDate.of(1990, 2, 1))
                .role(role)
                .adresse(adresse)
                .build();
        return entityManager.persist(user);
    }

    private Discipline createDiscipline(Adresse adresse) {
        Discipline discipline = Discipline.builder()
                .nomDiscipline("Natation")
                .dateDiscipline(LocalDateTime.now().plusDays(7))
                .nbPlaceDispo(100)
                .adresse(adresse)
                .build();
        return entityManager.persist(discipline);
    }

    private Offre createOffre(Discipline discipline) {
        Offre offre = new Offre();
        offre.setTypeOffre(TypeOffre.SOLO);
        offre.setPrix(java.math.BigDecimal.valueOf(60.00));
        offre.setDiscipline(discipline);
        return entityManager.persist(offre);
    }

    private Role createUserRole() {
        Role role = Role.builder()
                .typeRole(TypeRole.USER)
                .build();
        return entityManager.persist(role);
    }

    @Test
    @Transactional
    void testBilletCreationAvecQRCode() {
        Pays france = createFrance();
        Adresse userAdresse = createAdresse("Rue Ticket", "Nice", "06000", france);
        Role userRole = createUserRole();
        Utilisateur user = createUser(userAdresse, userRole);

        Adresse disciplineAdresse = createAdresse("Rue des Athlètes", "Paris", "75001", france);
        Discipline discipline = createDiscipline(disciplineAdresse);
        Offre offre = createOffre(discipline);

        Billet billet = Billet.builder()
                .cleFinaleBillet("BILLET-UNIQUE-123456")
                .qrCodeImage("fake-qrcode-image".getBytes())
                .utilisateur(user)
                .offres(List.of(offre)) // Utilisation du Builder pour la liste d'offres
                .build();
        billetRepository.save(billet);

        assertThat(billetRepository.findById(billet.getIdBillet()))
                .isPresent()
                .hasValueSatisfying(savedBillet -> {
                    assertThat(savedBillet.getUtilisateur().getEmail()).isEqualTo("billet@jo.fr");
                    assertThat(savedBillet.getOffres())
                            .hasSize(1)
                            .anySatisfy(offreAssociee -> assertThat(offreAssociee.getIdOffre()).isEqualTo(offre.getIdOffre()));
                    assertThat(savedBillet.getCleFinaleBillet()).isEqualTo("BILLET-UNIQUE-123456");
                    assertThat(savedBillet.getQrCodeImage()).isEqualTo("fake-qrcode-image".getBytes());
                });
    }
}