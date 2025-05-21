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
import fr.studi.bloc3jo2024.repository.*;
import fr.studi.bloc3jo2024.service.PanierService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class PanierServiceImpl implements PanierService {

    private static final Logger log = LoggerFactory.getLogger(PanierServiceImpl.class);

    private final PanierRepository panierRepository;
    private final ContenuPanierRepository contenuPanierRepository;
    private final OffreRepository offreRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DisciplineRepository disciplineRepository;
    private final ModelMapper modelMapper;

    private PanierService self;

    @Autowired
    public void setSelf(@Lazy PanierService self) {
        this.self = self;
    }

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

    @Override
    @Transactional(readOnly = true)
    public PanierDto getPanierUtilisateur(String utilisateurIdStr) {
        Panier panier = getPanierUtilisateurEntity(utilisateurIdStr);
        return mapPanierToDto(panier);
    }

    private Panier creerNouveauPanier(Utilisateur utilisateur) {
        Panier nouveauPanier = new Panier();
        nouveauPanier.setUtilisateur(utilisateur);
        nouveauPanier.setStatut(StatutPanier.EN_ATTENTE);
        nouveauPanier.setMontantTotal(BigDecimal.ZERO);
        nouveauPanier.setContenuPaniers(new HashSet<>());
        return panierRepository.save(nouveauPanier);
    }

    @Override
    @Transactional
    public PanierDto ajouterOffreAuPanier(String utilisateurIdStr, AjouterOffrePanierDto ajouterOffrePanierDto) {
        Panier panier = getPanierUtilisateurEntity(utilisateurIdStr);
        Offre offre = offreRepository.findById(ajouterOffrePanierDto.getIdOffre())
                .orElseThrow(() -> new ResourceNotFoundException(OFFRE_NOT_FOUND + ajouterOffrePanierDto.getIdOffre()));

        if (offre.getStatutOffre() != StatutOffre.DISPONIBLE || offre.getQuantite() < ajouterOffrePanierDto.getQuantite()) {
            throw new IllegalArgumentException(QUANTITE_INVALIDE_OU_NON_DISPONIBLE);
        }
        if (ajouterOffrePanierDto.getQuantite() <= 0) {
            throw new IllegalArgumentException(QUANTITE_NON_POSITIVE);
        }

        if (offre.getDiscipline() == null || offre.getDiscipline().getIdDiscipline() == null) {
            throw new IllegalStateException(String.format(OFFRE_DISCIPLINE_NULL, offre.getIdOffre()));
        }

        Discipline discipline = disciplineRepository.findById(offre.getDiscipline().getIdDiscipline())
                .orElseThrow(() -> new ResourceNotFoundException(DISCIPLINE_NOT_FOUND + offre.getDiscipline().getIdDiscipline()));

        Optional<ContenuPanier> existingContenuPanierOpt = panier.getContenuPaniers().stream()
                .filter(cp -> cp != null && cp.getOffre() != null && cp.getOffre().getIdOffre().equals(offre.getIdOffre()))
                .findFirst();

        int currentQuantityInCart = existingContenuPanierOpt.map(ContenuPanier::getQuantiteCommandee).orElse(0);
        int quantityToAdd = ajouterOffrePanierDto.getQuantite();
        int newTotalQuantityForThisItem = currentQuantityInCart + quantityToAdd;

        int newTotalPlacesInDiscipline = calculateTotalPlacesForDiscipline(panier, discipline, offre, newTotalQuantityForThisItem);

        if (discipline.getNbPlaceDispo() < newTotalPlacesInDiscipline) {
            throw new IllegalStateException(PLACES_INSUFFISANTES + " (Discipline : " + discipline.getNomDiscipline() + ")");
        }

        ContenuPanier contenuPanier = existingContenuPanierOpt.orElseGet(() -> {
            ContenuPanier nouveauContenuPanier = new ContenuPanier();
            nouveauContenuPanier.setPanier(panier);
            nouveauContenuPanier.setOffre(offre);
            if (panier.getContenuPaniers() == null) {
                panier.setContenuPaniers(new HashSet<>());
            }
            panier.getContenuPaniers().add(nouveauContenuPanier);
            return nouveauContenuPanier;
        });

        contenuPanier.setQuantiteCommandee(newTotalQuantityForThisItem);
        contenuPanierRepository.save(contenuPanier);

        recalculerMontantTotal(panier);
        return mapPanierToDto(panier);
    }

    @Override
    @Transactional
    public PanierDto modifierQuantiteOffrePanier(String utilisateurIdStr, ModifierContenuPanierDto modifierContenuPanierDto) {
        Panier panier = getPanierUtilisateurEntity(utilisateurIdStr);
        Offre offre = offreRepository.findById(modifierContenuPanierDto.getIdOffre())
                .orElseThrow(() -> new ResourceNotFoundException(OFFRE_NOT_FOUND + modifierContenuPanierDto.getIdOffre()));

        int nouvelleQuantite = modifierContenuPanierDto.getNouvelleQuantite();

        if (nouvelleQuantite < 0) {
            throw new IllegalArgumentException("La quantité à modifier ne peut pas être négative.");
        }

        if (nouvelleQuantite == 0) {
            return self.supprimerOffreDuPanier(utilisateurIdStr, offre.getIdOffre());
        }

        ContenuPanier contenuPanierExistant = panier.getContenuPaniers().stream()
                .filter(cp -> cp != null && cp.getOffre() != null && cp.getOffre().getIdOffre().equals(offre.getIdOffre()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(String.format(OFFRE_NOT_IN_PANIER, offre.getIdOffre())));

        if (offre.getDiscipline() == null || offre.getDiscipline().getIdDiscipline() == null) {
            throw new IllegalStateException(String.format(OFFRE_DISCIPLINE_NULL, offre.getIdOffre()));
        }

        Discipline discipline = disciplineRepository.findById(offre.getDiscipline().getIdDiscipline())
                .orElseThrow(() -> new ResourceNotFoundException(DISCIPLINE_NOT_FOUND + offre.getDiscipline().getIdDiscipline()));

        int newTotalPlacesInDiscipline = calculateTotalPlacesForDiscipline(panier, discipline, offre, nouvelleQuantite);

        if (discipline.getNbPlaceDispo() < newTotalPlacesInDiscipline) {
            throw new IllegalStateException(PLACES_INSUFFISANTES + " (Discipline : " + discipline.getNomDiscipline() + ")");
        }

        contenuPanierExistant.setQuantiteCommandee(nouvelleQuantite);
        contenuPanierRepository.save(contenuPanierExistant);

        recalculerMontantTotal(panier);
        return mapPanierToDto(panier);
    }

    @Override
    @Transactional
    public PanierDto supprimerOffreDuPanier(String utilisateurIdStr, Long offreId) {
        Panier panier = getPanierUtilisateurEntity(utilisateurIdStr);

        Optional<ContenuPanier> contenuARetirerOpt = panier.getContenuPaniers().stream()
                .filter(cp -> cp != null && cp.getOffre() != null && cp.getOffre().getIdOffre().equals(offreId))
                .findFirst();

        if (contenuARetirerOpt.isPresent()) {
            ContenuPanier contenuARetirer = contenuARetirerOpt.get();
            ContenuPanierId key = new ContenuPanierId(panier.getIdPanier(), contenuARetirer.getOffre().getIdOffre());
            contenuPanierRepository.deleteById(key);
            panier.getContenuPaniers().remove(contenuARetirer);
            recalculerMontantTotal(panier);
        }
        return mapPanierToDto(panier);
    }

    @Override
    @Transactional
    public PanierDto viderPanier(String utilisateurIdStr) {
        Panier panier = getPanierUtilisateurEntity(utilisateurIdStr);

        if (panier != null && panier.getContenuPaniers() != null && !panier.getContenuPaniers().isEmpty()) {
            contenuPanierRepository.deleteByPanier(panier);
            panier.getContenuPaniers().clear();
            panier.setMontantTotal(BigDecimal.ZERO);
            panierRepository.save(panier);
        }
        return mapPanierToDto(panier);
    }

    @Override
    @Transactional
    public void supprimerOffreDeTousLesPaniers(Offre offre) {
        if (offre != null) {
            contenuPanierRepository.deleteByOffre(offre);
        }
    }

    private void processContenuItemForFinalisation(ContenuPanier contenu, Long panierIdForLog) {
        if (contenu == null || contenu.getOffre() == null || contenu.getOffre().getIdOffre() == null) {
            throw new IllegalStateException(CONTENU_PANIER_INVALIDE + " Détails: Contenu ou Offre null dans le panier ID " + panierIdForLog);
        }
        Offre offre = contenu.getOffre();
        int quantiteCommandee = contenu.getQuantiteCommandee();

        if (quantiteCommandee <= 0) {
            throw new IllegalStateException("Quantité commandée nulle ou négative trouvée pour l'offre " + offre.getIdOffre() + " dans le panier ID " + panierIdForLog);
        }

        if (offre.getDiscipline() == null || offre.getDiscipline().getIdDiscipline() == null) {
            throw new IllegalStateException(String.format(OFFRE_DISCIPLINE_NULL, offre.getIdOffre()));
        }
        Discipline discipline = disciplineRepository.findById(offre.getDiscipline().getIdDiscipline())
                .orElseThrow(() -> new ResourceNotFoundException(DISCIPLINE_NOT_FOUND + offre.getDiscipline().getIdDiscipline()));

        int placesOccupeesParCetItem = offre.getCapacite() * quantiteCommandee;

        int updatedRows = disciplineRepository.decrementerPlaces(discipline.getIdDiscipline(), placesOccupeesParCetItem);
        if (updatedRows == 0) {
            throw new IllegalStateException(String.format(PLACES_INSUFFISANTES + " (Discipline : %s, Offre : %d, Places demandées : %d)",
                    discipline.getNomDiscipline(), offre.getIdOffre(), placesOccupeesParCetItem));
        }
        if (offre.getQuantite() < quantiteCommandee) {
            throw new IllegalStateException(String.format(STOCK_INSUFFISANT_FINALISATION + ". Stock actuel: %d, Demandé: %d",
                    offre.getIdOffre(), offre.getQuantite(), quantiteCommandee));
        }
        offre.setQuantite(offre.getQuantite() - quantiteCommandee);
        offreRepository.save(offre);
    }


    @Override
    @Transactional
    public PanierDto finaliserAchat(String utilisateurIdStr) {
        Panier panier = getPanierUtilisateurEntity(utilisateurIdStr);

        if (panier.getStatut() != StatutPanier.EN_ATTENTE) {
            throw new IllegalStateException(PANIER_DEJA_PAYE + panier.getStatut());
        }
        if (panier.getContenuPaniers() == null || panier.getContenuPaniers().isEmpty()) {
            throw new IllegalStateException(PANIER_VIDE);
        }

        for (ContenuPanier contenu : panier.getContenuPaniers()) {
            processContenuItemForFinalisation(contenu, panier.getIdPanier());
        }

        panier.setStatut(StatutPanier.PAYE);
        panierRepository.save(panier);
        return mapPanierToDto(panier);
    }

    private void recalculerMontantTotal(Panier panier) {
        if (panier == null) {
            log.warn("Tentative de recalcul du montant total sur un panier null.");
            return;
        }
        if (panier.getContenuPaniers() == null) {
            log.warn("ContenuPaniers est null pour le panier ID: {}. Initialisation à une collection vide.", panier.getIdPanier());
            panier.setContenuPaniers(new HashSet<>());
        }

        BigDecimal total = panier.getContenuPaniers().stream()
                .filter(contenu -> contenu != null && contenu.getOffre() != null && contenu.getOffre().getPrix() != null && contenu.getQuantiteCommandee() > 0)
                .map(contenu -> {
                    BigDecimal prix = contenu.getOffre().getPrix();
                    BigDecimal quantite = BigDecimal.valueOf(contenu.getQuantiteCommandee());
                    return prix.multiply(quantite);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        panier.setMontantTotal(total);
        panierRepository.save(panier);
    }

    private Panier getPanierUtilisateurEntity(String utilisateurIdStr) {
        UUID utilisateurId;
        try {
            utilisateurId = UUID.fromString(utilisateurIdStr);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Format de l'ID utilisateur invalide : " + utilisateurIdStr);
        }

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException(UTILISATEUR_NOT_FOUND + utilisateurId));

        return panierRepository.findByUtilisateur_idUtilisateurAndStatut(utilisateur.getIdUtilisateur(), StatutPanier.EN_ATTENTE)
                .orElseGet(() -> creerNouveauPanier(utilisateur));
    }

    private PanierDto mapPanierToDto(Panier panier) {
        if (panier == null) {
            return null;
        }

        PanierDto panierDto = modelMapper.map(panier, PanierDto.class);

        // Gestion manuelle de l'ID utilisateur
        if (panier.getUtilisateur() != null && panier.getUtilisateur().getIdUtilisateur() != null) {
            panierDto.setIdUtilisateur(panier.getUtilisateur().getIdUtilisateur());
        } else {
            panierDto.setIdUtilisateur(null);
            log.warn("Panier ID {} mappé sans utilisateur associé ou ID utilisateur manquant.", panier.getIdPanier());
        }

        // Mappage manuel de la liste contenuPaniers
        if (panier.getContenuPaniers() == null) {
            panierDto.setContenuPaniers(Collections.emptyList());
        } else {
            List<ContenuPanierDto> contenuPaniersDto = panier.getContenuPaniers().stream()
                    .filter(contenu -> contenu != null && contenu.getOffre() != null && contenu.getOffre().getPrix() != null)
                    .map(contenu -> {
                        ContenuPanierDto dto = new ContenuPanierDto();
                        Offre offreAssociee = contenu.getOffre();

                        dto.setIdOffre(offreAssociee.getIdOffre());
                        dto.setQuantiteCommandee(contenu.getQuantiteCommandee());
                        dto.setPrixUnitaire(offreAssociee.getPrix());

                        if (offreAssociee.getTypeOffre() != null) {
                            dto.setTypeOffre(offreAssociee.getTypeOffre());
                        } else {
                            dto.setTypeOffre(TypeOffre.SOLO);
                            log.warn("TypeOffre est null pour l'offre ID: {}. Utilisation de SOLO par défaut.", offreAssociee.getIdOffre());
                        }

                        if (dto.getPrixUnitaire() != null && dto.getQuantiteCommandee() > 0) {
                            dto.setPrixTotalOffre(dto.getPrixUnitaire().multiply(BigDecimal.valueOf(dto.getQuantiteCommandee())));
                        } else {
                            dto.setPrixTotalOffre(BigDecimal.ZERO);
                        }
                        return dto;
                    })
                    .toList();

            panierDto.setContenuPaniers(contenuPaniersDto);
        }
        return panierDto;
    }

    private int calculateTotalPlacesForDiscipline(Panier panier, Discipline discipline, Offre targetOffre, int quantityForTargetOffre) {
        if (panier == null || panier.getContenuPaniers() == null || discipline == null || discipline.getIdDiscipline() == null || targetOffre == null) {
            log.warn("Paramètres invalides pour calculateTotalPlacesForDiscipline. Panier: {}, Discipline: {}, TargetOffre: {}", panier, discipline, targetOffre);
            return 0;
        }

        int placesFromOtherItemsInCart = calculateExistingPlacesInCartForDiscipline(panier, discipline, targetOffre.getIdOffre());
        int targetOffreCapacity = Math.max(0, targetOffre.getCapacite());
        int placesForTargetItem = targetOffreCapacity * quantityForTargetOffre;

        return placesFromOtherItemsInCart + placesForTargetItem;
    }

    private int calculateExistingPlacesInCartForDiscipline(Panier panier, Discipline discipline, Long excludeOffreId) {
        // La discipline est validée non-null par l'appelant (calculateTotalPlacesForDiscipline)
        int existingPlaces = 0;
        if (panier.getContenuPaniers() == null) {
            return 0;
        }

        for (ContenuPanier cp : panier.getContenuPaniers()) {
            if (cp == null || cp.getOffre() == null || cp.getOffre().getDiscipline() == null || cp.getOffre().getDiscipline().getIdDiscipline() == null) {
                log.trace("ContenuPanier ou son offre/discipline est null, ignoré. ContenuPanier: {}", cp);
                continue;
            }

            Offre offreInCart = cp.getOffre();
            // L'objet 'discipline' passé en paramètre est utilisé ici pour la comparaison d'ID.
            if (offreInCart.getDiscipline().getIdDiscipline().equals(discipline.getIdDiscipline()) &&
                    !offreInCart.getIdOffre().equals(excludeOffreId)) {
                int itemCapacity = Math.max(0, offreInCart.getCapacite());
                existingPlaces += itemCapacity * cp.getQuantiteCommandee();
            }
        }
        return existingPlaces;
    }
}