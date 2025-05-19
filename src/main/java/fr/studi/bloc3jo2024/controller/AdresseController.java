package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Pays;
import fr.studi.bloc3jo2024.exception.AdresseLieeAUneDisciplineException;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.service.AdresseService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/adresses")
@RequiredArgsConstructor
public class AdresseController {

    private static final Logger log = LoggerFactory.getLogger(AdresseController.class);
    private final AdresseService adresseService;

    /**
     * Crée une nouvelle adresse si elle n'existe pas déjà.
     * Si elle existe, retourne l'ID de l'adresse existante.
     * @param adresse L'objet Adresse à créer.
     * @return ResponseEntity avec l'ID existant (200 OK) ou la nouvelle Adresse (201 CREATED).
     * @throws ResponseStatusException avec statut 400 si les données d'entrée sont invalides (ex: pays manquant).
     */
    @PostMapping
    public ResponseEntity<Object> creerAdresseSiNonExistante(@RequestBody Adresse adresse) {
        try {
            Long existingAdresseId = adresseService.getIdAdresseSiExistante(adresse);
            if (existingAdresseId != null) {
                log.info("Tentative de création d'une adresse existante, ID retourné : {}", existingAdresseId);
                return ResponseEntity.ok().body(Collections.singletonMap("idAdresseExistante", existingAdresseId));
            } else {
                Adresse nouvelleAdresse = adresseService.creerAdresseSiNonExistante(adresse);
                log.info("Nouvelle adresse créée avec ID : {}", nouvelleAdresse.getIdAdresse());
                return new ResponseEntity<>(nouvelleAdresse, HttpStatus.CREATED);
            }
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    /**
     * Récupère une adresse par son ID.
     * @param id L'ID de l'adresse.
     * @return ResponseEntity avec l'Adresse.
     * @throws ResponseStatusException avec statut 404 si non trouvée.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Adresse> getAdresseById(@PathVariable Long id) {
        try {
            Adresse adresse = adresseService.getAdresseById(id);
            return ResponseEntity.ok(adresse);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    /**
     * Récupère toutes les adresses avec pagination.
     * @param pageable Objet de pagination.
     * @return Une page d'adresses.
     */
    @GetMapping
    public ResponseEntity<Page<Adresse>> getAllAdresses(Pageable pageable) {
        Page<Adresse> adresses = adresseService.getAllAdresses(pageable);
        return ResponseEntity.ok(adresses);
    }


    /**
     * Récupère les adresses associées à un utilisateur.
     * @param idUtilisateur L'ID de l'utilisateur.
     * @return ResponseEntity avec une liste d'Adresses.
     */
    @GetMapping("/utilisateur/{idUtilisateur}")
    public ResponseEntity<List<Adresse>> getAdressesByUserId(@PathVariable UUID idUtilisateur) {
        List<Adresse> adresses = adresseService.getAdressesByUtilisateurId(idUtilisateur);
        return ResponseEntity.ok(adresses);
    }

    /**
     * Récupère l'adresse associée à une discipline.
     * @param idDiscipline L'ID de la discipline.
     * @return ResponseEntity avec l'Adresse.
     * @throws ResponseStatusException avec statut 404 si non trouvée, ou 400 si l'ID de discipline est invalide.
     */
    @GetMapping("/discipline/{idDiscipline}")
    public ResponseEntity<Adresse> getAdresseByDiscipline(@PathVariable Long idDiscipline) {
        Discipline disciplineRef = new Discipline();
        disciplineRef.setIdDiscipline(idDiscipline);
        try {
            Adresse adresse = adresseService.getAdresseByDiscipline(disciplineRef);
            return ResponseEntity.ok(adresse);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    /**
     * Endpoint pour récupérer toutes les adresses liées à une discipline spécifique.
     * @param idDiscipline L'identifiant unique de la discipline.
     * @return ResponseEntity contenant une liste des objets Adresse.
     * @throws ResponseStatusException avec statut 400 si l'ID de discipline est invalide.
     */
    @GetMapping("/discipline/{idDiscipline}/all")
    public ResponseEntity<List<Adresse>> getAdressesByDiscipline(@PathVariable Long idDiscipline) {
        Discipline disciplineRef = new Discipline();
        disciplineRef.setIdDiscipline(idDiscipline);
        try {
            List<Adresse> adresses = adresseService.getAdressesByDiscipline(disciplineRef);
            return ResponseEntity.ok(adresses);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    /**
     * Recherche une adresse par ses attributs complets.
     * @param numeroRue Numéro de rue (Integer).
     * @param nomRue Nom de rue.
     * @param ville Ville.
     * @param codePostal Code postal.
     * @param idPays ID du pays.
     * @return ResponseEntity avec l'Adresse.
     * @throws ResponseStatusException avec statut 404 si non trouvée, ou 400 si les paramètres sont invalides.
     */
    @GetMapping("/recherche")
    public ResponseEntity<Adresse> rechercherAdresseComplete(
            @RequestParam Integer numeroRue,
            @RequestParam String nomRue,
            @RequestParam String ville,
            @RequestParam String codePostal,
            @RequestParam Long idPays) {

        Pays paysRef = new Pays();
        paysRef.setIdPays(idPays);

        try {
            Adresse adresse = adresseService.rechercherAdresseComplete(numeroRue, nomRue, ville, codePostal, paysRef);
            if (adresse != null) {
                return ResponseEntity.ok(adresse);
            } else {
                log.info("Aucune adresse complète trouvée pour les critères : num={}, rue={}, ville={}, cp={}, paysId={}",
                        numeroRue, nomRue, ville, codePostal, idPays);
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    /**
     * Recherche les adresses par ville (pour les disciplines).
     * @param ville La ville.
     * @return ResponseEntity avec une liste d'Adresses.
     */
    @GetMapping("/ville/{ville}")
    public ResponseEntity<List<Adresse>> rechercherAdressesParVillePourDisciplines(@PathVariable String ville) {
        List<Adresse> adresses = adresseService.rechercherAdressesParVillePourDisciplines(ville);
        return ResponseEntity.ok(adresses);
    }


    /**
     * Recherche les adresses par discipline et ID de pays.
     * @param idDiscipline L'ID de la discipline.
     * @param idPays L'ID du pays.
     * @return ResponseEntity avec une liste d'Adresses.
     */
    @GetMapping("/discipline/{idDiscipline}/pays/{idPays}")
    public ResponseEntity<List<Adresse>> rechercherAdressesParDisciplineEtPays(
            @PathVariable Long idDiscipline,
            @PathVariable Long idPays) {
        Discipline disciplineRef = new Discipline();
        disciplineRef.setIdDiscipline(idDiscipline);
        List<Adresse> adresses = adresseService.rechercherAdressesParDisciplineEtPays(disciplineRef, idPays);
        return ResponseEntity.ok(adresses);
    }

    /**
     * Met à jour une adresse existante.
     * @param id L'ID de l'adresse.
     * @param nouvelleAdresse Les nouvelles informations de l'adresse.
     * @return ResponseEntity avec l'Adresse mise à jour.
     * @throws ResponseStatusException avec statut 404 si non trouvée, ou 400 si les données sont invalides.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Adresse> updateAdresse(@PathVariable Long id, @RequestBody Adresse nouvelleAdresse) {
        try {
            Adresse adresseMiseAJour = adresseService.updateAdresse(id, nouvelleAdresse);
            return ResponseEntity.ok(adresseMiseAJour);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    /**
     * Supprime une adresse par son ID.
     * @param id L'ID de l'adresse.
     * @return ResponseEntity avec statut 204 (NO_CONTENT).
     * @throws ResponseStatusException avec statut 404 si non trouvée, ou 409 si l'adresse est liée.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdresse(@PathVariable Long id) {
        try {
            adresseService.deleteAdresse(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (AdresseLieeAUneDisciplineException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        }
    }
}
