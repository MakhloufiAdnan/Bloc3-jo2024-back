package fr.studi.bloc3jo2024.service.offres;

import fr.studi.bloc3jo2024.dto.offres.CreerOffreDto;
import fr.studi.bloc3jo2024.dto.offres.MettreAJourOffreDto;
import fr.studi.bloc3jo2024.dto.offres.OffreAdminDto;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.DisciplineRepository;
import fr.studi.bloc3jo2024.repository.OffreRepository;
import fr.studi.bloc3jo2024.service.PanierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminOffreServiceTest {

    @InjectMocks
    private AdminOffreService adminOffreService;

    @Mock
    private OffreRepository offreRepository;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @Mock
    private DisciplineRepository disciplineRepository;

    @Mock
    private PanierService panierService;

    private Discipline disciplineExistante;
    private Offre offreExistante;
    private Offre offreAutre;
    private CreerOffreDto creerOffreDto;
    private MettreAJourOffreDto mettreAJourOffreDto;
    private LocalDateTime fixedTimeNow; // Pour les dates relatives à "maintenant"
    private LocalDateTime specificDateExpirationForUpdate;


    @BeforeEach
    void setUp() {
        assertNotNull(modelMapper); // Pour "utiliser" le spy si l'IDE se plaint

        fixedTimeNow = LocalDateTime.of(2025, 8, 1, 12, 0, 0); // Une date fixe pour "maintenant"
        specificDateExpirationForUpdate = LocalDateTime.of(2025, 10, 10, 12, 0, 0);


        disciplineExistante = Discipline.builder()
                .idDiscipline(1L)
                .nomDiscipline("Football")
                .dateDiscipline(fixedTimeNow.plusMonths(2)) // La discipline a lieu dans 2 mois
                .build();

        offreExistante = Offre.builder()
                .idOffre(1L)
                .discipline(disciplineExistante)
                .typeOffre(TypeOffre.SOLO)
                .prix(new BigDecimal("50.00"))
                .quantite(100)
                .capacite(1)
                .statutOffre(StatutOffre.DISPONIBLE)
                .dateExpiration(fixedTimeNow.plusDays(30)) // Expiration propre à l'offre
                .featured(false)
                .build();

        offreAutre = Offre.builder()
                .idOffre(2L)
                .discipline(disciplineExistante)
                .typeOffre(TypeOffre.DUO)
                .prix(new BigDecimal("90.00"))
                .quantite(50)
                .capacite(2)
                .statutOffre(StatutOffre.DISPONIBLE)
                .dateExpiration(fixedTimeNow.plusDays(60))
                .featured(true)
                .build();

        creerOffreDto = CreerOffreDto.builder()
                .idDiscipline(disciplineExistante.getIdDiscipline())
                .typeOffre(TypeOffre.SOLO)
                .prix(new BigDecimal("60.00"))
                .quantite(120)
                .capacite(1)
                .statutOffre(StatutOffre.DISPONIBLE)
                .featured(false)
                .dateExpiration(fixedTimeNow.plusDays(45)) // Date d'expiration pour la création
                .build();

        mettreAJourOffreDto = MettreAJourOffreDto.builder()
                .idDiscipline(disciplineExistante.getIdDiscipline())
                .typeOffre(TypeOffre.FAMILIALE)
                .prix(new BigDecimal("150.00"))
                .quantite(80)
                .capacite(4)
                .statutOffre(StatutOffre.DISPONIBLE)
                .featured(true)
                .dateExpiration(specificDateExpirationForUpdate) // Date d'expiration spécifique pour la mise à jour
                .build();
    }

    @Test
    void ajouterOffre_ValidInput_ReturnsOffreAdminDto() {
        // Arrange
        when(disciplineRepository.findById(creerOffreDto.getIdDiscipline())).thenReturn(Optional.of(disciplineExistante));

        // Simuler ce que le repository retournerait après sauvegarde
        Offre offreSauvegardeeSimulee = new Offre();
        // Le service va mapper creerOffreDto vers une nouvelle instance de Offre
        // puis la sauvegarder. On simule le résultat de cette sauvegarde.
        modelMapper.map(creerOffreDto, offreSauvegardeeSimulee); // Simule le mapping interne du service
        offreSauvegardeeSimulee.setIdOffre(3L); // Simule l'ID généré
        offreSauvegardeeSimulee.setDiscipline(disciplineExistante); // Le service lie la discipline

        when(offreRepository.save(any(Offre.class))).thenReturn(offreSauvegardeeSimulee);

        // Act
        OffreAdminDto result = adminOffreService.ajouterOffre(creerOffreDto);

        // Assert
        assertNotNull(result);
        assertEquals(offreSauvegardeeSimulee.getIdOffre(), result.getId());
        assertEquals(creerOffreDto.getIdDiscipline(), result.getIdDiscipline());
        assertEquals(creerOffreDto.getTypeOffre(), result.getTypeOffre());
        // La date d'expiration du DTO de retour doit être l'effective date de l'offre sauvegardée
        assertEquals(offreSauvegardeeSimulee.getEffectiveDateExpiration(), result.getDateExpiration());

        // Vérifier les interactions
        verify(disciplineRepository).findById(creerOffreDto.getIdDiscipline());
        verify(offreRepository).save(any(Offre.class));

        // Vérifier les appels au ModelMapper DANS LE SERVICE
        // 1. creerOffreDto -> nouvelle instance de Offre
        // 2. offreSauvegardeeSimulee -> OffreAdminDto
        // Il ne devrait pas y avoir d'appels à modelMapper.map DANS CE TEST en dehors de ceux faits par le service.
        // Le spy permet de vérifier ces appels internes.
        ArgumentCaptor<Object> sourceCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Class<?>> targetClassCaptor = ArgumentCaptor.forClass(Class.class);
        verify(modelMapper, times(2)).map(sourceCaptor.capture(), targetClassCaptor.capture());

        List<Object> sources = sourceCaptor.getAllValues();
        List<Class<?>> targetClasses = targetClassCaptor.getAllValues();

        // Premier appel: CreerOffreDto -> Offre
        assertTrue(sources.get(0) instanceof CreerOffreDto);
        assertEquals(Offre.class, targetClasses.get(0));

        // Deuxième appel: Offre -> OffreAdminDto
        assertTrue(sources.get(1) instanceof Offre);
        assertEquals(OffreAdminDto.class, targetClasses.get(1));
    }

    @Test
    void mettreAJourOffre_ValidInput_ReturnsOffreAdminDto() {
        // Arrange
        Long offreIdAMettreAJour = offreExistante.getIdOffre();
        // L'offre existante a sa propre date d'expiration: fixedTimeNow.plusDays(30)
        // Sa discipline a une date: fixedTimeNow.plusMonths(2)
        // Donc, sa date effective d'expiration est fixedTimeNow.plusDays(30)

        // mettreAJourOffreDto a une date d'expiration: specificDateExpirationForUpdate (2025-10-10T12:00)
        // La discipline reste la même (expiration fixedTimeNow.plusMonths(2) ~ 2025-10-01T12:00)

        when(offreRepository.findById(offreIdAMettreAJour)).thenReturn(Optional.of(offreExistante));
        when(disciplineRepository.findById(mettreAJourOffreDto.getIdDiscipline())).thenReturn(Optional.of(disciplineExistante));
        when(offreRepository.save(any(Offre.class))).thenAnswer(invocation -> {
            // Simuler que l'entité passée à save est celle qui est retournée
            // et que modelMapper.map(dto, entite) a déjà eu lieu dans le service.
            Offre offreModifiee = invocation.getArgument(0);
            // Appliquer les changements du DTO à l'offre pour simuler l'état après mapping
            // Cette simulation est délicate car le spy modelMapper est déjà en jeu.
            // Le plus simple est de s'assurer que l'objet retourné par save() a les bonnes valeurs
            // qui seront ensuite utilisées pour le mapping vers OffreAdminDto.
            // Ici, on assume que offreExistante est modifiée en place par le service.
            offreExistante.setTypeOffre(mettreAJourOffreDto.getTypeOffre());
            offreExistante.setQuantite(mettreAJourOffreDto.getQuantite());
            offreExistante.setPrix(mettreAJourOffreDto.getPrix());
            offreExistante.setDateExpiration(mettreAJourOffreDto.getDateExpiration()); // La date du DTO est appliquée
            offreExistante.setStatutOffre(mettreAJourOffreDto.getStatutOffre());
            offreExistante.setCapacite(mettreAJourOffreDto.getCapacite());
            offreExistante.setFeatured(mettreAJourOffreDto.isFeatured());
            offreExistante.setDiscipline(disciplineExistante); // Ré-associer
            return offreExistante;
        });

        // Act
        OffreAdminDto result = adminOffreService.mettreAJourOffre(offreIdAMettreAJour, mettreAJourOffreDto);

        // Assert
        assertNotNull(result);
        assertEquals(offreIdAMettreAJour, result.getId());
        assertEquals(mettreAJourOffreDto.getTypeOffre(), result.getTypeOffre());

        // L'offre existante a discipline.dateDiscipline = fixedTimeNow.plusMonths(2) (approx 2025-10-01)
        // mettreAJourOffreDto.dateExpiration = specificDateExpirationForUpdate (2025-10-10)
        // Donc, la date effective d'expiration de l'offre mise à jour sera celle de la discipline (la plus proche).
        LocalDateTime expectedEffectiveDate = disciplineExistante.getDateDiscipline();
        assertEquals(expectedEffectiveDate, result.getDateExpiration(), "La date d'expiration du DTO de retour doit être la date effective la plus proche.");

        verify(modelMapper).map(eq(mettreAJourOffreDto), eq(offreExistante)); // DTO -> Entité
        verify(offreRepository).save(eq(offreExistante));
        verify(modelMapper).map(eq(offreExistante), eq(OffreAdminDto.class)); // Entité mise à jour -> DTO
    }

    // ... (autres tests, en s'assurant que la pagination est gérée pour obtenirToutesLesOffres)
    @Test
    void obtenirToutesLesOffres_ReturnsPageOfOffreAdminDto() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Offre> offresList = Arrays.asList(offreExistante, offreAutre);
        Page<Offre> offresPageEntites = new PageImpl<>(offresList, pageable, offresList.size());

        when(offreRepository.findAllWithDiscipline(pageable)).thenReturn(offresPageEntites);

        Page<OffreAdminDto> resultPageDto = adminOffreService.obtenirToutesLesOffres(pageable);

        assertNotNull(resultPageDto);
        assertThat(resultPageDto.getContent()).hasSize(2);
        assertEquals(offreExistante.getEffectiveDateExpiration(), resultPageDto.getContent().get(0).getDateExpiration());
        assertEquals(offreAutre.getEffectiveDateExpiration(), resultPageDto.getContent().get(1).getDateExpiration());

        verify(offreRepository).findAllWithDiscipline(eq(pageable));
        verify(modelMapper, times(1)).map(eq(offreExistante), eq(OffreAdminDto.class));
        verify(modelMapper, times(1)).map(eq(offreAutre), eq(OffreAdminDto.class));
    }


    // ... (les tests pour getNombreDeVentesParOffre, supprimerOffre et les exceptions)
    @Test
    void supprimerOffre_ExistingId_CallsDeleteAndNotifyPanierService() {
        Long offreIdASupprimer = offreExistante.getIdOffre();
        when(offreRepository.findById(offreIdASupprimer)).thenReturn(Optional.of(offreExistante));
        doNothing().when(panierService).supprimerOffreDeTousLesPaniers(any(Offre.class));

        adminOffreService.supprimerOffre(offreIdASupprimer);

        verify(offreRepository).findById(offreIdASupprimer);
        verify(panierService).supprimerOffreDeTousLesPaniers(eq(offreExistante));
        verify(offreRepository).delete(eq(offreExistante));
    }

    @Test
    void ajouterOffre_DisciplineNotFound_ThrowsResourceNotFoundException() {
        when(disciplineRepository.findById(creerOffreDto.getIdDiscipline())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> adminOffreService.ajouterOffre(creerOffreDto));
        verify(offreRepository, never()).save(any(Offre.class));
    }
}
