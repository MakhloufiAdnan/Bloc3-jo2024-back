package fr.bloc_jo2024.controller;

import fr.bloc_jo2024.entity.Adresse;
import fr.bloc_jo2024.service.AdresseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/adresses")
@RequiredArgsConstructor
public class AdresseController {

    private final AdresseService adresseService;

    @PostMapping
    public ResponseEntity<Adresse> creerAdresse(@RequestBody Adresse adresse) {
        Adresse nouvelleAdresse = adresseService.creerAdresse(adresse);
        return new ResponseEntity<>(nouvelleAdresse, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Adresse> getAdresseById(@PathVariable Long id) {
        Adresse adresse = adresseService.getAdresseById(id);
        return new ResponseEntity<>(adresse, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Adresse>> getAllAdresses() {
        List<Adresse> adresses = adresseService.getAllAdresses();
        return new ResponseEntity<>(adresses, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Adresse> updateAdresse(@PathVariable Long id, @RequestBody Adresse nouvelleAdresse) {
        Adresse adresseMiseAJour = adresseService.updateAdresse(id, nouvelleAdresse);
        return new ResponseEntity<>(adresseMiseAJour, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdresse(@PathVariable Long id) {
        adresseService.deleteAdresse(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}