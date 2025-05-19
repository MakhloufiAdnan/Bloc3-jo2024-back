package fr.studi.bloc3jo2024.service.offres;

import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.OffreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OffreServiceTest {

    @InjectMocks
    private OffreService offreService;

    @Mock
    private OffreRepository offreRepository;

    private Offre offre;

    @BeforeEach
    void setUp() {
        offre = new Offre();
        offre.setIdOffre(1L);
    }

    @Test
    void getOffreById_ExistingId_ReturnsOffre() {
        // Arrange
        when(offreRepository.findById(1L)).thenReturn(Optional.of(offre));

        // Act
        Offre result = offreService.getOffreById(1L);

        // Assert
        assertEquals(offre, result);
    }

    @Test
    void getOffreById_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        when(offreRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> offreService.getOffreById(1L));
    }

    @Test
    void mettreAJourStatutOffresExpirees_shouldCallRepositoryUpdateMethod() {
        // Arrange
        int expectedUpdatedCount = 5;
        when(offreRepository.updateStatusForExpiredOffers(any(LocalDateTime.class))).thenReturn(expectedUpdatedCount);

        // Act
        offreService.mettreAJourStatutOffresExpirees();

        // Assert
        verify(offreRepository, times(1)).updateStatusForExpiredOffers(any(LocalDateTime.class));
    }

    @Test
    void mettreAJourStatutOffresExpirees_NoOffersUpdated_shouldHandleZeroCount() {
        // Arrange
        int expectedUpdatedCount = 0;
        when(offreRepository.updateStatusForExpiredOffers(any(LocalDateTime.class))).thenReturn(expectedUpdatedCount);

        // Act
        offreService.mettreAJourStatutOffresExpirees();

        // Assert
        verify(offreRepository, times(1)).updateStatusForExpiredOffers(any(LocalDateTime.class));
    }
}