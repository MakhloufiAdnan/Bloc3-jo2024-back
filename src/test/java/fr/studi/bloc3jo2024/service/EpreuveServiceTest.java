package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.dto.epreuves.MettreAJourEpreuveVedetteDto;
import fr.studi.bloc3jo2024.entity.Epreuve;
import fr.studi.bloc3jo2024.repository.EpreuveRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EpreuveServiceTest {

    @Mock
    private EpreuveRepository epreuveRepository;

    @InjectMocks
    private EpreuveService epreuveService;

    private Epreuve epreuve1;
    private Epreuve epreuve2;
    private MettreAJourEpreuveVedetteDto dtoUpdateVedette;

    @BeforeEach
    void setUp() {
        epreuve1 = Epreuve.builder()
                .idEpreuve(1L)
                .nomEpreuve("100m Sprint")
                .isFeatured(false)
                .build();

        epreuve2 = Epreuve.builder()
                .idEpreuve(2L)
                .nomEpreuve("Saut en longueur")
                .isFeatured(true)
                .build();

        dtoUpdateVedette = new MettreAJourEpreuveVedetteDto();
        dtoUpdateVedette.setIdEpreuve(1L);
        dtoUpdateVedette.setIsFeatured(true);
    }

    @Test
    void getEpreuvesEnVedette_shouldReturnListOfFeaturedEpreuves() {
        when(epreuveRepository.findByIsFeaturedTrueWithComportersAndDisciplines()).thenReturn(Collections.singletonList(epreuve2));
        List<Epreuve> result = epreuveService.getEpreuvesEnVedette();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(epreuve2, result.getFirst());
        verify(epreuveRepository).findByIsFeaturedTrueWithComportersAndDisciplines();
    }

    @Test
    void getEpreuvesEnVedette_withPageable_shouldReturnPagedFeaturedEpreuves() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Epreuve> page = new PageImpl<>(Collections.singletonList(epreuve2), pageable, 1);
        when(epreuveRepository.findByIsFeaturedTrueWithComportersAndDisciplines(pageable)).thenReturn(page);

        Page<Epreuve> result = epreuveService.getEpreuvesEnVedette(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(epreuve2, result.getContent().getFirst());
        verify(epreuveRepository).findByIsFeaturedTrueWithComportersAndDisciplines(pageable);
    }

    @Test
    void mettreAJourStatutVedette_existingEpreuve_shouldUpdateAndReturnEpreuve() {
        when(epreuveRepository.findById(dtoUpdateVedette.getIdEpreuve())).thenReturn(Optional.of(epreuve1));
        // Mock the save operation to return the argument passed to it
        when(epreuveRepository.save(any(Epreuve.class))).thenAnswer(invocation -> {
            Epreuve epreuveToSave = invocation.getArgument(0);
            epreuveToSave.setFeatured(dtoUpdateVedette.getIsFeatured());
            return epreuveToSave;
        });


        Epreuve result = epreuveService.mettreAJourStatutVedette(dtoUpdateVedette);

        assertNotNull(result);
        assertTrue(result.isFeatured());
        assertEquals(dtoUpdateVedette.getIdEpreuve(), result.getIdEpreuve());
        verify(epreuveRepository).findById(dtoUpdateVedette.getIdEpreuve());
        verify(epreuveRepository).save(epreuve1);
        assertTrue(epreuve1.isFeatured(), "Epreuve1's featured status should be true after service call");
    }

    @Test
    void mettreAJourStatutVedette_nonExistingEpreuve_shouldThrowEntityNotFoundException() {
        when(epreuveRepository.findById(dtoUpdateVedette.getIdEpreuve())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                epreuveService.mettreAJourStatutVedette(dtoUpdateVedette)
        );
        verify(epreuveRepository).findById(dtoUpdateVedette.getIdEpreuve());
        verify(epreuveRepository, never()).save(any(Epreuve.class));
    }

    @Test
    void getAllEpreuves_shouldReturnPagedEpreuves() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Epreuve> epreuvesList = List.of(epreuve1, epreuve2);
        Page<Epreuve> expectedPage = new PageImpl<>(epreuvesList, pageable, epreuvesList.size());

        when(epreuveRepository.findAllWithComportersAndDisciplines(any(Pageable.class))).thenReturn(expectedPage);

        Page<Epreuve> actualPage = epreuveService.getAllEpreuves(pageable);

        assertNotNull(actualPage);
        assertEquals(2, actualPage.getTotalElements());
        assertEquals(epreuvesList, actualPage.getContent());
        verify(epreuveRepository, times(1)).findAllWithComportersAndDisciplines(any(Pageable.class));
    }

    @Test
    void getEpreuveById_existingId_shouldReturnEpreuve() {
        when(epreuveRepository.findByIdWithComportersAndDisciplines(1L)).thenReturn(Optional.of(epreuve1));

        Epreuve result = epreuveService.getEpreuveById(1L);

        assertNotNull(result);
        assertEquals(epreuve1, result);
        verify(epreuveRepository).findByIdWithComportersAndDisciplines(1L);
    }

    @Test
    void getEpreuveById_nonExistingId_shouldThrowEntityNotFoundException() {
        when(epreuveRepository.findByIdWithComportersAndDisciplines(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                epreuveService.getEpreuveById(99L)
        );
        verify(epreuveRepository).findByIdWithComportersAndDisciplines(99L);
    }
}
