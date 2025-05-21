package fr.studi.bloc3jo2024.service.impl;

import fr.studi.bloc3jo2024.dto.panier.AjouterOffrePanierDto;
import fr.studi.bloc3jo2024.dto.panier.ContenuPanierDto;
import fr.studi.bloc3jo2024.dto.panier.ModifierContenuPanierDto;
import fr.studi.bloc3jo2024.dto.panier.PanierDto;
import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.StatutPanier;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.ContenuPanierRepository;
import fr.studi.bloc3jo2024.repository.DisciplineRepository;
import fr.studi.bloc3jo2024.repository.OffreRepository;
import fr.studi.bloc3jo2024.repository.PanierRepository;
import fr.studi.bloc3jo2024.repository.UtilisateurRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Classe de tests unitaires pour PanierServiceImpl.
 * Chaque méthode publique du service est testée pour différents scénarios.
 */
@ExtendWith(MockitoExtension.class)
class PanierServiceImplTest {

    @Mock
    private PanierRepository panierRepository;
    @Mock
    private ContenuPanierRepository contenuPanierRepository;
    @Mock
    private OffreRepository offreRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private DisciplineRepository disciplineRepository;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PanierServiceImpl panierService;

    // Constantes pour les tests
    private final UUID utilisateurId = UUID.randomUUID();
    private final String utilisateurIdStr = utilisateurId.toString();
    private final Long offreId = 1L;
    private final Long autreOffreId = 2L;
    private final Long disciplineId = 10L;
    private final Long panierId = 100L;
    private LocalDateTime fixedDate = LocalDateTime.now();

    // Messages d'erreur
    private static final String UTILISATEUR_NOT_FOUND_MSG_PREFIX = "Utilisateur non trouvé avec l'ID : ";
    private static final String OFFRE_NOT_IN_PANIER_MSG_FORMAT = "L'offre avec l'ID %d n'est pas dans le panier";
    private static final String QUANTITE_INVALIDE_OU_NON_DISPONIBLE_MSG = "Quantité invalide ou offre non disponible.";
    private static final String PLACES_INSUFFISANTES_MSG_PREFIX = "Nombre de places disponibles insuffisant pour l'offre ou la discipline.";
    private static final String PANIER_DEJA_PAYE_MSG_PREFIX = "Le panier ne peut pas être payé car son statut est : ";
    private static final String PANIER_VIDE_MSG = "Le panier est vide. Impossible de finaliser l'achat.";
    private static final String OFFRE_DISCIPLINE_NULL_MSG_FORMAT = "L'offre avec l'ID %d n'est associée à aucune discipline ou l'objet discipline est nul.";
    private static final String STOCK_INSUFFISANT_FINALISATION_MSG_FORMAT = "Stock de l'offre (%d) insuffisant au moment de la finalisation.";
    private static final String QUANTITE_MODIFIER_NEGATIVE_MSG = "La quantité à modifier ne peut pas être négative.";
    private static final String UTILISATEUR_ID_FORMAT_INVALID_MSG_PREFIX = "Format de l'ID utilisateur invalide : ";

    private Utilisateur utilisateurMock;
    private Panier panierMock;
    private Offre offreMock;
    private Discipline disciplineMock;

    @BeforeEach
    void setUp() {
        utilisateurMock = Utilisateur.builder().idUtilisateur(utilisateurId).nom("Doe").prenom("John").build();
        disciplineMock = Discipline.builder().idDiscipline(disciplineId).nbPlaceDispo(100).nomDiscipline("Football").dateDiscipline(fixedDate.plusDays(10)).build();
        offreMock = Offre.builder().idOffre(offreId).prix(BigDecimal.TEN).statutOffre(StatutOffre.DISPONIBLE).quantite(50).capacite(1).discipline(disciplineMock).typeOffre(TypeOffre.SOLO).build();
        panierMock = Panier.builder().idPanier(panierId).utilisateur(utilisateurMock).statut(StatutPanier.EN_ATTENTE).montantTotal(BigDecimal.ZERO).contenuPaniers(new HashSet<>()).dateAjout(fixedDate).build();
        panierService.setSelf(panierService);
    }

    /**
     * Helper pour simuler le mappage manuel d'un ContenuPanier vers ContenuPanierDto.
     */
    private ContenuPanierDto mapContenuToDtoManuellement(ContenuPanier cp) {
        if (cp == null || cp.getOffre() == null || cp.getOffre().getPrix() == null) return null;
        Offre offreAssociee = cp.getOffre();
        ContenuPanierDto dto = new ContenuPanierDto();
        dto.setIdOffre(offreAssociee.getIdOffre());
        dto.setQuantiteCommandee(cp.getQuantiteCommandee());
        dto.setPrixUnitaire(offreAssociee.getPrix());
        dto.setTypeOffre(offreAssociee.getTypeOffre() != null ? offreAssociee.getTypeOffre() : TypeOffre.SOLO);
        if (dto.getPrixUnitaire() != null && dto.getQuantiteCommandee() > 0) {
            dto.setPrixTotalOffre(dto.getPrixUnitaire().multiply(BigDecimal.valueOf(dto.getQuantiteCommandee())));
        } else {
            dto.setPrixTotalOffre(BigDecimal.ZERO);
        }
        return dto;
    }

    /**
     * Helper pour construire le PanierDto final attendu, simulant la logique de PanierServiceImpl.mapPanierToDto.
     */
    private PanierDto buildCompleteExpectedPanierDto(Panier sourcePanier) {
        if (sourcePanier == null) return null;

        PanierDto finalDto = new PanierDto();
        // Simule les champs que ModelMapper mapperait par défaut
        finalDto.setIdPanier(sourcePanier.getIdPanier());
        finalDto.setMontantTotal(sourcePanier.getMontantTotal()); // Ce montant est celui après recalculs potentiels
        finalDto.setStatut(sourcePanier.getStatut());
        finalDto.setDateAjout(sourcePanier.getDateAjout());

        if (sourcePanier.getUtilisateur() != null && sourcePanier.getUtilisateur().getIdUtilisateur() != null) {
            finalDto.setIdUtilisateur(sourcePanier.getUtilisateur().getIdUtilisateur());
        } else {
            finalDto.setIdUtilisateur(null);
        }

        if (sourcePanier.getContenuPaniers() == null) {
            finalDto.setContenuPaniers(Collections.emptyList());
        } else {
            List<ContenuPanierDto> dtos = sourcePanier.getContenuPaniers().stream()
                    .filter(contenu -> contenu != null && contenu.getOffre() != null && contenu.getOffre().getPrix() != null)
                    .map(this::mapContenuToDtoManuellement)
                    .toList();
            finalDto.setContenuPaniers(dtos);
        }
        return finalDto;
    }

    /**
     * Configure le mock de ModelMapper pour l'appel initial panier -> panierDto.
     */
    private PanierDto setupModelMapperBasePanierMapping(Panier sourcePanier) {
        PanierDto intermediateDto = new PanierDto();
        intermediateDto.setIdPanier(sourcePanier.getIdPanier());
        intermediateDto.setMontantTotal(sourcePanier.getMontantTotal());
        intermediateDto.setStatut(sourcePanier.getStatut());
        intermediateDto.setDateAjout(sourcePanier.getDateAjout());
        // idUtilisateur et contenuPaniers seront gérés par le service
        when(modelMapper.map(sourcePanier, PanierDto.class)).thenReturn(intermediateDto);
        return intermediateDto; // Retourne pour référence si besoin dans buildCompleteExpectedPanierDto
    }


    // --- Tests pour getPanierUtilisateur ---
    @Nested
    @DisplayName("Tests pour getPanierUtilisateur")
    class GetPanierUtilisateurTests {
        @Test
        @DisplayName("Devrait retourner un panier existant")
        void shouldReturnExistingPanier_whenPanierExists() {
            ContenuPanier item = ContenuPanier.builder().panier(panierMock).offre(offreMock).quantiteCommandee(1).build();
            panierMock.getContenuPaniers().add(item);
            panierMock.setMontantTotal(BigDecimal.TEN);

            when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateurMock));
            when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE)).thenReturn(Optional.of(panierMock));
            setupModelMapperBasePanierMapping(panierMock);
            PanierDto expectedFinalDto = buildCompleteExpectedPanierDto(panierMock);

            PanierDto result = panierService.getPanierUtilisateur(utilisateurIdStr);

            assertNotNull(result);
            assertEquals(expectedFinalDto, result);
            verify(modelMapper).map(panierMock, PanierDto.class);
            verify(panierRepository, never()).save(any(Panier.class));
        }

        @Test
        @DisplayName("Devrait créer un nouveau panier si aucun n'existe")
        void shouldCreateNewPanier_whenNoPanierExists() {
            Panier nouveauPanier = Panier.builder().idPanier(panierId + 1).utilisateur(utilisateurMock).statut(StatutPanier.EN_ATTENTE).montantTotal(BigDecimal.ZERO).contenuPaniers(new HashSet<>()).dateAjout(fixedDate).build();
            when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateurMock));
            when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE)).thenReturn(Optional.empty());
            ArgumentCaptor<Panier> panierCaptor = ArgumentCaptor.forClass(Panier.class);
            when(panierRepository.save(panierCaptor.capture())).thenReturn(nouveauPanier);
            setupModelMapperBasePanierMapping(nouveauPanier); // Mappage pour le panier fraîchement créé
            PanierDto expectedFinalDto = buildCompleteExpectedPanierDto(nouveauPanier);

            PanierDto result = panierService.getPanierUtilisateur(utilisateurIdStr);

            assertNotNull(result);
            assertEquals(expectedFinalDto, result);
            verify(panierRepository).save(any(Panier.class));
            assertEquals(utilisateurMock, panierCaptor.getValue().getUtilisateur());
        }

        @Test
        @DisplayName("Devrait lever ResourceNotFoundException si l'utilisateur n'est pas trouvé")
        void shouldThrowResourceNotFoundException_whenUtilisateurNotFound() {
            when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.empty());
            ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, () -> panierService.getPanierUtilisateur(utilisateurIdStr));
            assertEquals(UTILISATEUR_NOT_FOUND_MSG_PREFIX + utilisateurId, e.getMessage());
        }

        @Test
        @DisplayName("Devrait lever ResourceNotFoundException pour un ID utilisateur invalide")
        void shouldThrowResourceNotFoundException_whenUtilisateurIdFormatIsInvalid() {
            String invalidId = "invalid-uuid";
            ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, () -> panierService.getPanierUtilisateur(invalidId));
            assertEquals(UTILISATEUR_ID_FORMAT_INVALID_MSG_PREFIX + invalidId, e.getMessage());
        }
    }

    // --- Tests pour ajouterOffreAuPanier ---
    @Nested
    @DisplayName("Tests pour ajouterOffreAuPanier")
    class AjouterOffreAuPanierTests {
        private AjouterOffrePanierDto dtoAjout;

        @BeforeEach
        void setup() {
            dtoAjout = new AjouterOffrePanierDto(offreId, 1);
            panierMock.getContenuPaniers().clear();
            panierMock.setMontantTotal(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Devrait ajouter une nouvelle offre au panier vide")
        void shouldAddNewOffreToEmptyPanier() {
            when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateurMock));
            when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE)).thenReturn(Optional.of(panierMock));
            when(offreRepository.findById(offreId)).thenReturn(Optional.of(offreMock));
            when(disciplineRepository.findById(disciplineId)).thenReturn(Optional.of(disciplineMock));
            when(contenuPanierRepository.save(any(ContenuPanier.class))).thenAnswer(inv -> {
                ContenuPanier cp = inv.getArgument(0);
                // Simuler l'ajout au Set du panierMock pour que buildCompleteExpectedPanierDto fonctionne
                panierMock.getContenuPaniers().add(cp);
                return cp;
            });
            when(panierRepository.save(panierMock)).thenAnswer(inv -> { // Capture le panier après recalculTotal
                Panier p = inv.getArgument(0);
                panierMock.setMontantTotal(p.getMontantTotal());
                return p;
            });
            PanierDto result = panierService.ajouterOffreAuPanier(utilisateurIdStr, dtoAjout);

            PanierDto expectedFinalDto = buildCompleteExpectedPanierDto(panierMock);


            assertNotNull(result);
            assertEquals(expectedFinalDto, result);
            assertEquals(1, result.getContenuPaniers().size());
            assertEquals(offreMock.getPrix(), result.getMontantTotal());
            verify(contenuPanierRepository).save(any(ContenuPanier.class));
            verify(panierRepository).save(panierMock);
        }

        @Test
        @DisplayName("Devrait mettre à jour la quantité si l'offre est déjà dans le panier")
        void shouldUpdateQuantityWhenOffreExistsInPanier() {
            ContenuPanier existingItem = ContenuPanier.builder().panier(panierMock).offre(offreMock).quantiteCommandee(1).build();
            panierMock.getContenuPaniers().add(existingItem);
            panierMock.setMontantTotal(offreMock.getPrix());
            dtoAjout.setQuantite(2); // Ajoute 2 de plus, total 3

            when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateurMock));
            when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE)).thenReturn(Optional.of(panierMock));
            when(offreRepository.findById(offreId)).thenReturn(Optional.of(offreMock));
            when(disciplineRepository.findById(disciplineId)).thenReturn(Optional.of(disciplineMock));
            when(contenuPanierRepository.save(existingItem)).thenReturn(existingItem); // L'item existant est mis à jour
            when(panierRepository.save(panierMock)).thenReturn(panierMock);

            PanierDto result = panierService.ajouterOffreAuPanier(utilisateurIdStr, dtoAjout);

            PanierDto expectedFinalDto = buildCompleteExpectedPanierDto(panierMock);

            assertNotNull(result);
            assertEquals(expectedFinalDto, result);
            assertEquals(1, result.getContenuPaniers().size());
            assertEquals(3, result.getContenuPaniers().get(0).getQuantiteCommandee());
            assertEquals(offreMock.getPrix().multiply(BigDecimal.valueOf(3)), result.getMontantTotal());
        }

        @Test @DisplayName("Devrait lever ResourceNotFoundException si Offre non trouvée")
        void shouldThrowRNE_whenOffreNotFound() {
            when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateurMock));
            when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE)).thenReturn(Optional.of(panierMock));
            when(offreRepository.findById(offreId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> panierService.ajouterOffreAuPanier(utilisateurIdStr, dtoAjout));
        }

        @Test @DisplayName("Devrait lever IllegalArgumentException pour quantité invalide")
        void shouldThrowIAE_whenInvalidQuantity() {
            offreMock.setQuantite(0); // Stock insuffisant
            when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateurMock));
            when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE)).thenReturn(Optional.of(panierMock));
            when(offreRepository.findById(offreId)).thenReturn(Optional.of(offreMock));
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> panierService.ajouterOffreAuPanier(utilisateurIdStr, dtoAjout));
            assertEquals(QUANTITE_INVALIDE_OU_NON_DISPONIBLE_MSG, e.getMessage());
        }

        @Test @DisplayName("Devrait lever IllegalStateException si discipline de l'offre est null")
        void shouldThrowISE_whenOffreDisciplineIsNull() {
            offreMock.setDiscipline(null);
            when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateurMock));
            when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE)).thenReturn(Optional.of(panierMock));
            when(offreRepository.findById(offreId)).thenReturn(Optional.of(offreMock));
            IllegalStateException e = assertThrows(IllegalStateException.class, () -> panierService.ajouterOffreAuPanier(utilisateurIdStr, dtoAjout));
            assertEquals(String.format(OFFRE_DISCIPLINE_NULL_MSG_FORMAT, offreId), e.getMessage());
        }

        @Test @DisplayName("Devrait lever IllegalStateException si places de discipline insuffisantes")
        void shouldThrowISE_whenNotEnoughPlacesInDiscipline() {
            disciplineMock.setNbPlaceDispo(0); // Pas de place
            when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateurMock));
            when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE)).thenReturn(Optional.of(panierMock));
            when(offreRepository.findById(offreId)).thenReturn(Optional.of(offreMock));
            when(disciplineRepository.findById(disciplineId)).thenReturn(Optional.of(disciplineMock));
            IllegalStateException e = assertThrows(IllegalStateException.class, () -> panierService.ajouterOffreAuPanier(utilisateurIdStr, dtoAjout));
            assertTrue(e.getMessage().startsWith(PLACES_INSUFFISANTES_MSG_PREFIX));
        }
    }

    // --- Tests pour modifierQuantiteOffrePanier ---
    @Nested
    @DisplayName("Tests pour modifierQuantiteOffrePanier")
    class ModifierQuantiteOffrePanierTests {
        private ModifierContenuPanierDto dtoModifier;
        private ContenuPanier itemDansPanier;

        @BeforeEach
        void setup() {
            itemDansPanier = ContenuPanier.builder().panier(panierMock).offre(offreMock).quantiteCommandee(2).build();
            panierMock.getContenuPaniers().add(itemDansPanier);
            panierMock.setMontantTotal(offreMock.getPrix().multiply(BigDecimal.valueOf(2)));
            dtoModifier = new ModifierContenuPanierDto(offreId, 5); // Nouvelle quantité
        }

        @Test @DisplayName("Devrait modifier la quantité avec succès")
        void shouldModifyQuantitySuccessfully() {
            when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateurMock));
            when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE)).thenReturn(Optional.of(panierMock));
            when(offreRepository.findById(offreId)).thenReturn(Optional.of(offreMock));
            when(disciplineRepository.findById(disciplineId)).thenReturn(Optional.of(disciplineMock)); // Assumer que offreMock a disciplineMock
            when(contenuPanierRepository.save(itemDansPanier)).thenReturn(itemDansPanier);
            when(panierRepository.save(panierMock)).thenReturn(panierMock);

            PanierDto result = panierService.modifierQuantiteOffrePanier(utilisateurIdStr, dtoModifier);

            PanierDto expectedFinalDto = buildCompleteExpectedPanierDto(panierMock);

            assertEquals(expectedFinalDto, result);
            assertEquals(5, result.getContenuPaniers().get(0).getQuantiteCommandee());
            assertEquals(offreMock.getPrix().multiply(BigDecimal.valueOf(5)), result.getMontantTotal());
        }

        @Test @DisplayName("Devrait supprimer l'item si la nouvelle quantité est zéro")
        void shouldRemoveItem_whenNewQuantityIsZero() {
            dtoModifier.setNouvelleQuantite(0);
            ContenuPanierId cpId = new ContenuPanierId(panierMock.getIdPanier(), offreMock.getIdOffre());

            when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateurMock));
            when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE)).thenReturn(Optional.of(panierMock));
            when(offreRepository.findById(offreId)).thenReturn(Optional.of(offreMock)); // Pour la recherche initiale de l'offre
            // Mocks pour les opérations DANS supprimerOffreDuPanier (appelé par 'self')
            doNothing().when(contenuPanierRepository).deleteById(cpId);
            when(panierRepository.save(panierMock)).thenAnswer(inv -> {
                panierMock.getContenuPaniers().remove(itemDansPanier); // Simuler la suppression
                panierMock.setMontantTotal(BigDecimal.ZERO); // Simuler recalcul
                return panierMock;
            });

            PanierDto result = panierService.modifierQuantiteOffrePanier(utilisateurIdStr, dtoModifier);

            PanierDto expectedFinalDto = buildCompleteExpectedPanierDto(panierMock);

            assertEquals(expectedFinalDto, result);
            assertTrue(result.getContenuPaniers().isEmpty());
            assertEquals(BigDecimal.ZERO, result.getMontantTotal());
            verify(contenuPanierRepository).deleteById(cpId);
        }

        @Test @DisplayName("Devrait lever IllegalArgumentException pour quantité négative")
        void shouldThrowIAE_whenNewQuantityIsNegative() {
            dtoModifier.setNouvelleQuantite(-1);
            when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateurMock));
            when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE)).thenReturn(Optional.of(panierMock));
            when(offreRepository.findById(offreId)).thenReturn(Optional.of(offreMock));

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> panierService.modifierQuantiteOffrePanier(utilisateurIdStr, dtoModifier));
            assertEquals(QUANTITE_MODIFIER_NEGATIVE_MSG, e.getMessage());
        }

        @Test @DisplayName("Devrait lever ResourceNotFoundException si l'offre à modifier n'est pas dans le panier")
        void shouldThrowRNE_whenOffreToModifyNotFoundInCart() {
            panierMock.getContenuPaniers().clear(); // Vider le panier
            dtoModifier.setIdOffre(offreId); // Tenter de modifier une offre qui n'y est plus

            when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateurMock));
            when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE)).thenReturn(Optional.of(panierMock));
            when(offreRepository.findById(offreId)).thenReturn(Optional.of(offreMock)); // L'offre existe, mais pas dans le panier

            ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, () -> panierService.modifierQuantiteOffrePanier(utilisateurIdStr, dtoModifier));
            assertEquals(String.format(OFFRE_NOT_IN_PANIER_MSG_FORMAT, offreId), e.getMessage());
        }
    }

    // --- Tests pour supprimerOffreDuPanier ---
    @Nested
    @DisplayName("Tests pour supprimerOffreDuPanier")
    class SupprimerOffreDuPanierTests {
        private ContenuPanier itemASupprimer;
        private Offre autreOffre;
        private ContenuPanier itemARester;

        @BeforeEach
        void setup() {
            itemASupprimer = ContenuPanier.builder().panier(panierMock).offre(offreMock).quantiteCommandee(1).build();
            autreOffre = Offre.builder().idOffre(autreOffreId).prix(BigDecimal.valueOf(5)).statutOffre(StatutOffre.DISPONIBLE).quantite(10).capacite(1).discipline(disciplineMock).typeOffre(TypeOffre.SOLO).build();
            itemARester = ContenuPanier.builder().panier(panierMock).offre(autreOffre).quantiteCommandee(2).build();

            panierMock.getContenuPaniers().clear();
            panierMock.getContenuPaniers().add(itemASupprimer);
            panierMock.getContenuPaniers().add(itemARester);
            panierMock.setMontantTotal(offreMock.getPrix().add(autreOffre.getPrix().multiply(BigDecimal.valueOf(2))));
        }

        @Test @DisplayName("Devrait supprimer une offre spécifique du panier")
        void shouldRemoveSpecificOffre() {
            ContenuPanierId cpId = new ContenuPanierId(panierMock.getIdPanier(), offreMock.getIdOffre());
            when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateurMock));
            when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE)).thenReturn(Optional.of(panierMock));
            doNothing().when(contenuPanierRepository).deleteById(cpId);
            when(panierRepository.save(panierMock)).thenAnswer(inv -> {
                panierMock.getContenuPaniers().remove(itemASupprimer); // Simuler la suppression pour le recalcul
                panierMock.setMontantTotal(autreOffre.getPrix().multiply(BigDecimal.valueOf(2)));
                return panierMock;
            });

            PanierDto result = panierService.supprimerOffreDuPanier(utilisateurIdStr, offreId);

            PanierDto expectedFinalDto = buildCompleteExpectedPanierDto(panierMock);

            assertEquals(expectedFinalDto, result);
            assertEquals(1, result.getContenuPaniers().size());
            assertEquals(autreOffreId, result.getContenuPaniers().get(0).getIdOffre());
            verify(contenuPanierRepository).deleteById(cpId);
        }

        @Test @DisplayName("Ne devrait rien faire si l'offre à supprimer n'est pas dans le panier")
        void shouldDoNothing_ifOffreToRemoveNotInCart() {
            Long idOffreNonExistante = 999L;
            BigDecimal montantInitial = panierMock.getMontantTotal();

            when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateurMock));
            when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE)).thenReturn(Optional.of(panierMock));

            // PanierDto avant et après devraient être identiques
            PanierDto expectedFinalDto = buildCompleteExpectedPanierDto(panierMock);


            PanierDto result = panierService.supprimerOffreDuPanier(utilisateurIdStr, idOffreNonExistante);

            assertEquals(expectedFinalDto, result);
            assertEquals(2, result.getContenuPaniers().size()); // Nombre d'items inchangé
            assertEquals(montantInitial, result.getMontantTotal());
            verify(contenuPanierRepository, never()).deleteById(any());
            verify(panierRepository, never()).save(panierMock); // recalculerMontantTotal ne devrait pas sauvegarder si rien ne change
        }
    }

    // --- Tests pour viderPanier ---
    @Nested
    @DisplayName("Tests pour viderPanier")
    class ViderPanierTests {
        @Test @DisplayName("Devrait vider un panier non vide")
        void shouldClearNonEmptyPanier() {
            ContenuPanier item = ContenuPanier.builder().panier(panierMock).offre(offreMock).quantiteCommandee(1).build();
            panierMock.getContenuPaniers().add(item);
            panierMock.setMontantTotal(BigDecimal.TEN);

            when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateurMock));
            when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE)).thenReturn(Optional.of(panierMock));
            doNothing().when(contenuPanierRepository).deleteByPanier(panierMock);
            when(panierRepository.save(panierMock)).thenAnswer(inv -> {
                panierMock.getContenuPaniers().clear();
                panierMock.setMontantTotal(BigDecimal.ZERO);
                return panierMock;
            });

            PanierDto result = panierService.viderPanier(utilisateurIdStr);

            PanierDto expectedFinalDto = buildCompleteExpectedPanierDto(panierMock);

            assertEquals(expectedFinalDto, result);
            assertTrue(result.getContenuPaniers().isEmpty());
            assertEquals(BigDecimal.ZERO, result.getMontantTotal());
            verify(contenuPanierRepository).deleteByPanier(panierMock);
            verify(panierRepository).save(panierMock);
        }

        @Test @DisplayName("Ne devrait rien faire si le panier est déjà vide")
        void shouldDoNothing_ifPanierAlreadyEmpty() {
            // panierMock est déjà vide par défaut dans ce cas
            when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateurMock));
            when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE)).thenReturn(Optional.of(panierMock));

            PanierDto expectedFinalDto = buildCompleteExpectedPanierDto(panierMock);

            PanierDto result = panierService.viderPanier(utilisateurIdStr);

            assertEquals(expectedFinalDto, result);
            assertTrue(result.getContenuPaniers().isEmpty());
            assertEquals(BigDecimal.ZERO, result.getMontantTotal());
            verify(contenuPanierRepository, never()).deleteByPanier(any());
            verify(panierRepository, never()).save(any());
        }
    }

    // --- Tests pour supprimerOffreDeTousLesPaniers ---
    @Nested
    @DisplayName("Tests pour supprimerOffreDeTousLesPaniers")
    class SupprimerOffreDeTousLesPaniersTests {
        @Test
        @DisplayName("Devrait appeler deleteByOffre du repository")
        void shouldCallRepositoryDeleteByOffre() {
            doNothing().when(contenuPanierRepository).deleteByOffre(offreMock);
            panierService.supprimerOffreDeTousLesPaniers(offreMock);
            verify(contenuPanierRepository).deleteByOffre(offreMock);
        }

        @Test
        @DisplayName("Ne devrait rien faire si l'offre est nulle")
        void shouldDoNothing_whenOffreIsNull() {
            panierService.supprimerOffreDeTousLesPaniers(null);
            verifyNoInteractions(contenuPanierRepository);
        }
    }

    // --- Tests pour finaliserAchat ---
    @Nested
    @DisplayName("Tests pour finaliserAchat")
    class FinaliserAchatTests {
        private Offre offre1, offre2;
        private ContenuPanier cp1, cp2;
        private Discipline disciplineFinale;

        @BeforeEach
        void setup() {
            disciplineFinale = Discipline.builder().idDiscipline(disciplineId +1).nbPlaceDispo(20).nomDiscipline("Finale Disc").build();
            offre1 = Offre.builder().idOffre(101L).prix(BigDecimal.valueOf(20)).statutOffre(StatutOffre.DISPONIBLE).quantite(5).capacite(2).discipline(disciplineFinale).typeOffre(TypeOffre.SOLO).build();
            offre2 = Offre.builder().idOffre(102L).prix(BigDecimal.valueOf(30)).statutOffre(StatutOffre.DISPONIBLE).quantite(10).capacite(1).discipline(disciplineFinale).typeOffre(TypeOffre.DUO).build();

            cp1 = ContenuPanier.builder().panier(panierMock).offre(offre1).quantiteCommandee(2).build(); // 4 places, 40€
            cp2 = ContenuPanier.builder().panier(panierMock).offre(offre2).quantiteCommandee(3).build(); // 3 places, 90€

            panierMock.getContenuPaniers().clear();
            panierMock.getContenuPaniers().addAll(Arrays.asList(cp1, cp2));
            panierMock.setMontantTotal(BigDecimal.valueOf(130)); // 40 + 90
            panierMock.setStatut(StatutPanier.EN_ATTENTE);
        }

        @Test
        @DisplayName("Devrait finaliser l'achat avec succès")
        void shouldFinalizePurchaseSuccessfully() {
            when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateurMock));
            when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE)).thenReturn(Optional.of(panierMock));
            // Mocker la recherche de discipline pour chaque offre
            when(disciplineRepository.findById(disciplineFinale.getIdDiscipline())).thenReturn(Optional.of(disciplineFinale));
            // Mocker la décrémentation des places (doit retourner 1 pour succès)
            when(disciplineRepository.decrementerPlaces(disciplineFinale.getIdDiscipline(), eq(4))).thenReturn(1); // offre1: 2 qte * 2 cap
            when(disciplineRepository.decrementerPlaces(disciplineFinale.getIdDiscipline(), eq(3))).thenReturn(1); // offre2: 3 qte * 1 cap
            // Mocker la sauvegarde des offres (quantité mise à jour)
            when(offreRepository.save(any(Offre.class))).thenAnswer(inv -> inv.getArgument(0));
            // Mocker la sauvegarde finale du panier (statut PAYE)
            when(panierRepository.save(panierMock)).thenReturn(panierMock);

            PanierDto result = panierService.finaliserAchat(utilisateurIdStr);

            // PanierMock est maintenant PAYE, les offres ont leurs quantités réduites.
            PanierDto expectedFinalDto = buildCompleteExpectedPanierDto(panierMock);

            assertEquals(expectedFinalDto, result);
            assertEquals(StatutPanier.PAYE, result.getStatut());
            assertEquals(1, offre1.getQuantite()); // 5 - (2*2) non, c'est 5 - 2. offre1.quantite -= cp1.quantiteCommandee
            assertEquals(7, offre2.getQuantite()); // 10 - 3

            verify(disciplineRepository).decrementerPlaces(disciplineFinale.getIdDiscipline(), 4);
            verify(disciplineRepository).decrementerPlaces(disciplineFinale.getIdDiscipline(), 3);
            verify(offreRepository, times(2)).save(any(Offre.class)); // Une fois pour offre1, une fois pour offre2
            verify(panierRepository).save(panierMock); // Pour le statut PAYE
        }

        @Test @DisplayName("Devrait lever IllegalStateException si le panier est déjà payé")
        void shouldThrowISE_whenPanierAlreadyPaid() {
            panierMock.setStatut(StatutPanier.PAYE);
            when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateurMock));
            when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE)).thenReturn(Optional.of(panierMock)); // Le service trouvera ce panier PAYE

            IllegalStateException e = assertThrows(IllegalStateException.class, () -> panierService.finaliserAchat(utilisateurIdStr));
            assertEquals(PANIER_DEJA_PAYE_MSG_PREFIX + StatutPanier.PAYE, e.getMessage());
        }

        @Test @DisplayName("Devrait lever IllegalStateException si le panier est vide")
        void shouldThrowISE_whenPanierIsEmptyForFinalisation() {
            panierMock.getContenuPaniers().clear();
            panierMock.setMontantTotal(BigDecimal.ZERO);
            when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateurMock));
            when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE)).thenReturn(Optional.of(panierMock));

            IllegalStateException e = assertThrows(IllegalStateException.class, () -> panierService.finaliserAchat(utilisateurIdStr));
            assertEquals(PANIER_VIDE_MSG, e.getMessage());
        }

        @Test @DisplayName("Devrait lever IllegalStateException si stock de l'offre insuffisant")
        void shouldThrowISE_whenStockInsuffisant() {
            offre1.setQuantite(1); // Stock = 1, commandé = 2 pour offre1 (cp1)
            when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateurMock));
            when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE)).thenReturn(Optional.of(panierMock));
            when(disciplineRepository.findById(disciplineFinale.getIdDiscipline())).thenReturn(Optional.of(disciplineFinale));
            when(disciplineRepository.decrementerPlaces(disciplineFinale.getIdDiscipline(), 4)).thenReturn(1); // Places OK pour offre1

            IllegalStateException e = assertThrows(IllegalStateException.class, () -> panierService.finaliserAchat(utilisateurIdStr));
            assertEquals(String.format(STOCK_INSUFFISANT_FINALISATION_MSG_FORMAT + ". Stock actuel: %d, Demandé: %d", offre1.getIdOffre(), 1, 2), e.getMessage());
        }

        @Test @DisplayName("Devrait lever IllegalStateException si places de discipline insuffisantes")
        void shouldThrowISE_whenDisciplinePlacesInsuffisantes() {
            when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateurMock));
            when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE)).thenReturn(Optional.of(panierMock));
            when(disciplineRepository.findById(disciplineFinale.getIdDiscipline())).thenReturn(Optional.of(disciplineFinale));
            when(disciplineRepository.decrementerPlaces(disciplineFinale.getIdDiscipline(), 4)).thenReturn(0); // Echec décrémentation pour offre1

            IllegalStateException e = assertThrows(IllegalStateException.class, () -> panierService.finaliserAchat(utilisateurIdStr));
            assertTrue(e.getMessage().startsWith(PLACES_INSUFFISANTES_MSG_PREFIX));
            assertTrue(e.getMessage().contains(String.format("Offre : %d", offre1.getIdOffre())));
        }
    }
}