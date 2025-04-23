package fr.bloc_jo2024.integrationTest;

import fr.bloc_jo2024.entity.*;
import fr.bloc_jo2024.entity.enums.TypeRole;
import fr.bloc_jo2024.entity.enums.TypeTel;
import fr.bloc_jo2024.repository.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UtilisateurIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private AdresseRepository adresseRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthentificationRepository authentificationRepository;

    @Autowired
    private TelephoneRepository telephoneRepository;

    @Autowired
    private PaysRepository paysRepository;

    @Test
    @Transactional
    void testCreateUtilisateurAvecAuthentificationEtTelephone() {
        // 1. Créer un pays
        Pays france = Pays.builder()
                .nom("France")
                .build();
        paysRepository.save(france);

        // 2. Créer une adresse
        Adresse adresse = Adresse.builder()
                .numeroRue(1)
                .nomRue("Rue de Paris")
                .ville("Paris")
                .codePostal("75000")
                .pays(france)
                .build();
        adresseRepository.save(adresse);

        // 3. Créer un rôle
        Role role = Role.builder()
                .typeRole(TypeRole.USER)
                .build();
        roleRepository.save(role);

        // 4. Créer un utilisateur
        Utilisateur user = Utilisateur.builder()
                .email("test@example.com")
                .nom("Doe")
                .prenom("John")
                .dateNaissance(LocalDate.of(1990, 1, 1))
                .adresse(adresse)
                .role(role)
                .build();
        utilisateurRepository.save(user);

        // 5. Ajouter une authentification
        Authentification auth = Authentification.builder()
                .token(UUID.randomUUID().toString())
                .utilisateur(user)
                .build();
        auth.setMotPasse("monSuperMotDePasse123");
        authentificationRepository.save(auth);

        // 6. Ajouter un téléphone
        Telephone tel = Telephone.builder()
                .typeTel(TypeTel.MOBILE)
                .numeroTelephone("+33612345678")
                .utilisateur(user)
                .build();
        telephoneRepository.save(tel);

        // 7. Vérifications
        Utilisateur fromDb = utilisateurRepository.findByEmail("test@example.com").orElseThrow();

        assertThat(fromDb.getNom()).isEqualTo("Doe");
        assertThat(fromDb.getAuthentification().getToken()).isNotNull();
        assertThat(fromDb.getTelephones()).hasSize(1);
    }
}
