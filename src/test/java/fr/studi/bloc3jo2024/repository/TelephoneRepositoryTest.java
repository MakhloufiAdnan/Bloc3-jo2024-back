package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Pays;
import fr.studi.bloc3jo2024.entity.Role;
import fr.studi.bloc3jo2024.entity.Telephone;
import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.entity.enums.TypeRole;
import fr.studi.bloc3jo2024.entity.enums.TypeTel;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
class TelephoneRepositoryTest {

    @Autowired
    private TelephoneRepository telephoneRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Role userRole;

    @BeforeEach
    void setUp() {
        // Vérifie si le rôle USER existe déjà, sinon le crée
        userRole = entityManager.getEntityManager() // Obtenez l'EntityManager JPA
                .createQuery("SELECT r FROM Role r WHERE r.typeRole = :type", Role.class)
                .setParameter("type", TypeRole.USER)
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    Role newRole = Role.builder().typeRole(TypeRole.USER).build();
                    entityManager.persist(newRole);
                    return newRole;
                });
    }

    private Utilisateur createUser(String email) {
        Pays pays = new Pays();
        pays.setNomPays("France");
        entityManager.persist(pays);

        Adresse adresse = Adresse.builder()
                .nomRue("rue test")
                .numeroRue(10)
                .ville("Paris")
                .codePostal("75000")
                .pays(pays)
                .build();
        entityManager.persist(adresse);

        Utilisateur user = Utilisateur.builder()
                .email(email)
                .nom("TestNom")
                .prenom("TestPrenom")
                .dateNaissance(LocalDate.of(1990, 1, 1))
                .role(userRole) // Utilise le rôle créé dans setUp
                .adresse(adresse)
                .build();
        entityManager.persist(user);
        return user;
    }

    @Test
    void testSaveTelephone() {
        Utilisateur user = createUser("save@jo.fr");
        Telephone telephone = Telephone.builder()
                .typeTel(TypeTel.MOBILE)
                .numeroTelephone("+33612345678")
                .utilisateur(user)
                .build();

        Telephone savedTelephone = telephoneRepository.save(telephone);
        assertThat(savedTelephone.getIdTelephone()).isNotNull();

        entityManager.flush();
        entityManager.clear();

        Telephone retrievedTelephone = entityManager.find(Telephone.class, savedTelephone.getIdTelephone());
        Optional<Telephone> retrievedTelephoneOptional = Optional.ofNullable(retrievedTelephone);

        assertThat(retrievedTelephoneOptional)
                .isPresent()
                .hasValueSatisfying(t -> {
                    assertThat(t.getNumeroTelephone()).isEqualTo("+33612345678");
                    assertThat(t.getTypeTel()).isEqualTo(TypeTel.MOBILE);
                    assertThat(t.getUtilisateur().getIdUtilisateur()).isEqualTo(user.getIdUtilisateur());
                });
    }

    @Test
    void testFindByNumeroTelephone() {
        Utilisateur user = createUser("findnum@jo.fr");
        Telephone telephone = Telephone.builder()
                .typeTel(TypeTel.MOBILE)
                .numeroTelephone("+33612345678")
                .utilisateur(user)
                .build();
        entityManager.persist(telephone);

        entityManager.flush();
        entityManager.clear();

        Optional<Telephone> foundTelephoneOptional = telephoneRepository.findByNumeroTelephone("+33612345678");
        assertThat(foundTelephoneOptional)
                .isPresent()
                .hasValueSatisfying(t -> {
                    assertThat(t.getNumeroTelephone()).isEqualTo("+33612345678");
                    assertThat(t.getTypeTel()).isEqualTo(TypeTel.MOBILE);
                    assertThat(t.getUtilisateur().getIdUtilisateur()).isEqualTo(user.getIdUtilisateur());
                });

        assertThat(telephoneRepository.findByNumeroTelephone("nonExistent")).isNotPresent();
    }

    @Test
    void testFindByUtilisateur_IdUtilisateur() {
        Utilisateur user1 = createUser("user1tel@jo.fr");
        Telephone tel1 = Telephone.builder()
                .typeTel(TypeTel.MOBILE)
                .numeroTelephone("+33611111111")
                .utilisateur(user1)
                .build();
        entityManager.persist(tel1);
        Telephone tel2 = Telephone.builder()
                .typeTel(TypeTel.FIXE)
                .numeroTelephone("0122222222")
                .utilisateur(user1)
                .build();
        entityManager.persist(tel2);

        Utilisateur user2 = createUser("user2tel@jo.fr");
        Telephone tel3 = Telephone.builder()
                .typeTel(TypeTel.MOBILE)
                .numeroTelephone("+33633333333")
                .utilisateur(user2)
                .build();
        entityManager.persist(tel3);

        List<Telephone> telephonesUser1 = telephoneRepository.findByUtilisateur_IdUtilisateur(user1.getIdUtilisateur());
        assertThat(telephonesUser1).hasSize(2);
        assertThat(telephonesUser1)
                .extracting(Telephone::getNumeroTelephone)
                .containsExactlyInAnyOrder("+33611111111", "0122222222");
        assertThat(telephonesUser1)
                .extracting(t -> t.getUtilisateur().getIdUtilisateur())
                .allSatisfy(id -> assertThat(id).isEqualTo(user1.getIdUtilisateur()));

        List<Telephone> telephonesUser2 = telephoneRepository.findByUtilisateur_IdUtilisateur(user2.getIdUtilisateur());
        assertThat(telephonesUser2).hasSize(1);
        assertThat(telephonesUser2.getFirst().getNumeroTelephone()).isEqualTo("+33633333333");
    }

    @Test
    void testFindByUtilisateur_IdUtilisateur_NoTelephones() {
        Utilisateur user = createUser("notel@jo.fr");
        List<Telephone> telephones = telephoneRepository.findByUtilisateur_IdUtilisateur(user.getIdUtilisateur());
        assertThat(telephones).isEmpty();
    }
}