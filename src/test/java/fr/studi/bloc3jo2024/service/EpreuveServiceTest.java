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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EpreuveServiceTest {

    @Mock
    private EpreuveRepository epreuveRepository;

    @InjectMocks
    private EpreuveService epreuveService;

    private Epreuve epreuveNonVedette;
    private Epreuve epreuveVedette;
    private MettreAJourEpreuveVedetteDto dtoMettreEnVedette;
    private MettreAJourEpreuveVedetteDto dtoRetirerVedette;

    @BeforeEach
    void setUp() {
        // Arrange (

        epreuveNonVedette = new Epreuve();
        epreuveNonVedette.setIdEpreuve(1L);
        epreuveNonVedette.setNomEpreuve("Épreuve Non Vedette");
        epreuveNonVedette.setFeatured(false);

        epreuveVedette = new Epreuve();
        epreuveVedette.setIdEpreuve(2L);
        epreuveVedette.setNomEpreuve("Épreuve Vedette");
        epreuveVedette.setFeatured(true);

        dtoMettreEnVedette = new MettreAJourEpreuveVedetteDto();
        dtoMettreEnVedette.setIdEpreuve(1L); // Cible epreuveNonVedette
        dtoMettreEnVedette.setIsFeatured(true); // Veut la mettre en vedette

        dtoRetirerVedette = new MettreAJourEpreuveVedetteDto();
        dtoRetirerVedette.setIdEpreuve(2L); // Cible epreuveVedette
        dtoRetirerVedette.setIsFeatured(false); // Veut lui retirer le statut vedette
    }

    // --- Tests pour getEpreuvesEnVedette ---

    @Test
    void getEpreuvesEnVedette_RetourneListeCorrecte() {
        // Arrange
        List<Epreuve> epreuvesEnVedetteList = Collections.singletonList(epreuveVedette);
        when(epreuveRepository.findByIsFeaturedTrue()).thenReturn(epreuvesEnVedetteList);

        // Act
        List<Epreuve> result = epreuveService.getEpreuvesEnVedette();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(epreuveVedette.getIdEpreuve(), result.getFirst().getIdEpreuve());
        assertTrue(result.getFirst().isFeatured());
        verify(epreuveRepository, times(1)).findByIsFeaturedTrue(); // Vérifie que la méthode du repository a été appelée
    }

    @Test
    void getEpreuvesEnVedette_RetourneListeVideSiAucune() {
        // Arrange
        List<Epreuve> epreuvesEnVedetteList = Collections.emptyList(); // Liste vide
        when(epreuveRepository.findByIsFeaturedTrue()).thenReturn(epreuvesEnVedetteList);

        // Act
        List<Epreuve> result = epreuveService.getEpreuvesEnVedette();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(epreuveRepository, times(1)).findByIsFeaturedTrue(); // Vérifie que la méthode du repository a été appelée
    }

    // --- Tests pour mettreAJourStatutVedette ---

    @Test
    void mettreAJourStatutVedette_MetEnVedetteAvecSucces() {
        // Arrange
        Long targetId = dtoMettreEnVedette.getIdEpreuve(); // ID de l'épreuve non vedette
        // Le repository trouve l'épreuve non vedette
        when(epreuveRepository.findById(targetId)).thenReturn(Optional.of(epreuveNonVedette));
        // Le repository retourne l'épreuve après la sauvegarde (simule le comportement de save)
        when(epreuveRepository.save(any(Epreuve.class))).thenReturn(epreuveNonVedette); // Save retourne la même instance modifiée

        // Act
        Epreuve updatedEpreuve = epreuveService.mettreAJourStatutVedette(dtoMettreEnVedette);

        // Assert
        assertNotNull(updatedEpreuve);
        assertEquals(targetId, updatedEpreuve.getIdEpreuve());
        assertTrue(updatedEpreuve.isFeatured()); // Vérifie que le statut a été mis à jour
        verify(epreuveRepository, times(1)).findById(targetId); // Vérifie la recherche par ID
        verify(epreuveRepository, times(1)).save(epreuveNonVedette); // Vérifie la sauvegarde de l'instance modifiée
    }

    @Test
    void mettreAJourStatutVedette_RetireStatutVedetteAvecSucces() {
        // Arrange
        Long targetId = dtoRetirerVedette.getIdEpreuve(); // ID de l'épreuve vedette
        // Le repository trouve l'épreuve vedette
        when(epreuveRepository.findById(targetId)).thenReturn(Optional.of(epreuveVedette));
        // Le repository retourne l'épreuve après la sauvegarde (simule le comportement de save)
        when(epreuveRepository.save(any(Epreuve.class))).thenReturn(epreuveVedette); // Save retourne la même instance modifiée

        // Act
        Epreuve updatedEpreuve = epreuveService.mettreAJourStatutVedette(dtoRetirerVedette);

        // Assert
        assertNotNull(updatedEpreuve);
        assertEquals(targetId, updatedEpreuve.getIdEpreuve());
        assertFalse(updatedEpreuve.isFeatured()); // Vérifie que le statut a été mis à jour
        verify(epreuveRepository, times(1)).findById(targetId); // Vérifie la recherche par ID
        verify(epreuveRepository, times(1)).save(epreuveVedette); // Vérifie la sauvegarde de l'instance modifiée
    }


    @Test
    void mettreAJourStatutVedette_LanceEntityNotFoundExceptionSiEpreuveNonTrouvee() {
        // Arrange
        Long targetId = 99L; // Un ID qui n'existe pas
        dtoMettreEnVedette.setIdEpreuve(targetId); // Modifier le DTO pour cibler l'ID non existant

        // Le repository ne trouve pas l'épreuve
        when(epreuveRepository.findById(targetId)).thenReturn(Optional.empty());

        // Act & Assert
        // On s'attend à ce que le service lance EntityNotFoundException
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> epreuveService.mettreAJourStatutVedette(dtoMettreEnVedette)
        );

        assertTrue(exception.getMessage().contains("Épreuve non trouvée avec l'id : " + targetId)); // Vérifie le message d'exception
        verify(epreuveRepository, times(1)).findById(targetId); // Vérifie la recherche par ID
        verify(epreuveRepository, never()).save(any(Epreuve.class)); // Vérifie que save n'a pas été appelée
    }

    // --- Tests pour getAllEpreuves ---

    @Test
    void getAllEpreuves_RetourneListeCorrecte() {
        // Arrange
        List<Epreuve> toutesLesEpreuves = Arrays.asList(epreuveNonVedette, epreuveVedette);
        when(epreuveRepository.findAll()).thenReturn(toutesLesEpreuves);

        // Act
        List<Epreuve> result = epreuveService.getAllEpreuves();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(epreuveNonVedette.getIdEpreuve(), result.get(0).getIdEpreuve());
        assertEquals(epreuveVedette.getIdEpreuve(), result.get(1).getIdEpreuve());
        verify(epreuveRepository, times(1)).findAll(); // Vérifie que la méthode du repository a été appelée
    }

    @Test
    void getAllEpreuves_RetourneListeVideSiAucune() {
        // Arrange
        List<Epreuve> toutesLesEpreuves = Collections.emptyList(); // Liste vide
        when(epreuveRepository.findAll()).thenReturn(toutesLesEpreuves);

        // Act
        List<Epreuve> result = epreuveService.getAllEpreuves();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(epreuveRepository, times(1)).findAll(); // Vérifie que la méthode du repository a été appelée
    }
}