package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.disciplines.CreerDisciplineDto;
import fr.studi.bloc3jo2024.dto.disciplines.DisciplineDto;
import fr.studi.bloc3jo2024.dto.disciplines.MettreAJourDisciplineDto;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.service.DisciplineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/disciplines")
@RequiredArgsConstructor
public class DisciplineController {

    private final DisciplineService disciplineService;
    private final ModelMapper modelMapper;

    /**
     * Convertit une entité Discipline en DisciplineDto en utilisant ModelMapper.
     * @param discipline L'entité Discipline.
     * @return Le DisciplineDto correspondant.
     */
    private DisciplineDto convertToDto(Discipline discipline) {
        return modelMapper.map(discipline, DisciplineDto.class);
    }

    @GetMapping
    public Page<DisciplineDto> getDisciplines(
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date, // Ajout de DateTimeFormat
            @RequestParam(required = false) Long epreuveId,
            Pageable pageable) {
        Page<Discipline> disciplinePage = disciplineService.findDisciplinesFiltered(ville, date, epreuveId, pageable);
        return disciplinePage.map(this::convertToDto);
    }

    @GetMapping("/avenir")
    public Page<DisciplineDto> getDisciplinesAvenir(Pageable pageable) {
        Page<Discipline> disciplinePage = disciplineService.getDisciplinesAvenir(pageable);
        return disciplinePage.map(this::convertToDto);
    }

    @GetMapping("/vedette")
    public Set<DisciplineDto> getDisciplinesEnVedette() {
        return disciplineService.getDisciplinesEnVedette().stream()
                .map(this::convertToDto)
                .collect(Collectors.toSet());
    }

    @PostMapping
    public ResponseEntity<DisciplineDto> creerDiscipline(
            @Valid @RequestBody CreerDisciplineDto creerDisciplineDto) {
        Discipline disciplineCreee = disciplineService.creerDiscipline(creerDisciplineDto);
        return new ResponseEntity<>(convertToDto(disciplineCreee), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DisciplineDto> mettreAJourDiscipline(
            @PathVariable Long id,
            @Valid @RequestBody MettreAJourDisciplineDto mettreAJourDisciplineDto) {
        if (!id.equals(mettreAJourDisciplineDto.getIdDiscipline())) {
            return ResponseEntity.badRequest().build();
        }
        Discipline disciplineMiseAJour = disciplineService.mettreAJourDiscipline(mettreAJourDisciplineDto);
        return ResponseEntity.ok(convertToDto(disciplineMiseAJour));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerDiscipline(@PathVariable Long id) {
        disciplineService.supprimerDiscipline(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/retirer-places")
    public ResponseEntity<DisciplineDto> retirerPlaces(
            @PathVariable("id") Long idDiscipline,
            @RequestParam int nb) {
        Discipline discipline = disciplineService.retirerPlaces(idDiscipline, nb);
        return ResponseEntity.ok(convertToDto(discipline));
    }

    @PatchMapping("/{id}/ajouter-places")
    public ResponseEntity<DisciplineDto> ajouterPlaces(
            @PathVariable("id") Long idDiscipline,
            @RequestParam int nb) {
        Discipline discipline = disciplineService.ajouterPlaces(idDiscipline, nb);
        return ResponseEntity.ok(convertToDto(discipline));
    }

    @PatchMapping("/{id}/modifier-date")
    public ResponseEntity<DisciplineDto> updateDate(
            @PathVariable("id") Long idDiscipline,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        Discipline discipline = disciplineService.updateDate(idDiscipline, date);
        return ResponseEntity.ok(convertToDto(discipline));
    }
}
