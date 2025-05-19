package fr.studi.bloc3jo2024.service.impl;

import fr.studi.bloc3jo2024.dto.panier.AjouterOffrePanierDto;
import fr.studi.bloc3jo2024.dto.panier.ModifierContenuPanierDto;
import fr.studi.bloc3jo2024.dto.panier.PanierDto;
import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.StatutPanier;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.*;
import fr.studi.bloc3jo2024.service.PanierService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
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
    public PanierServiceImpl(
            PanierRepository panierRepository,
            ContenuPanierRepository contenuPanierRepository,
            OffreRepository offreRepository,
            UtilisateurRepository utilisateurRepository,
            DisciplineRepository disciplineRepository,
            ModelMapper modelMapper) {
        this.panierRepository = panierRepository;
        this.contenuPanierRepository = contenuPanierRepository;
        this.offreRepository = offreRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.disciplineRepository = disciplineRepository;
        this.modelMapper = modelMapper;
    }

    @Autowired
    public void setSelf(@Lazy PanierService self) {
        this.self = self;
    }

    private static final String DISCIPLINE_NOT_FOUND_ID = "Discipline non trouvée avec l'ID : ";
    private static final String UTILISATEUR_NOT_FOUND_ID = "Utilisateur non trouvé avec l'ID : ";
    private static final String OFFRE_NOT_FOUND_ID = "Offre non trouvée avec l'ID : ";
    private static final String OFFRE_NOT_IN_PANIER_ID = "L'offre avec l'ID %d n'est pas dans le panier de l'utilisateur %s";
    private static final String QUANTITE_INVALIDE_OU_OFFRE_NON_DISPONIBLE = "Quantité invalide, offre non disponible ou stock insuffisant.";
    private static final String QUANTITE_NON_POSITIVE = "La quantité demandée doit être positive.";
    private static final String PLACES_INSUFFISANTES_DISCIPLINE = "Nombre de places disponibles insuffisant pour la discipline : ";
    private static final String PANIER_STATUT_INCORRECT_POUR_PAIEMENT = "Le panier ne peut pas être payé car son statut est : ";
    private static final String PANIER_VIDE_POUR_PAIEMENT = "Le panier est vide. Impossible de finaliser l'achat.";
    private static final String OFFRE_SANS_DISCIPLINE_ID = "L'offre avec l'ID %d n'est associée à aucune discipline valide.";
    private static final String CONTENU_PANIER_OU_OFFRE_INVALIDE = "Contenu de panier ou offre associée invalide (potentiellement null).";
    private static final String STOCK_OFFRE_INSUFFISANT_FINALISATION = "Stock de l'offre ID %d insuffisant (%d requis, %d disponibles) au moment de la finalisation.";


    @Override
    @Transactional(readOnly = true)
    public PanierDto getPanierUtilisateur(String utilisateurIdStr) {
        Panier panier = getPanierUtilisateurEntity(utilisateurIdStr);
        return mapPanierToDto(panier);
    }

    private Panier creerNouveauPanier(Utilisateur utilisateur) {
        Panier nouveauPanier = Panier.builder()
                .utilisateur(utilisateur)
                .statut(StatutPanier.EN_ATTENTE)
                .montantTotal(BigDecimal.ZERO)
                .contenuPaniers(new HashSet<>())
                .build();
        log.info("Création d'un nouveau panier pour l'utilisateur ID: {}", utilisateur.getIdUtilisateur());
        return panierRepository.save(nouveauPanier);
    }

    @Override
    @Transactional
    public PanierDto ajouterOffreAuPanier(String utilisateurIdStr, AjouterOffrePanierDto ajouterOffrePanierDto) {
        Panier panier = getPanierUtilisateurEntity(utilisateurIdStr); // Le paramètre fetchDetails a été supprimé
        Offre offre = offreRepository.findById(ajouterOffrePanierDto.getIdOffre())
                .orElseThrow(() -> new ResourceNotFoundException(OFFRE_NOT_FOUND_ID + ajouterOffrePanierDto.getIdOffre()));

        validationsAjoutOffre(offre, ajouterOffrePanierDto.getQuantite());
        Discipline discipline = validerEtRecupererDisciplineDeLoffre(offre);

        Optional<ContenuPanier> existingContenuPanierOpt = panier.getContenuPaniers().stream()
                .filter(cp -> cp != null && cp.getOffre() != null && cp.getOffre().getIdOffre().equals(offre.getIdOffre()))
                .findFirst();

        int quantiteDejaDansPanier = existingContenuPanierOpt.map(ContenuPanier::getQuantiteCommandee).orElse(0);
        int quantiteDesireePourCetItem = quantiteDejaDansPanier + ajouterOffrePanierDto.getQuantite();

        if (offre.getQuantite() < quantiteDesireePourCetItem) {
            throw new IllegalArgumentException(String.format("Stock insuffisant pour l'offre ID %d. Demandé: %d (total), Disponible: %d",
                    offre.getIdOffre(), quantiteDesireePourCetItem, offre.getQuantite()));
        }

        validerCapaciteDisciplinePourPanier(panier, discipline, offre, quantiteDesireePourCetItem);

        ContenuPanier contenuPanier = existingContenuPanierOpt.orElseGet(() -> {
            ContenuPanier nouveauContenu = ContenuPanier.builder()
                    .panier(panier)
                    .offre(offre)
                    .quantiteCommandee(0)
                    .build();
            panier.getContenuPaniers().add(nouveauContenu);
            return nouveauContenu;
        });

        contenuPanier.setQuantiteCommandee(quantiteDesireePourCetItem);

        recalculerEtSauvegarderPanier(panier);
        log.info("Offre ID {} ajoutée/mise à jour (quantité: {}) dans le panier ID {} pour l'utilisateur ID {}",
                offre.getIdOffre(), quantiteDesireePourCetItem, panier.getIdPanier(), utilisateurIdStr);
        return mapPanierToDto(panier);
    }

    private void validationsAjoutOffre(Offre offre, int quantiteDemandee) {
        if (offre.getStatutOffre() != StatutOffre.DISPONIBLE) {
            throw new IllegalArgumentException(QUANTITE_INVALIDE_OU_OFFRE_NON_DISPONIBLE + " L'offre ID " + offre.getIdOffre() + " n'est pas disponible (statut: " + offre.getStatutOffre() + ").");
        }
        if (quantiteDemandee <= 0) {
            throw new IllegalArgumentException(QUANTITE_NON_POSITIVE);
        }
    }

    private Discipline validerEtRecupererDisciplineDeLoffre(Offre offre) {
        if (offre.getDiscipline() == null || offre.getDiscipline().getIdDiscipline() == null) {
            throw new IllegalStateException(String.format(OFFRE_SANS_DISCIPLINE_ID, offre.getIdOffre()));
        }
        return disciplineRepository.findById(offre.getDiscipline().getIdDiscipline())
                .orElseThrow(() -> new ResourceNotFoundException(DISCIPLINE_NOT_FOUND_ID + offre.getDiscipline().getIdDiscipline()));
    }

    private int calculerPlacesRequisesAutresOffresMemeDiscipline(Panier panier, Discipline discipline, Offre offreCible) {
        int placesRequises = 0;
        if (panier.getContenuPaniers() != null) {
            for (ContenuPanier cp : panier.getContenuPaniers()) {
                if (cp != null && cp.getOffre() != null && cp.getOffre().getDiscipline() != null &&
                        cp.getOffre().getDiscipline().getIdDiscipline().equals(discipline.getIdDiscipline()) &&
                        !cp.getOffre().getIdOffre().equals(offreCible.getIdOffre())) {
                    placesRequises += cp.getOffre().getCapacite() * cp.getQuantiteCommandee();
                }
            }
        }
        return placesRequises;
    }

    private void validerCapaciteDisciplinePourPanier(Panier panier, Discipline discipline, Offre offreCible, int quantiteCiblePourOffre) {
        int placesRequisesPourOffreCible = offreCible.getCapacite() * quantiteCiblePourOffre;
        int placesRequisesParAutresOffresMemeDiscipline = calculerPlacesRequisesAutresOffresMemeDiscipline(panier, discipline, offreCible);

        int totalPlacesRequisesPourDiscipline = placesRequisesPourOffreCible + placesRequisesParAutresOffresMemeDiscipline;

        if (discipline.getNbPlaceDispo() < totalPlacesRequisesPourDiscipline) {
            throw new IllegalStateException(PLACES_INSUFFISANTES_DISCIPLINE + discipline.getNomDiscipline() +
                    String.format(". Requis: %d, Disponible: %d", totalPlacesRequisesPourDiscipline, discipline.getNbPlaceDispo()));
        }
    }

    @Override
    @Transactional
    public PanierDto modifierQuantiteOffrePanier(String utilisateurIdStr, ModifierContenuPanierDto dto) {
        if (dto.getNouvelleQuantite() < 0) {
            throw new IllegalArgumentException("La nouvelle quantité ne peut pas être négative.");
        }
        if (dto.getNouvelleQuantite() == 0) {
            log.info("Quantité mise à 0 pour l'offre ID {} dans le panier de l'utilisateur ID {}. Suppression de l'offre.", dto.getIdOffre(), utilisateurIdStr);
            // Appel via 'self' pour assurer que le proxy transactionnel est utilisé
            return self.supprimerOffreDuPanier(utilisateurIdStr, dto.getIdOffre());
        }

        Panier panier = getPanierUtilisateurEntity(utilisateurIdStr); // Le paramètre fetchDetails a été supprimé
        Offre offre = offreRepository.findById(dto.getIdOffre())
                .orElseThrow(() -> new ResourceNotFoundException(OFFRE_NOT_FOUND_ID + dto.getIdOffre()));

        ContenuPanier contenuPanier = panier.getContenuPaniers().stream()
                .filter(cp -> cp != null && cp.getOffre() != null && cp.getOffre().getIdOffre().equals(offre.getIdOffre()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(String.format(OFFRE_NOT_IN_PANIER_ID, offre.getIdOffre(), utilisateurIdStr)));

        validationsAjoutOffre(offre, dto.getNouvelleQuantite());
        Discipline discipline = validerEtRecupererDisciplineDeLoffre(offre);

        if (offre.getQuantite() < dto.getNouvelleQuantite()) {
            throw new IllegalArgumentException(String.format("Stock insuffisant pour l'offre ID %d. Demandé: %d, Disponible: %d",
                    offre.getIdOffre(), dto.getNouvelleQuantite(), offre.getQuantite()));
        }

        validerCapaciteDisciplinePourPanier(panier, discipline, offre, dto.getNouvelleQuantite());

        contenuPanier.setQuantiteCommandee(dto.getNouvelleQuantite());
        recalculerEtSauvegarderPanier(panier);
        log.info("Quantité de l'offre ID {} modifiée à {} dans le panier ID {} pour l'utilisateur ID {}",
                offre.getIdOffre(), dto.getNouvelleQuantite(), panier.getIdPanier(), utilisateurIdStr);
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
            panier.getContenuPaniers().remove(contenuARetirer);
            recalculerEtSauvegarderPanier(panier);
            log.info("Offre ID {} supprimée du panier ID {} pour l'utilisateur ID {}", offreId, panier.getIdPanier(), utilisateurIdStr);
        } else {
            log.warn("Tentative de suppression de l'offre ID {} non trouvée dans le panier ID {} de l'utilisateur ID {}", offreId, panier.getIdPanier(), utilisateurIdStr);
        }
        return mapPanierToDto(panier);
    }

    @Override
    @Transactional
    public PanierDto viderPanier(String utilisateurIdStr) {
        Panier panier = getPanierUtilisateurEntity(utilisateurIdStr);
        if (panier.getContenuPaniers() != null && !panier.getContenuPaniers().isEmpty()) {
            log.info("Vidage de {} items du panier ID {} pour l'utilisateur ID {}", panier.getContenuPaniers().size(), panier.getIdPanier(), utilisateurIdStr);
            panier.getContenuPaniers().clear();
            recalculerEtSauvegarderPanier(panier);
        } else {
            log.info("Panier ID {} pour l'utilisateur ID {} est déjà vide ou n'a pas de contenu initialisé.", panier.getIdPanier(), utilisateurIdStr);
        }
        return mapPanierToDto(panier);
    }

    @Override
    @Transactional
    public void supprimerOffreDeTousLesPaniers(Offre offre) {
        if (offre == null || offre.getIdOffre() == null) {
            log.warn("Tentative de supprimer une offre null ou sans ID des paniers.");
            return;
        }
        List<Panier> paniersAffectes = panierRepository.findPaniersContenantOffreWithDetails(offre.getIdOffre());

        int deletedCount = contenuPanierRepository.deleteByOffreId(offre.getIdOffre());
        log.info("{} instances de ContenuPanier pour l'offre ID {} ont été supprimées de la base de données.", deletedCount, offre.getIdOffre());

        for (Panier panierPrecedent : paniersAffectes) {
            panierRepository.findById(panierPrecedent.getIdPanier()).ifPresent(panierMisAJour -> {
                recalculerEtSauvegarderPanier(panierMisAJour);
                log.info("Panier ID {} recalculé après suppression de l'offre ID {}.", panierMisAJour.getIdPanier(), offre.getIdOffre());
            });
        }
    }

    /**
     * Valide et récupère la quantité commandée d'un contenu de panier pour la finalisation.
     * @param contenu Le contenu du panier.
     * @param offre L'offre associée (rechargée).
     * @param panier Le panier parent.
     * @return La quantité commandée validée.
     * @throws IllegalStateException si la quantité est invalide ou le stock de l'offre insuffisant.
     */
    private int getAndValidateQuantiteCommandeePourFinalisation(ContenuPanier contenu, Offre offre, Panier panier) {
        int quantiteCommandee = contenu.getQuantiteCommandee();

        if (quantiteCommandee <= 0) {
            throw new IllegalStateException("Quantité commandée invalide (" + quantiteCommandee + ") pour l'offre ID " + offre.getIdOffre() + " dans le panier ID " + panier.getIdPanier());
        }
        if (offre.getQuantite() < quantiteCommandee) {
            throw new IllegalStateException(String.format(STOCK_OFFRE_INSUFFISANT_FINALISATION, offre.getIdOffre(), quantiteCommandee, offre.getQuantite()));
        }
        return quantiteCommandee;
    }

    private void processContenuPanierPourFinalisation(ContenuPanier contenu, Panier panier) {
        if (contenu == null || contenu.getOffre() == null || contenu.getOffre().getIdOffre() == null) {
            log.error("Contenu de panier invalide (null) trouvé lors de la finalisation pour le panier ID {}", panier.getIdPanier());
            throw new IllegalStateException(CONTENU_PANIER_OU_OFFRE_INVALIDE);
        }

        Offre offre = offreRepository.findById(contenu.getOffre().getIdOffre())
                .orElseThrow(() -> new ResourceNotFoundException(OFFRE_NOT_FOUND_ID + contenu.getOffre().getIdOffre() + " (lors de la finalisation)."));

        int quantiteCommandee = getAndValidateQuantiteCommandeePourFinalisation(contenu, offre, panier);

        offre.setQuantite(offre.getQuantite() - quantiteCommandee);
        offreRepository.save(offre);

        Discipline discipline = validerEtRecupererDisciplineDeLoffre(offre);
        int placesOccupeesParCetteOffre = offre.getCapacite() * quantiteCommandee;
        int updatedRows = disciplineRepository.decrementerPlaces(discipline.getIdDiscipline(), placesOccupeesParCetteOffre);

        if (updatedRows == 0) {
            Discipline currentDisciplineState = disciplineRepository.findById(discipline.getIdDiscipline())
                    .orElseThrow(() -> new ResourceNotFoundException(DISCIPLINE_NOT_FOUND_ID + discipline.getIdDiscipline() + " (lors de la vérification des places)."));
            throw new IllegalStateException(
                    PLACES_INSUFFISANTES_DISCIPLINE + discipline.getNomDiscipline() +
                            String.format(". Requis: %d, Disponible: %d pour l'offre ID %d",
                                    placesOccupeesParCetteOffre, currentDisciplineState.getNbPlaceDispo(), offre.getIdOffre()));
        }
        log.info("Offre ID {} (quantité: {}) traitée, stock mis à jour. Places décrémentées ({} places) pour discipline ID {}.",
                offre.getIdOffre(), quantiteCommandee, placesOccupeesParCetteOffre, discipline.getIdDiscipline());
    }


    @Override
    @Transactional
    public PanierDto finaliserAchat(String utilisateurIdStr) {
        Panier panier = getPanierUtilisateurEntity(utilisateurIdStr);

        if (panier.getStatut() != StatutPanier.EN_ATTENTE) {
            throw new IllegalStateException(PANIER_STATUT_INCORRECT_POUR_PAIEMENT + panier.getStatut());
        }
        if (panier.getContenuPaniers() == null || panier.getContenuPaniers().isEmpty()) {
            throw new IllegalStateException(PANIER_VIDE_POUR_PAIEMENT);
        }

        for (ContenuPanier contenu : new HashSet<>(panier.getContenuPaniers())) {
            processContenuPanierPourFinalisation(contenu, panier);
        }

        panier.setStatut(StatutPanier.PAYE);
        panierRepository.save(panier);
        log.info("Achat finalisé pour le panier ID {} de l'utilisateur ID {}", panier.getIdPanier(), utilisateurIdStr);
        return mapPanierToDto(panier);
    }

    private void recalculerEtSauvegarderPanier(Panier panier) {
        if (panier == null) {
            log.warn("Tentative de recalculer le montant d'un panier null.");
            return;
        }

        BigDecimal total = BigDecimal.ZERO;
        if (panier.getContenuPaniers() != null) {
            total = panier.getContenuPaniers().stream()
                    .filter(contenu -> contenu != null && contenu.getOffre() != null && contenu.getOffre().getPrix() != null)
                    .map(contenu -> contenu.getOffre().getPrix().multiply(BigDecimal.valueOf(contenu.getQuantiteCommandee())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        panier.setMontantTotal(total);
        panierRepository.save(panier);
    }

    private Panier getPanierUtilisateurEntity(String utilisateurIdStr) {
        UUID utilisateurId;
        try {
            utilisateurId = UUID.fromString(utilisateurIdStr);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException(UTILISATEUR_NOT_FOUND_ID + utilisateurIdStr + " (Format UUID invalide)");
        }

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException(UTILISATEUR_NOT_FOUND_ID + utilisateurId));

        Optional<Panier> panierOpt = panierRepository.findByUtilisateurIdAndStatutWithDetails(utilisateur.getIdUtilisateur(), StatutPanier.EN_ATTENTE);

        return panierOpt.orElseGet(() -> creerNouveauPanier(utilisateur));
    }

    private PanierDto mapPanierToDto(Panier panier) {
        if (panier == null) {
            log.warn("Tentative de mapper un panier null en DTO.");
            return null;
        }
        PanierDto panierDto = modelMapper.map(panier, PanierDto.class);

        if (panier.getUtilisateur() != null) {
            panierDto.setIdUtilisateur(panier.getUtilisateur().getIdUtilisateur());
        } else {
            log.warn("Panier ID {} n'a pas d'utilisateur associé lors du mapping.", panier.getIdPanier());
        }

        if (panierDto.getContenuPaniers() == null && panier.getContenuPaniers() != null && !panier.getContenuPaniers().isEmpty()) {
            log.warn("La collection contenuPaniers était null dans PanierDto après le mapping initial par ModelMapper pour Panier ID {}. Vérifiez la configuration de ModelMapper.", panier.getIdPanier());
        } else if (panierDto.getContenuPaniers() == null) {
            panierDto.setContenuPaniers(Collections.emptyList());
        }

        return panierDto;
    }
}
