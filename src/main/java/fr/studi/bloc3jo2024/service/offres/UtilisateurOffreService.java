package fr.studi.bloc3jo2024.service.offres;

import fr.studi.bloc3jo2024.dto.offres.OffreDto;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.OffreRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UtilisateurOffreService {

    private static final Logger log = LoggerFactory.getLogger(UtilisateurOffreService.class);
    private static final String OFFRE_NON_TROUVEE_ID_PREFIX = "Offre non trouvée avec l'ID : ";
    private static final String OFFRE_NON_DISPONIBLE_ID_MSG = "L'offre avec l'ID %d n'est plus disponible ou n'existe pas.";

    private final OffreRepository offreRepository;
    private final ModelMapper modelMapper;

    public Page<OffreDto> obtenirToutesLesOffresDisponibles(Pageable pageable) {
        log.debug("Récupération des offres disponibles avec pagination : {}", pageable);
        Page<Offre> pageOffres = offreRepository.findByStatutOffreWithDiscipline(StatutOffre.DISPONIBLE, pageable);
        return pageOffres.map(this::convertToOffreDtoWithDetails);
    }

    public OffreDto obtenirOffreDisponibleParId(Long idOffre) {
        log.debug("Récupération de l'offre disponible par ID : {}", idOffre);
        Offre offre = offreRepository.findById(idOffre)
                .orElseThrow(() -> {
                    log.warn("Tentative d'accès à une offre non existante. ID : {}", idOffre);
                    return new ResourceNotFoundException(OFFRE_NON_TROUVEE_ID_PREFIX + idOffre);
                });

        if (offre.getStatutOffre() != StatutOffre.DISPONIBLE) {
            log.warn("Tentative d'accès à une offre non disponible (statut: {}). ID : {}", offre.getStatutOffre(), idOffre);
            throw new ResourceNotFoundException(String.format(OFFRE_NON_DISPONIBLE_ID_MSG, idOffre));
        }
        LocalDateTime effectiveExpiration = offre.getEffectiveDateExpiration();
        if (effectiveExpiration != null && effectiveExpiration.isBefore(LocalDateTime.now())) {
            log.warn("L'offre ID {} est fonctionnellement expirée (date discipline/offre passée) mais son statut est encore {}.", idOffre, offre.getStatutOffre());
            throw new ResourceNotFoundException(String.format(OFFRE_NON_DISPONIBLE_ID_MSG, idOffre));
        }

        return convertToOffreDtoWithDetails(offre);
    }

    private OffreDto convertToOffreDtoWithDetails(Offre offre) {
        OffreDto dto = modelMapper.map(offre, OffreDto.class);
        Discipline discipline = offre.getDiscipline();
        if (discipline != null) {
            dto.setIdDiscipline(discipline.getIdDiscipline());
            dto.setDateExpiration(offre.getEffectiveDateExpiration());
        } else {
            dto.setDateExpiration(offre.getEffectiveDateExpiration());
        }
        dto.setQuantiteDisponible(offre.getQuantite());
        dto.setFeatured(offre.isFeatured());
        return dto;
    }
}