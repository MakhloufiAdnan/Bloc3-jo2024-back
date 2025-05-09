package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.dto.epreuves.MettreAJourEpreuveVedetteDto;
import fr.studi.bloc3jo2024.entity.Epreuve;
import fr.studi.bloc3jo2024.repository.EpreuveRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class EpreuveService {

    private final EpreuveRepository epreuveRepository;

    public EpreuveService(EpreuveRepository epreuveRepository) {
        this.epreuveRepository = epreuveRepository;
    }

    public List<Epreuve> getEpreuvesEnVedette() {
        return epreuveRepository.findByIsFeaturedTrue();
    }

    public Epreuve mettreAJourStatutVedette(MettreAJourEpreuveVedetteDto dto) {
        Epreuve epreuve = epreuveRepository.findById(dto.getIdEpreuve())
                .orElseThrow(() -> new EntityNotFoundException("Épreuve non trouvée avec l'id : " + dto.getIdEpreuve()));
        epreuve.setFeatured(dto.getIsFeatured());
        return epreuveRepository.save(epreuve);
    }

    public List<Epreuve> getAllEpreuves() {
        return epreuveRepository.findAll();
    }
}