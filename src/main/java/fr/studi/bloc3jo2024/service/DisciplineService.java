package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.dto.disciplines.CreerDisciplineDto;
import fr.studi.bloc3jo2024.dto.disciplines.MettreAJourDisciplineDto;
import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Epreuve;
import fr.studi.bloc3jo2024.repository.AdresseRepository;
import fr.studi.bloc3jo2024.repository.DisciplineRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class DisciplineService {

    private final DisciplineRepository disciplineRepository;
    private final AdresseRepository adresseRepository;
    private final EpreuveService epreuveService;

    private DisciplineService self;

    public DisciplineService(DisciplineRepository disciplineRepository,
                             AdresseRepository adresseRepository,
                             EpreuveService epreuveService) {
        this.disciplineRepository = disciplineRepository;
        this.adresseRepository = adresseRepository;
        this.epreuveService = epreuveService;
    }
    @Autowired
    public void setSelf(@Lazy DisciplineService self) {
        this.self = self;
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
        // Utilisation de self pour appeler la méthode transactionnelle
        Discipline discipline = self.getDisciplineOrThrow(mettreAJourDisciplineDto.getIdDiscipline());
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
        if (nb <= 0) {
            throw new IllegalArgumentException("Le nombre de places à retirer doit être positif.");
        }
        int updatedRows = disciplineRepository.decrementerPlaces(idDiscipline, nb);
        if (updatedRows == 0) {
            // Utilisation de self pour appeler la méthode transactionnelle
            Discipline discipline = self.getDisciplineOrThrow(idDiscipline);
            throw new IllegalStateException("Impossible de retirer les places. Nombre de places disponibles ("+ discipline.getNbPlaceDispo() +") insuffisant pour la discipline ID " + idDiscipline + ".");
        }
        // Utilisation de self pour appeler la méthode transactionnelle
        return self.getDisciplineOrThrow(idDiscipline);
    }

    public Discipline ajouterPlaces(Long idDiscipline, int nb) {
        // Utilisation de self pour appeler la méthode transactionnelle
        Discipline discipline = self.getDisciplineOrThrow(idDiscipline);
        if (nb <= 0) {
            throw new IllegalArgumentException("Le nombre de places à ajouter doit être positif.");
        }
        discipline.setNbPlaceDispo(discipline.getNbPlaceDispo() + nb);
        return disciplineRepository.save(discipline);
    }

    public Discipline updateDate(Long idDiscipline, LocalDateTime nouvelleDate) {
        if (nouvelleDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La nouvelle date de la discipline ne peut pas être dans le passé.");
        }
        // Utilisation de self pour appeler la méthode transactionnelle
        Discipline disciplineToUpdate = self.getDisciplineOrThrow(idDiscipline);
        disciplineToUpdate.setDateDiscipline(nouvelleDate);
        return disciplineRepository.save(disciplineToUpdate);
    }

    @Transactional(readOnly = true)
    public Page<Discipline> getDisciplinesAvenir(Pageable pageable) {
        return disciplineRepository.findFutureDisciplinesWithAdresse(LocalDateTime.now(), pageable);
    }

    /**
     * Récupère une discipline par son ID ou lève une exception si non trouvée.
     * Changée en protected pour permettre la proxyfication par Spring AOP pour @Transactional.
     * Cette méthode sera appelée via l'instance 'self' injectée pour assurer le comportement transactionnel.
     * @param id L'ID de la discipline.
     * @return La discipline trouvée.
     * @throws EntityNotFoundException si la discipline n'est pas trouvée.
     */
    @Transactional(readOnly = true)
    protected Discipline getDisciplineOrThrow(Long id) {
        return disciplineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Discipline non trouvée avec l'id " + id));
    }

    @Transactional(readOnly = true)
    public Page<Discipline> findDisciplinesFiltered(String ville, LocalDateTime date, Long epreuveId, Pageable pageable) {
        if (ville != null && !ville.trim().isEmpty()) {
            return disciplineRepository.findDisciplinesByVilleWithAdresse(ville, pageable);
        } else if (date != null) {
            return disciplineRepository.findDisciplinesByDateDisciplineWithAdresse(date, pageable);
        } else if (epreuveId != null) {
            return disciplineRepository.findDisciplinesByEpreuveIdWithAdresse(epreuveId, pageable);
        } else {
            return disciplineRepository.findAllWithAdresse(pageable);
        }
    }

    @Transactional(readOnly = true)
    public Set<Discipline> getDisciplinesEnVedette() {
        List<Epreuve> epreuvesEnVedette = epreuveService.getEpreuvesEnVedette();
        if (epreuvesEnVedette.isEmpty()) {
            return Set.of();
        }

        List<Long> idsEpreuvesEnVedette = epreuvesEnVedette.stream()
                .map(Epreuve::getIdEpreuve)
                .collect(Collectors.toList());

        if (idsEpreuvesEnVedette.isEmpty()) {
            return Set.of();
        }
        return disciplineRepository.findDisciplinesByEpreuveIdsWithAdresse(idsEpreuvesEnVedette);
    }
}
