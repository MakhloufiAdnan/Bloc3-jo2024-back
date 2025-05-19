package fr.studi.bloc3jo2024.service.impl;

import fr.studi.bloc3jo2024.dto.panier.AjouterOffrePanierDto;
import fr.studi.bloc3jo2024.dto.panier.ContenuPanierDto;
import fr.studi.bloc3jo2024.dto.panier.PanierDto;
import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.StatutPanier;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat; // Importation statique pour AssertJ
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour {@link PanierServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class PanierServiceImplTest {

    @Mock private PanierRepository panierRepository;
    @Mock private ContenuPanierRepository contenuPanierRepository;
    @Mock private OffreRepository offreRepository;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private DisciplineRepository disciplineRepository;

    @Spy // ModelMapper est espionné pour utiliser sa vraie logique de mapping
    private ModelMapper modelMapper = new ModelMapper(); // Initialisé ici, Mockito créera un spy autour.

    @InjectMocks
    private PanierServiceImpl panierService;

    private Utilisateur testUtilisateur;
    private Panier panierEnAttente;
    private Offre offreSolo;
    private Discipline disciplineCourse;
    private LocalDateTime fixedTime;

    @BeforeEach
    void setUp() {
        fixedTime = LocalDateTime.of(2025, 1, 1, 10, 0);
        assertNotNull(modelMapper); // Pour "utiliser" le spy et satisfaire certains linters IDE

        testUtilisateur = Utilisateur.builder()
                .idUtilisateur(UUID.randomUUID())
                .email("panieruser@example.com")
                .nom("PanierNom")
                .prenom("PanierPrenom")
                .build();

        panierEnAttente = Panier.builder()
                .idPanier(1L)
                .utilisateur(testUtilisateur)
                .statut(StatutPanier.EN_ATTENTE)
                .montantTotal(BigDecimal.ZERO)
                .contenuPaniers(new HashSet<>())
                .dateAjout(fixedTime.minusDays(1))
                .build();

        disciplineCourse = Discipline.builder()
                .idDiscipline(1L)
                .nomDiscipline("Course 100m")
                .nbPlaceDispo(100)
                .dateDiscipline(fixedTime.plusMonths(1))
                .build();

        offreSolo = Offre.builder()
                .idOffre(1L)
                .typeOffre(TypeOffre.SOLO)
                .prix(new BigDecimal("50.00"))
                .quantite(10)
                .capacite(1)
                .statutOffre(StatutOffre.DISPONIBLE)
                .discipline(disciplineCourse)
                .dateExpiration(fixedTime.plusDays(30))
                .build();
    }

    private void mockGetOrCreatePanier(Panier panierARetourner, boolean creerNouveau) {
        when(utilisateurRepository.findById(testUtilisateur.getIdUtilisateur())).thenReturn(Optional.of(testUtilisateur));
        if (creerNouveau) {
            when(panierRepository.findByUtilisateurIdAndStatutWithDetails(testUtilisateur.getIdUtilisateur(), StatutPanier.EN_ATTENTE))
                    .thenReturn(Optional.empty());
            when(panierRepository.save(any(Panier.class))).thenAnswer(invocation -> {
                Panier p = invocation.getArgument(0);
                p.setIdPanier(panierARetourner.getIdPanier());
                p.setDateAjout(LocalDateTime.now());
                return p;
            });
        } else {
            when(panierRepository.findByUtilisateurIdAndStatutWithDetails(testUtilisateur.getIdUtilisateur(), StatutPanier.EN_ATTENTE))
                    .thenReturn(Optional.of(panierARetourner));
        }
    }

    @Test
    void getPanierUtilisateur_existingPanier_shouldMapAndReturnDto() {
        ContenuPanier cpExistant = ContenuPanier.builder()
                .panier(panierEnAttente)
                .offre(offreSolo)
                .quantiteCommandee(1)
                .build();
        panierEnAttente.getContenuPaniers().add(cpExistant);
        panierEnAttente.setMontantTotal(new BigDecimal("50.00"));
        mockGetOrCreatePanier(panierEnAttente, false);

        // Act
        PanierDto result = panierService.getPanierUtilisateur(testUtilisateur.getIdUtilisateur().toString());

        // Assert
        assertNotNull(result);
        assertEquals(panierEnAttente.getIdPanier(), result.getIdPanier());
        assertEquals(testUtilisateur.getIdUtilisateur(), result.getIdUtilisateur());
        assertThat(result.getContenuPaniers()).hasSize(1);
        assertEquals(offreSolo.getIdOffre(), result.getContenuPaniers().get(0).getIdOffre());
        assertEquals(0, new BigDecimal("50.00").compareTo(result.getMontantTotal()));
        verify(panierRepository, never()).save(any(Panier.class));
    }

    @Test
    void getPanierUtilisateur_noExistingPanier_shouldCreateMapAndReturnDto() {
        // Arrange
        Panier nouveauPanierSimule = Panier.builder().idPanier(2L).utilisateur(testUtilisateur).statut(StatutPanier.EN_ATTENTE).montantTotal(BigDecimal.ZERO).contenuPaniers(new HashSet<>()).build();
        mockGetOrCreatePanier(nouveauPanierSimule, true);

        // Act
        PanierDto result = panierService.getPanierUtilisateur(testUtilisateur.getIdUtilisateur().toString());

        // Assert
        assertNotNull(result);
        assertEquals(nouveauPanierSimule.getIdPanier(), result.getIdPanier());
        assertEquals(testUtilisateur.getIdUtilisateur(), result.getIdUtilisateur());
        assertThat(result.getContenuPaniers()).isEmpty();
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getMontantTotal()));
        verify(panierRepository).save(any(Panier.class));
    }

    @Test
    void ajouterOffreAuPanier_newOffreToExistingPanier_shouldAddAndRecalculate() {
        // Arrange
        mockGetOrCreatePanier(panierEnAttente, false);
        when(offreRepository.findById(offreSolo.getIdOffre())).thenReturn(Optional.of(offreSolo));
        when(disciplineRepository.findById(disciplineCourse.getIdDiscipline())).thenReturn(Optional.of(disciplineCourse));

        AjouterOffrePanierDto ajoutDto = new AjouterOffrePanierDto(offreSolo.getIdOffre(), 2);

        // Act
        PanierDto result = panierService.ajouterOffreAuPanier(testUtilisateur.getIdUtilisateur().toString(), ajoutDto);

        // Assert
        assertNotNull(result);
        assertThat(result.getContenuPaniers()).hasSize(1);
        ContenuPanierDto contenuResultat = result.getContenuPaniers().get(0);
        assertEquals(offreSolo.getIdOffre(), contenuResultat.getIdOffre());
        assertEquals(2, contenuResultat.getQuantiteCommandee());
        assertEquals(0, new BigDecimal("100.00").compareTo(result.getMontantTotal()), "Le montant total doit être 2 * 50.00");

        ArgumentCaptor<Panier> panierCaptor = ArgumentCaptor.forClass(Panier.class);
        verify(panierRepository).save(panierCaptor.capture());
        Panier panierSauvegarde = panierCaptor.getValue();
        assertEquals(0, new BigDecimal("100.00").compareTo(panierSauvegarde.getMontantTotal()));
        assertThat(panierSauvegarde.getContenuPaniers()).hasSize(1);
    }


    @Test
    void supprimerOffreDeTousLesPaniers_shouldDeleteContentsAndUpdateAffectedPaniers() {
        // Arrange
        Long offreIdToDelete = offreSolo.getIdOffre();
        Panier panierAffecte = Panier.builder()
                .idPanier(10L).utilisateur(testUtilisateur).statut(StatutPanier.EN_ATTENTE)
                .montantTotal(new BigDecimal("80.00"))
                .contenuPaniers(new HashSet<>())
                .build();

        Offre autreOffre = Offre.builder().idOffre(2L).prix(new BigDecimal("30.00")).discipline(disciplineCourse).build();
        // Pas besoin de .idContenuPanier(...) ici
        ContenuPanier cpOffreASupprimer = ContenuPanier.builder().offre(offreSolo).quantiteCommandee(1).panier(panierAffecte).build();
        ContenuPanier cpAutreOffre = ContenuPanier.builder().offre(autreOffre).quantiteCommandee(1).panier(panierAffecte).build();
        panierAffecte.getContenuPaniers().addAll(Arrays.asList(cpOffreASupprimer, cpAutreOffre));

        when(panierRepository.findPaniersContenantOffreWithDetails(offreIdToDelete)).thenReturn(Collections.singletonList(panierAffecte));
        when(contenuPanierRepository.deleteByOffreId(offreIdToDelete)).thenReturn(1);

        ArgumentCaptor<Panier> panierCaptor = ArgumentCaptor.forClass(Panier.class);
        when(panierRepository.save(panierCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        panierService.supprimerOffreDeTousLesPaniers(offreSolo);

        // Assert
        verify(contenuPanierRepository).deleteByOffreId(offreIdToDelete);
        verify(panierRepository).findPaniersContenantOffreWithDetails(offreIdToDelete);
        verify(panierRepository).save(any(Panier.class));

        Panier panierSauvegarde = panierCaptor.getValue();
        assertNotNull(panierSauvegarde, "Le panier sauvegardé ne devrait pas être null.");
        assertEquals(panierAffecte.getIdPanier(), panierSauvegarde.getIdPanier());
        assertEquals(0, new BigDecimal("30.00").compareTo(panierSauvegarde.getMontantTotal()),
                "Le montant total du panier affecté devrait être recalculé à 30.00. Actuel: " + panierSauvegarde.getMontantTotal());
    }


    @Test
    void finaliserAchat_validPanier_shouldSucceedAndDecrementStock() {
        // Arrange
        int initialStockOffreSolo = offreSolo.getQuantite();
        // int initialPlacesDiscipline = disciplineCourse.getNbPlaceDispo(); // Peut être enlevé si non utilisé directement dans une assertion
        int quantiteCommandee = 1;

        // Pas besoin de .idContenuPanier(...)
        ContenuPanier cpSolo = ContenuPanier.builder()
                .panier(panierEnAttente)
                .offre(offreSolo)
                .quantiteCommandee(quantiteCommandee)
                .build();
        panierEnAttente.getContenuPaniers().add(cpSolo);
        panierEnAttente.setMontantTotal(offreSolo.getPrix().multiply(BigDecimal.valueOf(quantiteCommandee)));

        mockGetOrCreatePanier(panierEnAttente, false);
        when(disciplineRepository.findById(disciplineCourse.getIdDiscipline())).thenReturn(Optional.of(disciplineCourse));
        when(offreRepository.findById(offreSolo.getIdOffre())).thenReturn(Optional.of(offreSolo));
        when(disciplineRepository.decrementerPlaces(eq(disciplineCourse.getIdDiscipline()), eq(offreSolo.getCapacite() * quantiteCommandee))).thenReturn(1);
        when(offreRepository.save(any(Offre.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(panierRepository.save(any(Panier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PanierDto result = panierService.finaliserAchat(testUtilisateur.getIdUtilisateur().toString());

        // Assert
        assertNotNull(result);
        assertEquals(StatutPanier.PAYE, result.getStatut());

        ArgumentCaptor<Panier> panierCaptor = ArgumentCaptor.forClass(Panier.class);
        verify(panierRepository).save(panierCaptor.capture());
        assertEquals(StatutPanier.PAYE, panierCaptor.getValue().getStatut());

        ArgumentCaptor<Offre> offreCaptor = ArgumentCaptor.forClass(Offre.class);
        verify(offreRepository).save(offreCaptor.capture());
        Offre offreSauvegardee = offreCaptor.getValue();
        assertEquals(initialStockOffreSolo - quantiteCommandee, offreSauvegardee.getQuantite(),
                "La quantité de l'offre (stock) devrait diminuer.");

        verify(disciplineRepository).decrementerPlaces(disciplineCourse.getIdDiscipline(), offreSolo.getCapacite() * quantiteCommandee);
    }

    @Test
    void finaliserAchat_offreNotFound_throwsResourceNotFoundException() {
        // Arrange
        // Pas besoin de .idContenuPanier(...)
        ContenuPanier cpOffreInconnue = ContenuPanier.builder()
                .offre(Offre.builder().idOffre(999L).discipline(disciplineCourse).build()) // Offre avec ID qui ne sera pas trouvé
                .quantiteCommandee(1)
                .panier(panierEnAttente)
                .build();
        panierEnAttente.getContenuPaniers().add(cpOffreInconnue);
        mockGetOrCreatePanier(panierEnAttente, false);
        when(offreRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> panierService.finaliserAchat(testUtilisateur.getIdUtilisateur().toString()));
        assertTrue(exception.getMessage().contains("Offre non trouvée avec l'ID : 999"));
    }

    @Test
    void finaliserAchat_disciplineNotFoundForOffre_throwsResourceNotFoundException() {
        // Arrange
        // Pas besoin de .idContenuPanier(...)
        ContenuPanier cp = ContenuPanier.builder()
                .offre(offreSolo)
                .quantiteCommandee(1)
                .panier(panierEnAttente)
                .build();
        panierEnAttente.getContenuPaniers().add(cp);
        mockGetOrCreatePanier(panierEnAttente, false);
        when(offreRepository.findById(offreSolo.getIdOffre())).thenReturn(Optional.of(offreSolo));
        when(disciplineRepository.findById(disciplineCourse.getIdDiscipline())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> panierService.finaliserAchat(testUtilisateur.getIdUtilisateur().toString()));
        assertTrue(exception.getMessage().contains("Discipline non trouvée avec l'ID : " + disciplineCourse.getIdDiscipline()));
    }
}