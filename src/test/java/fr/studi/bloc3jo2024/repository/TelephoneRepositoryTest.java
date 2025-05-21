package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.TypeRole;
import fr.studi.bloc3jo2024.entity.enums.TypeTel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TelephoneRepositoryTest {

    @Autowired
    private TelephoneRepository telephoneRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Role userRoleEntity;
    private Adresse userAdresse;

    @BeforeEach
    void setUp() {
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

        userAdresse = Adresse.builder()
                .nomRue("Rue du Telephone")
                .numeroRue(33)
                .ville("Lyon")
                .codePostal("69000")
                .pays(francePays)
                .build();
        entityManager.persist(userAdresse);
        entityManager.flush();
    }

    private Utilisateur createUser(String emailSuffix) {
        Utilisateur user = Utilisateur.builder()
                .email("teluser_" + emailSuffix + "@jo.fr")
                .nom("TelNom")
                .prenom("TelPrenom")
                .dateNaissance(LocalDate.of(1985, 3, 10))
                .role(userRoleEntity)
                .adresse(userAdresse)
                .dateCreation(LocalDateTime.now())
                .isVerified(true)
                .build();
        return entityManager.persistAndFlush(user);
    }

    @Test
    void testSaveTelephone() {
        Utilisateur user = createUser("save");
        Telephone telephone = Telephone.builder()
                .typeTel(TypeTel.MOBILE)
                .numeroTelephone("+33698765432")
                .utilisateur(user)
                .build();

        Telephone savedTelephone = telephoneRepository.save(telephone);
        assertThat(savedTelephone.getIdTelephone()).isNotNull();
        entityManager.flush();

        Optional<Telephone> retrievedTelephoneOptional = telephoneRepository.findById(savedTelephone.getIdTelephone());
        assertThat(retrievedTelephoneOptional)
                .isPresent()
                .hasValueSatisfying(t -> {
                    assertThat(t.getNumeroTelephone()).isEqualTo("+33698765432");
                    assertThat(t.getTypeTel()).isEqualTo(TypeTel.MOBILE);
                    assertThat(t.getUtilisateur().getIdUtilisateur()).isEqualTo(user.getIdUtilisateur());
                });
    }

    @Test
    void testFindByNumeroTelephone() {
        Utilisateur user = createUser("findnum");
        String numeroUnique = "+33600000001";
        Telephone telephone = Telephone.builder()
                .typeTel(TypeTel.MOBILE)
                .numeroTelephone(numeroUnique)
                .utilisateur(user)
                .build();
        entityManager.persistAndFlush(telephone);

        Optional<Telephone> foundTelephoneOptional = telephoneRepository.findByNumeroTelephone(numeroUnique);
        assertThat(foundTelephoneOptional)
                .isPresent()
                .hasValueSatisfying(t -> {
                    assertThat(t.getNumeroTelephone()).isEqualTo(numeroUnique);
                    assertThat(t.getUtilisateur().getIdUtilisateur()).isEqualTo(user.getIdUtilisateur());
                });

        assertThat(telephoneRepository.findByNumeroTelephone("nonExistentNumber")).isNotPresent();
    }

    @Test
    void testFindByUtilisateur_IdUtilisateur() {
        Utilisateur user1 = createUser("user1tel");
        Telephone tel1 = Telephone.builder().typeTel(TypeTel.MOBILE).numeroTelephone("+33611111111").utilisateur(user1).build();
        entityManager.persist(tel1);
        Telephone tel2 = Telephone.builder().typeTel(TypeTel.FIXE).numeroTelephone("0122222222").utilisateur(user1).build();
        entityManager.persist(tel2);

        Utilisateur user2 = createUser("user2tel");
        Telephone tel3 = Telephone.builder().typeTel(TypeTel.MOBILE).numeroTelephone("+33633333333").utilisateur(user2).build();
        entityManager.persist(tel3);
        entityManager.flush();

        List<Telephone> telephonesUser1 = telephoneRepository.findByUtilisateur_IdUtilisateur(user1.getIdUtilisateur());
        assertThat(telephonesUser1).hasSize(2)
                .extracting(Telephone::getNumeroTelephone)
                .containsExactlyInAnyOrder("+33611111111", "0122222222");

        List<Telephone> telephonesUser2 = telephoneRepository.findByUtilisateur_IdUtilisateur(user2.getIdUtilisateur());
        assertThat(telephonesUser2).hasSize(1);
        assertThat(telephonesUser2.getFirst().getNumeroTelephone()).isEqualTo("+33633333333");
    }

    @Test
    void testFindByUtilisateur_IdUtilisateur_NoTelephones() {
        Utilisateur user = createUser("notel");
        List<Telephone> telephones = telephoneRepository.findByUtilisateur_IdUtilisateur(user.getIdUtilisateur());
        assertThat(telephones).isEmpty();
    }
}