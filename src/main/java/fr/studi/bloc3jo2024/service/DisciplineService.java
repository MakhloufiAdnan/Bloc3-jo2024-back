package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.repository.DisciplineRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DisciplineService {

    private final DisciplineRepository disciplineRepository;

    public DisciplineService(DisciplineRepository disciplineRepository) {
        this.disciplineRepository = disciplineRepository;
    }

    // Réserve des places disponibles.
    public Discipline retirerPlaces(Long idDiscipline, int nb) {
        Discipline discipline = getDisciplineOrThrow(idDiscipline);
        if (nb <= 0) throw new IllegalArgumentException("Le nombre à retirer doit être positif.");
        if (nb > discipline.getNbPlaceDispo()) throw new IllegalArgumentException("Pas assez de places disponibles.");
        discipline.setNbPlaceDispo(discipline.getNbPlaceDispo() - nb);
        return disciplineRepository.save(discipline);
    }

    // Ajoute des places à une discipline.
    public Discipline ajouterPlaces(Long idDiscipline, int nb) {
        Discipline discipline = getDisciplineOrThrow(idDiscipline);
        if (nb <= 0) throw new IllegalArgumentException("Le nombre à ajouter doit être positif.");
        discipline.setNbPlaceDispo(discipline.getNbPlaceDispo() + nb);
        return disciplineRepository.save(discipline);
    }

    // Met à jour la date d'une discipline.
    public Discipline updateDate(Long idDiscipline, LocalDateTime nouvelleDate) {
        if (nouvelleDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La nouvelle date ne peut pas être dans le passé.");
        }
        Discipline discipline = getDisciplineOrThrow(idDiscipline);
        discipline.setDateDiscipline(nouvelleDate);
        return disciplineRepository.save(discipline);
    }

    // Récupère les disciplines à venir.
    public List<Discipline> getDisciplinesAvenir() {
        return disciplineRepository.findByDateDisciplineAfter(LocalDateTime.now());
    }

    // Récupère une discipline par son ID, ou exception.
    private Discipline getDisciplineOrThrow(Long id) {
        return disciplineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Discipline non trouvée avec l'id " + id));
    }

    // Filtrage combiné (ville, date, épreuve)
    public List<Discipline> findDisciplinesFiltered(String ville, LocalDateTime date, Long epreuveId) {
        if (ville != null) {
            return disciplineRepository.findDisciplinesByVille(ville);
        } else if (date != null) {
            return disciplineRepository.findDisciplinesByDateDiscipline(date);
        } else if (epreuveId != null) {
            return disciplineRepository.findDisciplinesByEpreuveId(epreuveId);
        } else {
            return disciplineRepository.findAll();
        }
    }
}
