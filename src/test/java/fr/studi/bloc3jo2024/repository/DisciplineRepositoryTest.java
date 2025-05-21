package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class DisciplineRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DisciplineRepository disciplineRepository;

    private Pays paysFrance;
    private Adresse adresseParis;
    private Adresse adresseLyon;
    private Epreuve epreuve1;
    private Epreuve epreuve2;

    @BeforeEach
    void setUp() {
        paysFrance = entityManager.persistFlushFind(Pays.builder().nomPays("France").build());
        adresseParis = entityManager.persistFlushFind(Adresse.builder().numeroRue(10).nomRue("Rue de Paris").ville("Paris").codePostal("75001").pays(paysFrance).build());
        adresseLyon = entityManager.persistFlushFind(Adresse.builder().numeroRue(20).nomRue("Rue de Lyon").ville("Lyon").codePostal("69001").pays(paysFrance).build());
        epreuve1 = entityManager.persistFlushFind(Epreuve.builder().nomEpreuve("Natation 100m").build());
        epreuve2 = entityManager.persistFlushFind(Epreuve.builder().nomEpreuve("Judo Individuel").build());

        LocalDateTime safeFutureDateBase = LocalDateTime.now().plusMinutes(5);

        Discipline disciplineFuture1 = Discipline.builder()
                .nomDiscipline("Natation")
                .dateDiscipline(safeFutureDateBase.plusDays(5))
                .nbPlaceDispo(100)
                .adresse(adresseParis)
                .comporters(new HashSet<>())
                .build();
        disciplineFuture1 = entityManager.persistAndFlush(disciplineFuture1);

        Discipline disciplineFuture2 = Discipline.builder()
                .nomDiscipline("Judo")
                .dateDiscipline(safeFutureDateBase.plusDays(15))
                .nbPlaceDispo(50)
                .adresse(adresseLyon)
                .comporters(new HashSet<>())
                .build();
        disciplineFuture2 = entityManager.persistAndFlush(disciplineFuture2);

        Discipline disciplineForPastScenarioTesting = Discipline.builder()
                .nomDiscipline("Escrime")
                .dateDiscipline(safeFutureDateBase.plusDays(1))
                .nbPlaceDispo(20)
                .adresse(adresseParis)
                .comporters(new HashSet<>())
                .build();
        entityManager.persistAndFlush(disciplineForPastScenarioTesting);

        Comporter comporter1 = Comporter.builder()
                .id(new ComporterKey(epreuve1.getIdEpreuve(), disciplineFuture1.getIdDiscipline()))
                .epreuve(epreuve1).discipline(disciplineFuture1).jrDeMedaille(true).build();
        entityManager.persistAndFlush(comporter1);

        Comporter comporter2 = Comporter.builder()
                .id(new ComporterKey(epreuve2.getIdEpreuve(), disciplineFuture2.getIdDiscipline()))
                .epreuve(epreuve2).discipline(disciplineFuture2).jrDeMedaille(false).build();
        entityManager.persistAndFlush(comporter2);
    }

    @Test
    void findByDateDisciplineAfter_shouldReturnFutureDisciplines() {
        LocalDateTime queryThresholdTime = LocalDateTime.now();
        List<Discipline> resultList = disciplineRepository.findByDateDisciplineAfter(queryThresholdTime);
        assertThat(resultList).isNotNull();
        assertThat(resultList)
                .hasSize(3)
                .extracting(Discipline::getNomDiscipline)
                .containsExactlyInAnyOrder("Natation", "Judo", "Escrime");
    }

    @Test
    void findDisciplinesByVille_shouldReturnMatchingDisciplines() {
        String ville = "Paris";
        List<Discipline> resultList = disciplineRepository.findDisciplinesByVille(ville);
        assertThat(resultList).isNotNull();
        assertThat(resultList)
                .hasSize(2)
                .extracting(Discipline::getNomDiscipline)
                .containsExactlyInAnyOrder("Natation", "Escrime");
    }
}