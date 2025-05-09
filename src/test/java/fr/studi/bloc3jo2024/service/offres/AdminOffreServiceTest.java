package fr.studi.bloc3jo2024.service.offres;

import fr.studi.bloc3jo2024.dto.offres.CreerOffreDto;
import fr.studi.bloc3jo2024.dto.offres.MettreAJourOffreDto;
import fr.studi.bloc3jo2024.dto.offres.OffreAdminDto;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.DisciplineRepository;
import fr.studi.bloc3jo2024.repository.OffreRepository;
import fr.studi.bloc3jo2024.service.PanierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminOffreServiceTest {

    @InjectMocks
    private AdminOffreService adminOffreService;

    @Mock
    private OffreRepository offreRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private DisciplineRepository disciplineRepository;

    @Mock
    private PanierService panierService;

    private Discipline discipline;
    private Offre offre;
    private OffreAdminDto offreAdminDto;
    private CreerOffreDto creerOffreDto;
    private MettreAJourOffreDto mettreAJourOffreDto;

    @BeforeEach
    void setUp() {
        discipline = new Discipline();
        discipline.setIdDiscipline(1L);
        discipline.setNomDiscipline("Football");

        offre = new Offre();
        offre.setIdOffre(1L);
        offre.setDiscipline(discipline);

        offreAdminDto = new OffreAdminDto();
        offreAdminDto.setId(1L);

        creerOffreDto = new CreerOffreDto();
        creerOffreDto.setIdDiscipline(1L);

        mettreAJourOffreDto = new MettreAJourOffreDto();
        mettreAJourOffreDto.setIdDiscipline(1L);
    }

    @Test
    void ajouterOffre_ValidInput_ReturnsOffreAdminDto() {
        // Arrange
        when(disciplineRepository.findById(1L)).thenReturn(Optional.of(discipline));
        when(modelMapper.map(creerOffreDto, Offre.class)).thenReturn(offre);
        when(offreRepository.save(offre)).thenReturn(offre);
        when(modelMapper.map(offre, OffreAdminDto.class)).thenReturn(offreAdminDto);

        // Act
        OffreAdminDto result = adminOffreService.ajouterOffre(creerOffreDto);

        // Assert
        assertEquals(offreAdminDto, result);
        verify(offreRepository, times(1)).save(offre);
    }

    @Test
    void ajouterOffre_DisciplineNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(disciplineRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> adminOffreService.ajouterOffre(creerOffreDto));
        verify(offreRepository, never()).save(any());
    }

    @Test
    void obtenirOffreParId_ExistingId_ReturnsOffreAdminDto() {
        // Arrange
        when(offreRepository.findById(1L)).thenReturn(Optional.of(offre));
        when(modelMapper.map(offre, OffreAdminDto.class)).thenReturn(offreAdminDto);

        // Act
        OffreAdminDto result = adminOffreService.obtenirOffreParId(1L);

        // Assert
        assertEquals(offreAdminDto, result);
    }

    @Test
    void obtenirOffreParId_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        when(offreRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> adminOffreService.obtenirOffreParId(1L));
    }

    @Test
    void mettreAJourOffre_ValidInput_ReturnsOffreAdminDto() {
        Long testOffreId = 1L;
        MettreAJourOffreDto testDto = new MettreAJourOffreDto();
        testDto.setIdDiscipline(1L); // ← Clé du problème

        Offre testOffre = new Offre();
        Discipline testDiscipline = new Discipline();
        testDiscipline.setIdDiscipline(1L);
        OffreAdminDto expectedDto = new OffreAdminDto();

        when(offreRepository.findById(testOffreId)).thenReturn(Optional.of(testOffre));
        when(disciplineRepository.findById(1L)).thenReturn(Optional.of(testDiscipline));
        doNothing().when(modelMapper).map(testDto, testOffre);
        testOffre.setDiscipline(testDiscipline);
        when(offreRepository.save(testOffre)).thenReturn(testOffre);
        when(modelMapper.map(testOffre, OffreAdminDto.class)).thenReturn(expectedDto);

        OffreAdminDto result = adminOffreService.mettreAJourOffre(testOffreId, testDto);

        assertEquals(expectedDto, result);
    }

    @Test
    void mettreAJourOffre_OffreNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(offreRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> adminOffreService.mettreAJourOffre(1L, mettreAJourOffreDto));
        verify(disciplineRepository, never()).findById(anyLong());
        verify(offreRepository, never()).save(any());
    }

    @Test
    void mettreAJourOffre_DisciplineNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(offreRepository.findById(1L)).thenReturn(Optional.of(offre));
        when(disciplineRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> adminOffreService.mettreAJourOffre(1L, mettreAJourOffreDto));
        verify(offreRepository, never()).save(any());
    }

    @Test
    void supprimerOffre_ExistingId_CallsDeleteAndNotifyPanierService() {
        // Arrange
        when(offreRepository.findById(1L)).thenReturn(Optional.of(offre));

        // Act
        adminOffreService.supprimerOffre(1L);

        // Assert
        verify(offreRepository, times(1)).delete(offre);
        verify(panierService, times(1)).supprimerOffreDeTousLesPaniers(offre);
    }

    @Test
    void supprimerOffre_NonExistingId_ThrowsResourceNotFoundException() {
        // Arrange
        when(offreRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> adminOffreService.supprimerOffre(1L));
        verify(offreRepository, never()).delete(any());
        verify(panierService, never()).supprimerOffreDeTousLesPaniers(any());
    }

    @Test
    void obtenirToutesLesOffres_ReturnsListOfOffreAdminDto() {
        // Arrange
        List<Offre> offres = Arrays.asList(offre, new Offre());
        OffreAdminDto offreAdminDto1 = new OffreAdminDto();
        offreAdminDto1.setId(1L);
        OffreAdminDto offreAdminDto2 = new OffreAdminDto();
        offreAdminDto2.setId(2L);
        List<OffreAdminDto> offreAdminDtos = Arrays.asList(offreAdminDto1, offreAdminDto2);
        when(offreRepository.findAll()).thenReturn(offres);
        when(modelMapper.map(offres.get(0), OffreAdminDto.class)).thenReturn(offreAdminDto1);
        when(modelMapper.map(offres.get(1), OffreAdminDto.class)).thenReturn(offreAdminDto2);


        // Act
        List<OffreAdminDto> result = adminOffreService.obtenirToutesLesOffres();

        // Assert
        assertEquals(offreAdminDtos.size(), result.size());
        verify(offreRepository, times(1)).findAll();
        verify(modelMapper, times(2)).map(any(), eq(OffreAdminDto.class));
        assertEquals(offreAdminDtos.get(0).getId(), result.get(0).getId());
        assertEquals(offreAdminDtos.get(1).getId(), result.get(1).getId());
    }

    @Test
    void getNombreDeVentesParOffre_ReturnsMapOfOffreIdToVentes() {
        // Arrange
        Object[] result1 = {offre, 10L};
        Object[] result2 = {new Offre(), 5L};
        List<Object[]> results = Arrays.asList(result1, result2);
        when(offreRepository.countBilletsByOffre()).thenReturn(results);

        // Act
        Map<Long, Long> ventesParOffre = adminOffreService.getNombreDeVentesParOffre();

        // Assert
        assertEquals(2, ventesParOffre.size());
        assertEquals(10L, ventesParOffre.get(1L));
        assertEquals(5L, ventesParOffre.get(((Offre) result2[0]).getIdOffre()));
    }

    @Test
    void getVentesParTypeOffre_ReturnsMapOfTypeOffreToVentes() {
        // Arrange
        Object[] result1 = {TypeOffre.SOLO.name(), 20L};
        Object[] result2 = {TypeOffre.DUO.name(), 30L};
        List<Object[]> results = Arrays.asList(result1, result2);
        when(offreRepository.countBilletsByTypeOffre()).thenReturn(results);

        // Act
        Map<String, Long> ventesParTypeOffre = adminOffreService.getVentesParTypeOffre();

        // Assert
        assertEquals(2, ventesParTypeOffre.size());
        assertEquals(20L, ventesParTypeOffre.get(TypeOffre.SOLO.name()));
        assertEquals(30L, ventesParTypeOffre.get(TypeOffre.DUO.name()));
    }
}
