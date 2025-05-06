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
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminOffreService {

    private static final String DISCIPLINE_NON_TROUVEE = "Discipline non trouvée avec l'ID : ";
    private static final String OFFRE_NON_TROUVEE = "Offre non trouvée avec l'ID : ";

    private final OffreRepository offreRepository;
    private final DisciplineRepository disciplineRepository;
    private final PanierService panierService;
    private final ModelMapper modelMapper;

    public AdminOffreService(OffreRepository offreRepository, DisciplineRepository disciplineRepository, PanierService panierService, ModelMapper modelMapper) {
        this.offreRepository = offreRepository;
        this.disciplineRepository = disciplineRepository;
        this.panierService = panierService;
        this.modelMapper = modelMapper;
    }

    private Discipline getDisciplineById(Long idDiscipline) {
        return disciplineRepository.findById(idDiscipline)
                .orElseThrow(() -> new ResourceNotFoundException(DISCIPLINE_NON_TROUVEE + idDiscipline));
    }

    public OffreAdminDto ajouterOffre(CreerOffreDto creerOffreDto) {
        Discipline discipline = getDisciplineById(creerOffreDto.getIdDiscipline());
        Offre nouvelleOffre = modelMapper.map(creerOffreDto, Offre.class);
        nouvelleOffre.setDiscipline(discipline);
        Offre offreCreee = offreRepository.save(nouvelleOffre);
        return modelMapper.map(offreCreee, OffreAdminDto.class);
    }

    public OffreAdminDto obtenirOffreParId(Long idOffre) {
        Offre offre = offreRepository.findById(idOffre)
                .orElseThrow(() -> new ResourceNotFoundException(OFFRE_NON_TROUVEE + idOffre));
        return modelMapper.map(offre, OffreAdminDto.class);
    }

    public OffreAdminDto mettreAJourOffre(Long idOffre, MettreAJourOffreDto mettreAJourOffreDto) {
        Offre offreExistante = offreRepository.findById(idOffre)
                .orElseThrow(() -> new ResourceNotFoundException(OFFRE_NON_TROUVEE + idOffre));
        Discipline discipline = getDisciplineById(mettreAJourOffreDto.getIdDiscipline());
        modelMapper.map(mettreAJourOffreDto, offreExistante);
        offreExistante.setDiscipline(discipline);
        Offre offreMisAJour = offreRepository.save(offreExistante);
        return modelMapper.map(offreMisAJour, OffreAdminDto.class);
    }

    public void supprimerOffre(Long idOffre) {
        Offre offre = offreRepository.findById(idOffre)
                .orElseThrow(() -> new ResourceNotFoundException(OFFRE_NON_TROUVEE + idOffre));
        offreRepository.delete(offre);
        panierService.supprimerOffreDeTousLesPaniers(offre);
    }

    public List<OffreAdminDto> obtenirToutesLesOffres() {
        return offreRepository.findAll().stream()
                .map(offre -> modelMapper.map(offre, OffreAdminDto.class))
                .toList();
    }

    public Map<Long, Long> getNombreDeVentesParOffre() {
        return offreRepository.countBilletsByOffre().stream()
                .collect(Collectors.toMap(
                        result -> ((Offre) result[0]).getIdOffre(),
                        result -> (Long) result[1]
                ));
    }

    public Map<String, Long> getVentesParTypeOffre() {
        return offreRepository.countBilletsByTypeOffre().stream()
                .collect(Collectors.toMap(
                        result -> result[0] != null ? result[0].toString() : "Inconnu",
                        result -> (Long) result[1]
                ));
    }
}