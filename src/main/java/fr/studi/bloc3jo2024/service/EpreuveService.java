package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.dto.epreuves.MettreAJourEpreuveVedetteDto;
import fr.studi.bloc3jo2024.entity.Epreuve;
import fr.studi.bloc3jo2024.repository.EpreuveRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;


import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EpreuveService {

    private final EpreuveRepository epreuveRepository;

    /**
     * Récupère la liste de toutes les épreuves marquées comme "en vedette",
     * avec leurs entités Comporter et Discipline associées.
     * @return Liste des épreuves en vedette.
     */
    @Transactional(readOnly = true)
    public List<Epreuve> getEpreuvesEnVedette() {
        return epreuveRepository.findByIsFeaturedTrueWithComportersAndDisciplines();
    }

    /**
     * Récupère une page d'épreuves marquées comme "en vedette",
     * avec leurs entités Comporter et Discipline associées.
     * @param pageable L'objet de pagination.
     * @return Une page d'épreuves en vedette.
     */
    @Transactional(readOnly = true)
    public Page<Epreuve> getEpreuvesEnVedette(Pageable pageable) {
        return epreuveRepository.findByIsFeaturedTrueWithComportersAndDisciplines(pageable);
    }

    /**
     * Met à jour le statut "en vedette" d'une épreuve.
     * @param dto DTO contenant l'ID de l'épreuve et le nouveau statut.
     * @return L'épreuve mise à jour.
     * @throws EntityNotFoundException si l'épreuve n'est pas trouvée.
     */
    public Epreuve mettreAJourStatutVedette(MettreAJourEpreuveVedetteDto dto) {
        Epreuve epreuve = epreuveRepository.findById(dto.getIdEpreuve())
                .orElseThrow(() -> new EntityNotFoundException("Épreuve non trouvée avec l'id : " + dto.getIdEpreuve()));
        // Ensure the Epreuve entity has a setFeatured method
        epreuve.setFeatured(dto.getIsFeatured());
        return epreuveRepository.save(epreuve);
    }

    /**
     * Récupère toutes les épreuves avec pagination,
     * en chargeant les entités Comporter et Discipline associées.
     * @param pageable L'objet de pagination.
     * @return Une page de toutes les épreuves.
     */
    @Transactional(readOnly = true)
    public Page<Epreuve> getAllEpreuves(Pageable pageable) {
        return epreuveRepository.findAllWithComportersAndDisciplines(pageable);
    }

    /**
     * Récupère une épreuve par son ID,
     * en chargeant les entités Comporter et Discipline associées.
     * @param id L'ID de l'épreuve.
     * @return L'épreuve trouvée.
     * @throws EntityNotFoundException si l'épreuve n'est pas trouvée.
     */
    @Transactional(readOnly = true)
    public Epreuve getEpreuveById(Long id) {
        return epreuveRepository.findByIdWithComportersAndDisciplines(id)
                .orElseThrow(() -> new EntityNotFoundException("Épreuve non trouvée avec l'id : " + id));
    }
}