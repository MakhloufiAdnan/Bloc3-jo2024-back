package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.disciplines.CreerDisciplineDto;
import fr.studi.bloc3jo2024.dto.disciplines.DisciplineDto;
import fr.studi.bloc3jo2024.dto.disciplines.MettreAJourDisciplineDto;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.service.DisciplineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/disciplines")
public class DisciplineController {

    private final DisciplineService disciplineService;

    public DisciplineController(DisciplineService disciplineService) {
        this.disciplineService = disciplineService;
    }

    private DisciplineDto convertToDto(Discipline discipline) {
        return new DisciplineDto(
                discipline.getIdDiscipline(),
                discipline.getNomDiscipline(),
                discipline.getDateDiscipline(),
                discipline.getNbPlaceDispo(),
                discipline.getAdresse().getIdAdresse()
        );
    }

    /**
     * Recherche des disciplines avec filtres facultatifs.
     */
    @GetMapping
    public List<DisciplineDto> getDisciplines(
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) LocalDateTime date,
            @RequestParam(required = false) Long epreuveId
    ) {
        return disciplineService.findDisciplinesFiltered(ville, date, epreuveId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @GetMapping("/avenir")
    public List<DisciplineDto> getDisciplinesAvenir() {
        return disciplineService.getDisciplinesAvenir()
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @GetMapping("/vedette")
    public List<DisciplineDto> getDisciplinesEnVedette() {
        return disciplineService.getDisciplinesEnVedette()
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    /**
     * Crée une nouvelle discipline.
     */
    @PostMapping
    public ResponseEntity<DisciplineDto> creerDiscipline(
            @Valid @RequestBody CreerDisciplineDto creerDisciplineDto
    ) {
        Discipline disciplineCreee = disciplineService.creerDiscipline(creerDisciplineDto);
        return new ResponseEntity<>(convertToDto(disciplineCreee), HttpStatus.CREATED);
    }

    /**
     * Met à jour une discipline existante.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DisciplineDto> mettreAJourDiscipline(
            @PathVariable Long id,
            @Valid @RequestBody MettreAJourDisciplineDto mettreAJourDisciplineDto
    ) {
        if (!id.equals(mettreAJourDisciplineDto.getIdDiscipline())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Discipline disciplineMiseAJour = disciplineService.mettreAJourDiscipline(mettreAJourDisciplineDto);
        return ResponseEntity.ok(convertToDto(disciplineMiseAJour));
    }

    /**
     * Supprime une discipline.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerDiscipline(@PathVariable Long id) {
        disciplineService.supprimerDiscipline(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Retire des places d'une discipline.
     * Exemple : PATCH /disciplines/5/retirer-places?nb=3
     */
    @PatchMapping("/{id}/retirer-places")
    public ResponseEntity<DisciplineDto> retirerPlaces(
            @PathVariable Long id,
            @RequestParam int nb
    ) {
        Discipline discipline = disciplineService.retirerPlaces(id, nb);
        return ResponseEntity.ok(convertToDto(discipline));
    }

    /**
     * Ajoute des places à une discipline.
     * Exemple : PATCH /disciplines/5/ajouter-places?nb=10
     */
    @PatchMapping("/{id}/ajouter-places")
    public ResponseEntity<DisciplineDto> ajouterPlaces(
            @PathVariable Long id,
            @RequestParam int nb
    ) {
        Discipline discipline = disciplineService.ajouterPlaces(id, nb);
        return ResponseEntity.ok(convertToDto(discipline));
    }

    /**
     * Met à jour la date d'une discipline.
     * Exemple : PATCH /disciplines/5/modifier-date?date=2025-06-01T14:00:00
     */
    @PatchMapping("/{id}/modifier-date")
    public ResponseEntity<DisciplineDto> updateDate(
            @PathVariable Long id,
            @RequestParam LocalDateTime date
    ) {
        Discipline discipline = disciplineService.updateDate(id, date);
        return ResponseEntity.ok(convertToDto(discipline));
    }
}