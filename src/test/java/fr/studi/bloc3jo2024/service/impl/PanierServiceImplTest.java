package fr.studi.bloc3jo2024.service.impl;

import fr.studi.bloc3jo2024.dto.panier.AjouterOffrePanierDto;
import fr.studi.bloc3jo2024.dto.panier.ContenuPanierDto;
import fr.studi.bloc3jo2024.dto.panier.ModifierContenuPanierDto;
import fr.studi.bloc3jo2024.dto.panier.PanierDto;
import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.StatutPanier;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.ContenuPanierRepository;
import fr.studi.bloc3jo2024.repository.DisciplineRepository;
import fr.studi.bloc3jo2024.repository.OffreRepository;
import fr.studi.bloc3jo2024.repository.PanierRepository;
import fr.studi.bloc3jo2024.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    // Constantes utilisées pour les tests
    private final UUID utilisateurId = UUID.randomUUID();
    private final String utilisateurIdStr = utilisateurId.toString();
    private final Long offreId = 1L;
    private final Long disciplineId = 10L;
    private final Long panierId = 100L;

    // Constantes pour les messages d'erreur
    private static final String UTILISATEUR_NOT_FOUND = "Utilisateur non trouvé avec l'ID : ";
    private static final String OFFRE_NOT_IN_PANIER = "L'offre avec l'ID %d n'est pas dans le panier";
    private static final String QUANTITE_INVALIDE_OU_NON_DISPONIBLE = "Quantité invalide ou offre non disponible.";
    private static final String PLACES_INSUFFISANTES = "Nombre de places disponibles insuffisant pour l'offre ou la discipline.";
    private static final String PANIER_DEJA_PAYE = "Le panier ne peut pas être payé car son statut est : ";
    private static final String PANIER_VIDE = "Le panier est vide. Impossible de finaliser l'achat.";
    private static final String OFFRE_DISCIPLINE_NULL = "L'offre avec l'ID %d n'est associée à aucune discipline ou l'objet discipline est nul.";
    private static final String STOCK_INSUFFISANT_FINALISATION = "Stock de l'offre (%d) insuffisant au moment de la finalisation.";

    /**
     * Teste la récupération d'un panier existant pour un utilisateur.
     * Vérifie que le service trouve et retourne le panier correct sans en créer un nouveau.
     */
    @Test
    void getPanierUtilisateur_shouldReturnExistingPanier_whenPanierExists() {
        // Arrange
        Utilisateur utilisateur = Utilisateur.builder().idUtilisateur(utilisateurId).build();
        Panier panier = Panier.builder().idPanier(panierId).utilisateur(utilisateur).statut(StatutPanier.EN_ATTENTE).build();
        PanierDto panierDto = PanierDto.builder().idPanier(panierId).idUtilisateur(utilisateurId).statut(StatutPanier.EN_ATTENTE).montantTotal(BigDecimal.ZERO).contenuPaniers(new ArrayList<>()).build();

        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE))
                .thenReturn(Optional.of(panier));
        when(modelMapper.map(panier, PanierDto.class)).thenReturn(panierDto);

        // Act
        PanierDto result = panierService.getPanierUtilisateur(utilisateurIdStr);

        // Assert
        assertNotNull(result);
        assertEquals(panierDto.getIdPanier(), result.getIdPanier());
        assertEquals(panierDto.getStatut(), result.getStatut());
        verify(utilisateurRepository, times(1)).findById(utilisateurId);
        verify(panierRepository, times(1)).findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE);
        verify(panierRepository, never()).save(any(Panier.class)); // Aucune sauvegarde ne devrait avoir lieu
        verify(modelMapper, times(1)).map(panier, PanierDto.class);
    }

    /**
     * Teste la création d'un nouveau panier pour un utilisateur.
     * Vérifie que le service crée, sauvegarde et retourne un nouveau panier lorsque aucun panier en attente n'existe.
     */
    @Test
    void getPanierUtilisateur_shouldCreateNewPanier_whenNoPanierExists() {
        // Arrange
        Utilisateur utilisateur = Utilisateur.builder().idUtilisateur(utilisateurId).build();
        // Simule le panier qui serait créé et sauvegardé par le service
        Panier nouveauPanier = Panier.builder()
                .utilisateur(utilisateur)
                .statut(StatutPanier.EN_ATTENTE)
                .montantTotal(BigDecimal.ZERO)
                .contenuPaniers(new HashSet<>())
                .build();
        // Simule le panier retourné par le repository après save (avec un ID)
        Panier panierSauvegarde = Panier.builder()
                .idPanier(panierId + 1) // ID généré
                .utilisateur(utilisateur)
                .statut(StatutPanier.EN_ATTENTE)
                .montantTotal(BigDecimal.ZERO)
                .contenuPaniers(new HashSet<>())
                .build();
        PanierDto panierDto = PanierDto.builder()
                .idPanier(panierSauvegarde.getIdPanier())
                .idUtilisateur(utilisateurId)
                .statut(StatutPanier.EN_ATTENTE)
                .montantTotal(BigDecimal.ZERO)
                .contenuPaniers(new ArrayList<>())
                .build();

        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE))
                .thenReturn(Optional.empty());
        when(panierRepository.save(any(Panier.class))).thenReturn(panierSauvegarde);
        when(modelMapper.map(panierSauvegarde, PanierDto.class)).thenReturn(panierDto);

        // Act
        PanierDto result = panierService.getPanierUtilisateur(utilisateurIdStr);

        // Assert
        assertNotNull(result);
        assertEquals(panierDto.getIdPanier(), result.getIdPanier());
        assertEquals(StatutPanier.EN_ATTENTE, result.getStatut());
        assertEquals(BigDecimal.ZERO, result.getMontantTotal());
        assertTrue(result.getContenuPaniers().isEmpty());
        verify(utilisateurRepository, times(1)).findById(utilisateurId);
        verify(panierRepository, times(1)).findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE);
        verify(panierRepository, times(1)).save(any(Panier.class)); // Un nouveau panier devrait être sauvegardé
        verify(modelMapper, times(1)).map(panierSauvegarde, PanierDto.class);
    }

    /**
     * Teste le scénario où l'utilisateur n'est pas trouvé lors de la tentative de récupération du panier.
     * Vérifie qu'une exception {@link ResourceNotFoundException} est levée avec le message correct.
     */
    @Test
    void getPanierUtilisateur_shouldThrowResourceNotFoundException_whenUtilisateurNotFound() {
        // Arrange
        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> panierService.getPanierUtilisateur(utilisateurIdStr));
        // Vérifie le message de l'exception levée
        assertEquals(UTILISATEUR_NOT_FOUND + utilisateurId, exception.getMessage());

        verify(utilisateurRepository, times(1)).findById(utilisateurId);
        // Aucune autre interaction ne devrait avoir lieu si l'utilisateur n'est pas trouvé
        verify(panierRepository, never()).findByUtilisateur_idUtilisateurAndStatut(any(), any());
        verify(panierRepository, never()).save(any(Panier.class));
        verify(modelMapper, never()).map(any(), any());
    }

    /**
     * Teste l'ajout d'une offre au panier lorsque l'offre n'est pas disponible (statut EPUISE, ANNULE, EXPIRE...).
     * Vérifie qu'une exception {@link IllegalArgumentException} est levée avec le message correct.
     */
    @Test
    void ajouterOffreAuPanier_shouldThrowIllegalArgumentException_whenOffreNotDisponible() {
        // Arrange
        Utilisateur utilisateur = Utilisateur.builder().idUtilisateur(utilisateurId).build();
        Panier panier = Panier.builder().idPanier(panierId).utilisateur(utilisateur).statut(StatutPanier.EN_ATTENTE).montantTotal(BigDecimal.ZERO).contenuPaniers(new HashSet<>()).build();
        // Offre non disponible
        Offre offre = Offre.builder().idOffre(offreId).statutOffre(StatutOffre.EPUISE).quantite(5).build(); // Quantité > 0, mais statut non DISPONIBLE
        AjouterOffrePanierDto ajouterOffrePanierDto = new AjouterOffrePanierDto(offreId, 1); // Quantité demandée valide

        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE))
                .thenReturn(Optional.of(panier));
        when(offreRepository.findById(offreId)).thenReturn(Optional.of(offre));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> panierService.ajouterOffreAuPanier(utilisateurIdStr, ajouterOffrePanierDto));
        // Vérifie le message de l'exception levée en utilisant la constante
        assertEquals(QUANTITE_INVALIDE_OU_NON_DISPONIBLE, exception.getMessage());


        // Vérifications : seuls les appels nécessaires pour la validation initiale sont effectués
        verify(utilisateurRepository, times(1)).findById(utilisateurId);
        verify(panierRepository, times(1)).findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE);
        verify(offreRepository, times(1)).findById(offreId);
        verify(disciplineRepository, never()).findById(any()); // Pas besoin de la discipline si l'offre n'est pas dispo ou stock insuffisant
        verify(contenuPanierRepository, never()).findById(any());
        verify(contenuPanierRepository, never()).save(any());
        verify(panierRepository, never()).save(any());
        verify(modelMapper, never()).map(any(), any());
    }

    /**
     * Teste l'ajout d'une offre au panier lorsque la quantité demandée est supérieure à la quantité disponible de l'offre (stock).
     * Vérifie qu'une exception {@link IllegalArgumentException} est levée avec le message correct.
     */
    @Test
    void ajouterOffreAuPanier_shouldThrowIllegalArgumentException_whenQuantiteInsuffisante() {
        // Arrange
        Utilisateur utilisateur = Utilisateur.builder().idUtilisateur(utilisateurId).build();
        Panier panier = Panier.builder().idPanier(panierId).utilisateur(utilisateur).statut(StatutPanier.EN_ATTENTE).montantTotal(BigDecimal.ZERO).contenuPaniers(new HashSet<>()).build();
        // Offre disponible mais avec un stock insuffisant pour la demande
        Offre offre = Offre.builder().idOffre(offreId).statutOffre(StatutOffre.DISPONIBLE).quantite(1).build(); // Quantité disponible = 1
        AjouterOffrePanierDto ajouterOffrePanierDto = new AjouterOffrePanierDto(offreId, 2); // Quantité demandée = 2

        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE))
                .thenReturn(Optional.of(panier));
        when(offreRepository.findById(offreId)).thenReturn(Optional.of(offre));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> panierService.ajouterOffreAuPanier(utilisateurIdStr, ajouterOffrePanierDto));
        // Vérifie le message de l'exception levée en utilisant la constante correcte
        assertEquals(QUANTITE_INVALIDE_OU_NON_DISPONIBLE, exception.getMessage());

        // Vérifications : seuls les appels nécessaires pour la validation initiale sont effectués
        verify(utilisateurRepository, times(1)).findById(utilisateurId);
        verify(panierRepository, times(1)).findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE);
        verify(offreRepository, times(1)).findById(offreId);
        verify(disciplineRepository, never()).findById(any());
        verify(contenuPanierRepository, never()).findById(any());
        verify(contenuPanierRepository, never()).save(any());
        verify(panierRepository, never()).save(any());
        verify(modelMapper, never()).map(any(), any()); // Mapping ne devrait pas être appelé
    }


    /**
     * Teste l'ajout d'une offre de discipline au panier lorsque les places disponibles dans la discipline sont insuffisantes
     * pour la quantité demandée, même si le stock global de l'offre est suffisant.
     * Vérifie qu'une exception {@link IllegalStateException} est levée avec le message correct.
     * Ce test est ajusté pour commencer avec un panier non vide pour mieux isoler le scénario de places insuffisantes.
     */
    @Test
    void ajouterOffreAuPanier_shouldThrowIllegalStateException_whenPlacesInsuffisantesInDiscipline() {
        // Arrange
        Utilisateur utilisateur = Utilisateur.builder().idUtilisateur(utilisateurId).build();

        // Discipline avec peu de places disponibles
        Discipline discipline = Discipline.builder().idDiscipline(disciplineId).nbPlaceDispo(3).nomDiscipline("Natation").build(); // Discipline avec 3 places disponibles, nom pour message

        // Offre A (qui sera déjà dans le panier) : capacité 1, liée à la discipline
        Offre offreA = Offre.builder().idOffre(offreId + 1).capacite(1).discipline(discipline).build();
        // Contenu panier existant pour Offre A : quantité 2 (utilise 2 * 1 = 2 places)
        ContenuPanier contenuExistantA = ContenuPanier.builder().offre(offreA).quantiteCommandee(2).build();

        // Offre B (celle qu'on essaie d'ajouter) : capacité 2, liée à la même discipline
        Offre offreB = Offre.builder().idOffre(offreId).statutOffre(StatutOffre.DISPONIBLE).quantite(10).capacite(2).discipline(discipline).build(); // Offre B disponible, stock suffisant
        AjouterOffrePanierDto ajouterOffrePanierDto = new AjouterOffrePanierDto(offreB.getIdOffre(), 1); // On essaie d'ajouter 1 exemplaire de Offre B (utilise 1 * 2 = 2 places)

        // Panier contenant déjà Offre A. Montant total initial non pertinent pour ce test.
        Set<ContenuPanier> contenuPaniersSet = new HashSet<>(); // Utiliser un HashSet mutable
        contenuPaniersSet.add(contenuExistantA);

        Panier panier = Panier.builder()
                .idPanier(panierId)
                .utilisateur(utilisateur)
                .statut(StatutPanier.EN_ATTENTE)
                .montantTotal(BigDecimal.ZERO)
                .contenuPaniers(contenuPaniersSet) // Panier contient déjà Offre A
                .build();
        // Lier le contenu existant au panier
        contenuExistantA.setPanier(panier);


        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE))
                .thenReturn(Optional.of(panier)); // Retourne le panier avec Offre A
        when(offreRepository.findById(offreB.getIdOffre())).thenReturn(Optional.of(offreB)); // Offre B trouvée
        when(disciplineRepository.findById(disciplineId)).thenReturn(Optional.of(discipline)); // Discipline trouvée


        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> panierService.ajouterOffreAuPanier(utilisateurIdStr, ajouterOffrePanierDto));

        // Vérifie le message de l'exception levée en utilisant la constante correcte et une partie du message attendu
        assertTrue(exception.getMessage().startsWith(PLACES_INSUFFISANTES));
        assertTrue(exception.getMessage().contains("(Discipline : " + discipline.getNomDiscipline() + ")"));

        // Vérifications : les appels nécessaires pour vérifier les places sont effectués
        verify(utilisateurRepository, times(1)).findById(utilisateurId);
        verify(panierRepository, times(1)).findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE);
        verify(offreRepository, times(1)).findById(offreB.getIdOffre());
        verify(disciplineRepository, times(1)).findById(disciplineId);
        // Ces mocks ne devraient jamais être appelés dans ce scénario d'échec
        verify(contenuPanierRepository, never()).findById(any());
        verify(contenuPanierRepository, never()).save(any());
        verify(panierRepository, never()).save(any());
        verify(modelMapper, never()).map(any(Panier.class), eq(PanierDto.class));
    }

    /**
     * Teste l'ajout d'une offre de discipline au panier lorsque l'offre n'est pas associée à une discipline (ou discipline nulle).
     * Vérifie qu'une exception {@link IllegalStateException} est levée avec le message correct.
     */
    @Test
    void ajouterOffreAuPanier_shouldThrowIllegalStateException_whenOffreDisciplineIsNull() {
        // Arrange
        Utilisateur utilisateur = Utilisateur.builder().idUtilisateur(utilisateurId).build();
        Panier panier = Panier.builder().idPanier(panierId).utilisateur(utilisateur).statut(StatutPanier.EN_ATTENTE).montantTotal(BigDecimal.ZERO).contenuPaniers(new HashSet<>()).build();
        // Offre sans discipline
        Offre offre = Offre.builder().idOffre(offreId).statutOffre(StatutOffre.DISPONIBLE).quantite(10).capacite(2).discipline(null).build();
        AjouterOffrePanierDto ajouterOffrePanierDto = new AjouterOffrePanierDto(offreId, 1);

        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE))
                .thenReturn(Optional.of(panier));
        when(offreRepository.findById(offreId)).thenReturn(Optional.of(offre));

        // Act & Assert
        // On s'attend à ce que l'exception soit lancée car la discipline est nulle dans l'offre
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> panierService.ajouterOffreAuPanier(utilisateurIdStr, ajouterOffrePanierDto));

        assertEquals(String.format(OFFRE_DISCIPLINE_NULL, offreId), exception.getMessage());

        // Verify only initial checks were made
        verify(utilisateurRepository, times(1)).findById(utilisateurId);
        verify(panierRepository, times(1)).findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE);
        verify(offreRepository, times(1)).findById(offreId);
        verify(disciplineRepository, never()).findById(any()); // Pas besoin de la discipline car l'offre.getDiscipline() est null
        verify(contenuPanierRepository, never()).save(any());
        verify(panierRepository, never()).save(any());
        verify(modelMapper, never()).map(any(), any());
    }

    /**
     * Teste l'ajout d'une offre au panier lorsque l'offre est déjà présente.
     * Vérifie que la quantité est mise à jour et le montant total recalculé.
     */
    @Test
    void ajouterOffreAuPanier_shouldUpdateQuantityAndRecalculateTotal_whenOffreAlreadyInPanier() {
        // Arrange
        Utilisateur utilisateur = Utilisateur.builder().idUtilisateur(utilisateurId).build();
        Discipline discipline = Discipline.builder().idDiscipline(disciplineId).nbPlaceDispo(10).build(); // Suffisamment de places
        Offre offre = Offre.builder().idOffre(offreId).prix(BigDecimal.TEN).statutOffre(StatutOffre.DISPONIBLE).quantite(5).capacite(1).discipline(discipline).build(); // Stock 5, Capacité 1

        // Contenu panier existant avec quantité 1 (utilise 1 * 1 = 1 place)
        ContenuPanier contenuPanierExistant = ContenuPanier.builder()
                .offre(offre)
                .quantiteCommandee(1)
                .build();

        // Utiliser un HashSet mutable
        Set<ContenuPanier> contenuPaniersSet = new HashSet<>();
        contenuPaniersSet.add(contenuPanierExistant);

        Panier panier = Panier.builder()
                .idPanier(panierId)
                .utilisateur(utilisateur)
                .statut(StatutPanier.EN_ATTENTE)
                .montantTotal(BigDecimal.TEN) // Total initial 1 * 10 = 10
                .contenuPaniers(contenuPaniersSet) // Panier contient déjà l'offre
                .build();
        // Lier le contenu existant au panier
        contenuPanierExistant.setPanier(panier);

        // On essaie d'ajouter 2 exemplaires supplémentaires (nouvelle quantité = 1 + 2 = 3)
        // Total places utilisées après ajout : 3 * 1 = 3. Moins que la capacité discipline (10). OK.
        // Stock restant : 5 - 2 = 3. OK.
        AjouterOffrePanierDto ajouterOffrePanierDto = new AjouterOffrePanierDto(offreId, 2);

        // Simuler le ContenuPanier après mise à jour par le service (quantité = 3)
        ContenuPanier contenuPanierModifieSimule = ContenuPanier.builder()
                .offre(offre)
                .quantiteCommandee(3)
                .panier(panier) // Lié au même panier
                .build();
        // Le Set dans le panier en mémoire devrait contenir cet objet mis à jour

        // Simuler le PanierDto retourné (total mis à jour : 3 * 10 = 30)
        ContenuPanierDto contenuPanierDtoModifie = ContenuPanierDto.builder()
                .idOffre(offreId)
                .prixUnitaire(BigDecimal.TEN)
                .quantiteCommandee(3)
                .prixTotalOffre(BigDecimal.valueOf(30))
                .build();
        PanierDto panierDtoModifie = PanierDto.builder()
                .idPanier(panierId)
                .idUtilisateur(utilisateurId)
                .statut(StatutPanier.EN_ATTENTE)
                .montantTotal(BigDecimal.valueOf(30))
                .contenuPaniers(new ArrayList<>(Collections.singletonList(contenuPanierDtoModifie))) // Liste avec le contenu mis à jour
                .build();


        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE))
                .thenReturn(Optional.of(panier)); // Retourne le panier existant
        when(offreRepository.findById(offreId)).thenReturn(Optional.of(offre)); // Offre trouvée
        when(disciplineRepository.findById(disciplineId)).thenReturn(Optional.of(discipline)); // Discipline trouvée


        // Mock pour l'appel save sur le ContenuPanier mis à jour
        when(contenuPanierRepository.save(any(ContenuPanier.class))).thenReturn(contenuPanierModifieSimule);

        // Mock pour l'appel save sur le Panier (fait dans recalculerMontantTotal)
        when(panierRepository.save(any(Panier.class))).thenReturn(panier); // Retourne le panier (en mémoire)

        // Mock pour le mappage final du panier
        when(modelMapper.map(panier, PanierDto.class)).thenReturn(panierDtoModifie);
        // Mock pour le mappage du ContenuPanier vers ContenuPanierDto (nécessaire pour le mappage du panier final)
        when(modelMapper.map(any(ContenuPanier.class), eq(ContenuPanierDto.class)))
                .thenAnswer(invocation -> {
                    ContenuPanier cp = invocation.getArgument(0);
                    // Vérifications null ajoutées pour robustesse du mock
                    if (cp == null || cp.getOffre() == null || cp.getOffre().getPrix() == null) {
                        return null;
                    }
                    return ContenuPanierDto.builder()
                            .idOffre(cp.getOffre().getIdOffre())
                            .prixUnitaire(cp.getOffre().getPrix())
                            .quantiteCommandee(cp.getQuantiteCommandee())
                            .prixTotalOffre(cp.getOffre().getPrix().multiply(BigDecimal.valueOf(cp.getQuantiteCommandee())))
                            .build();
                });

        // Act
        PanierDto result = panierService.ajouterOffreAuPanier(utilisateurIdStr, ajouterOffrePanierDto);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(30), result.getMontantTotal()); // Vérifie le montant total recalculé
        assertEquals(1, result.getContenuPaniers().size()); // Le panier contient toujours 1 élément (mis à jour)
        assertEquals(offreId, result.getContenuPaniers().get(0).getIdOffre()); // Vérifie l'ID de l'offre
        assertEquals(3, result.getContenuPaniers().get(0).getQuantiteCommandee()); // Vérifie la quantité mise à jour

        verify(utilisateurRepository, times(1)).findById(utilisateurId);
        verify(panierRepository, times(1)).findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE);
        verify(offreRepository, times(1)).findById(offreId);
        verify(disciplineRepository, times(1)).findById(disciplineId);

        // Dans ce scénario, on cherche dans le Set en mémoire, pas par findById sur contenuPanierRepository
        verify(contenuPanierRepository, never()).findById(any(ContenuPanierId.class));
        verify(contenuPanierRepository, times(1)).save(contenuPanierModifieSimule); // Save du contenu mis à jour
        verify(panierRepository, times(1)).save(panier); // Save du panier (via recalculerMontantTotal)
        verify(modelMapper, times(1)).map(panier, PanierDto.class); // Mapping final du panier
        // Le mappage du ContenuPanier vers ContenuPanierDto est appelé une fois pour l'élément restant/mis à jour
        verify(modelMapper, times(1)).map(any(ContenuPanier.class), eq(ContenuPanierDto.class));
    }

    /**
     * Teste l'ajout d'une offre de discipline au panier lorsque l'offre n'est pas associée à une discipline et que le stock est insuffisant.
     * Vérifie qu'une exception {@link IllegalArgumentException} est levée.
     */
    @Test
    void ajouterOffreAuPanier_shouldThrowIllegalArgumentException_whenOffreNoDisciplineAndStockInsuffisant() {
        // Arrange
        Utilisateur utilisateur = Utilisateur.builder().idUtilisateur(utilisateurId).build();
        Panier panier = Panier.builder().idPanier(panierId).utilisateur(utilisateur).statut(StatutPanier.EN_ATTENTE).montantTotal(BigDecimal.ZERO).contenuPaniers(new HashSet<>()).build();
        // Offre sans discipline et stock insuffisant
        Offre offre = Offre.builder().idOffre(offreId).statutOffre(StatutOffre.DISPONIBLE).quantite(1).capacite(0).discipline(null).build(); // Stock 1, capacité 0 (pas de place restriction)
        AjouterOffrePanierDto ajouterOffrePanierDto = new AjouterOffrePanierDto(offreId, 2); // Demande 2 (stock insuffisant)

        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE))
                .thenReturn(Optional.of(panier));
        when(offreRepository.findById(offreId)).thenReturn(Optional.of(offre));

        // Act & Assert
        // On s'attend à une exception car le stock est insuffisant
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> panierService.ajouterOffreAuPanier(utilisateurIdStr, ajouterOffrePanierDto));

        assertEquals(QUANTITE_INVALIDE_OU_NON_DISPONIBLE, exception.getMessage());

        // Verify only initial checks were made
        verify(utilisateurRepository, times(1)).findById(utilisateurId);
        verify(panierRepository, times(1)).findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE);
        verify(offreRepository, times(1)).findById(offreId);
        verify(disciplineRepository, never()).findById(any()); // Pas besoin de la discipline
        verify(contenuPanierRepository, never()).save(any());
        verify(panierRepository, never()).save(any());
        verify(modelMapper, never()).map(any(), any());
    }


    /**
     * Teste l'ajout d'une offre au panier lorsque l'offre n'est pas associée à une discipline.
     * Vérifie qu'une exception {@link IllegalStateException} est levée avec le message correct,
     * car le service attend une discipline pour ce type d'offre.
     * **(Anciennement ajouterOffreAuPanier_shouldAddQuantityAndRecalculateTotal_whenOffreNoDisciplineAndStockSuffisant)**
     */
    @Test
    void ajouterOffreAuPanier_shouldThrowIllegalStateException_whenOffreNoDiscipline() { // Nom de test corrigé
        // Arrange
        Utilisateur utilisateur = Utilisateur.builder().idUtilisateur(utilisateurId).build();
        Panier panier = Panier.builder().idPanier(panierId).utilisateur(utilisateur).statut(StatutPanier.EN_ATTENTE).montantTotal(BigDecimal.ZERO).contenuPaniers(new HashSet<>()).build();

        // Offre SANS discipline
        Offre offre = Offre.builder().idOffre(offreId).prix(BigDecimal.valueOf(50)).statutOffre(StatutOffre.DISPONIBLE).quantite(10).capacite(0).discipline(null).build(); // Discipline = null
        AjouterOffrePanierDto ajouterOffrePanierDto = new AjouterOffrePanierDto(offreId, 1); // Quantité demandée

        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE))
                .thenReturn(Optional.of(panier)); // Retourne le panier existant
        when(offreRepository.findById(offreId)).thenReturn(Optional.of(offre)); // Offre trouvée

        // On s'attend à ce que le service lève une IllegalStateException car la discipline est nulle.
        // Utilisez assertThrows au lieu d'attendre un résultat réussi.
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> panierService.ajouterOffreAuPanier(utilisateurIdStr, ajouterOffrePanierDto));

        // Vérifiez le message de l'exception levée en utilisant la constante correcte.
        assertEquals(String.format(OFFRE_DISCIPLINE_NULL, offreId), exception.getMessage());

        // Vérifications : seuls les appels nécessaires pour la validation initiale sont effectués.
        // Aucun appel pour la sauvegarde ou le mappage final ne devrait avoir lieu en cas d'échec.
        verify(utilisateurRepository, times(1)).findById(utilisateurId);
        verify(panierRepository, times(1)).findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE);
        verify(offreRepository, times(1)).findById(offreId);
        verify(disciplineRepository, never()).findById(any());
        verify(contenuPanierRepository, never()).save(any()); // Pas de save en cas d'échec
        verify(panierRepository, never()).save(any()); // Pas de save en cas d'échec
        verify(modelMapper, never()).map(any(), any()); // Mapping ne devrait pas être appelé
    }

    /**
     * Teste la modification de la quantité d'une offre existante dans le panier.
     * Vérifie que la quantité est mise à jour et le montant total recalculé.
     */
    @Test
    void modifierQuantiteOffrePanier_shouldUpdateQuantityAndRecalculateTotal() {
        // Arrange
        Utilisateur utilisateur = Utilisateur.builder().idUtilisateur(utilisateurId).build();
        Offre offre = Offre.builder().idOffre(offreId).prix(BigDecimal.TEN).capacite(1).discipline(Discipline.builder().idDiscipline(disciplineId).nbPlaceDispo(10).build()).build(); // Prix 10, capacité 1
        ContenuPanier contenuPanier = ContenuPanier.builder().offre(offre).quantiteCommandee(1).build(); // Qte initiale 1

        // Utiliser un HashSet mutable
        Set<ContenuPanier> contenuPaniersSet = new HashSet<>();
        contenuPaniersSet.add(contenuPanier);

        Panier panier = Panier.builder().idPanier(panierId).utilisateur(utilisateur).statut(StatutPanier.EN_ATTENTE).montantTotal(BigDecimal.TEN).contenuPaniers(contenuPaniersSet).build(); // Total initial 10
        contenuPanier.setPanier(panier); // Lier le contenu au panier

        ModifierContenuPanierDto modifierDto = new ModifierContenuPanierDto(offreId, 3); // Nouvelle quantité 3

        Panier panierApresRecalcul = Panier.builder().idPanier(panierId).utilisateur(utilisateur).statut(StatutPanier.EN_ATTENTE).montantTotal(BigDecimal.valueOf(30)).contenuPaniers(contenuPaniersSet).build(); // Total 30
        PanierDto panierDto = PanierDto.builder().idPanier(panierId).idUtilisateur(utilisateurId).statut(StatutPanier.EN_ATTENTE).montantTotal(BigDecimal.valueOf(30)).contenuPaniers(new ArrayList<>()).build();


        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE))
                .thenReturn(Optional.of(panier));
        when(offreRepository.findById(offreId)).thenReturn(Optional.of(offre));
        when(disciplineRepository.findById(disciplineId)).thenReturn(Optional.of(offre.getDiscipline())); // Retourne la discipline liée
        when(contenuPanierRepository.save(any(ContenuPanier.class))).thenReturn(contenuPanier); // Mock save du contenu
        when(panierRepository.save(any(Panier.class))).thenReturn(panierApresRecalcul); // Mock save du panier après recalcul
        when(modelMapper.map(panierApresRecalcul, PanierDto.class)).thenReturn(panierDto); // Mock map du panier final

        // On doit aussi mocker le mappage du contenuPanier vers ContenuPanierDto pour le mapPanierToDto final
        when(modelMapper.map(any(ContenuPanier.class), eq(ContenuPanierDto.class)))
                .thenAnswer(invocation -> {
                    ContenuPanier cp = invocation.getArgument(0);
                    return ContenuPanierDto.builder()
                            .idOffre(cp.getOffre().getIdOffre())
                            .prixUnitaire(cp.getOffre().getPrix())
                            .quantiteCommandee(cp.getQuantiteCommandee())
                            .prixTotalOffre(cp.getOffre().getPrix().multiply(BigDecimal.valueOf(cp.getQuantiteCommandee())))
                            .build();
                });

        // Act
        PanierDto result = panierService.modifierQuantiteOffrePanier(utilisateurIdStr, modifierDto);

        // Assert
        assertNotNull(result);
        assertEquals(3, contenuPanier.getQuantiteCommandee()); // Vérifie que la quantité de l'entité en mémoire a été modifiée
        assertEquals(BigDecimal.valueOf(30), result.getMontantTotal()); // Vérifie le montant total recalculé dans le DTO

        verify(utilisateurRepository, times(1)).findById(utilisateurId);
        verify(panierRepository, times(1)).findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE);
        verify(offreRepository, times(1)).findById(offreId);
        verify(disciplineRepository, times(1)).findById(disciplineId); // Vérification de la discipline
        verify(contenuPanierRepository, times(1)).save(contenuPanier); // Save du contenu
        verify(panierRepository, times(1)).save(any(Panier.class)); // Save du panier (via recalculerMontantTotal)
        verify(modelMapper, times(1)).map(panierApresRecalcul, PanierDto.class); // Mapping final du panier
        verify(modelMapper, times(1)).map(any(ContenuPanier.class), eq(ContenuPanierDto.class)); // Mapping du contenu pour le DTO final
    }

    /**
     * Teste la modification de la quantité d'une offre à 0, ce qui devrait la supprimer du panier.
     */
    @Test
    void modifierQuantiteOffrePanier_shouldRemoveItem_whenNewQuantityIsZero() {
        // Arrange
        Utilisateur utilisateur = Utilisateur.builder().idUtilisateur(utilisateurId).build();
        Offre offre = Offre.builder().idOffre(offreId).prix(BigDecimal.TEN).capacite(1).discipline(Discipline.builder().idDiscipline(disciplineId).nbPlaceDispo(10).build()).build(); // Prix 10, capacité 1
        ContenuPanier contenuPanier = ContenuPanier.builder().offre(offre).quantiteCommandee(1).build(); // Qte initiale 1

        // Utiliser un HashSet mutable
        Set<ContenuPanier> contenuPaniersSet = new HashSet<>();
        contenuPaniersSet.add(contenuPanier);

        Panier panier = Panier.builder().idPanier(panierId).utilisateur(utilisateur).statut(StatutPanier.EN_ATTENTE).montantTotal(BigDecimal.TEN).contenuPaniers(contenuPaniersSet).build(); // Total initial 10
        contenuPanier.setPanier(panier); // Lier le contenu au panier

        ModifierContenuPanierDto modifierDto = new ModifierContenuPanierDto(offreId, 0); // Nouvelle quantité 0

        PanierDto panierDto = PanierDto.builder().idPanier(panierId).idUtilisateur(utilisateurId).statut(StatutPanier.EN_ATTENTE).montantTotal(BigDecimal.ZERO).contenuPaniers(new ArrayList<>()).build(); // Total final 0, contenu vide

        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));

        // getPanierUtilisateurEntity est appelée deux fois. Les deux appels devraient trouver le panier existant.
        when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE))
                .thenReturn(Optional.of(panier), Optional.of(panier)); // Retourne Optional.of(panier) pour le 1er ET le 2ème appel

        when(offreRepository.findById(offreId)).thenReturn(Optional.of(offre)); // Offre trouvée pour la validation initiale
        // Mock la suppression par ID composite
        doNothing().when(contenuPanierRepository).deleteById(any(ContenuPanierId.class));
        when(panierRepository.save(any(Panier.class))).thenReturn(panier); // Mock save du panier après suppression/recalcul
        when(modelMapper.map(panier, PanierDto.class)).thenReturn(panierDto); // Mock map du panier final

        // Act
        PanierDto result = panierService.modifierQuantiteOffrePanier(utilisateurIdStr, modifierDto);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getMontantTotal()); // Vérifie le montant total
        assertTrue(result.getContenuPaniers().isEmpty()); // Vérifie que la liste de contenus dans le DTO est vide

        // panierRepository.findByUtilisateur_idUtilisateurAndStatut est appelé deux fois.
        verify(panierRepository, times(2)).findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE); // Attendre 2 appels
        verify(contenuPanierRepository, times(1)).deleteById(new ContenuPanierId(panierId, offreId));
        verify(contenuPanierRepository, never()).save(any()); // Pas de save si on supprime
        verify(panierRepository, times(1)).save(panier); // Save du panier (via recalculerMontantTotal)
        verify(modelMapper, times(1)).map(panier, PanierDto.class); // Mapping final
    }

    /**
     * Teste le scénario où la nouvelle quantité modifiée rend les places dans la discipline insuffisantes.
     * Vérifie qu'une exception {@link IllegalStateException} est levée avec le message correct.
     */
    @Test
    void modifierQuantiteOffrePanier_shouldThrowIllegalStateException_whenPlacesInsuffisantesAfterUpdate() {
        // Arrange
        Utilisateur utilisateur = Utilisateur.builder().idUtilisateur(utilisateurId).build();

        // Discipline avec peu de places disponibles
        Discipline discipline = Discipline.builder().idDiscipline(disciplineId).nbPlaceDispo(3).nomDiscipline("Basket").build(); // Discipline avec 3 places disponibles

        // Offre A (qui sera modifiée) : capacité 2, liée à la discipline
        Offre offreA = Offre.builder().idOffre(offreId).prix(BigDecimal.TEN).capacite(2).discipline(discipline).build(); // Prix 10, capacité 2
        // Contenu panier existant pour Offre A : quantité initiale 1 (utilise 1 * 2 = 2 places)
        ContenuPanier contenuPanierA = ContenuPanier.builder().offre(offreA).quantiteCommandee(1).build();

        // Offre B (qui sera déjà dans le panier) : capacité 1, liée à la même discipline
        Offre offreB = Offre.builder().idOffre(offreId + 1).capacite(1).discipline(discipline).build();
        // Contenu panier existant pour Offre B : quantité 1 (utilise 1 * 1 = 1 place)
        ContenuPanier contenuPanierB = ContenuPanier.builder().offre(offreB).quantiteCommandee(1).build();

        // Panier contenant Offre A et Offre B. Total initial 2+1 = 3 places utilisées.
        // Utiliser un HashSet mutable
        Set<ContenuPanier> contenuPaniersSet = new HashSet<>();
        contenuPaniersSet.add(contenuPanierA);
        contenuPaniersSet.add(contenuPanierB);

        Panier panier = Panier.builder()
                .idPanier(panierId)
                .utilisateur(utilisateur)
                .statut(StatutPanier.EN_ATTENTE)
                .montantTotal(BigDecimal.valueOf(30)) // 1*10 + 1*x (prix offre B)
                .contenuPaniers(contenuPaniersSet)
                .build();
        // Lier les contenus au panier
        contenuPanierA.setPanier(panier);
        contenuPanierB.setPanier(panier);

        ModifierContenuPanierDto modifierDto = new ModifierContenuPanierDto(offreId, 2); // Nouvelle quantité pour Offre A = 2
        // Si Offre A quantité devient 2, elle utilise 2 * 2 = 4 places.
        // Places totales dans la discipline après modification : 4 (Offre A) + 1 (Offre B) = 5 places.
        // Capacité de la discipline = 3 places. L'ajout devrait échouer.

        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE))
                .thenReturn(Optional.of(panier));
        when(offreRepository.findById(offreA.getIdOffre())).thenReturn(Optional.of(offreA)); // Offre A trouvée
        when(disciplineRepository.findById(disciplineId)).thenReturn(Optional.of(discipline)); // Discipline trouvée

        // Act & Assert
        // On s'attend à ce que la modification échoue car 4 places (nouvelle qté) + 1 place (autre item) = 5 places > 3 places dispo.
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> panierService.modifierQuantiteOffrePanier(utilisateurIdStr, modifierDto));

        // Vérifie le message de l'exception levée en utilisant la constante correcte et une partie du message attendu
        assertTrue(exception.getMessage().startsWith(PLACES_INSUFFISANTES));
        assertTrue(exception.getMessage().contains("(Discipline : " + discipline.getNomDiscipline() + ")"));


        // Vérifications : les appels nécessaires pour vérifier les places sont effectués
        verify(utilisateurRepository, times(1)).findById(utilisateurId);
        verify(panierRepository, times(1)).findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE);
        verify(offreRepository, times(1)).findById(offreA.getIdOffre());
        verify(disciplineRepository, times(1)).findById(disciplineId);
        // Ces mocks ne devraient jamais être appelés dans ce scénario d'échec
        verify(contenuPanierRepository, never()).save(any()); // Pas de save si échec
        verify(panierRepository, never()).save(any()); // Pas de save si échec
        verify(modelMapper, never()).map(any(Panier.class), eq(PanierDto.class)); // Mapping ne devrait PAS être appelé
    }

    /**
     * Teste le scénario où l'offre à modifier n'est pas trouvée dans le panier de l'utilisateur.
     * Vérifie qu'une exception {@link ResourceNotFoundException} est levée avec le message correct.
     */
    @Test
    void modifierQuantiteOffrePanier_shouldThrowResourceNotFoundException_whenOffreNotInPanier() {
        // Arrange
        Utilisateur utilisateur = Utilisateur.builder().idUtilisateur(utilisateurId).build();
        // Panier vide ou ne contenant pas l'offreId recherchée
        // Utiliser un HashSet mutable
        Set<ContenuPanier> contenuPaniersSet = new HashSet<>();

        Panier panier = Panier.builder().idPanier(panierId).utilisateur(utilisateur).statut(StatutPanier.EN_ATTENTE).montantTotal(BigDecimal.ZERO).contenuPaniers(contenuPaniersSet).build();
        Offre offre = Offre.builder().idOffre(offreId).build(); // L'offre recherchée
        ModifierContenuPanierDto modifierDto = new ModifierContenuPanierDto(offreId, 1); // Nouvelle quantité 1


        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE))
                .thenReturn(Optional.of(panier));
        when(offreRepository.findById(offreId)).thenReturn(Optional.of(offre));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> panierService.modifierQuantiteOffrePanier(utilisateurIdStr, modifierDto));

        // Vérifie le message de l'exception levée en utilisant la constante correcte
        assertEquals(String.format(OFFRE_NOT_IN_PANIER, offreId), exception.getMessage());


        // Vérifications : les appels nécessaires pour la validation initiale sont effectués
        verify(utilisateurRepository, times(1)).findById(utilisateurId);
        verify(panierRepository, times(1)).findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE);
        verify(offreRepository, times(1)).findById(offreId);
        verify(disciplineRepository, never()).findById(any()); // Pas besoin de la discipline si l'offre n'est pas dans le panier
        verify(contenuPanierRepository, never()).findById(any());
        verify(contenuPanierRepository, never()).save(any());
        verify(panierRepository, never()).save(any());
        verify(modelMapper, never()).map(any(), any()); // Mapping ne devrait pas être appelé
    }

    /**
     * Teste la suppression d'une offre du panier.
     * Vérifie que l'élément est supprimé et le montant total recalculé.
     */
    @Test
    void supprimerOffreDuPanier_shouldRemoveItemAndRecalculateTotal() {
        // Arrange
        Utilisateur utilisateur = Utilisateur.builder().idUtilisateur(utilisateurId).build();
        Offre offreASupprimer = Offre.builder().idOffre(offreId).prix(BigDecimal.TEN).build(); // Prix 10
        Offre offreAutre = Offre.builder().idOffre(offreId + 1).prix(BigDecimal.valueOf(20)).build(); // Prix 20

        ContenuPanier contenuASupprimer = ContenuPanier.builder().offre(offreASupprimer).quantiteCommandee(2).build(); // Qte 2, Total 20
        ContenuPanier contenuAutre = ContenuPanier.builder().offre(offreAutre).quantiteCommandee(1).build(); // Qte 1, Total 20

        // Utiliser un HashSet mutable
        Set<ContenuPanier> contenuPaniersSet = new HashSet<>();
        contenuPaniersSet.add(contenuASupprimer);
        contenuPaniersSet.add(contenuAutre);

        Panier panier = Panier.builder().idPanier(panierId).utilisateur(utilisateur).statut(StatutPanier.EN_ATTENTE).montantTotal(BigDecimal.valueOf(40)).contenuPaniers(contenuPaniersSet).build(); // Total initial 40
        contenuASupprimer.setPanier(panier);
        contenuAutre.setPanier(panier);

        // Simuler le PanierDto retourné AVEC l'élément supprimé absent et le bon montant total
        // Le mock de ModelMapper.map(panier, PanierDto.class) retournera CE DTO.
        PanierDto panierDto = PanierDto.builder()
                .idPanier(panierId)
                .idUtilisateur(utilisateurId)
                .statut(StatutPanier.EN_ATTENTE)
                .montantTotal(BigDecimal.valueOf(20)) // Total final 20
                // Le DTO mocké doit contenir la liste attendue après suppression (ici, un seul élément)
                .contenuPaniers(new ArrayList<>(Collections.singletonList(
                        ContenuPanierDto.builder()
                                .idOffre(offreAutre.getIdOffre())
                                .prixUnitaire(offreAutre.getPrix())
                                .quantiteCommandee(contenuAutre.getQuantiteCommandee())
                                .prixTotalOffre(offreAutre.getPrix().multiply(BigDecimal.valueOf(contenuAutre.getQuantiteCommandee())))
                                .build()
                )))
                .build();


        // Configuration du mock ModelMapper pour le mappage individuel des contenus (utilisé lors du mappage du panier)
        when(modelMapper.map(any(ContenuPanier.class), eq(ContenuPanierDto.class)))
                .thenAnswer(invocation -> {
                    ContenuPanier cp = invocation.getArgument(0);
                    return ContenuPanierDto.builder()
                            .idOffre(cp.getOffre().getIdOffre())
                            .prixUnitaire(cp.getOffre().getPrix())
                            .quantiteCommandee(cp.getQuantiteCommandee())
                            .prixTotalOffre(cp.getOffre().getPrix().multiply(BigDecimal.valueOf(cp.getQuantiteCommandee())))
                            .build();
                });

        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE))
                .thenReturn(Optional.of(panier));

        // Mock la suppression par ID composite
        doNothing().when(contenuPanierRepository).deleteById(new ContenuPanierId(panierId, offreId)); // Mock la suppression

        // Mock la sauvegarde du panier après recalcul (retourne le panier mocké en mémoire)
        when(panierRepository.save(any(Panier.class))).thenReturn(panier);

        // C'est CE MOCK qui détermine le contenu de 'result'.
        when(modelMapper.map(panier, PanierDto.class)).thenReturn(panierDto);

        // Act
        PanierDto result = panierService.supprimerOffreDuPanier(utilisateurIdStr, offreId); // Supprime offreId

        // Assert
        assertNotNull(result);
        // Vérifie le montant total recalculé dans le DTO retourné par le mock
        assertEquals(BigDecimal.valueOf(20), result.getMontantTotal());

        // Vérifiez l'interaction avec le repository : la suppression doit avoir été appelée.
        verify(contenuPanierRepository, times(1)).deleteById(new ContenuPanierId(panierId, offreId));
        verify(utilisateurRepository, times(1)).findById(utilisateurId);
        verify(panierRepository, times(1)).findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE);
        verify(contenuPanierRepository, never()).save(any()); // Pas de save si on supprime
        verify(panierRepository, times(1)).save(panier); // Save du panier (via recalculerMontantTotal)
        verify(modelMapper, times(1)).map(panier, PanierDto.class); // Mapping final du panier (avec le DTO mocké)
        // Vérifie que le mappage du contenu restant a bien été appelé (utilisé dans le mock map du panier)
        verify(modelMapper, times(1)).map(contenuAutre, ContenuPanierDto.class);
    }

    /**
     * Teste la suppression d'une offre qui n'est pas dans le panier.
     * Vérifie que le panier reste inchangé et qu'aucune suppression n'a lieu.
     */
    @Test
    void supprimerOffreDuPanier_shouldDoNothing_whenOffreNotInPanier() {
        // Arrange
        Utilisateur utilisateur = Utilisateur.builder().idUtilisateur(utilisateurId).build();
        // Panier ne contenant pas l'offreId recherchée
        Offre offreAutre = Offre.builder().idOffre(offreId + 1).prix(BigDecimal.valueOf(20)).build(); // Prix 20
        ContenuPanier contenuAutre = ContenuPanier.builder().offre(offreAutre).quantiteCommandee(1).build(); // Qte 1, Total 20

        // Utiliser un HashSet mutable
        Set<ContenuPanier> contenuPaniersSet = new HashSet<>();
        contenuPaniersSet.add(contenuAutre);

        Panier panier = Panier.builder().idPanier(panierId).utilisateur(utilisateur).statut(StatutPanier.EN_ATTENTE).montantTotal(BigDecimal.valueOf(20)).contenuPaniers(contenuPaniersSet).build(); // Total initial 20
        contenuAutre.setPanier(panier);

        Long offreIdNonPresente = 99L; // ID de l'offre qui n'est pas dans le panier

        // Simuler le PanierDto retourné INCHANGÉ (avec l'élément toujours présent et le montant total initial)
        PanierDto panierDto = PanierDto.builder()
                .idPanier(panierId)
                .idUtilisateur(utilisateurId)
                .statut(StatutPanier.EN_ATTENTE)
                .montantTotal(BigDecimal.valueOf(20)) // Total final inchangé
                .contenuPaniers(new ArrayList<>(Collections.singletonList(
                        ContenuPanierDto.builder()
                                .idOffre(offreAutre.getIdOffre())
                                .prixUnitaire(offreAutre.getPrix())
                                .quantiteCommandee(contenuAutre.getQuantiteCommandee())
                                .prixTotalOffre(offreAutre.getPrix().multiply(BigDecimal.valueOf(contenuAutre.getQuantiteCommandee())))
                                .build()
                )))
                .build();

        // Configuration du mock ModelMapper pour les contenus
        when(modelMapper.map(any(ContenuPanier.class), eq(ContenuPanierDto.class)))
                .thenAnswer(invocation -> {
                    ContenuPanier cp = invocation.getArgument(0);
                    return ContenuPanierDto.builder()
                            .idOffre(cp.getOffre().getIdOffre())
                            .prixUnitaire(cp.getOffre().getPrix())
                            .quantiteCommandee(cp.getQuantiteCommandee())
                            .prixTotalOffre(cp.getOffre().getPrix().multiply(BigDecimal.valueOf(cp.getQuantiteCommandee())))
                            .build();
                });

        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE))
                .thenReturn(Optional.of(panier));

        when(modelMapper.map(panier, PanierDto.class)).thenReturn(panierDto);

        // Act
        PanierDto result = panierService.supprimerOffreDuPanier(utilisateurIdStr, offreIdNonPresente); // Supprime offreIdNonPresente

        // Assert
        assertNotNull(result);
        // Vérifie le montant total (inchangé)
        assertEquals(BigDecimal.valueOf(20), result.getMontantTotal());
        // Vérifie la taille de la liste de contenus DTO retournée (inchangée)
        assertEquals(1, result.getContenuPaniers().size());
        // Vérifie que le DTO contient bien l'élément initial
        assertEquals(offreAutre.getIdOffre(), result.getContenuPaniers().get(0).getIdOffre());

        // Vérifications : aucune suppression ni sauvegarde ne devrait avoir lieu
        verify(utilisateurRepository, times(1)).findById(utilisateurId);
        verify(panierRepository, times(1)).findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE);
        verify(offreRepository, never()).findById(any()); // Offre non recherchée
        verify(contenuPanierRepository, never()).deleteById(any()); // Vérifie qu'aucune suppression n'a lieu
        verify(contenuPanierRepository, never()).delete(any()); // Au cas où delete(entity) serait appelé
        verify(panierRepository, never()).save(any()); // Pas de sauvegarde
        verify(modelMapper, times(1)).map(panier, PanierDto.class); // Mapping final
        verify(modelMapper, times(1)).map(contenuAutre, ContenuPanierDto.class); // Mapping du contenu
    }

    /**
     * Teste la suppression d'une offre de tous les paniers.
     * Vérifie que la méthode de repository appropriée est appelée.
     */
    @Test
    void supprimerOffreDeTousLesPaniers_shouldCallRepositoryMethod() {
        // Arrange
        Offre offre = Offre.builder().idOffre(offreId).build();

        // Mock la méthode de suppression en masse (renvoie void dans le repo réel)
        doNothing().when(contenuPanierRepository).deleteByOffre(offre);

        // Act
        panierService.supprimerOffreDeTousLesPaniers(offre);

        // Assert
        // Vérifie que la méthode de suppression par offre a été appelée
        verify(contenuPanierRepository, times(1)).deleteByOffre(offre);
    }

    /**
     * Teste la finalisation d'un achat avec succès.
     * Vérifie que le statut du panier passe à PAYE, que les places et le stock sont décrémentés.
     */
    @Test
    void finaliserAchat_shouldProcessPaymentAndUpdateEntities() {
        // Arrange
        Utilisateur utilisateur = Utilisateur.builder().idUtilisateur(utilisateurId).build();

        // Discipline et Offres avec suffisamment de places et de stock
        Discipline discipline = Discipline.builder().idDiscipline(disciplineId).nbPlaceDispo(10).nomDiscipline("Course").build(); // 10 places
        Offre offre1 = Offre.builder().idOffre(1L).capacite(2).quantite(5).prix(BigDecimal.TEN).discipline(discipline).statutOffre(StatutOffre.DISPONIBLE).build(); // Cap 2, Stock 5, Prix 10
        Offre offre2 = Offre.builder().idOffre(2L).capacite(1).quantite(10).prix(BigDecimal.valueOf(15)).discipline(discipline).statutOffre(StatutOffre.DISPONIBLE).build(); // Cap 1, Stock 10, Prix 15

        // Contenu du panier
        ContenuPanier cp1 = ContenuPanier.builder().offre(offre1).quantiteCommandee(2).build(); // Qte 2 (utilise 4 places, stock -2)
        ContenuPanier cp2 = ContenuPanier.builder().offre(offre2).quantiteCommandee(3).build(); // Qte 3 (utilise 3 places, stock -3)

        // Utiliser un HashSet mutable
        Set<ContenuPanier> contenuSet = new HashSet<>();
        contenuSet.add(cp1);
        contenuSet.add(cp2);

        Panier panier = Panier.builder().idPanier(panierId).utilisateur(utilisateur).statut(StatutPanier.EN_ATTENTE).montantTotal(BigDecimal.valueOf(20 + 45)).contenuPaniers(contenuSet).build(); // Total 65
        cp1.setPanier(panier);
        cp2.setPanier(panier);

        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE))
                .thenReturn(Optional.of(panier));
        when(disciplineRepository.findById(disciplineId)).thenReturn(Optional.of(discipline));

        // Mock la décrémentation des places (retourne 1 ligne affectée pour succès)
        when(disciplineRepository.decrementerPlaces(disciplineId, 4)).thenReturn(1); // Offre 1 (Qte 2 * Cap 2)
        when(disciplineRepository.decrementerPlaces(disciplineId, 3)).thenReturn(1); // Offre 2 (Qte 3 * Cap 1)

        // Mock la sauvegarde de l'offre (met à jour le stock)
        when(offreRepository.save(any(Offre.class))).thenAnswer(i -> i.getArguments()[0]); // Retourne l'offre modifiée

        // Mock la sauvegarde du panier final (statut PAYE)
        when(panierRepository.save(any(Panier.class))).thenAnswer(i -> i.getArguments()[0]); // Retourne le panier modifié

        // Mock le mappage final
        PanierDto panierDtoFinal = PanierDto.builder().idPanier(panierId).idUtilisateur(utilisateurId).statut(StatutPanier.PAYE).montantTotal(BigDecimal.valueOf(65)).build();
        when(modelMapper.map(any(Panier.class), eq(PanierDto.class))).thenReturn(panierDtoFinal);

        // Mock le mappage ContenuPanier -> DTO pour le mappage du panier final
        when(modelMapper.map(any(ContenuPanier.class), eq(ContenuPanierDto.class)))
                .thenAnswer(invocation -> {
                    ContenuPanier cp = invocation.getArgument(0);
                    if (cp == null || cp.getOffre() == null || cp.getOffre().getPrix() == null) {
                        return null;
                    }
                    return ContenuPanierDto.builder()
                            .idOffre(cp.getOffre().getIdOffre())
                            .prixUnitaire(cp.getOffre().getPrix())
                            .quantiteCommandee(cp.getQuantiteCommandee())
                            .prixTotalOffre(cp.getOffre().getPrix().multiply(BigDecimal.valueOf(cp.getQuantiteCommandee())))
                            .build();
                });


        // Act
        PanierDto result = panierService.finaliserAchat(utilisateurIdStr);

        // Assert
        assertNotNull(result);
        assertEquals(StatutPanier.PAYE, result.getStatut()); // Vérifie le statut du panier
        assertEquals(BigDecimal.valueOf(65), result.getMontantTotal()); // Vérifie le montant total

        // Vérifie que les places dans la discipline ont été décrémentées
        verify(disciplineRepository, times(1)).decrementerPlaces(disciplineId, 4); // Qte 2 * Cap 2
        verify(disciplineRepository, times(1)).decrementerPlaces(disciplineId, 3); // Qte 3 * Cap 1

        // Vérifie que le stock des offres a été mis à jour et sauvegardé
        // Note : les modifications de quantité sur les mocks d'entité sont vérifiées par ces assertions
        assertEquals(3, offre1.getQuantite()); // 5 - 2 = 3
        assertEquals(7, offre2.getQuantite()); // 10 - 3 = 7
        verify(offreRepository, times(2)).save(any(Offre.class)); // 2 offres sauvegardées

        // Vérifie que le panier final a été sauvegardé avec le nouveau statut
        verify(panierRepository, times(1)).save(panier); // Le panier en mémoire a été mis à jour avant save

        verify(modelMapper, times(1)).map(panier, PanierDto.class); // Mapping final du panier
        verify(modelMapper, times(2)).map(any(ContenuPanier.class), eq(ContenuPanierDto.class)); // Mapping des contenus pour le DTO final
    }

    /**
     * Teste la finalisation d'un achat lorsque le panier est déjà PAYE.
     * Vérifie qu'une exception {@link IllegalStateException} est levée avec le message correct.
     */
    @Test
    void finaliserAchat_shouldThrowIllegalStateException_whenPanierAlreadyPaid() {
        // Arrange
        Utilisateur utilisateur = Utilisateur.builder().idUtilisateur(utilisateurId).build();
        // Panier avec statut PAYE
        Panier panier = Panier.builder().idPanier(panierId).utilisateur(utilisateur).statut(StatutPanier.PAYE).build();


        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE))
                .thenReturn(Optional.of(panier)); // Le service trouve ce panier PAYE

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> panierService.finaliserAchat(utilisateurIdStr));

        // Vérifie le message de l'exception levée en utilisant la constante et le statut du panier
        assertEquals(PANIER_DEJA_PAYE + StatutPanier.PAYE, exception.getMessage());

        // Vérifie qu'aucune modification n'a eu lieu
        verify(disciplineRepository, never()).decrementerPlaces(any(), anyInt());
        verify(offreRepository, never()).save(any(Offre.class));
        verify(panierRepository, never()).save(any(Panier.class));
        verify(modelMapper, never()).map(any(), any()); // Mapping ne devrait pas être appelé
    }

    /**
     * Teste la finalisation d'un achat lorsque le panier est vide.
     * Vérifie qu'une exception {@link IllegalStateException} est levée avec le message correct.
     */
    @Test
    void finaliserAchat_shouldThrowIllegalStateException_whenPanierIsEmpty() {
        // Arrange
        Utilisateur utilisateur = Utilisateur.builder().idUtilisateur(utilisateurId).build();
        // Panier vide
        // Utiliser un HashSet mutable
        Set<ContenuPanier> contenuSet = new HashSet<>();

        Panier panier = Panier.builder().idPanier(panierId).utilisateur(utilisateur).statut(StatutPanier.EN_ATTENTE).montantTotal(BigDecimal.ZERO).contenuPaniers(contenuSet).build();

        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE))
                .thenReturn(Optional.of(panier));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> panierService.finaliserAchat(utilisateurIdStr));

        // Vérifie le message de l'exception levée en utilisant la constante
        assertEquals(PANIER_VIDE, exception.getMessage());

        // Vérifie qu'aucune modification n'a eu lieu
        verify(disciplineRepository, never()).decrementerPlaces(any(), anyInt());
        verify(offreRepository, never()).save(any(Offre.class));
        verify(panierRepository, never()).save(any(Panier.class));
        verify(modelMapper, never()).map(any(), any()); // Mapping ne devrait pas être appelé
    }

    /**
     * Teste la finalisation d'un achat lorsque les places disponibles dans la discipline sont insuffisantes
     * au moment de la décrémentation atomique par le repository.
     * Vérifie qu'une exception {@link IllegalStateException} est levée avec le message correct.
     */
    @Test
    void finaliserAchat_shouldThrowIllegalStateException_whenPlacesInsuffisantesForOffre() {
        // Arrange
        Utilisateur utilisateur = Utilisateur.builder().idUtilisateur(utilisateurId).build();

        // Discipline avec peu de places
        Discipline discipline = Discipline.builder().idDiscipline(disciplineId).nbPlaceDispo(1).nomDiscipline("Athlétisme").build(); // 1 place disponible

        // Offre nécessitant plus de places que disponible
        Offre offre = Offre.builder().idOffre(offreId).capacite(2).quantite(5).prix(BigDecimal.TEN).discipline(discipline).statutOffre(StatutOffre.DISPONIBLE).build(); // Capacité 2
        ContenuPanier cp = ContenuPanier.builder().offre(offre).quantiteCommandee(1).build(); // Qte 1 (utilise 2 places)

        // Utiliser un HashSet mutable
        Set<ContenuPanier> contenuSet = new HashSet<>();
        contenuSet.add(cp);

        Panier panier = Panier.builder().idPanier(panierId).utilisateur(utilisateur).statut(StatutPanier.EN_ATTENTE).montantTotal(BigDecimal.TEN).contenuPaniers(contenuSet).build(); // Total 10
        cp.setPanier(panier);

        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE))
                .thenReturn(Optional.of(panier));
        when(disciplineRepository.findById(disciplineId)).thenReturn(Optional.of(discipline));

        // Mock la décrémentation des places pour simuler l'échec (retourne 0 lignes affectées)
        // On essaie de décrémenter 2 places (Qte 1 * Cap 2), alors que nbPlaceDispo est 1.
        // La requête atomique du repo ne trouvera pas de ligne à modifier car 1 < 2.
        when(disciplineRepository.decrementerPlaces(disciplineId, 2)).thenReturn(0); // Simule l'échec de la décrémentation

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                (() -> panierService.finaliserAchat(utilisateurIdStr))); // Utiliser lambda pour assertThrows

        // Vérifie le message de l'exception levée en utilisant la constante correcte
        assertTrue(exception.getMessage().startsWith(PLACES_INSUFFISANTES));
        assertTrue(exception.getMessage().contains("(Discipline : " + discipline.getNomDiscipline() + ", Offre : " + offreId + ")"));

        // Vérifie que la décrémentation des places a eu lieu mais pas la suite
        verify(disciplineRepository, times(1)).decrementerPlaces(disciplineId, 2); // La tentative de décrémentation a eu lieu
        verify(offreRepository, never()).save(any(Offre.class)); // Pas de save d'offre si échec places
        verify(panierRepository, never()).save(any(Panier.class)); // Pas de save panier final si échec
        verify(modelMapper, never()).map(any(), any()); // Mapping ne devrait pas être appelé
    }

    /**
     * Teste la finalisation d'un achat lorsque le stock d'une offre est insuffisant au moment de la finalisation,
     * même si les places dans la discipline étaient suffisantes.
     * Vérifie qu'une exception {@link IllegalStateException} est levée avec le message correct.
     */
    @Test
    void finaliserAchat_shouldThrowIllegalStateException_whenStockInsuffisantForOffre() {
        // Arrange
        Utilisateur utilisateur = Utilisateur.builder().idUtilisateur(utilisateurId).build();

        // Discipline avec suffisamment de places
        Discipline discipline = Discipline.builder().idDiscipline(disciplineId).nbPlaceDispo(10).build(); // 10 places

        // Offre avec stock insuffisant
        Offre offre = Offre.builder().idOffre(offreId).capacite(2).quantite(1).prix(BigDecimal.TEN).discipline(discipline).statutOffre(StatutOffre.DISPONIBLE).build(); // Cap 2, Stock 1
        ContenuPanier cp = ContenuPanier.builder().offre(offre).quantiteCommandee(2).build(); // Qte commandée 2 (stock nécessaire 2)

        // Utiliser un HashSet mutable
        Set<ContenuPanier> contenuSet = new HashSet<>();
        contenuSet.add(cp);

        Panier panier = Panier.builder().idPanier(panierId).utilisateur(utilisateur).statut(StatutPanier.EN_ATTENTE).montantTotal(BigDecimal.valueOf(20)).contenuPaniers(contenuSet).build(); // Total 20
        cp.setPanier(panier);

        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateurId, StatutPanier.EN_ATTENTE))
                .thenReturn(Optional.of(panier));
        when(disciplineRepository.findById(disciplineId)).thenReturn(Optional.of(discipline));

        // Mock la décrémentation des places pour simuler le succès (suffisamment de places dans la discipline)
        // On essaie de décrémenter 4 places (Qte 2 * Cap 2), nbPlaceDispo est 10. Succès.
        when(disciplineRepository.decrementerPlaces(disciplineId, 4)).thenReturn(1); // Simule succès de la décrémentation des places

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> panierService.finaliserAchat(utilisateurIdStr));

        // Vérifie le message de l'exception levée en utilisant la constante correcte
        assertEquals(String.format(STOCK_INSUFFISANT_FINALISATION, offreId), exception.getMessage());

        // Vérifie que la décrémentation des places a eu lieu mais pas la suite
        verify(disciplineRepository, times(1)).decrementerPlaces(disciplineId, 4); // La décrémentation des places a eu lieu
        verify(offreRepository, never()).save(any(Offre.class)); // Pas de save d'offre car stock insuffisant
        verify(panierRepository, never()).save(any(Panier.class)); // Pas de save panier final si échec
        verify(modelMapper, never()).map(any(), any()); // Mapping ne devrait pas être appelé
    }
}