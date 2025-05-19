package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.dto.disciplines.CreerDisciplineDto;
import fr.studi.bloc3jo2024.dto.disciplines.MettreAJourDisciplineDto;
import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Epreuve;
import fr.studi.bloc3jo2024.repository.AdresseRepository;
import fr.studi.bloc3jo2024.repository.DisciplineRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisciplineServiceTest {

    @Mock
    private DisciplineRepository disciplineRepository;

    @Mock
    private AdresseRepository adresseRepository;

    @Mock
    private EpreuveService epreuveService;

    @InjectMocks
    @Spy // Pour espionner les appels internes comme self.getDisciplineOrThrow
    private DisciplineService disciplineService;


    private Adresse adresse;
    private Discipline discipline;
    private CreerDisciplineDto creerDisciplineDto;
    private MettreAJourDisciplineDto mettreAJourDisciplineDto;
    private Epreuve epreuveEnVedette;

    @BeforeEach
    void setUp() {
        adresse = new Adresse(1L, 10, "Rue Test", "TestVille", "75000", null, null, null);
        // Discipline de base pour les tests où une discipline existante est nécessaire
        discipline = new Discipline(1L, "Test Discipline", LocalDateTime.now().plusDays(1), 100, false, 0L, adresse, Collections.emptySet(), Collections.emptySet());

        creerDisciplineDto = new CreerDisciplineDto();
        creerDisciplineDto.setNomDiscipline("Nouvelle Discipline");
        creerDisciplineDto.setDateDiscipline(LocalDateTime.now().plusDays(5));
        creerDisciplineDto.setNbPlaceDispo(50);
        creerDisciplineDto.setIdAdresse(1L);

        mettreAJourDisciplineDto = new MettreAJourDisciplineDto();
        mettreAJourDisciplineDto.setIdDiscipline(1L); // ID de la discipline à mettre à jour
        mettreAJourDisciplineDto.setNomDiscipline("Discipline Mise à Jour");
        mettreAJourDisciplineDto.setDateDiscipline(LocalDateTime.now().plusDays(10));
        mettreAJourDisciplineDto.setNbPlaceDispo(75);
        mettreAJourDisciplineDto.setIdAdresse(1L); // ID de l'adresse pour la mise à jour

        epreuveEnVedette = Epreuve.builder()
                .idEpreuve(101L)
                .nomEpreuve("Epreuve Vedette Test")
                .isFeatured(true)
                .build();

        lenient().doCallRealMethod().when(disciplineService).getDisciplineOrThrow(anyLong());
    }

    @Test
    void creerDiscipline_shouldReturnSavedDiscipline_whenAdresseExists() {
        when(adresseRepository.findById(1L)).thenReturn(Optional.of(adresse));
        when(disciplineRepository.save(any(Discipline.class))).thenAnswer(invocation -> {
            Discipline d = invocation.getArgument(0);
            d.setIdDiscipline(2L); // Simule la génération d'ID
            return d;
        });

        Discipline result = disciplineService.creerDiscipline(creerDisciplineDto);

        assertThat(result).isNotNull();
        assertThat(result.getNomDiscipline()).isEqualTo(creerDisciplineDto.getNomDiscipline());
        assertThat(result.getIdDiscipline()).isEqualTo(2L);
        verify(adresseRepository).findById(1L);
        verify(disciplineRepository).save(any(Discipline.class));
    }

    @Test
    void creerDiscipline_shouldThrowEntityNotFoundException_whenAdresseDoesNotExist() {
        when(adresseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> disciplineService.creerDiscipline(creerDisciplineDto));
        verify(adresseRepository).findById(1L);
        verify(disciplineRepository, never()).save(any(Discipline.class));
    }

    @Test
    void mettreAJourDiscipline_shouldReturnUpdatedDiscipline_whenExists() {
        when(disciplineRepository.findById(1L)).thenReturn(Optional.of(discipline)); // Mock pour getDisciplineOrThrow
        when(adresseRepository.findById(mettreAJourDisciplineDto.getIdAdresse())).thenReturn(Optional.of(adresse));
        when(disciplineRepository.save(any(Discipline.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Retourne l'entité modifiée

        Discipline result = disciplineService.mettreAJourDiscipline(mettreAJourDisciplineDto);

        assertThat(result).isNotNull();
        assertThat(result.getNomDiscipline()).isEqualTo(mettreAJourDisciplineDto.getNomDiscipline());
        assertThat(result.getNbPlaceDispo()).isEqualTo(mettreAJourDisciplineDto.getNbPlaceDispo());
        verify(disciplineService).getDisciplineOrThrow(1L); // Vérifie l'appel interne espionné
        verify(disciplineRepository).findById(1L); // Vérifie l'appel fait par la vraie méthode getDisciplineOrThrow
        verify(adresseRepository).findById(mettreAJourDisciplineDto.getIdAdresse());
        verify(disciplineRepository).save(any(Discipline.class));
    }

    @Test
    void mettreAJourDiscipline_shouldThrowEntityNotFoundException_whenDisciplineDoesNotExist() {
        Long nonExistentId = 99L;
        mettreAJourDisciplineDto.setIdDiscipline(nonExistentId); // Cibler un ID non existant
        when(disciplineRepository.findById(nonExistentId)).thenReturn(Optional.empty()); // Mock pour getDisciplineOrThrow

        assertThrows(EntityNotFoundException.class, () -> disciplineService.mettreAJourDiscipline(mettreAJourDisciplineDto));
        verify(disciplineService).getDisciplineOrThrow(nonExistentId);
        verify(disciplineRepository).findById(nonExistentId);
        verify(adresseRepository, never()).findById(anyLong());
        verify(disciplineRepository, never()).save(any(Discipline.class));
    }

    @Test
    void supprimerDiscipline_shouldDeleteDiscipline_whenExists() {
        when(disciplineRepository.existsById(1L)).thenReturn(true);
        doNothing().when(disciplineRepository).deleteById(1L);

        disciplineService.supprimerDiscipline(1L);

        verify(disciplineRepository).existsById(1L);
        verify(disciplineRepository).deleteById(1L);
    }

    @Test
    void supprimerDiscipline_shouldThrowEntityNotFoundException_whenNotExists() {
        when(disciplineRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> disciplineService.supprimerDiscipline(1L));
        verify(disciplineRepository).existsById(1L);
        verify(disciplineRepository, never()).deleteById(anyLong());
    }


    @Test
    void retirerPlaces_shouldReturnUpdatedDiscipline_whenSuccessful() {
        int nbToRetire = 10;
        discipline.setNbPlaceDispo(50); // Assurer assez de places

        when(disciplineRepository.findById(1L)).thenReturn(Optional.of(discipline)); // Mock pour getDisciplineOrThrow
        when(disciplineRepository.decrementerPlaces(1L, nbToRetire)).thenReturn(1); // Simule succès

        Discipline result = disciplineService.retirerPlaces(1L, nbToRetire);

        assertThat(result).isNotNull();
        verify(disciplineService, times(2)).getDisciplineOrThrow(1L); // Une fois au début, une fois à la fin
        verify(disciplineRepository, times(2)).findById(1L); // Suite aux appels de getDisciplineOrThrow
        verify(disciplineRepository).decrementerPlaces(1L, nbToRetire);
    }

    @Test
    void retirerPlaces_shouldThrowIllegalStateException_whenNotEnoughPlaces() {
        int nbToRetire = 10;
        discipline.setNbPlaceDispo(5); // Pas assez de places

        when(disciplineRepository.findById(1L)).thenReturn(Optional.of(discipline)); // Mock pour getDisciplineOrThrow
        when(disciplineRepository.decrementerPlaces(1L, nbToRetire)).thenReturn(0); // Simule échec

        assertThrows(IllegalStateException.class, () -> disciplineService.retirerPlaces(1L, nbToRetire));
        verify(disciplineService).getDisciplineOrThrow(1L); // Appel initial
        verify(disciplineRepository).findById(1L);
        verify(disciplineRepository).decrementerPlaces(1L, nbToRetire);
    }

    @Test
    void retirerPlaces_shouldThrowIllegalArgumentException_whenNbIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> disciplineService.retirerPlaces(1L, -5));
        verify(disciplineRepository, never()).decrementerPlaces(anyLong(), anyInt());
        verify(disciplineService, never()).getDisciplineOrThrow(anyLong()); // Ne devrait pas être atteint
    }

    @Test
    void ajouterPlaces_shouldReturnUpdatedDiscipline() {
        int nbToAdd = 10;
        int initialPlaces = discipline.getNbPlaceDispo();

        when(disciplineRepository.findById(1L)).thenReturn(Optional.of(discipline)); // Mock pour getDisciplineOrThrow
        when(disciplineRepository.save(any(Discipline.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Discipline result = disciplineService.ajouterPlaces(1L, nbToAdd);

        assertThat(result).isNotNull();
        assertThat(result.getNbPlaceDispo()).isEqualTo(initialPlaces + nbToAdd);
        verify(disciplineService).getDisciplineOrThrow(1L);
        verify(disciplineRepository).findById(1L);
        verify(disciplineRepository).save(discipline);
    }

    @Test
    void ajouterPlaces_shouldThrowIllegalArgumentException_whenNbIsNegative() {
        when(disciplineRepository.findById(1L)).thenReturn(Optional.of(discipline));

        assertThrows(IllegalArgumentException.class, () -> disciplineService.ajouterPlaces(1L, -5));
        verify(disciplineService).getDisciplineOrThrow(1L); // Appel a lieu
        verify(disciplineRepository).findById(1L);         // Suite à getDisciplineOrThrow
        verify(disciplineRepository, never()).save(any(Discipline.class));
    }

    @Test
    void updateDate_shouldReturnUpdatedDiscipline_whenDateIsValid() {
        LocalDateTime nouvelleDate = LocalDateTime.now().plusDays(20);
        when(disciplineRepository.findById(1L)).thenReturn(Optional.of(discipline)); // Mock pour getDisciplineOrThrow
        when(disciplineRepository.save(any(Discipline.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Discipline result = disciplineService.updateDate(1L, nouvelleDate);

        assertThat(result).isNotNull();
        assertThat(result.getDateDiscipline()).isEqualTo(nouvelleDate);
        verify(disciplineService).getDisciplineOrThrow(1L);
        verify(disciplineRepository).findById(1L);
        verify(disciplineRepository).save(discipline);
    }

    @Test
    void updateDate_shouldThrowIllegalArgumentException_whenDateIsInPast() {
        LocalDateTime nouvelleDate = LocalDateTime.now().minusDays(1);
        assertThrows(IllegalArgumentException.class, () -> disciplineService.updateDate(1L, nouvelleDate));
        verify(disciplineService, never()).getDisciplineOrThrow(anyLong());
        verify(disciplineRepository, never()).save(any(Discipline.class));
    }

    @Test
    void getDisciplinesAvenir_shouldReturnPagedDisciplines() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Discipline> disciplineList = List.of(discipline);
        Page<Discipline> disciplinePage = new PageImpl<>(disciplineList, pageable, disciplineList.size());
        when(disciplineRepository.findFutureDisciplinesWithAdresse(any(LocalDateTime.class), eq(pageable))).thenReturn(disciplinePage);

        Page<Discipline> result = disciplineService.getDisciplinesAvenir(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getNomDiscipline()).isEqualTo("Test Discipline");
        verify(disciplineRepository).findFutureDisciplinesWithAdresse(any(LocalDateTime.class), eq(pageable));
    }

    @Test
    void getDisciplineOrThrow_shouldReturnDiscipline_whenExists() {
        when(disciplineRepository.findById(1L)).thenReturn(Optional.of(discipline));

        Discipline result = disciplineService.getDisciplineOrThrow(1L);

        assertThat(result).isNotNull();
        assertThat(result.getIdDiscipline()).isEqualTo(1L);
        verify(disciplineRepository).findById(1L);
    }

    @Test
    void getDisciplineOrThrow_shouldThrowEntityNotFoundException_whenNotExists() {
        when(disciplineRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> disciplineService.getDisciplineOrThrow(1L));
        verify(disciplineRepository).findById(1L);
    }

    @Test
    void findDisciplinesFiltered_shouldCallFindAll_whenNoFiltersProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Discipline> expectedPage = new PageImpl<>(List.of(discipline));
        when(disciplineRepository.findAllWithAdresse(pageable)).thenReturn(expectedPage);

        Page<Discipline> result = disciplineService.findDisciplinesFiltered(null, null, null, pageable);

        assertThat(result).isEqualTo(expectedPage);
        verify(disciplineRepository).findAllWithAdresse(pageable);
    }


    @Test
    void getDisciplinesEnVedette_shouldReturnDisciplines_whenEpreuvesEnVedetteExist() {
        List<Epreuve> epreuvesVedette = List.of(epreuveEnVedette);
        List<Long> idsEpreuvesVedette = List.of(epreuveEnVedette.getIdEpreuve());
        Set<Discipline> expectedDisciplines = Set.of(discipline);

        when(epreuveService.getEpreuvesEnVedette()).thenReturn(epreuvesVedette);
        when(disciplineRepository.findDisciplinesByEpreuveIdsWithAdresse(idsEpreuvesVedette)).thenReturn(expectedDisciplines);

        Set<Discipline> result = disciplineService.getDisciplinesEnVedette();

        assertThat(result).isEqualTo(expectedDisciplines);
        verify(epreuveService).getEpreuvesEnVedette();
        verify(disciplineRepository).findDisciplinesByEpreuveIdsWithAdresse(idsEpreuvesVedette);
    }

    @Test
    void getDisciplinesEnVedette_shouldReturnEmptySet_whenNoEpreuvesEnVedette() {
        when(epreuveService.getEpreuvesEnVedette()).thenReturn(Collections.emptyList());

        Set<Discipline> result = disciplineService.getDisciplinesEnVedette();

        assertThat(result).isEmpty();
        verify(epreuveService).getEpreuvesEnVedette();
        verify(disciplineRepository, never()).findDisciplinesByEpreuveIdsWithAdresse(anyList());
    }
}