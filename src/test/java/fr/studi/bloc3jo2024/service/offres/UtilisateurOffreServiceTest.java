package fr.studi.bloc3jo2024.service.offres;

import fr.studi.bloc3jo2024.dto.offres.OffreDto;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.OffreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never; // Importation pour Mockito.never()
import static org.mockito.Mockito.times; // Importation pour Mockito.times()
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour {@link UtilisateurOffreService}.
 */
@ExtendWith(MockitoExtension.class)
class UtilisateurOffreServiceTest {

    @Mock
    private OffreRepository offreRepository;

    @Spy // ModelMapper est espionné pour utiliser sa vraie logique de mapping
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private UtilisateurOffreService utilisateurOffreService;

    private Offre offreDisponible1;
    private Offre offreDisponible2;
    private Offre offreNonDisponible;
    private Offre offreExpireeAvecStatutDisponible; // Pour tester la logique d'expiration effective
    private Discipline disciplineTest;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        // assertNotNull(modelMapper); // Optionnel, pour "utiliser" le spy si l'IDE se plaint

        disciplineTest = Discipline.builder()
                .idDiscipline(1L)
                .nomDiscipline("Natation Test")
                .dateDiscipline(now.plusMonths(1)) // Date de la discipline dans le futur
                .build();

        offreDisponible1 = Offre.builder()
                .idOffre(1L)
                .typeOffre(TypeOffre.SOLO)
                .prix(new BigDecimal("25.00"))
                .quantite(10)
                .capacite(1)
                .statutOffre(StatutOffre.DISPONIBLE)
                .discipline(disciplineTest)
                .dateExpiration(now.plusDays(30)) // Date d'expiration propre à l'offre
                .featured(false)
                .build();

        offreDisponible2 = Offre.builder()
                .idOffre(2L)
                .typeOffre(TypeOffre.DUO)
                .prix(new BigDecimal("45.00"))
                .quantite(5)
                .capacite(2)
                .statutOffre(StatutOffre.DISPONIBLE)
                .discipline(disciplineTest)
                // Pas de dateExpiration propre, devrait utiliser celle de la discipline
                .featured(true)
                .build();

        offreNonDisponible = Offre.builder()
                .idOffre(3L)
                .typeOffre(TypeOffre.SOLO)
                .prix(new BigDecimal("30.00"))
                .quantite(10)
                .capacite(1)
                .statutOffre(StatutOffre.EPUISE) // Non disponible
                .discipline(disciplineTest)
                .dateExpiration(now.plusDays(30))
                .featured(false)
                .build();

        offreExpireeAvecStatutDisponible = Offre.builder()
                .idOffre(4L)
                .typeOffre(TypeOffre.SOLO)
                .prix(new BigDecimal("20.00"))
                .quantite(10)
                .capacite(1)
                .statutOffre(StatutOffre.DISPONIBLE) // Statut incorrect en base
                .discipline(Discipline.builder().idDiscipline(2L).dateDiscipline(now.minusDays(1)).build()) // Discipline passée
                .dateExpiration(null) // Expiration dictée par la discipline
                .featured(false)
                .build();
    }

    @Test
    void obtenirToutesLesOffresDisponibles_shouldReturnPagedOffreDtos() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Offre> offresList = Arrays.asList(offreDisponible1, offreDisponible2);
        Page<Offre> offresPageEntites = new PageImpl<>(offresList, pageable, offresList.size());

        when(offreRepository.findByStatutOffreWithDiscipline(StatutOffre.DISPONIBLE, pageable))
                .thenReturn(offresPageEntites);

        // Act
        Page<OffreDto> resultPageDto = utilisateurOffreService.obtenirToutesLesOffresDisponibles(pageable);

        // Assert
        assertNotNull(resultPageDto);
        assertThat(resultPageDto.getContent()).hasSize(2);
        assertEquals(offresList.size(), resultPageDto.getTotalElements());

        OffreDto dto1 = resultPageDto.getContent().get(0);
        assertEquals(offreDisponible1.getIdOffre(), dto1.getId());
        assertEquals(offreDisponible1.getDiscipline().getIdDiscipline(), dto1.getIdDiscipline());
        assertEquals(offreDisponible1.getEffectiveDateExpiration(), dto1.getDateExpiration());

        OffreDto dto2 = resultPageDto.getContent().get(1);
        assertEquals(offreDisponible2.getIdOffre(), dto2.getId());
        assertEquals(offreDisponible2.getDiscipline().getIdDiscipline(), dto2.getIdDiscipline());
        assertEquals(offreDisponible2.getEffectiveDateExpiration(), dto2.getDateExpiration());


        verify(offreRepository).findByStatutOffreWithDiscipline(eq(StatutOffre.DISPONIBLE), eq(pageable));
        // Vérifier que modelMapper.map a été appelé pour chaque offre dans la liste
        verify(modelMapper, times(1)).map(eq(offreDisponible1), eq(OffreDto.class));
        verify(modelMapper, times(1)).map(eq(offreDisponible2), eq(OffreDto.class));
    }

    @Test
    void obtenirToutesLesOffresDisponibles_shouldReturnEmptyPage_whenNoOffresDisponibles() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Offre> emptyPageEntites = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(offreRepository.findByStatutOffreWithDiscipline(StatutOffre.DISPONIBLE, pageable))
                .thenReturn(emptyPageEntites);

        // Act
        Page<OffreDto> resultPageDto = utilisateurOffreService.obtenirToutesLesOffresDisponibles(pageable);

        // Assert
        assertNotNull(resultPageDto);
        assertThat(resultPageDto.getContent()).isEmpty();
        assertEquals(0, resultPageDto.getTotalElements());
        verify(offreRepository).findByStatutOffreWithDiscipline(eq(StatutOffre.DISPONIBLE), eq(pageable));
        verify(modelMapper, never()).map(any(Offre.class), eq(OffreDto.class));
    }

    @Test
    void obtenirOffreDisponibleParId_shouldReturnOffreDto_whenFoundAndDisponible() {
        // Arrange
        Long offreId = offreDisponible1.getIdOffre();
        when(offreRepository.findById(offreId)).thenReturn(Optional.of(offreDisponible1));

        // Act
        OffreDto result = utilisateurOffreService.obtenirOffreDisponibleParId(offreId);

        // Assert
        assertNotNull(result);
        assertEquals(offreDisponible1.getIdOffre(), result.getId());
        assertEquals(offreDisponible1.getTypeOffre(), result.getTypeOffre());
        assertEquals(offreDisponible1.getDiscipline().getIdDiscipline(), result.getIdDiscipline());
        assertEquals(offreDisponible1.getEffectiveDateExpiration(), result.getDateExpiration());
        assertEquals(offreDisponible1.getQuantite(), result.getQuantiteDisponible());
        verify(modelMapper).map(offreDisponible1, OffreDto.class);
    }

    @Test
    void obtenirOffreDisponibleParId_shouldThrowResourceNotFoundException_whenOffreNonExistent() {
        // Arrange
        Long nonExistentId = 99L;
        when(offreRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> utilisateurOffreService.obtenirOffreDisponibleParId(nonExistentId));
        assertTrue(exception.getMessage().contains("Offre non trouvée avec l'ID : " + nonExistentId));
    }

    @Test
    void obtenirOffreDisponibleParId_shouldThrowResourceNotFoundException_whenOffreNotDisponibleByStatut() {
        // Arrange
        Long offreId = offreNonDisponible.getIdOffre(); // Statut EPUISE
        when(offreRepository.findById(offreId)).thenReturn(Optional.of(offreNonDisponible));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> utilisateurOffreService.obtenirOffreDisponibleParId(offreId));
        assertTrue(exception.getMessage().contains("L'offre avec l'ID " + offreId + " n'est plus disponible ou n'existe pas."));
    }

    @Test
    void obtenirOffreDisponibleParId_shouldThrowResourceNotFoundException_whenOffreEffectivelyExpired() {
        // Arrange
        Long offreId = offreExpireeAvecStatutDisponible.getIdOffre();
        when(offreRepository.findById(offreId)).thenReturn(Optional.of(offreExpireeAvecStatutDisponible));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> utilisateurOffreService.obtenirOffreDisponibleParId(offreId));
        assertTrue(exception.getMessage().contains("L'offre avec l'ID " + offreId + " n'est plus disponible ou n'existe pas."));
    }
}
