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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UtilisateurOffreServiceTest {

    @Mock
    private OffreRepository offreRepository;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private UtilisateurOffreService utilisateurOffreService;

    private Offre offreDisponible1;
    private Offre offreNonDisponible;

    @BeforeEach
    void setUp() {
        Discipline discipline1Setup = Discipline.builder().idDiscipline(1L).nomDiscipline("Natation").build();

        offreDisponible1 = Offre.builder()
                .idOffre(1L)
                .typeOffre(TypeOffre.SOLO)
                .prix(new BigDecimal("100.00"))
                .quantite(10)
                .capacite(1)
                .statutOffre(StatutOffre.DISPONIBLE)
                .discipline(discipline1Setup)
                .dateExpiration(LocalDateTime.now().plusDays(5))
                .featured(false)
                .build();

        offreNonDisponible = Offre.builder()
                .idOffre(3L)
                .typeOffre(TypeOffre.SOLO)
                .prix(new BigDecimal("75.00"))
                .quantite(0)
                .capacite(1)
                .statutOffre(StatutOffre.EPUISE)
                .discipline(discipline1Setup)
                .dateExpiration(LocalDateTime.now().plusDays(5))
                .featured(false)
                .build();

        modelMapper.typeMap(Offre.class, OffreDto.class).addMappings(mapper -> {
            mapper.map(src -> src.getDiscipline().getIdDiscipline(), OffreDto::setIdDiscipline);
            mapper.map(Offre::getQuantite, OffreDto::setQuantiteDisponible);
            mapper.map(Offre::isFeatured, OffreDto::setFeatured);
        });
    }

    @Test
    void obtenirToutesLesOffresDisponibles_shouldReturnPagedOffreDto() {
        Pageable pageable = PageRequest.of(0, 10);
        Discipline disciplineLocal = Discipline.builder().idDiscipline(1L).nomDiscipline("Natation").build();
        Offre offreLocale1 = Offre.builder().idOffre(1L).typeOffre(TypeOffre.SOLO).prix(new BigDecimal("100.00")).quantite(10).capacite(1).statutOffre(StatutOffre.DISPONIBLE).discipline(disciplineLocal).dateExpiration(LocalDateTime.now().plusDays(5)).featured(false).build();
        Offre offreLocale2 = Offre.builder().idOffre(2L).typeOffre(TypeOffre.DUO).prix(new BigDecimal("180.00")).quantite(5).capacite(2).statutOffre(StatutOffre.DISPONIBLE).discipline(disciplineLocal).dateExpiration(LocalDateTime.now().plusDays(10)).featured(true).build();

        List<Offre> offresList = Arrays.asList(offreLocale1, offreLocale2);
        Page<Offre> offresPage = new PageImpl<>(offresList, pageable, offresList.size());

        when(offreRepository.findByStatutOffre(eq(StatutOffre.DISPONIBLE), any(Pageable.class))) // Corrigé
                .thenReturn(offresPage);

        Page<OffreDto> resultPage = utilisateurOffreService.obtenirToutesLesOffresDisponibles(pageable);

        assertNotNull(resultPage);
        assertEquals(2, resultPage.getContent().size());
        assertEquals(offreLocale1.getIdOffre(), resultPage.getContent().get(0).getId());
        assertEquals(offreLocale2.getIdOffre(), resultPage.getContent().get(1).getId());
        assertEquals(offreLocale1.getDiscipline().getIdDiscipline(), resultPage.getContent().get(0).getIdDiscipline());
        assertEquals(offreLocale1.getQuantite(), resultPage.getContent().get(0).getQuantiteDisponible());
        assertEquals(offreLocale2.isFeatured(), resultPage.getContent().get(1).isFeatured());

        verify(offreRepository, times(1)).findByStatutOffre(eq(StatutOffre.DISPONIBLE), any(Pageable.class));
    }

    @Test
    void obtenirToutesLesOffresDisponibles_whenNoOffers_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Offre> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(offreRepository.findByStatutOffre(eq(StatutOffre.DISPONIBLE), any(Pageable.class)))
                .thenReturn(emptyPage);

        Page<OffreDto> resultPage = utilisateurOffreService.obtenirToutesLesOffresDisponibles(pageable);

        assertNotNull(resultPage);
        assertTrue(resultPage.isEmpty());
        assertEquals(0, resultPage.getTotalElements());
        verify(offreRepository, times(1)).findByStatutOffre(eq(StatutOffre.DISPONIBLE), any(Pageable.class));
    }

    @Test
    void obtenirOffreDisponibleParId_existingAndDisponible_shouldReturnOffreDto() {
        Long offreId = offreDisponible1.getIdOffre();
        when(offreRepository.findById(offreId)).thenReturn(Optional.of(offreDisponible1));

        OffreDto result = utilisateurOffreService.obtenirOffreDisponibleParId(offreId);

        assertNotNull(result);
        assertEquals(offreId, result.getId());
        assertEquals(offreDisponible1.getTypeOffre(), result.getTypeOffre());
        assertEquals(offreDisponible1.getDiscipline().getIdDiscipline(), result.getIdDiscipline());
        assertEquals(offreDisponible1.isFeatured(), result.isFeatured());
        verify(offreRepository, times(1)).findById(offreId);
    }

    @Test
    void obtenirOffreDisponibleParId_nonExistingId_shouldThrowResourceNotFoundException() {
        Long nonExistingId = 99L;
        when(offreRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                utilisateurOffreService.obtenirOffreDisponibleParId(nonExistingId)
        );
        assertTrue(exception.getMessage().contains(String.valueOf(nonExistingId)));
        verify(offreRepository, times(1)).findById(nonExistingId);
    }

    @Test
    void obtenirOffreDisponibleParId_existingButNotDisponible_shouldThrowResourceNotFoundException() {
        Long offreId = offreNonDisponible.getIdOffre();
        when(offreRepository.findById(offreId)).thenReturn(Optional.of(offreNonDisponible));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                utilisateurOffreService.obtenirOffreDisponibleParId(offreId)
        );
        assertTrue(exception.getMessage().contains("n'est pas disponible"));
        assertTrue(exception.getMessage().contains(String.valueOf(offreId)));
        verify(offreRepository, times(1)).findById(offreId);
    }
}