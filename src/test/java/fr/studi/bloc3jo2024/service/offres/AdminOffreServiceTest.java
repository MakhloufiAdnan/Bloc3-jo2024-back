package fr.studi.bloc3jo2024.service.offres;

import fr.studi.bloc3jo2024.dto.offres.CreerOffreDto;
import fr.studi.bloc3jo2024.dto.offres.MettreAJourOffreDto;
import fr.studi.bloc3jo2024.dto.offres.OffreAdminDto;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import fr.studi.bloc3jo2024.repository.DisciplineRepository;
import fr.studi.bloc3jo2024.repository.OffreRepository;
import fr.studi.bloc3jo2024.service.PanierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminOffreServiceTest {

    @InjectMocks
    private AdminOffreService adminOffreService;

    @Mock
    private OffreRepository offreRepository;

    @Spy
    private ModelMapper modelMapper = new ModelMapper(); // ModelMapper espionné

    @Mock
    private DisciplineRepository disciplineRepository;

    @Mock
    private PanierService panierService; // Mock si AdminOffreService l'utilise

    private Discipline discipline;
    private Offre offre1;

    private CreerOffreDto creerOffreDto;
    private MettreAJourOffreDto mettreAJourOffreDto;

    @BeforeEach
    void setUp() {
        discipline = Discipline.builder().idDiscipline(1L).nomDiscipline("Football").build();

        offre1 = Offre.builder()
                .idOffre(1L)
                .discipline(discipline)
                .typeOffre(TypeOffre.SOLO)
                .prix(new BigDecimal("50.00"))
                .quantite(100)
                .capacite(1)
                .statutOffre(StatutOffre.DISPONIBLE)
                .dateExpiration(LocalDateTime.now().plusDays(30))
                .featured(false)
                .build();

        creerOffreDto = new CreerOffreDto();
        creerOffreDto.setIdDiscipline(1L);
        creerOffreDto.setTypeOffre(TypeOffre.SOLO);
        creerOffreDto.setPrix(new BigDecimal("60.00"));
        creerOffreDto.setQuantite(120);
        creerOffreDto.setCapacite(1);
        creerOffreDto.setStatutOffre(StatutOffre.DISPONIBLE);
        creerOffreDto.setFeatured(false);
        creerOffreDto.setDateExpiration(LocalDateTime.now().plusDays(45));


        mettreAJourOffreDto = new MettreAJourOffreDto();
        mettreAJourOffreDto.setIdDiscipline(1L); // Assumons la même discipline pour simplifier
        mettreAJourOffreDto.setTypeOffre(TypeOffre.FAMILIALE);
        mettreAJourOffreDto.setPrix(new BigDecimal("150.00"));
        mettreAJourOffreDto.setQuantite(80);
        mettreAJourOffreDto.setCapacite(4);
        mettreAJourOffreDto.setStatutOffre(StatutOffre.DISPONIBLE);
        mettreAJourOffreDto.setFeatured(true);
        mettreAJourOffreDto.setDateExpiration(LocalDateTime.now().plusDays(70));
    }

    @Test
    void ajouterOffre_ValidInput_ReturnsOffreAdminDto() {
        // Arrange
        when(disciplineRepository.findById(creerOffreDto.getIdDiscipline())).thenReturn(Optional.of(discipline));

        Offre offreSauvegardeSimulee = new Offre();
        modelMapper.map(creerOffreDto, offreSauvegardeSimulee); // Simuler le mapping que le service ferait
        offreSauvegardeSimulee.setIdOffre(3L); // Simuler l'ID généré par la DB
        offreSauvegardeSimulee.setDiscipline(discipline); // Assurer que la discipline est liée

        when(offreRepository.save(any(Offre.class))).thenReturn(offreSauvegardeSimulee);

        // Act
        OffreAdminDto result = adminOffreService.ajouterOffre(creerOffreDto);

        // Assert
        assertNotNull(result);
        assertEquals(offreSauvegardeSimulee.getIdOffre(), result.getId());
        assertEquals(creerOffreDto.getIdDiscipline(), result.getIdDiscipline());
        assertEquals(creerOffreDto.getTypeOffre(), result.getTypeOffre());
        assertEquals(creerOffreDto.isFeatured(), result.isFeatured());
        assertEquals(creerOffreDto.getDateExpiration(), result.getDateExpiration());


        // Vérifier que disciplineRepository.findById a été appelé
        verify(disciplineRepository).findById(creerOffreDto.getIdDiscipline());
        // Vérifier que offreRepository.save a été appelé
        verify(offreRepository).save(any(Offre.class));
        // Vérifier les appels à modelMapper (Spy)
        verify(modelMapper).map(eq(creerOffreDto), any(Offre.class)); // DTO -> Entité
        verify(modelMapper).map(eq(offreSauvegardeSimulee), eq(OffreAdminDto.class)); // Entité sauvegardée -> DTO
    }

    @Test
    void mettreAJourOffre_ValidInput_ReturnsOffreAdminDto() {
        Long offreId = 1L;

        when(offreRepository.findById(offreId)).thenReturn(Optional.of(offre1));
        when(disciplineRepository.findById(mettreAJourOffreDto.getIdDiscipline())).thenReturn(Optional.of(discipline));

        Offre offreMiseAJourSimulee = new Offre();
        modelMapper.map(mettreAJourOffreDto, offreMiseAJourSimulee);
        offreMiseAJourSimulee.setIdOffre(offreId);
        offreMiseAJourSimulee.setDiscipline(discipline);

        when(offreRepository.save(any(Offre.class))).thenReturn(offreMiseAJourSimulee);

        // Act
        OffreAdminDto result = adminOffreService.mettreAJourOffre(offreId, mettreAJourOffreDto);

        // Assert
        assertNotNull(result);
        assertEquals(offreId, result.getId());
        assertEquals(mettreAJourOffreDto.getTypeOffre(), result.getTypeOffre());
        assertEquals(mettreAJourOffreDto.getPrix(), result.getPrix());
        assertEquals(mettreAJourOffreDto.isFeatured(), result.isFeatured());
        assertEquals(mettreAJourOffreDto.getDateExpiration(), result.getDateExpiration());

        verify(offreRepository).findById(offreId);
        verify(disciplineRepository).findById(mettreAJourOffreDto.getIdDiscipline());
        verify(modelMapper).map(eq(mettreAJourOffreDto), eq(offre1));
        verify(offreRepository).save(eq(offre1));
        verify(modelMapper).map(eq(offreMiseAJourSimulee), eq(OffreAdminDto.class));
    }

    @Test
    void obtenirOffreParId_ExistingId_ReturnsOffreAdminDto() {
        when(offreRepository.findById(1L)).thenReturn(Optional.of(offre1));
        OffreAdminDto mappedDto = new OffreAdminDto(); // Remplir avec les valeurs de offre1
        modelMapper.map(offre1, mappedDto);
        when(modelMapper.map(offre1, OffreAdminDto.class)).thenReturn(mappedDto);


        OffreAdminDto result = adminOffreService.obtenirOffreParId(1L);

        assertNotNull(result);
        assertEquals(offre1.getIdOffre(), result.getId());
        verify(modelMapper).map(offre1, OffreAdminDto.class);
    }
}