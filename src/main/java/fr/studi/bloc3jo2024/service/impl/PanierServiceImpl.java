package fr.studi.bloc3jo2024.service.impl;

import fr.studi.bloc3jo2024.dto.panier.AjouterOffrePanierDto;
import fr.studi.bloc3jo2024.dto.panier.ContenuPanierDto;
import fr.studi.bloc3jo2024.dto.panier.ModifierContenuPanierDto;
import fr.studi.bloc3jo2024.dto.panier.PanierDto;
import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.StatutPanier;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.*;
import fr.studi.bloc3jo2024.service.PanierService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PanierServiceImpl implements PanierService {

    // Déclaration des dépendances injectées via Lombok's @RequiredArgsConstructor
    private final PanierRepository panierRepository;
    private final ContenuPanierRepository contenuPanierRepository;
    private final OffreRepository offreRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DisciplineRepository disciplineRepository;
    private final ModelMapper modelMapper;

    // Déclaration des constantes pour les messages d'erreur
    private static final String DISCIPLINE_NOT_FOUND = "Discipline non trouvée avec l'ID : ";
    private static final String UTILISATEUR_NOT_FOUND = "Utilisateur non trouvé avec l'ID : ";
    private static final String OFFRE_NOT_FOUND = "Offre non trouvée avec l'ID : ";
    private static final String OFFRE_NOT_IN_PANIER = "L'offre avec l'ID %d n'est pas dans le panier";
    private static final String QUANTITE_INVALIDE_OU_NON_DISPONIBLE = "Quantité invalide ou offre non disponible.";
    private static final String QUANTITE_NON_POSITIVE = "La quantité demandée doit être positive.";
    private static final String PLACES_INSUFFISANTES = "Nombre de places disponibles insuffisant pour l'offre ou la discipline.";
    private static final String PANIER_DEJA_PAYE = "Le panier ne peut pas être payé car son statut est : ";
    private static final String PANIER_VIDE = "Le panier est vide. Impossible de finaliser l'achat.";
    private static final String OFFRE_DISCIPLINE_NULL = "L'offre avec l'ID %d n'est associée à aucune discipline ou l'objet discipline est nul.";
    private static final String CONTENU_PANIER_INVALIDE = "Contenu de panier ou offre invalide trouvé lors de l'opération.";
    private static final String STOCK_INSUFFISANT_FINALISATION = "Stock de l'offre (%d) insuffisant au moment de la finalisation.";

    /**
     * Récupère le panier en cours (avec statut EN_ATTENTE) de l'utilisateur.
     * Si l'utilisateur n'a pas de panier EN_ATTENTE, un nouveau panier est créé,
     * sauvegardé et retourné.
     *
     * @param utilisateurIdStr L'ID de l'utilisateur au format String.
     * @return Un PanierDto représentant le panier de l'utilisateur.
     * @throws ResourceNotFoundException Si l'utilisateur n'est pas trouvé.
     */
    @Override
    @Transactional(readOnly = true) // Peut être en lecture seule si creerNouveauPanier n'est pas appelé
    public PanierDto getPanierUtilisateur(String utilisateurIdStr) {
        // getPanierUtilisateurEntity gère déjà la recherche de l'utilisateur et la création du panier si nécessaire
        Panier panier = getPanierUtilisateurEntity(utilisateurIdStr);
        return mapPanierToDto(panier);
    }

    /**
     * Crée un nouveau panier pour un utilisateur spécifié.
     * Initialise le panier avec le statut EN_ATTENTE, un montant total de 0
     * et un ensemble vide de contenus panier.
     *
     * @param utilisateur L'utilisateur pour lequel créer le panier.
     * @return Le panier nouvellement créé et persisté.
     */
    private Panier creerNouveauPanier(Utilisateur utilisateur) {
        Panier nouveauPanier = new Panier(); // Utilisation du constructeur par défaut
        nouveauPanier.setUtilisateur(utilisateur);
        nouveauPanier.setStatut(StatutPanier.EN_ATTENTE);
        // Initialisation du montant total et du contenu pour éviter les NPE
        nouveauPanier.setMontantTotal(BigDecimal.ZERO);
        nouveauPanier.setContenuPaniers(new java.util.HashSet<>());
        // La date d'ajout peut être définie ici si nécessaire, mais pas dans l'original, donc omis.
        return panierRepository.save(nouveauPanier); // Persistance du panier
    }

    /**
     * Ajoute une offre au panier de l'utilisateur.
     * Si l'offre est déjà présente, sa quantité est augmentée.
     * Si l'offre n'est pas présente, un nouvel élément est ajouté au panier.
     * Les validations sur la disponibilité de l'offre (stock) et les places dans la discipline sont effectuées.
     *
     * @param utilisateurIdStr      L'ID de l'utilisateur au format String.
     * @param ajouterOffrePanierDto Les détails de l'offre à ajouter (ID et quantité).
     * @return Le PanierDto mis à jour.
     * @throws ResourceNotFoundException Si l'utilisateur, l'offre ou la discipline n'est pas trouvé.
     * @throws IllegalArgumentException  Si la quantité demandée est invalide ou l'offre n'est pas disponible/quantité insuffisante.
     * @throws IllegalStateException     Si l'offre n'est pas associée à une discipline ou si les places disponibles sont insuffisantes dans la discipline.
     */
    @Override
    @Transactional
    public PanierDto ajouterOffreAuPanier(String utilisateurIdStr, AjouterOffrePanierDto ajouterOffrePanierDto) {
        Panier panier = getPanierUtilisateurEntity(utilisateurIdStr); // Récupération/création du panier
        Offre offre = offreRepository.findById(ajouterOffrePanierDto.getIdOffre())
                .orElseThrow(() -> new ResourceNotFoundException(OFFRE_NOT_FOUND + ajouterOffrePanierDto.getIdOffre())); // Récupération de l'offre

        // Vérification de la disponibilité de l'offre et de la quantité globale de l'offre (stock)
        if (offre.getStatutOffre() != StatutOffre.DISPONIBLE || offre.getQuantite() < ajouterOffrePanierDto.getQuantite()) {
            throw new IllegalArgumentException(QUANTITE_INVALIDE_OU_NON_DISPONIBLE);
        }
        // Vérification de la quantité demandée (doit être positive)
        if (ajouterOffrePanierDto.getQuantite() <= 0) {
            throw new IllegalArgumentException(QUANTITE_NON_POSITIVE);
        }

        // Toutes les offres ajoutables au panier ont une discipline.
        if (offre.getDiscipline() == null || offre.getDiscipline().getIdDiscipline() == null) {
            throw new IllegalStateException(String.format(OFFRE_DISCIPLINE_NULL, offre.getIdOffre()));
        }

        Discipline discipline = disciplineRepository.findById(offre.getDiscipline().getIdDiscipline())
                .orElseThrow(() -> new ResourceNotFoundException(DISCIPLINE_NOT_FOUND + offre.getDiscipline().getIdDiscipline())); // Récupération de la discipline

        // Trouver l'élément de ContenuPanier existant pour cette offre (s'il y en a un)
        Optional<ContenuPanier> existingContenuPanierOpt = panier.getContenuPaniers().stream()
                .filter(cp -> cp != null && cp.getOffre() != null && cp.getOffre().getIdOffre().equals(offre.getIdOffre())) // Ajout null check
                .findFirst();

        int currentQuantityInCart = existingContenuPanierOpt.map(ContenuPanier::getQuantiteCommandee).orElse(0);
        int quantityToAdd = ajouterOffrePanierDto.getQuantite();
        int newTotalQuantityForThisItem = currentQuantityInCart + quantityToAdd;

        // Calculer les places totales dans la discipline après avoir ajouté la nouvelle quantité pour l'offre actuelle
        int newTotalPlacesInDiscipline = calculateTotalPlacesForDiscipline(panier, discipline, offre, newTotalQuantityForThisItem); // Utilisation de la méthode d'aide

        if (discipline.getNbPlaceDispo() < newTotalPlacesInDiscipline) {
            throw new IllegalStateException(PLACES_INSUFFISANTES + " (Discipline : " + discipline.getNomDiscipline() + ")"); // Message plus précis
        }

        ContenuPanier contenuPanier = existingContenuPanierOpt.orElseGet(() -> {
            // Créer un nouvel élément si l'offre n'est pas déjà dans le panier
            ContenuPanier nouveauContenuPanier = new ContenuPanier();
            nouveauContenuPanier.setPanier(panier);
            nouveauContenuPanier.setOffre(offre);
            // Assurez-vous que la collection dans le panier est initialisée avant d'y ajouter
            if (panier.getContenuPaniers() == null) {
                panier.setContenuPaniers(new java.util.HashSet<>());
            }
            panier.getContenuPaniers().add(nouveauContenuPanier); // Ajouter au Set en mémoire
            return nouveauContenuPanier;
        });

        // Mettre à jour la quantité de l'élément (nouvellement créé ou existant)
        contenuPanier.setQuantiteCommandee(newTotalQuantityForThisItem);
        contenuPanierRepository.save(contenuPanier); // Persistance de ContenuPanier

        // Recalcul du montant total du panier et persistance du panier mis à jour
        recalculerMontantTotal(panier);

        return mapPanierToDto(panier); // Conversion en DTO
    }

    /**
     * Modifie la quantité d'une offre spécifique dans le panier de l'utilisateur.
     * Effectue des validations sur la nouvelle quantité et les places disponibles dans la discipline associée.
     * Si la nouvelle quantité est 0, l'offre est supprimée du panier en appelant {@link #supprimerOffreDuPanier}.
     *
     * @param utilisateurIdStr         L'ID de l'utilisateur au format String.
     * @param modifierContenuPanierDto Les détails de la modification (ID de l'offre et nouvelle quantité).
     * @return Le PanierDto mis à jour.
     * @throws ResourceNotFoundException Si l'utilisateur, l'offre n'est pas trouvé, ou si l'offre n'est pas dans le panier.
     * @throws IllegalArgumentException  Si la nouvelle quantité est négative.
     * @throws IllegalStateException     Si l'offre n'est pas associée à une discipline ou si les places disponibles sont insuffisantes pour la nouvelle quantité dans la discipline.
     */
    @Override
    @Transactional
    public PanierDto modifierQuantiteOffrePanier(String utilisateurIdStr, ModifierContenuPanierDto modifierContenuPanierDto) {
        Panier panier = getPanierUtilisateurEntity(utilisateurIdStr); // Récupération du panier
        Offre offre = offreRepository.findById(modifierContenuPanierDto.getIdOffre())
                .orElseThrow(() -> new ResourceNotFoundException(OFFRE_NOT_FOUND + modifierContenuPanierDto.getIdOffre())); // Récupération de l'offre

        int nouvelleQuantite = modifierContenuPanierDto.getNouvelleQuantite();

        // Vérifier si la nouvelle quantité est raisonnable (non négative)
        if (nouvelleQuantite < 0) {
            throw new IllegalArgumentException("La quantité à modifier ne peut pas être négative.");
        }

        // Si la nouvelle quantité est 0, supprimer l'offre du panier
        if (nouvelleQuantite == 0) {
            return supprimerOffreDuPanier(utilisateurIdStr, offre.getIdOffre());
        }

        // Rechercher l'élément ContenuPanier dans le Set en mémoire du panier
        ContenuPanier contenuPanierExistant = panier.getContenuPaniers().stream()
                .filter(cp -> cp != null && cp.getOffre() != null && cp.getOffre().getIdOffre().equals(offre.getIdOffre()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(String.format(OFFRE_NOT_IN_PANIER, offre.getIdOffre())));


        // Assurez-vous que l'offre a une discipline associée
        if (offre.getDiscipline() == null || offre.getDiscipline().getIdDiscipline() == null) {
            throw new IllegalStateException(String.format(OFFRE_DISCIPLINE_NULL, offre.getIdOffre()));
        }

        Discipline discipline = disciplineRepository.findById(offre.getDiscipline().getIdDiscipline())
                .orElseThrow(() -> new ResourceNotFoundException(DISCIPLINE_NOT_FOUND + offre.getDiscipline().getIdDiscipline())); // Récupération de la discipline

        // Calculer les places totales dans la discipline après avoir appliqué la nouvelle quantité pour l'offre actuelle
        int newTotalPlacesInDiscipline = calculateTotalPlacesForDiscipline(panier, discipline, offre, nouvelleQuantite); // Utilisation de la méthode d'aide

        // Vérifier si le nombre total de places après la modification dépasse la capacité de la discipline
        if (discipline.getNbPlaceDispo() < newTotalPlacesInDiscipline) {
            throw new IllegalStateException(PLACES_INSUFFISANTES + " (Discipline : " + discipline.getNomDiscipline() + ")"); // Message plus précis
        }

        // La modification est valide, mettre à jour la quantité sur l'objet en mémoire
        contenuPanierExistant.setQuantiteCommandee(nouvelleQuantite);

        // Persister l'élément de contenu modifié
        contenuPanierRepository.save(contenuPanierExistant);

        // Recalcul du montant total du panier et persistance du panier mis à jour
        recalculerMontantTotal(panier);

        return mapPanierToDto(panier); // Retourne le DTO
    }

    /**
     * Supprime une offre spécifique du panier de l'utilisateur.
     * Si l'offre n'est pas trouvée dans le panier, l'opération n'a aucun effet.
     * Le montant total du panier est recalculé après la suppression.
     *
     * @param utilisateurIdStr L'ID de l'utilisateur au format String.
     * @param offreId          L'ID de l'offre à supprimer du panier.
     * @return Le PanierDto mis à jour.
     * @throws ResourceNotFoundException Si l'utilisateur n'est pas trouvé.
     */
    @Override
    @Transactional
    public PanierDto supprimerOffreDuPanier(String utilisateurIdStr, Long offreId) {
        Panier panier = getPanierUtilisateurEntity(utilisateurIdStr); // Récupération du panier

        // Trouver l'élément dans le Set en mémoire pour le retirer
        Optional<ContenuPanier> contenuARetirerOpt = panier.getContenuPaniers().stream()
                .filter(cp -> cp != null && cp.getOffre() != null && cp.getOffre().getIdOffre().equals(offreId))
                .findFirst();

        if (contenuARetirerOpt.isPresent()) {
            ContenuPanier contenuARetirer = contenuARetirerOpt.get();

            // Création de la clé composite pour la suppression en base
            ContenuPanierId key = new ContenuPanierId(panier.getIdPanier(), offreId);

            // Supprimer de la base de données en utilisant la clé composite
            contenuPanierRepository.deleteById(key);

            // Retirer l'objet du Set en mémoire pour maintenir la cohérence
            panier.getContenuPaniers().remove(contenuARetirer);

            // Recalculer le montant total maintenant que l'élément a été retiré du Set
            recalculerMontantTotal(panier); // Persiste aussi le panier
        }

        // Le mappage se fera sur l'objet panier mis à jour (ou inchangé si l'offre n'était pas là)
        return mapPanierToDto(panier);
    }

    /**
     * Vide complètement le panier en cours de l'utilisateur en supprimant tous ses éléments de contenu.
     * Le montant total du panier est également mis à zéro.
     *
     * @param utilisateurIdStr L'ID de l'utilisateur au format String.
     * @return Le PanierDto vidé.
     * @throws ResourceNotFoundException Si l'utilisateur n'est pas trouvé.
     */
    @Override
    @Transactional
    public PanierDto viderPanier(String utilisateurIdStr) {
        Panier panier = getPanierUtilisateurEntity(utilisateurIdStr); // Récupération du panier

        // Vérifier si le panier a du contenu avant de tenter de supprimer
        if (panier != null && panier.getContenuPaniers() != null && !panier.getContenuPaniers().isEmpty()) {
            // Supprimer tous les éléments de contenu associés à ce panier en base
            contenuPanierRepository.deleteByPanier(panier);

            // Vider le Set en mémoire pour maintenir la cohérence
            panier.getContenuPaniers().clear();

            // Réinitialisation du montant total
            panier.setMontantTotal(BigDecimal.ZERO);

            // Persistance du panier modifié (vide)
            panierRepository.save(panier);

        }
        return mapPanierToDto(panier);
    }

    /**
     * Supprime toutes les occurrences d'une offre donnée dans tous les paniers.
     * Cette méthode est typiquement appelée lors de la suppression d'une offre.
     * Note : Cette opération ne met pas à jour automatiquement les montants totaux
     * des paniers affectés en temps réel. Un mécanisme supplémentaire pourrait être nécessaire
     * si l'affichage immédiat des totaux corrigés est requis après la suppression d'une offre.
     *
     * @param offre L'offre à supprimer de tous les paniers.
     */
    @Override
    @Transactional // Assurez-vous que cette méthode est transactionnelle car elle modifie la base
    public void supprimerOffreDeTousLesPaniers(Offre offre) {
        // Vérification null pour l'offre avant de tenter la suppression
        if (offre != null) {
            // Utilise une méthode de repository personnalisée pour supprimer en masse
            // Selon l'erreur et la signature du repo, cette méthode renvoie void.
            contenuPanierRepository.deleteByOffre(offre); // Appel sans capturer la valeur de retour
        }
    }

    /**
     * Finalise le processus d'achat pour le panier en cours de l'utilisateur.
     * Change le statut du panier à PAYE, décrémente la quantité des offres
     * correspondantes dans le stock et décrémente les places disponibles dans les disciplines associées.
     * Cette opération est transactionnelle pour garantir la cohérence des données.
     *
     * @param utilisateurIdStr L'ID de l'utilisateur au format String.
     * @return Le PanierDto mis à jour avec le statut PAYE.
     * @throws ResourceNotFoundException Si l'utilisateur ou une discipline associée n'est pas trouvée.
     * @throws IllegalStateException     Si le panier ne peut pas être payé (statut incorrect, vide)
     *                                   ou si le nombre de places ou le stock de l'offre est insuffisant au moment de la finalisation.
     */
    @Override
    @Transactional // Assure l'atomicité de l'opération d'achat
    public PanierDto finaliserAchat(String utilisateurIdStr) {
        Panier panier = getPanierUtilisateurEntity(utilisateurIdStr); // Récupération du panier

        // Vérification du statut du panier
        if (panier.getStatut() != StatutPanier.EN_ATTENTE) {
            throw new IllegalStateException(PANIER_DEJA_PAYE + panier.getStatut());
        }

        // Vérification si le panier est vide
        if (panier.getContenuPaniers() == null || panier.getContenuPaniers().isEmpty()) {
            throw new IllegalStateException(PANIER_VIDE);
        }

        // Parcours des contenus du panier pour valider et mettre à jour
        for (ContenuPanier contenu : panier.getContenuPaniers()) {
            // Ajout de vérifications null rigoureuses pour les objets liés essentiels
            if (contenu == null || contenu.getOffre() == null || contenu.getOffre().getIdOffre() == null) {
                throw new IllegalStateException(CONTENU_PANIER_INVALIDE + " Détails: Contenu ou Offre null.");
            }
            Offre offre = contenu.getOffre();
            int quantiteCommandee = contenu.getQuantiteCommandee();

            // Vérification de la quantité commandée (doit être positive pour finaliser)
            if (quantiteCommandee <= 0) {
                throw new IllegalStateException("Quantité commandée nulle ou négative trouvée pour l'offre " + offre.getIdOffre() + " dans le panier.");
            }


            // Vérification que l'offre a une discipline si elle est censée en avoir une
            if (offre.getDiscipline() == null || offre.getDiscipline().getIdDiscipline() == null) {
                throw new IllegalStateException(String.format(OFFRE_DISCIPLINE_NULL, offre.getIdOffre()));
            }
            Discipline discipline = disciplineRepository.findById(offre.getDiscipline().getIdDiscipline())
                    .orElseThrow(() -> {
                        return new ResourceNotFoundException(DISCIPLINE_NOT_FOUND + offre.getDiscipline().getIdDiscipline());
                    });

            int placesOccupees = offre.getCapacite() * quantiteCommandee; // Calcul des places à décrémenter

            int updatedPlacesCount = disciplineRepository.decrementerPlaces(discipline.getIdDiscipline(), placesOccupees);

            if (updatedPlacesCount == 0) {
                throw new IllegalStateException(String.format(PLACES_INSUFFISANTES + " (Discipline : %s, Offre : %d)", discipline.getNomDiscipline(), offre.getIdOffre()));
            }

            // Vérification de stock avant décrémentation pour double sécurité et robustesse
            if (offre.getQuantite() < quantiteCommandee) {
                throw new IllegalStateException(String.format(STOCK_INSUFFISANT_FINALISATION, offre.getIdOffre()));
            }
            offre.setQuantite(offre.getQuantite() - quantiteCommandee); // Mise à jour de la quantité de l'offre (stock)
            offreRepository.save(offre); // Persistance de l'offre modifiée
        }

        // Si la boucle se termine sans exception, toutes les validations sont passées et les mises à jour sont faites en mémoire.
        panier.setStatut(StatutPanier.PAYE); // Mise à jour du statut du panier
        panierRepository.save(panier); // Persistance du panier avec le nouveau statut
        return mapPanierToDto(panier); // Retourne le PanierDto finalisé
    }

    /**
     * Recalcule le montant total du panier en sommant le prix total de chaque élément de contenu.
     * Le panier est ensuite sauvegardé pour persister le nouveau montant total.
     * Assure que le Set de contenus panier est initialisé pour éviter les erreurs.
     *
     * @param panier Le panier dont le montant total doit être recalculé.
     */
    private void recalculerMontantTotal(Panier panier) {
        // Vérification null pour le panier source
        if (panier == null) {
            return;
        }
        // S'assurer que le Set de contenus est initialisé
        if (panier.getContenuPaniers() == null) {
            panier.setContenuPaniers(new java.util.HashSet<>()); // Initialiser si null
        }

        BigDecimal total = panier.getContenuPaniers().stream()
                .filter(contenu -> contenu != null && contenu.getOffre() != null && contenu.getOffre().getPrix() != null) // Filtrer les contenus invalides
                .map(contenu -> {
                    try {
                        BigDecimal prix = contenu.getOffre().getPrix();
                        BigDecimal quantite = BigDecimal.valueOf(contenu.getQuantiteCommandee());
                        return prix.multiply(quantite);
                    } catch (Exception e) {
                        // Utiliser les IDs du panier et de l'offre pour l'identification
                        Long panierId = (contenu != null && contenu.getPanier() != null) ? contenu.getPanier().getIdPanier() : null;
                        Long offreId = (contenu != null && contenu.getOffre() != null) ? contenu.getOffre().getIdOffre() : null;
                        return BigDecimal.ZERO; // Retourner 0 pour ce contenu invalide
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Démarrer l'addition avec ZERO

        panier.setMontantTotal(total);
        panierRepository.save(panier); // Persister le panier avec le nouveau total
    }

    /**
     * Méthode utilitaire pour récupérer l'entité Panier en cours (statut EN_ATTENTE)
     * de l'utilisateur identifié par son ID String.
     * Gère la conversion de l'ID String en UUID et la recherche de l'utilisateur.
     * Si aucun panier EN_ATTENTE n'est trouvé, en crée un nouveau.
     *
     * @param utilisateurIdStr L'ID de l'utilisateur au format String.
     * @return L'entité Panier de l'utilisateur avec le statut EN_ATTENTE.
     * @throws ResourceNotFoundException Si l'utilisateur n'est pas trouvé.
     */
    private Panier getPanierUtilisateurEntity(String utilisateurIdStr) {
        UUID utilisateurId = UUID.fromString(utilisateurIdStr);
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException(UTILISATEUR_NOT_FOUND + utilisateurId));
        // Tente de trouver un panier EN_ATTENTE existant
        return panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateur.getIdUtilisateur(), StatutPanier.EN_ATTENTE)
                // Si aucun panier EN_ATTENTE n'est trouvé, en crée un nouveau et le retourne
                .orElseGet(() -> creerNouveauPanier(utilisateur));
    }

    /**
     * Convertit une entité {@link Panier} en son DTO correspondant {@link PanierDto}.
     * Utilise ModelMapper pour la conversion et gère spécifiquement la conversion
     * de l'ensemble des {@link ContenuPanier} en liste de {@link ContenuPanierDto}.
     * Ajoute des vérifications null pour garantir la robustesse.
     *
     * @param panier L'entité Panier à convertir. Peut être null.
     * @return Le PanierDto correspondant, ou null si le panier d'entrée est null.
     */
    private PanierDto mapPanierToDto(Panier panier) {
        // Vérification null pour le panier source
        if (panier == null) {
            return null;
        }

        // Mappage de l l'entité Panier vers PanierDto par ModelMapper
        PanierDto panierDto = modelMapper.map(panier, PanierDto.class);

        // Mappage manuel de l'ID utilisateur si ModelMapper ne le gère pas via la relation
        if (panier.getUtilisateur() != null && panier.getUtilisateur().getIdUtilisateur() != null) {
            panierDto.setIdUtilisateur(panier.getUtilisateur().getIdUtilisateur());
        } else {
            panierDto.setIdUtilisateur(null); // S'assurer que l'ID est null dans le DTO aussi
        }

        // S'assurer que le Set de contenus en mémoire est initialisé avant de le streamer
        if (panier.getContenuPaniers() == null) {
            panier.setContenuPaniers(new java.util.HashSet<>()); // Initialiser un set vide si null
        }


        // Conversion de l'ensemble des ContenuPanier en liste de ContenuPanierDto
        // Utilise ModelMapper pour chaque élément de contenu via le flux Stream
        List<ContenuPanierDto> contenuPaniersDto = panier.getContenuPaniers().stream()
                // Filtrer les éléments de contenu qui pourraient être nuls ou invalides (sans offre ou sans prix)
                .filter(contenu -> contenu != null && contenu.getOffre() != null && contenu.getOffre().getPrix() != null)
                .map(contenu -> {
                    try {
                        // Mappe l'entité ContenuPanier vers ContenuPanierDto
                        // Votre ModelMapperConfig devrait gérer le mappage des champs, y compris le calcul de prixTotalOffre
                        ContenuPanierDto dto = modelMapper.map(contenu, ContenuPanierDto.class);
                        if (dto.getIdOffre() == null && contenu.getOffre().getIdOffre() != null) {
                            dto.setIdOffre(contenu.getOffre().getIdOffre());
                        }
                        if (dto.getPrixUnitaire() == null && contenu.getOffre().getPrix() != null) {
                            dto.setPrixUnitaire(contenu.getOffre().getPrix());
                        }
                        // Si prixTotalOffre n'est pas mappé automatiquement et correctement par ModelMapperConfig:
                        if (dto.getPrixTotalOffre() == null && dto.getPrixUnitaire() != null) {
                            dto.setPrixTotalOffre(dto.getPrixUnitaire().multiply(BigDecimal.valueOf(dto.getQuantiteCommandee())));
                        }

                        return dto;
                    } catch (Exception e) {
                        // Utiliser les IDs du panier et de l'offre pour l'identification
                        Long panierId = (contenu != null && contenu.getPanier() != null) ? contenu.getPanier().getIdPanier() : null;
                        Long offreId = (contenu != null && contenu.getOffre() != null) ? contenu.getOffre().getIdOffre() : null;
                        return null; // Retourner null pour cet élément invalide après logging
                    }
                })
                .filter(Objects::nonNull) // Supprimer les éléments null qui pourraient résulter du mappage d'éléments invalides
                .collect(Collectors.toList()); // Collecter dans une liste

        // Assigner la liste des DTOs de contenu au PanierDto
        panierDto.setContenuPaniers(contenuPaniersDto);

        return panierDto;
    }

    /**
     * Calcule le nombre total de places occupées dans une discipline spécifique par les offres
     * présentes dans un panier, en utilisant une quantité potentiellement modifiée pour une offre donnée.
     * Cette méthode est utilisée pour les validations de capacité lors de l'ajout ou la modification
     * d'éléments dans le panier.
     *
     * @param panier                   L'entité Panier.
     * @param discipline               La discipline concernée.
     * @return Le nombre total de places occupées pour cette discipline dans le panier après application de la quantité spécifiée pour l'offre donnée.
     */
    private int calculateTotalPlacesForDiscipline(Panier panier, Discipline discipline, Offre targetOffre, int quantityForTargetOffre) {
        if (panier == null || panier.getContenuPaniers() == null || discipline == null || discipline.getIdDiscipline() == null || targetOffre == null) {
            return 0;
        }


        int currentTotalPlacesExcludingTarget = 0;
        for (ContenuPanier cp : panier.getContenuPaniers()) {
            if (cp == null || cp.getOffre() == null || cp.getOffre().getDiscipline() == null || cp.getOffre().getDiscipline().getIdDiscipline() == null) {
                continue;
            }

            if (cp.getOffre().getDiscipline().getIdDiscipline().equals(discipline.getIdDiscipline()) && !cp.getOffre().getIdOffre().equals(targetOffre.getIdOffre())) {
                int itemPlaces = cp.getOffre().getCapacite() * cp.getQuantiteCommandee();
                currentTotalPlacesExcludingTarget += itemPlaces;
            }
        }
        int finalTotal = currentTotalPlacesExcludingTarget + (targetOffre.getCapacite() * quantityForTargetOffre);
        return finalTotal;
    }
}