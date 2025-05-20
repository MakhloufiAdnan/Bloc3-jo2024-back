package fr.studi.bloc3jo2024.service.offres;

import fr.studi.bloc3jo2024.entity.Offre;
// StatutOffre n'est plus directement utilisé dans les mocks de findByStatutOffre pour la méthode testée
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.OffreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*; // Importation de Mockito.* pour times, verify, etc.

/**
 * Tests unitaires pour {@link OffreService}.
 */
@ExtendWith(MockitoExtension.class)
class OffreServiceTest {

    @InjectMocks
    private OffreService offreService;

    @Mock
    private OffreRepository offreRepository;

    private Offre offreMock; // Un mock d'offre pour les tests de getOffreById

    @BeforeEach
    void setUp() {
        offreMock = new Offre();
        offreMock.setIdOffre(1L);
        // Pas besoin d'initialiser dateExpiration ou statut ici pour les tests de mettreAJourStatutOffresExpireesAutomatiquement,
        // car cette méthode ne charge plus les offres individuellement.
    }

    @Test
    void getOffreById_ExistingId_ReturnsOffre() {
        // Arrange
        when(offreRepository.findById(1L)).thenReturn(Optional.of(offreMock));

        // Act
        Offre result = offreService.getOffreById(1L);

        // Assert
        assertEquals(offreMock, result);
        verify(offreRepository).findById(1L);
    }

    @Test
    void getOffreById_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        Long nonExistingId = 2L;
        when(offreRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> offreService.getOffreById(nonExistingId));
        verify(offreRepository).findById(nonExistingId);
    }

    /**
     * Teste la méthode planifiée mettreAJourStatutOffresExpireesAutomatiquement.
     * Vérifie que la méthode de mise à jour en masse du repository est appelée.
     */
    @Test
    void mettreAJourStatutOffresExpireesAutomatiquement_shouldCallRepositoryUpdateMethod() {
        // Arrange
        // On s'attend à ce que la méthode du repository soit appelée, peu importe ce qu'elle retourne.
        // On peut mocker son retour si on veut tester la logique de logging dans le service.
        when(offreRepository.updateStatusForEffectivelyExpiredOffers(any(LocalDateTime.class), any(LocalDate.class)))
                .thenReturn(5); // Simuler que 5 offres ont été mises à jour

        // Act
        offreService.mettreAJourStatutOffresExpireesAutomatiquement();

        // Assert
        // Vérifier que la méthode correcte du repository a été appelée une fois
        // avec n'importe quelle LocalDateTime et n'importe quelle LocalDate.
        verify(offreRepository, times(1)).updateStatusForEffectivelyExpiredOffers(
                any(LocalDateTime.class),
                any(LocalDate.class)
        );
        // On pourrait ajouter des assertions sur les logs si le logger était mocké et injecté.
    }

    /**
     * Teste la méthode planifiée lorsque aucune offre n'est mise à jour.
     */
    @Test
    void mettreAJourStatutOffresExpireesAutomatiquement_whenNoOffersUpdated_shouldCallRepositoryUpdateMethod() {
        // Arrange
        when(offreRepository.updateStatusForEffectivelyExpiredOffers(any(LocalDateTime.class), any(LocalDate.class)))
                .thenReturn(0); // Simuler qu'aucune offre n'a été mise à jour

        // Act
        offreService.mettreAJourStatutOffresExpireesAutomatiquement();

        // Assert
        verify(offreRepository, times(1)).updateStatusForEffectivelyExpiredOffers(
                any(LocalDateTime.class),
                any(LocalDate.class)
        );
    }

    /**
     * Teste la gestion d'exception dans la méthode planifiée.
     * Si le repository lève une exception, le service doit la logger mais ne pas la propager
     * pour ne pas arrêter la planification.
     */
    @Test
    void mettreAJourStatutOffresExpireesAutomatiquement_whenRepositoryThrowsException_shouldHandleAndLog() {
        // Arrange
        // Simuler une exception lors de l'appel au repository
        when(offreRepository.updateStatusForEffectivelyExpiredOffers(any(LocalDateTime.class), any(LocalDate.class)))
                .thenThrow(new RuntimeException("Erreur base de données simulée"));

        // Act
        // La méthode du service ne devrait pas propager l'exception
        assertDoesNotThrow(() -> offreService.mettreAJourStatutOffresExpireesAutomatiquement());

        // Assert
        // Vérifier que la méthode du repository a bien été appelée
        verify(offreRepository, times(1)).updateStatusForEffectivelyExpiredOffers(
                any(LocalDateTime.class),
                any(LocalDate.class)
        );
        // Ici, on pourrait vérifier qu'une erreur a été logguée si le logger était un mock.
        // Par exemple, avec un Appender mocké ou un framework de test de logging.
        // Pour l'instant, on se contente de vérifier que l'appel a eu lieu et que le service n'a pas planté.
    }
}
