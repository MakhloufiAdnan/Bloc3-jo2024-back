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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminOffreService {

    private static final String DISCIPLINE_NON_TROUVEE_ID = "Discipline non trouvée avec l'ID : ";
    private static final String OFFRE_NON_TROUVEE_ID = "Offre non trouvée avec l'ID : ";

    private final OffreRepository offreRepository;
    private final DisciplineRepository disciplineRepository;
    private final PanierService panierService;
    private final ModelMapper modelMapper;

    private Discipline getDisciplineById(Long idDiscipline) {
        return disciplineRepository.findById(idDiscipline)
                .orElseThrow(() -> new ResourceNotFoundException(DISCIPLINE_NON_TROUVEE_ID + idDiscipline));
    }

    public OffreAdminDto ajouterOffre(CreerOffreDto creerOffreDto) {
        Discipline discipline = getDisciplineById(creerOffreDto.getIdDiscipline());
        Offre nouvelleOffre = modelMapper.map(creerOffreDto, Offre.class);
        nouvelleOffre.setDiscipline(discipline);
        Offre offreCreee = offreRepository.save(nouvelleOffre);
        return convertToOffreAdminDtoWithDetails(offreCreee);
    }

    @Transactional(readOnly = true)
    public OffreAdminDto obtenirOffreParId(Long idOffre) {
        Offre offre = offreRepository.findById(idOffre)
                .orElseThrow(() -> new ResourceNotFoundException(OFFRE_NON_TROUVEE_ID + idOffre));
        return convertToOffreAdminDtoWithDetails(offre);
    }

    public OffreAdminDto mettreAJourOffre(Long idOffre, MettreAJourOffreDto mettreAJourOffreDto) {
        Offre offreExistante = offreRepository.findById(idOffre)
                .orElseThrow(() -> new ResourceNotFoundException(OFFRE_NON_TROUVEE_ID + idOffre));
        Discipline discipline = getDisciplineById(mettreAJourOffreDto.getIdDiscipline());

        modelMapper.map(mettreAJourOffreDto, offreExistante);
        offreExistante.setDiscipline(discipline);

        Offre offreMiseAJour = offreRepository.save(offreExistante);
        return convertToOffreAdminDtoWithDetails(offreMiseAJour);
    }

    public void supprimerOffre(Long idOffre) {
        Offre offre = offreRepository.findById(idOffre)
                .orElseThrow(() -> new ResourceNotFoundException(OFFRE_NON_TROUVEE_ID + idOffre));
        panierService.supprimerOffreDeTousLesPaniers(offre);
        offreRepository.delete(offre);
    }

    @Transactional(readOnly = true)
    public Page<OffreAdminDto> obtenirToutesLesOffres(Pageable pageable) {
        Page<Offre> pageOffres = offreRepository.findAll(pageable);
        return pageOffres.map(this::convertToOffreAdminDtoWithDetails);
    }

    /**
     * Récupère le nombre de ventes (basé sur les billets associés) par offre.
     * @return Une map avec l'ID de l'offre comme clé et le nombre de ventes comme valeur.
     */
    @Transactional(readOnly = true)
    public Map<Long, Long> getNombreDeVentesParOffre() {
        return offreRepository.countBilletsByOffreId().stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0], // ID de l'offre
                        result -> (Long) result[1]  // Nombre de ventes (COUNT)
                ));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getVentesParTypeOffre() {
        return offreRepository.countBilletsByTypeOffre().stream()
                .collect(Collectors.toMap(
                        result -> (result[0] != null) ? result[0].toString() : "INCONNU",
                        result -> (Long) result[1]
                ));
    }
    private OffreAdminDto convertToOffreAdminDtoWithDetails(Offre offre) {
        OffreAdminDto dto = modelMapper.map(offre, OffreAdminDto.class);
        if (offre.getDiscipline() != null) {
            dto.setIdDiscipline(offre.getDiscipline().getIdDiscipline());
        }
        return dto;
    }
}