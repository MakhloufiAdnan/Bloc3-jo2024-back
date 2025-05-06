package fr.studi.bloc3jo2024.controller.offres;

import fr.studi.bloc3jo2024.dto.offres.OffreDto;
import fr.studi.bloc3jo2024.service.offres.UtilisateurOffreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/offres")
public class UtilisateurOffreController {
    private final UtilisateurOffreService utilisateurOffreService;

    public UtilisateurOffreController(UtilisateurOffreService utilisateurOffreService) {
        this.utilisateurOffreService = utilisateurOffreService;
    }

    @GetMapping
    public ResponseEntity<List<OffreDto>> obtenirToutesLesOffresDisponibles() {
        List<OffreDto> offresDisponibles = utilisateurOffreService.obtenirToutesLesOffresDisponibles();
        return ResponseEntity.ok(offresDisponibles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OffreDto> obtenirOffreDisponibleParId(@PathVariable Long id) {
        try {
            OffreDto offreDisponible = utilisateurOffreService.obtenirOffreDisponibleParId(id);
            return ResponseEntity.ok(offreDisponible);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}