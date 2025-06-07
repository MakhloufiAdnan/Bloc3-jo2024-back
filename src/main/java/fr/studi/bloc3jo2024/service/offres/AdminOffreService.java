package fr.studi.bloc3jo2024.service.offres;

import fr.studi.bloc3jo2024.dto.offres.CreerOffreDto;
import fr.studi.bloc3jo2024.dto.offres.MettreAJourOffreDto;
import fr.studi.bloc3jo2024.dto.offres.OffreAdminDto;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.DisciplineRepository;
import fr.studi.bloc3jo2024.repository.OffreRepository;
import fr.studi.bloc3jo2024.service.PanierService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des offres par les administrateurs.
 * Fournit des fonctionnalités CRUD complètes pour les offres, ainsi que des méthodes
 * pour obtenir des statistiques de vente.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AdminOffreService {

    private static final Logger log = LoggerFactory.getLogger(AdminOffreService.class);
    private static final String DISCIPLINE_NON_TROUVEE_ID_PREFIX = "Discipline non trouvée avec l'ID : ";
    private static final String OFFRE_NON_TROUVEE_ID_PREFIX = "Offre non trouvée avec l'ID : ";

    private final OffreRepository offreRepository;
    private final DisciplineRepository disciplineRepository;
    private final PanierService panierService;
    private final ModelMapper modelMapper;

    private Discipline findDisciplineByIdOrThrow(Long idDiscipline) {
        return disciplineRepository.findById(idDiscipline)
                .orElseThrow(() -> {
                    log.warn("Tentative d'opération sur une discipline non existante. ID : {}", idDiscipline);
                    return new ResourceNotFoundException(DISCIPLINE_NON_TROUVEE_ID_PREFIX + idDiscipline);
                });
    }

    /**
     * Crée une nouvelle offre et l'associe à une discipline.
     *
     * @param creerOffreDto DTO contenant les informations pour la création de l'offre.
     * @return Le DTO de l'offre nouvellement créée.
     */
    public OffreAdminDto ajouterOffre(CreerOffreDto creerOffreDto) {
        log.info("Ajout d'une nouvelle offre pour la discipline ID : {}", creerOffreDto.getIdDiscipline());
        Discipline discipline = findDisciplineByIdOrThrow(creerOffreDto.getIdDiscipline());

        Offre nouvelleOffre = modelMapper.map(creerOffreDto, Offre.class);
        nouvelleOffre.setDiscipline(discipline);

        Offre offreCreee = offreRepository.save(nouvelleOffre);
        log.info("Offre créée avec ID : {}", offreCreee.getIdOffre());
        return convertToOffreAdminDtoWithDetails(offreCreee);
    }

    /**
     * Récupère une offre par son identifiant.
     *
     * @param idOffre L'identifiant de l'offre.
     * @return Le DTO de l'offre correspondante.
     * @throws ResourceNotFoundException si aucune offre n'est trouvée pour l'ID donné.
     */
    @Transactional(readOnly = true)
    public OffreAdminDto obtenirOffreParId(Long idOffre) {
        log.debug("Récupération de l'offre ID : {}", idOffre);
        Offre offre = offreRepository.findById(idOffre)
                .orElseThrow(() -> {
                    log.warn("Offre non trouvée avec l'ID : {}", idOffre);
                    return new ResourceNotFoundException(OFFRE_NON_TROUVEE_ID_PREFIX + idOffre);
                });
        return convertToOffreAdminDtoWithDetails(offre);
    }

    /**
     * Met à jour une offre existante.
     *
     * @param idOffre L'identifiant de l'offre à mettre à jour.
     * @param mettreAJourOffreDto DTO contenant les nouvelles informations de l'offre.
     * @return Le DTO de l'offre mise à jour.
     * @throws ResourceNotFoundException si aucune offre ou discipline n'est trouvée.
     */
    public OffreAdminDto mettreAJourOffre(Long idOffre, MettreAJourOffreDto mettreAJourOffreDto) {
        log.info("Mise à jour de l'offre ID : {}", idOffre);
        Offre offreExistante = offreRepository.findById(idOffre)
                .orElseThrow(() -> {
                    log.warn("Offre non trouvée avec l'ID : {}", idOffre);
                    return new ResourceNotFoundException(OFFRE_NON_TROUVEE_ID_PREFIX + idOffre);
                });

        Discipline discipline = findDisciplineByIdOrThrow(mettreAJourOffreDto.getIdDiscipline());

        modelMapper.map(mettreAJourOffreDto, offreExistante);
        offreExistante.setDiscipline(discipline);

        Offre offreMiseAJour = offreRepository.save(offreExistante);
        log.info("Offre ID : {} mise à jour avec succès.", idOffre);
        return convertToOffreAdminDtoWithDetails(offreMiseAJour);
    }

    /**
     * Supprime une offre par son identifiant.
     * Avant la suppression, l'offre est retirée de tous les paniers utilisateurs.
     *
     * @param idOffre L'identifiant de l'offre à supprimer.
     * @throws ResourceNotFoundException si aucune offre n'est trouvée pour l'ID donné.
     */
    public void supprimerOffre(Long idOffre) {
        log.info("Tentative de suppression de l'offre ID : {}", idOffre);
        Offre offre = offreRepository.findById(idOffre)
                .orElseThrow(() -> {
                    log.warn("Tentative de suppression d'une offre non existante. ID : {}", idOffre);
                    return new ResourceNotFoundException(OFFRE_NON_TROUVEE_ID_PREFIX + idOffre);
                });
        panierService.supprimerOffreDeTousLesPaniers(offre);
        log.info("Impacts sur les paniers gérés pour l'offre ID : {}", idOffre);
        offreRepository.delete(offre);
        log.info("Offre ID : {} supprimée avec succès.", idOffre);
    }

    /**
     * Récupère une liste paginée de toutes les offres.
     *
     * @param pageable L'information de pagination.
     * @return Une page de DTOs d'offres.
     */
    @Transactional(readOnly = true)
    public Page<OffreAdminDto> obtenirToutesLesOffres(Pageable pageable) {
        log.debug("Récupération de toutes les offres (admin) avec pagination : {}", pageable);
        Page<Offre> pageOffres = offreRepository.findAllWithDiscipline(pageable);
        return pageOffres.map(this::convertToOffreAdminDtoWithDetails);
    }

    /**
     * Calcule le nombre total de billets vendus pour chaque offre.
     *
     * @return Une Map associant l'ID de l'offre au nombre de billets vendus.
     */
    @Transactional(readOnly = true)
    public Map<Long, Long> getNombreDeVentesParOffre() {
        log.debug("Calcul du nombre de ventes par offre.");
        return offreRepository.countBilletsByOffreId().stream()
                .collect(Collectors.toMap(
                        result -> (Long) ((Object[]) result)[0],
                        result -> (Long) ((Object[]) result)[1]
                ));
    }

    /**
     * Calcule le nombre total de billets vendus regroupés par type d'offre.
     *
     * @return Une Map associant le type d'offre (String) au nombre de billets vendus.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getVentesParTypeOffre() {
        log.debug("Calcul du nombre de ventes par type d'offre.");
        return offreRepository.countBilletsByTypeOffre().stream()
                .collect(Collectors.toMap(
                        result -> (((Object[]) result)[0] != null) ? ((Object[]) result)[0].toString() : "INCONNU",
                        result -> (Long) ((Object[]) result)[1]
                ));
    }

    private OffreAdminDto convertToOffreAdminDtoWithDetails(Offre offre) {
        OffreAdminDto dto = modelMapper.map(offre, OffreAdminDto.class);
        Discipline discipline = offre.getDiscipline();
        if (discipline != null) {
            dto.setIdDiscipline(discipline.getIdDiscipline());
            dto.setDateExpiration(offre.getEffectiveDateExpiration());
        } else {
            dto.setDateExpiration(offre.getDateExpiration());
        }
        dto.setFeatured(offre.isFeatured());
        return dto;
    }
}