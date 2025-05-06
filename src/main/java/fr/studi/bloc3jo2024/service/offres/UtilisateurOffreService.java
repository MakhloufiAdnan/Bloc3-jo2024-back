package fr.studi.bloc3jo2024.service.offres;

import fr.studi.bloc3jo2024.dto.offres.OffreDto;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.OffreRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UtilisateurOffreService {
    private static final String OFFRE_NON_TROUVEE = "Offre non trouvée avec l'ID : ";

    private final OffreRepository offreRepository;
    private final ModelMapper modelMapper;

    public UtilisateurOffreService(OffreRepository offreRepository, ModelMapper modelMapper) {
        this.offreRepository = offreRepository;
        this.modelMapper = modelMapper;
    }

    public List<OffreDto> obtenirToutesLesOffresDisponibles() {
        return offreRepository.findByStatutOffre(StatutOffre.DISPONIBLE).stream()
                .map(this::convertToOffreDto)
                .collect(Collectors.toList());
    }

    public OffreDto obtenirOffreDisponibleParId(Long idOffre) {
        Offre offre = offreRepository.findById(idOffre)
                .orElseThrow(() -> new ResourceNotFoundException(OFFRE_NON_TROUVEE + idOffre));
        if (offre.getStatutOffre() != StatutOffre.DISPONIBLE) {
            throw new ResourceNotFoundException(OFFRE_NON_TROUVEE + idOffre); // Ou une exception plus spécifique
        }
        return convertToOffreDto(offre);
    }

    private OffreDto convertToOffreDto(Offre offre) {
        OffreDto dto = modelMapper.map(offre, OffreDto.class);
        if (offre.getDiscipline() != null) {
            dto.setIdDiscipline(offre.getDiscipline().getIdDiscipline());
        }
        // Calculer la quantité disponible (si nécessaire, en soustrayant les réservations/ventes)
        // Pour l'instant, on utilise la quantité totale comme quantité disponible
        dto.setQuantiteDisponible(offre.getQuantite());
        return dto;
    }
}