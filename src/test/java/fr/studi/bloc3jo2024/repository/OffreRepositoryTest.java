package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class OffreRepositoryTest {

    @Autowired
    private OffreRepository offreRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Offre offre;
    private Discipline discipline;

    @BeforeEach
    @Transactional
    void setUp() {
        // Créer et persister une Discipline
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

        discipline = Discipline.builder()
                .nomDiscipline("Natation")
                .dateDiscipline(LocalDateTime.now().plusDays(7))
                .nbPlaceDispo(100)
                .adresse(adresse)
                .build();
        entityManager.persist(discipline);
        entityManager.persist(discipline); // Persist Discipline avant de créer une Offre

        offre = Offre.builder()
                .typeOffre(TypeOffre.SOLO)
                .quantite(3)
                .prix(java.math.BigDecimal.valueOf(60.00))
                .capacite(1)
                .statutOffre(StatutOffre.DISPONIBLE)
                .discipline(discipline) // Associer l'Offre à la Discipline
                .build();
    }

    @Test
    @Transactional // Ajouter @Transactional pour gérer la persistance du test
    void testSaveOffre() {
        Offre savedOffre = offreRepository.save(offre);
        entityManager.flush();  // Force la sauvegarde immédiate
        entityManager.clear();  // Nettoie le contexte de persistance

        Offre retrievedOffre = offreRepository.findById(savedOffre.getIdOffre()).orElse(null); // Récupère depuis la base

        assertNotNull(retrievedOffre);
        assertNotNull(retrievedOffre.getIdOffre());
        assertEquals(TypeOffre.SOLO, retrievedOffre.getTypeOffre());
        assertEquals(3, retrievedOffre.getQuantite());
        assertEquals(0, java.math.BigDecimal.valueOf(60.00).compareTo(retrievedOffre.getPrix()));
        assertEquals(1, retrievedOffre.getCapacite());
        assertEquals(StatutOffre.DISPONIBLE, retrievedOffre.getStatutOffre());
        assertEquals(discipline.getIdDiscipline(), retrievedOffre.getDiscipline().getIdDiscipline()); // Vérifie l'association
    }
}