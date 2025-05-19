package fr.studi.bloc3jo2024.service.offres;

import fr.studi.bloc3jo2024.dto.offres.OffreDto;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.OffreRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UtilisateurOffreService {

    private static final String OFFRE_NON_TROUVEE_ID = "Offre non trouvée avec l'ID : ";
    private static final String OFFRE_NON_DISPONIBLE_ID = "L'offre avec l'ID %d n'est pas disponible.";


    private final OffreRepository offreRepository;
    private final ModelMapper modelMapper;

    /**
     * Récupère toutes les offres disponibles pour les utilisateurs, avec pagination.
     * @param pageable L'objet de pagination.
     * @return Une page de DTOs d'offres disponibles.
     */
    public Page<OffreDto> obtenirToutesLesOffresDisponibles(Pageable pageable) {
        Page<Offre> pageOffres = offreRepository.findByStatutOffre(StatutOffre.DISPONIBLE, pageable);
        return pageOffres.map(this::convertToOffreDtoWithDetails);
    }

    /**
     * Récupère une offre spécifique disponible par son ID.
     * @param idOffre L'ID de l'offre.
     * @return Le DTO de l'offre.
     * @throws ResourceNotFoundException si l'offre n'est pas trouvée ou n'est pas disponible.
     */
    public OffreDto obtenirOffreDisponibleParId(Long idOffre) {
        Offre offre = offreRepository.findById(idOffre)
                .orElseThrow(() -> new ResourceNotFoundException(OFFRE_NON_TROUVEE_ID + idOffre));

        if (offre.getStatutOffre() != StatutOffre.DISPONIBLE) {
            throw new ResourceNotFoundException(String.format(OFFRE_NON_DISPONIBLE_ID, idOffre));
        }
        return convertToOffreDtoWithDetails(offre);
    }

    /**
     * Convertit une entité Offre en OffreDto.
     * S'assure que l'ID de la discipline est inclus.
     * Calcule la quantité disponible (ici, simplement la quantité de l'offre).
     * @param offre L'entité Offre.
     * @return Le DTO OffreDto.
     */
    private OffreDto convertToOffreDtoWithDetails(Offre offre) {
        OffreDto dto = modelMapper.map(offre, OffreDto.class);
        if (offre.getDiscipline() != null) {
            dto.setIdDiscipline(offre.getDiscipline().getIdDiscipline());
        }
        dto.setQuantiteDisponible(offre.getQuantite());
        return dto;
    }
}