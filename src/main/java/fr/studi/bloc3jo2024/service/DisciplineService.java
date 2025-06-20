package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.dto.disciplines.CreerDisciplineDto;
import fr.studi.bloc3jo2024.dto.disciplines.MettreAJourDisciplineDto;
import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Epreuve;
import fr.studi.bloc3jo2024.repository.AdresseRepository;
import fr.studi.bloc3jo2024.repository.DisciplineRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class DisciplineService {

    private final DisciplineRepository disciplineRepository;
    private final AdresseRepository adresseRepository;
    private final EpreuveService epreuveService;

    public DisciplineService(DisciplineRepository disciplineRepository, AdresseRepository adresseRepository, EpreuveService epreuveService) {
        this.disciplineRepository = disciplineRepository;
        this.adresseRepository = adresseRepository;
        this.epreuveService = epreuveService;
    }

    public Discipline creerDiscipline(CreerDisciplineDto creerDisciplineDto) {
        Adresse adresse = adresseRepository.findById(creerDisciplineDto.getIdAdresse())
                .orElseThrow(() -> new EntityNotFoundException("Adresse non trouvée avec l'id " + creerDisciplineDto.getIdAdresse()));

        Discipline discipline = new Discipline();
        discipline.setNomDiscipline(creerDisciplineDto.getNomDiscipline());
        discipline.setDateDiscipline(creerDisciplineDto.getDateDiscipline());
        discipline.setNbPlaceDispo(creerDisciplineDto.getNbPlaceDispo());
        discipline.setAdresse(adresse);
        return disciplineRepository.save(discipline);
    }

    public Discipline mettreAJourDiscipline(MettreAJourDisciplineDto mettreAJourDisciplineDto) {
        // Le getDisciplineOrThrow récupérera déjà la discipline avec son adresse si la méthode a un JOIN FETCH par défaut.
        // Sinon, assurez-vous que l'adresse est chargée ici si nécessaire pour la mise à jour.
        Discipline discipline = getDisciplineOrThrow(mettreAJourDisciplineDto.getIdDiscipline());
        Adresse adresse = adresseRepository.findById(mettreAJourDisciplineDto.getIdAdresse())
                .orElseThrow(() -> new EntityNotFoundException("Adresse non trouvée avec l'id " + mettreAJourDisciplineDto.getIdAdresse()));

        discipline.setNomDiscipline(mettreAJourDisciplineDto.getNomDiscipline());
        discipline.setDateDiscipline(mettreAJourDisciplineDto.getDateDiscipline());
        discipline.setNbPlaceDispo(mettreAJourDisciplineDto.getNbPlaceDispo());
        discipline.setAdresse(adresse);
        return disciplineRepository.save(discipline);
    }

    public void supprimerDiscipline(Long id) {
        if (!disciplineRepository.existsById(id)) {
            throw new EntityNotFoundException("Discipline non trouvée avec l'id " + id);
        }
        disciplineRepository.deleteById(id);
    }

    public Discipline retirerPlaces(Long idDiscipline, int nb) {
        getDisciplineOrThrow(idDiscipline); // Vérifier que la discipline existe avant de tenter la mise à jour
        if (nb <= 0) throw new IllegalArgumentException("Le nombre à retirer doit être positif.");
        int updatedRows = disciplineRepository.decrementerPlaces(idDiscipline, nb);
        if (updatedRows == 0) {
            // Cela peut arriver si l'ID n'existe pas, ou si nbPlaceDispo est inférieur à nb.
            // Puisque getDisciplineOrThrow() est appelé avant, c'est forcément un problème de places.
            throw new IllegalStateException("Impossible de retirer les places. Vérifiez le nombre de places disponibles.");
        }
        return getDisciplineOrThrow(idDiscipline); // Récupérer l'entité mise à jour
    }

    public Discipline ajouterPlaces(Long idDiscipline, int nb) {
        Discipline discipline = getDisciplineOrThrow(idDiscipline);
        if (nb <= 0) throw new IllegalArgumentException("Le nombre à ajouter doit être positif.");
        discipline.setNbPlaceDispo(discipline.getNbPlaceDispo() + nb);
        return disciplineRepository.save(discipline);
    }

    public Discipline updateDate(Long idDiscipline, LocalDateTime nouvelleDate) {
        if (nouvelleDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La nouvelle date ne peut pas être dans le passé.");
        }
        Discipline disciplineToUpdate = getDisciplineOrThrow(idDiscipline);
        disciplineToUpdate.setDateDiscipline(nouvelleDate);
        return disciplineRepository.save(disciplineToUpdate);
    }

    public List<Discipline> getDisciplinesAvenir() {
        return disciplineRepository.findByDateDisciplineAfter(LocalDateTime.now());
    }

    private Discipline getDisciplineOrThrow(Long id) {
        return disciplineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Discipline non trouvée avec l'id " + id));
    }

    public List<Discipline> findDisciplinesFiltered(String ville, LocalDateTime date, Long epreuveId) {
        if (ville != null) {
            return disciplineRepository.findDisciplinesByVille(ville);
        } else if (date != null) {
            return disciplineRepository.findDisciplinesByDateDiscipline(date);
        } else if (epreuveId != null) {
            return disciplineRepository.findDisciplinesByEpreuveId(epreuveId);
        } else {
            return disciplineRepository.findAllWithAdresse();
        }
    }

    public Set<Discipline> getDisciplinesEnVedette() {
        List<Epreuve> epreuvesEnVedette = epreuveService.getEpreuvesEnVedette();
        if (epreuvesEnVedette.isEmpty()) {
            return Collections.emptySet();
        }
        // Extraire les IDs des épreuves en vedette
        List<Long> epreuveIds = epreuvesEnVedette.stream()
                .map(Epreuve::getIdEpreuve)
                .toList();
        return disciplineRepository.findDisciplinesByEpreuveIdsWithAdresse(epreuveIds);
    }
}