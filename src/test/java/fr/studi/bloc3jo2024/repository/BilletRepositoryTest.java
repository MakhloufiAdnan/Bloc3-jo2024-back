package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.integration.AbstractPostgresIntegrationTest;
import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BilletRepositoryTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private BilletRepository billetRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Utilisateur testUser;
    private Offre testOffre;
    @BeforeEach
    void setUpTestData() {
        // --- Nettoyage des données pour garantir l'isolation des tests ---
        entityManager.getEntityManager().createNativeQuery("DELETE FROM billet_offre").executeUpdate(); // FIX: Utilisation d'une requête SQL native
        entityManager.getEntityManager().createQuery("DELETE FROM Billet").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Offre").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Discipline").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Utilisateur").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Adresse").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Pays").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Role").executeUpdate();
        entityManager.flush(); // Assure que les suppressions sont commitées

        Role userRoleEntity;
        userRoleEntity = entityManager.getEntityManager()
                .createQuery("SELECT r FROM Role r WHERE r.typeRole = :type", Role.class)
                .setParameter("type", TypeRole.USER)
                .getResultStream().findFirst()
                .orElseGet(() -> entityManager.persist(Role.builder().typeRole(TypeRole.USER).build()));

        Pays francePays;
        francePays = entityManager.getEntityManager()
                .createQuery("SELECT p FROM Pays p WHERE p.nomPays = :nom", Pays.class)
                .setParameter("nom", "France")
                .getResultStream().findFirst().orElseGet(() -> {
                    Pays newPays = new Pays();
                    newPays.setNomPays("France");
                    return entityManager.persist(newPays);
                });

        Adresse userAdresse;
        userAdresse = Adresse.builder()
                .numeroRue(10)
                .nomRue("Rue Ticket")
                .ville("Nice")
                .codePostal("06000")
                .pays(francePays)
                .build();
        entityManager.persist(userAdresse);

        testUser = Utilisateur.builder()
                .email("billet_" + UUID.randomUUID().toString().substring(0,8) + "@jo.fr")
                .nom("Toto")
                .prenom("Ticket")
                .dateNaissance(LocalDate.of(1990, 2, 1))
                .role(userRoleEntity)
                .adresse(userAdresse)
                .dateCreation(LocalDateTime.now())
                .isVerified(true)
                .build();
        entityManager.persist(testUser);

        Adresse disciplineAdresseEntity;
        disciplineAdresseEntity = Adresse.builder()
                .numeroRue(20)
                .nomRue("Rue des Athlètes")
                .ville("Paris")
                .codePostal("75001")
                .pays(francePays)
                .build();
        entityManager.persist(disciplineAdresseEntity);

        Discipline disciplineEntity;
        disciplineEntity = Discipline.builder()
                .nomDiscipline("Natation Test Billet")
                .dateDiscipline(LocalDateTime.now().plusDays(7))
                .nbPlaceDispo(100)
                .adresse(disciplineAdresseEntity)
                .build();
        entityManager.persist(disciplineEntity);

        // Créer Offre
        testOffre = new Offre();
        testOffre.setTypeOffre(TypeOffre.SOLO);
        testOffre.setPrix(java.math.BigDecimal.valueOf(60.00));
        testOffre.setQuantite(10);
        testOffre.setCapacite(1);
        testOffre.setStatutOffre(StatutOffre.DISPONIBLE);
        testOffre.setDiscipline(disciplineEntity);
        entityManager.persist(testOffre);
        entityManager.flush();
    }

    @Test
    void testBilletCreationAvecQRCode() {
        Billet billet = new Billet();
        billet.setCleFinaleBillet("BILLET-UNIQUE-" + UUID.randomUUID());
        billet.setQrCodeImage("fake-qrcode-image-billet-test".getBytes());
        billet.setUtilisateur(testUser);
        billet.setOffres(List.of(testOffre));

        Billet savedBillet = billetRepository.save(billet);
        entityManager.flush();

        assertThat(billetRepository.findById(savedBillet.getIdBillet()))
                .isPresent()
                .hasValueSatisfying(retrievedBillet -> {
                    assertThat(retrievedBillet.getUtilisateur().getEmail()).isEqualTo(testUser.getEmail());
                    assertThat(retrievedBillet.getOffres())
                            .hasSize(1)
                            .anySatisfy(offreAssociee -> assertThat(offreAssociee.getIdOffre()).isEqualTo(testOffre.getIdOffre()));
                    assertThat(retrievedBillet.getCleFinaleBillet()).isEqualTo(billet.getCleFinaleBillet());
                    assertThat(retrievedBillet.getQrCodeImage()).isEqualTo("fake-qrcode-image-billet-test".getBytes());
                });
    }
}