package fr.studi.bloc3jo2024.service.impl;

import fr.studi.bloc3jo2024.dto.panier.AjouterOffrePanierDto;
import fr.studi.bloc3jo2024.dto.panier.ContenuPanierDto;
import fr.studi.bloc3jo2024.dto.panier.ModifierContenuPanierDto;
import fr.studi.bloc3jo2024.dto.panier.PanierDto;
import fr.studi.bloc3jo2024.entity.ContenuPanier;
import fr.studi.bloc3jo2024.entity.ContenuPanierId;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.Panier;
import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.entity.enums.StatutPanier;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.ContenuPanierRepository;
import fr.studi.bloc3jo2024.repository.DisciplineRepository;
import fr.studi.bloc3jo2024.repository.OffreRepository;
import fr.studi.bloc3jo2024.repository.PanierRepository;
import fr.studi.bloc3jo2024.repository.UtilisateurRepository;
import fr.studi.bloc3jo2024.service.PanierService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PanierServiceImpl implements PanierService {

    // Déclaration des dépendances (repositories, mapper, service)
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
    private static final String QUANTITE_INVALIDE_OU_NON_DISPONIBLE = "Quantité invalide ou offre non disponible";
    private static final String PLACES_INSUFFISANTES = "Nombre de places disponibles insuffisant pour cette offre.";
    private static final String PANIER_DEJA_PAYE = "Le panier ne peut pas être payé car son statut est : ";

    /**
     * Récupère le panier en cours de l'utilisateur.
     * Si l'utilisateur n'a pas de panier en cours, un nouveau panier est créé.
     *
     * @param utilisateurIdStr L'ID de l'utilisateur au format String.
     * @return Un PanierDto représentant le panier de l'utilisateur.
     * @throws ResourceNotFoundException Si l'utilisateur n'est pas trouvé.
     */
    @Override
    @Transactional
    public PanierDto getPanierUtilisateur(String utilisateurIdStr) {
        UUID utilisateurId = UUID.fromString(utilisateurIdStr); // Conversion de l'ID utilisateur
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException(UTILISATEUR_NOT_FOUND + utilisateurId)); // Récupération de l'utilisateur
        // Récupération du panier "EN_ATTENTE" ou création d'un nouveau
        Panier panier = panierRepository.findByUtilisateurIdUtilisateurAndStatut(utilisateur.getIdUtilisateur(), StatutPanier.EN_ATTENTE)
                .orElseGet(() -> creerNouveauPanier(utilisateur));
        return mapPanierToDto(panier); // Conversion de l'entité Panier en DTO
    }

    /**
     * Crée un nouveau panier pour un utilisateur.
     *
     * @param utilisateur L'utilisateur pour lequel créer le panier.
     * @return Le panier nouvellement créé et persisté.
     */
    private Panier creerNouveauPanier(Utilisateur utilisateur) {
        Panier nouveauPanier = new Panier();
        nouveauPanier.setUtilisateur(utilisateur);
        nouveauPanier.setStatut(StatutPanier.EN_ATTENTE);
        return panierRepository.save(nouveauPanier); // Persistance du panier
    }

    /**
     * Ajoute une offre au panier de l'utilisateur.
     *
     * @param utilisateurIdStr L'ID de l'utilisateur au format String.
     * @param ajouterOffrePanierDto Les détails de l'offre à ajouter (ID et quantité).
     * @return Le PanierDto mis à jour.
     * @throws ResourceNotFoundException Si l'utilisateur ou l'offre n'est pas trouvé.
     * @throws IllegalArgumentException Si la quantité est invalide ou l'offre n'est pas disponible.
     * @throws IllegalStateException Si le nombre de places disponibles est insuffisant.
     */
    @Override
    @Transactional
    public PanierDto ajouterOffreAuPanier(String utilisateurIdStr, AjouterOffrePanierDto ajouterOffrePanierDto) {
        Panier panier = getPanierUtilisateurEntity(utilisateurIdStr); // Récupération du panier de l'utilisateur
        Offre offre = offreRepository.findById(ajouterOffrePanierDto.getIdOffre())
                .orElseThrow(() -> new ResourceNotFoundException(OFFRE_NOT_FOUND + ajouterOffrePanierDto.getIdOffre())); // Récupération de l'offre

        // Vérification de la disponibilité de l'offre et de la quantité
        if (offre.getStatutOffre() != fr.studi.bloc3jo2024.entity.enums.StatutOffre.DISPONIBLE || offre.getQuantite() < ajouterOffrePanierDto.getQuantite()) {
            throw new IllegalArgumentException(QUANTITE_INVALIDE_OU_NON_DISPONIBLE);
        }

        Discipline discipline = disciplineRepository.findById(offre.getDiscipline().getIdDiscipline())
                .orElseThrow(() -> new ResourceNotFoundException(DISCIPLINE_NOT_FOUND + offre.getDiscipline().getIdDiscipline())); // Récupération de la discipline

        int placesAAjouter = offre.getCapacite() * ajouterOffrePanierDto.getQuantite(); // Calcul des places à ajouter
        int placesOccupeesPanier = panier.getContenuPaniers().stream()
                .mapToInt(cp -> cp.getOffre().getCapacite() * cp.getQuantiteCommandee())
                .sum(); // Calcul des places déjà occupées dans le panier

        // Vérification de la disponibilité des places
        if (discipline.getNbPlaceDispo() < placesOccupeesPanier + placesAAjouter) {
            throw new IllegalStateException(PLACES_INSUFFISANTES);
        }

        ContenuPanierId key = new ContenuPanierId(panier.getIdPanier(), offre.getIdOffre()); // Création de la clé composite pour ContenuPanier
        // Récupération du ContenuPanier existant ou création d'un nouveau
        ContenuPanier contenuPanier = contenuPanierRepository.findById(key).orElseGet(() -> {
            ContenuPanier nouveauContenuPanier = new ContenuPanier();
            nouveauContenuPanier.setPanier(panier);
            nouveauContenuPanier.setOffre(offre);
            nouveauContenuPanier.setQuantiteCommandee(0); // Initialisation à 0 pour l'incrémentation
            return nouveauContenuPanier;
        });
        contenuPanier.setQuantiteCommandee(contenuPanier.getQuantiteCommandee() + ajouterOffrePanierDto.getQuantite()); // Mise à jour de la quantité
        contenuPanierRepository.save(contenuPanier); // Persistance de ContenuPanier
        recalculerMontantTotal(panier); // Recalcul du montant total du panier
        return mapPanierToDto(panier); // Conversion en DTO
    }

    /**
     * Modifie la quantité d'une offre dans le panier de l'utilisateur.
     *
     * @param utilisateurIdStr L'ID de l'utilisateur au format String.
     * @param modifierContenuPanierDto Les détails de la modification (ID de l'offre et nouvelle quantité).
     * @return Le PanierDto mis à jour.
     * @throws ResourceNotFoundException Si l'utilisateur ou l'offre n'est pas trouvé, ou si l'offre n'est pas dans le panier.
     * @throws IllegalStateException Si le nombre de places disponibles est insuffisant pour la nouvelle quantité.
     */
    @Override
    @Transactional
    public PanierDto modifierQuantiteOffrePanier(String utilisateurIdStr, ModifierContenuPanierDto modifierContenuPanierDto) {
        Panier panier = getPanierUtilisateurEntity(utilisateurIdStr); // Récupération du panier
        Offre offre = offreRepository.findById(modifierContenuPanierDto.getIdOffre())
                .orElseThrow(() -> new ResourceNotFoundException(OFFRE_NOT_FOUND + modifierContenuPanierDto.getIdOffre())); // Récupération de l'offre

        Discipline discipline = disciplineRepository.findById(offre.getDiscipline().getIdDiscipline())
                .orElseThrow(() -> new ResourceNotFoundException(DISCIPLINE_NOT_FOUND + offre.getDiscipline().getIdDiscipline())); // Récupération de la discipline

        // Récupération du ContenuPanier existant
        ContenuPanier contenuPanierExistant = panier.getContenuPaniers().stream()
                .filter(cp -> cp.getOffre().getIdOffre().equals(offre.getIdOffre()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(String.format(OFFRE_NOT_IN_PANIER, offre.getIdOffre())));

        int quantiteActuelle = contenuPanierExistant.getQuantiteCommandee();
        int nouvelleQuantite = modifierContenuPanierDto.getNouvelleQuantite();
        int difference = nouvelleQuantite - quantiteActuelle; // Calcul de la différence de quantité
        int placesNecessaires = offre.getCapacite() * difference; // Calcul des places nécessaires pour la modification

        int placesOccupeesPanier = panier.getContenuPaniers().stream()
                .mapToInt(cp -> cp.getOffre().getCapacite() * cp.getQuantiteCommandee())
                .sum(); // Calcul du nombre de places occupées

        // Vérification de la disponibilité des places
        if (discipline.getNbPlaceDispo() < placesOccupeesPanier + placesNecessaires) {
            throw new IllegalStateException(PLACES_INSUFFISANTES);
        }

        ContenuPanierId key = new ContenuPanierId(panier.getIdPanier(), offre.getIdOffre()); // Création de la clé
        ContenuPanier contenuPanier = contenuPanierRepository.findById(key)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(OFFRE_NOT_IN_PANIER, offre.getIdOffre()))); // Récupération du contenu du panier

        contenuPanier.setQuantiteCommandee(nouvelleQuantite); // Mise à jour de la quantité
        contenuPanierRepository.save(contenuPanier); // Persistance
        recalculerMontantTotal(panier); // Recalcul du total
        return mapPanierToDto(panier); // Retourne le DTO
    }

    /**
     * Supprime une offre du panier de l'utilisateur.
     *
     * @param utilisateurIdStr L'ID de l'utilisateur au format String.
     * @param offreId L'ID de l'offre à supprimer du panier.
     * @return Le PanierDto mis à jour.
     * @throws ResourceNotFoundException Si l'utilisateur n'est pas trouvé.
     */
    @Override
    @Transactional
    public PanierDto supprimerOffreDuPanier(String utilisateurIdStr, Long offreId) {
        Panier panier = getPanierUtilisateurEntity(utilisateurIdStr); // Récupération du panier
        ContenuPanierId key = new ContenuPanierId(panier.getIdPanier(), offreId); // Création de la clé
        if (contenuPanierRepository.existsById(key)) { // Vérification de l'existence de l'offre dans le panier
            contenuPanierRepository.deleteById(key); // Suppression
            recalculerMontantTotal(panier); // Recalcul du total
        }
        return mapPanierToDto(panier); // Retourne le panierDTO
    }

    /**
     * Vide complètement le panier de l'utilisateur.
     *
     * @param utilisateurIdStr L'ID de l'utilisateur au format String.
     * @return Le PanierDto vidé.
     * @throws ResourceNotFoundException Si l'utilisateur n'est pas trouvé.
     */
    @Override
    @Transactional
    public PanierDto viderPanier(String utilisateurIdStr) {
        Panier panier = getPanierUtilisateurEntity(utilisateurIdStr); // Récupération du panier
        contenuPanierRepository.deleteByPanier(panier); // Suppression de tous les contenus du panier
        panier.setMontantTotal(BigDecimal.ZERO); // Réinitialisation du montant total
        panierRepository.save(panier); // Persistance du panier modifié
        return mapPanierToDto(panier); // Retourne le panierDTO
    }

    /**
     * Supprime une offre de tous les paniers.  Utilisé lors de la suppression d'une offre.
     *
     * @param offre L'offre à supprimer de tous les paniers.
     */
    @Override
    @Transactional
    public void supprimerOffreDeTousLesPaniers(Offre offre) {
        contenuPanierRepository.deleteByOffre(offre); // Suppression des contenus paniers contenant l'offre spécifiée
    }

    /**
     * Finalise l'achat en validant le panier de l'utilisateur.
     * Met à jour le statut du panier à "PAYE" et décrémente les quantités d'offre et les places disponibles.
     *
     * @param utilisateurIdStr L'ID de l'utilisateur au format String.
     * @return Le PanierDto mis à jour avec le statut "PAYE".
     * @throws ResourceNotFoundException Si l'utilisateur n'est pas trouvé, ou si une discipline n'est pas trouvée.
     * @throws IllegalStateException Si le panier ne peut pas être payé (statut incorrect)
     * ou si le nombre de places disponibles est insuffisant pour une offre.
     */
    @Override
    @Transactional
    public PanierDto finaliserAchat(String utilisateurIdStr) {
        Panier panier = getPanierUtilisateurEntity(utilisateurIdStr); // Récupération du panier

        // Vérification du statut du panier
        if (panier.getStatut() != StatutPanier.EN_ATTENTE) {
            throw new IllegalStateException(PANIER_DEJA_PAYE + panier.getStatut());
        }

        // Parcours des contenus du panier pour mettre à jour les quantités et les places
        for (ContenuPanier contenu : panier.getContenuPaniers()) {
            Offre offre = contenu.getOffre();
            int quantiteCommandee = contenu.getQuantiteCommandee();
            int placesOccupees = offre.getCapacite() * quantiteCommandee; // Calcul des places occupées par cette commande

            // Récupération de la discipline associée à l'offre
            Discipline discipline = disciplineRepository.findById(offre.getDiscipline().getIdDiscipline())
                    .orElseThrow(() -> new ResourceNotFoundException(DISCIPLINE_NOT_FOUND + offre.getDiscipline().getIdDiscipline()));
            // Décrémentation des places disponibles et vérification du succès
            int updated = disciplineRepository.decrementerPlaces(discipline.getIdDiscipline(), placesOccupees);
            if (updated == 0) {
                throw new IllegalStateException(String.format("Nombre de places disponibles insuffisants pour l'offre : %d", offre.getIdOffre()));
            }

            offre.setQuantite(offre.getQuantite() - quantiteCommandee); // Mise à jour de la quantité de l'offre
            offreRepository.save(offre); // Persistance de l'offre
        }

        panier.setStatut(StatutPanier.PAYE); // Mise à jour du statut du panier
        panierRepository.save(panier); // Persistance du panier
        return mapPanierToDto(panier); // Retourne le panierDTO
    }

    /**
     * Recalcule le montant total du panier.
     * Cette méthode est transactionnelle pour assurer la cohérence des données.
     *
     * @param panier Le panier dont le montant total doit être recalculé.
     */
    private void recalculerMontantTotal(Panier panier) {
        BigDecimal total = panier.getContenuPaniers().stream()
                .map(contenuPanier -> {
                    BigDecimal prix = contenuPanier.getOffre().getPrix();
                    BigDecimal quantite = BigDecimal.valueOf(contenuPanier.getQuantiteCommandee());
                    return prix.multiply(quantite);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        panier.setMontantTotal(total);
        panierRepository.save(panier);
    }

    /**
     * Récupère l'entité Panier de l'utilisateur à partir de son ID.
     *
     * @param utilisateurIdStr L'ID de l'utilisateur au format String.
     * @return L'entité Panier de l'utilisateur avec le statut EN_ATTENTE.
     * @throws ResourceNotFoundException Si l'utilisateur n'est pas trouvé.
     */
    private Panier getPanierUtilisateurEntity(String utilisateurIdStr) {
        UUID utilisateurId = UUID.fromString(utilisateurIdStr);
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException(UTILISATEUR_NOT_FOUND + utilisateurId));
        return panierRepository.findByUtilisateurIdUtilisateurAndStatut(utilisateur.getIdUtilisateur(), StatutPanier.EN_ATTENTE)
                .orElseGet(() -> creerNouveauPanier(utilisateur));
    }

    /**
     * Convertit une entité Panier en PanierDto.
     *
     * @param panier L'entité Panier à convertir.
     * @return Le PanierDto correspondant.
     */
    private PanierDto mapPanierToDto(Panier panier) {
        PanierDto panierDto = modelMapper.map(panier, PanierDto.class); // Utilisation de ModelMapper pour la conversion
        // Conversion de la liste de ContenuPanier en liste de ContenuPanierDto
        List<ContenuPanierDto> contenuPaniersDto = panier.getContenuPaniers().stream()
                .map(contenu -> {
                    ContenuPanierDto dto = modelMapper.map(contenu, ContenuPanierDto.class);
                    dto.setIdOffre(contenu.getOffre().getIdOffre());
                    dto.setPrixUnitaire(contenu.getOffre().getPrix());
                    dto.setQuantiteCommandee(contenu.getQuantiteCommandee());
                    // Calcul du prix total de l'offre (correction du problème de type)
                    dto.setPrixTotalOffre(contenu.getOffre().getPrix().multiply(BigDecimal.valueOf(contenu.getQuantiteCommandee())));
                    return dto;
                })
                .toList(); // Collecte les DTO dans une liste
        if (panier.getUtilisateur() != null) {
            panierDto.setIdUtilisateur(panier.getUtilisateur().getIdUtilisateur());
        }
        panierDto.setContenuPaniers(contenuPaniersDto);
        return panierDto;
    }
}