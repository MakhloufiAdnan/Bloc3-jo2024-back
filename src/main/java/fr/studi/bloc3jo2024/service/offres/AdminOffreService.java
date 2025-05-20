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

    public OffreAdminDto ajouterOffre(CreerOffreDto creerOffreDto) {
        log.info("Ajout d'une nouvelle offre pour la discipline ID : {}", creerOffreDto.getIdDiscipline());
        Discipline discipline = findDisciplineByIdOrThrow(creerOffreDto.getIdDiscipline());

        Offre nouvelleOffre = modelMapper.map(creerOffreDto, Offre.class);
        nouvelleOffre.setDiscipline(discipline);

        Offre offreCreee = offreRepository.save(nouvelleOffre);
        log.info("Offre créée avec ID : {}", offreCreee.getIdOffre());
        return convertToOffreAdminDtoWithDetails(offreCreee);
    }

    @Transactional(readOnly = true)
    public OffreAdminDto obtenirOffreParId(Long idOffre) {
        log.debug("Récupération de l'offre ID : {}", idOffre);
        Offre offre = offreRepository.findById(idOffre)
                .orElseThrow(() -> {
                    log.warn(OFFRE_NON_TROUVEE_ID_PREFIX + "{}", idOffre);
                    return new ResourceNotFoundException(OFFRE_NON_TROUVEE_ID_PREFIX + idOffre);
                });
        return convertToOffreAdminDtoWithDetails(offre);
    }

    public OffreAdminDto mettreAJourOffre(Long idOffre, MettreAJourOffreDto mettreAJourOffreDto) {
        log.info("Mise à jour de l'offre ID : {}", idOffre);
        Offre offreExistante = offreRepository.findById(idOffre)
                .orElseThrow(() -> {
                    log.warn(OFFRE_NON_TROUVEE_ID_PREFIX + "{}", idOffre);
                    return new ResourceNotFoundException(OFFRE_NON_TROUVEE_ID_PREFIX + idOffre);
                });

        Discipline discipline = findDisciplineByIdOrThrow(mettreAJourOffreDto.getIdDiscipline());

        modelMapper.map(mettreAJourOffreDto, offreExistante);
        offreExistante.setDiscipline(discipline);

        Offre offreMiseAJour = offreRepository.save(offreExistante);
        log.info("Offre ID : {} mise à jour avec succès.", idOffre);
        return convertToOffreAdminDtoWithDetails(offreMiseAJour);
    }

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

    @Transactional(readOnly = true)
    public Page<OffreAdminDto> obtenirToutesLesOffres(Pageable pageable) {
        log.debug("Récupération de toutes les offres (admin) avec pagination : {}", pageable);
        Page<Offre> pageOffres = offreRepository.findAllWithDiscipline(pageable);
        return pageOffres.map(this::convertToOffreAdminDtoWithDetails);
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> getNombreDeVentesParOffre() {
        // ... (inchangé)
        log.debug("Calcul du nombre de ventes par offre.");
        return offreRepository.countBilletsByOffreId().stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> (Long) result[1]
                ));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getVentesParTypeOffre() {
        // ... (inchangé)
        log.debug("Calcul du nombre de ventes par type d'offre.");
        return offreRepository.countBilletsByTypeOffre().stream()
                .collect(Collectors.toMap(
                        result -> (result[0] != null) ? result[0].toString() : "INCONNU",
                        result -> (Long) result[1]
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