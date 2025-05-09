package fr.studi.bloc3jo2024.service.offres;

import fr.studi.bloc3jo2024.dto.offres.OffreDto;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.OffreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilisateurOffreServiceTest {

    @InjectMocks
    private UtilisateurOffreService utilisateurOffreService;

    @Mock
    private OffreRepository offreRepository;

    @Mock
    private ModelMapper modelMapper;

    private Offre offre;
    private OffreDto offreDto;

    @BeforeEach
    void setUp() {
        offre = new Offre();
        offre.setIdOffre(1L);
        offre.setStatutOffre(StatutOffre.DISPONIBLE);
        offre.setDiscipline(new Discipline());

        offreDto = new OffreDto();
        offreDto.setId(1L);
    }

    @Test
    void obtenirToutesLesOffresDisponibles_OffersAvailable_ReturnsListOfOffreDto() {
        // Arrange
        List<Offre> offres = Collections.singletonList(offre);
        when(offreRepository.findByStatutOffre(StatutOffre.DISPONIBLE)).thenReturn(offres);
        when(modelMapper.map(offre, OffreDto.class)).thenReturn(offreDto);

        // Act
        List<OffreDto> result = utilisateurOffreService.obtenirToutesLesOffresDisponibles();

        // Assert
        assertEquals(1, result.size());
        assertEquals(offreDto, result.getFirst());
    }

    @Test
    void obtenirToutesLesOffresDisponibles_NoOffersAvailable_ReturnsEmptyList() {
        // Arrange
        when(offreRepository.findByStatutOffre(StatutOffre.DISPONIBLE)).thenReturn(Collections.emptyList());

        // Act
        List<OffreDto> result = utilisateurOffreService.obtenirToutesLesOffresDisponibles();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void obtenirOffreDisponibleParId_OfferIsAvailable_ReturnsOffreDto() {
        // Arrange
        when(offreRepository.findById(1L)).thenReturn(Optional.of(offre));
        when(modelMapper.map(offre, OffreDto.class)).thenReturn(offreDto);

        // Act
        OffreDto result = utilisateurOffreService.obtenirOffreDisponibleParId(1L);

        // Assert
        assertEquals(offreDto, result);
    }

    @Test
    void obtenirOffreDisponibleParId_OfferIsNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(offreRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> utilisateurOffreService.obtenirOffreDisponibleParId(1L));
    }

    @Test
    void obtenirOffreDisponibleParId_OfferIsNotAvailable_ThrowsResourceNotFoundException() {
        // Arrange
        offre.setStatutOffre(StatutOffre.EXPIRE);
        when(offreRepository.findById(1L)).thenReturn(Optional.of(offre));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> utilisateurOffreService.obtenirOffreDisponibleParId(1L));
    }
}