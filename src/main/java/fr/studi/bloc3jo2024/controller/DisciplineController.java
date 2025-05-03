package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.service.DisciplineService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/disciplines")
public class DisciplineController {

    private final DisciplineService disciplineService;

    public DisciplineController(DisciplineService disciplineService) {
        this.disciplineService = disciplineService;
    }

    /**
     * Recherche des disciplines avec filtres facultatifs :
     * - ville
     * - date (AAAA-MM-JJ)
     * - epreuveId
     */
    @GetMapping
    public List<Discipline> getDisciplines(
            @RequestParam(required = false) String ville,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(required = false) Long epreuveId
    ) {
        return disciplineService.findDisciplinesFiltered(ville, date, epreuveId);
    }

    // Récupère les disciplines dont la date est dans le futur.
    @GetMapping("/avenir")
    public List<Discipline> getDisciplinesAvenir() {
        return disciplineService.getDisciplinesAvenir();
    }

    /**
     * Retire des places d'une discipline.
     * Exemple : PATCH /disciplines/5/retirer-places?nb=3
     */
    @PatchMapping("/{id}/retirer-places")
    public Discipline retirerPlaces(
            @PathVariable Long id,
            @RequestParam int nb
    ) {
        return disciplineService.retirerPlaces(id, nb);
    }

    /**
     * Ajoute des places à une discipline.
     * Exemple : PATCH /disciplines/5/ajouter-places?nb=10
     */
    @PatchMapping("/{id}/ajouter-places")
    public Discipline ajouterPlaces(
            @PathVariable Long id,
            @RequestParam int nb
    ) {
        return disciplineService.ajouterPlaces(id, nb);
    }

    /**
     * Met à jour la date d'une discipline.
     * Exemple : PATCH /disciplines/5/modifier-date?date=2025-06-01T14:00:00
     */
    @PatchMapping("/{id}/modifier-date")
    public Discipline updateDate(
            @PathVariable Long id,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date
    ) {
        return disciplineService.updateDate(id, date);
    }
}
