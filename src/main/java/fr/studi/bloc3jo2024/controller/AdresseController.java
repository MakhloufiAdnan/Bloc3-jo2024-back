package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Pays;
import fr.studi.bloc3jo2024.exception.AdresseLieeAUneDisciplineException;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.service.AdresseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/adresses")
@RequiredArgsConstructor
public class AdresseController {

    private final AdresseService adresseService;

    /**
     * Endpoint pour créer une nouvelle adresse.
     * Vérifie au préalable si une adresse identique existe déjà.
     * Si une adresse identique est trouvée, retourne l'ID de l'adresse existante avec le statut HTTP OK.
     * Si aucune adresse identique n'est trouvée, crée une nouvelle adresse et retourne l'objet créé avec le statut HTTP CREATED.
     *
     * @param adresse L'objet {@link Adresse} à créer, passé dans le corps de la requête au format JSON.
     * @return {@link ResponseEntity} contenant soit l'objet {@link Adresse} créé (ou existant), soit l'ID de l'adresse existante,
     * et le statut HTTP approprié:
     * - {@link HttpStatus#OK} si l'adresse existait déjà (retourne l'ID de l'adresse existante).
     * - {@link HttpStatus#CREATED} si une nouvelle adresse a été créée avec succès (retourne l'objet {@link Adresse} créé).
     */
    @PostMapping
    public ResponseEntity<Object> creerAdresseSiNonExistante(@RequestBody Adresse adresse) {
        Long existingAdresseId = adresseService.getIdAdresseSiExistante(adresse);
        if (existingAdresseId != null) {
            return new ResponseEntity<>(existingAdresseId, HttpStatus.OK); // L'adresse existait déjà, retourne l'ID.
        } else {
            Adresse nouvelleAdresse = adresseService.creerAdresseSiNonExistante(adresse); // Utilisation de la méthode renommée dans le service.
            return new ResponseEntity<>(nouvelleAdresse, HttpStatus.CREATED);// Nouvelle adresse créée avec succès, retourne l'objet.
        }
    }

    /**
     * Endpoint pour récupérer une adresse spécifique par son ID.
     *
     * @param id L'identifiant unique de l'adresse à récupérer, passé dans le chemin de l'URI.
     * @return {@link ResponseEntity} contenant l'objet {@link Adresse} trouvé et le statut HTTP:
     * - {@link HttpStatus#OK} si l'adresse est trouvée.
     * - {@link HttpStatus#NOT_FOUND} si aucune adresse n'est trouvée avec cet ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Adresse> getAdresseById(@PathVariable Long id) {
        try {
            Adresse adresse = adresseService.getAdresseById(id);
            return new ResponseEntity<>(adresse, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Endpoint pour récupérer toutes les adresses associées à un utilisateur spécifique.
     *
     * @param idUtilisateur L'identifiant unique de l'utilisateur dont on souhaite récupérer les adresses.
     * @return {@link ResponseEntity} contenant une liste des objets {@link Adresse} associés à l'utilisateur et le statut HTTP {@link HttpStatus#OK}.
     * Si aucune adresse n'est associée à cet utilisateur, une liste vide sera retournée avec le statut {@link HttpStatus#OK}.
     */
    @GetMapping("/utilisateur/{idUtilisateur}")
    public ResponseEntity<List<Adresse>> getAdressesByUtilisateurId(@PathVariable UUID idUtilisateur) {
        List<Adresse> adresses = adresseService.getAdressesByUtilisateurId(idUtilisateur);
        return new ResponseEntity<>(adresses, HttpStatus.OK);
    }

    /**
     * Endpoint pour récupérer l'adresse associée à une discipline spécifique.
     *
     * @param idDiscipline L'identifiant unique de la discipline dont on souhaite récupérer l'adresse.
     * @return {@link ResponseEntity} contenant l'objet {@link Adresse} associé à la discipline et le statut HTTP:
     * - {@link HttpStatus#OK} si une adresse est trouvée pour cette discipline.
     * - {@link HttpStatus#NOT_FOUND} si aucune adresse n'est trouvée pour cette discipline.
     */
    @GetMapping("/discipline/{idDiscipline}")
    public ResponseEntity<Adresse> getAdresseByDiscipline(@PathVariable Long idDiscipline) {
        Discipline discipline = new Discipline();
        discipline.setIdDiscipline(idDiscipline);
        try {
            Adresse adresse = adresseService.getAdresseByDiscipline(discipline);
            return new ResponseEntity<>(adresse, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Endpoint pour récupérer toutes les adresses liées à une discipline spécifique.
     *
     * @param idDiscipline L'identifiant unique de la discipline dont on souhaite récupérer les adresses liées.
     * @return {@link ResponseEntity} contenant une liste des objets {@link Adresse} liés à la discipline et le statut HTTP {@link HttpStatus#OK}.
     * Si aucune adresse n'est liée à cette discipline, une liste vide sera retournée avec le statut {@link HttpStatus#OK}.
     */
    @GetMapping("/discipline/{idDiscipline}/all")
    public ResponseEntity<List<Adresse>> getAdressesByDiscipline(@PathVariable Long idDiscipline) {
        Discipline discipline = new Discipline();
        discipline.setIdDiscipline(idDiscipline);
        List<Adresse> adresses = adresseService.getAdressesByDiscipline(discipline);
        return new ResponseEntity<>(adresses, HttpStatus.OK);
    }

    /**
     * Endpoint pour rechercher une adresse complète en fonction de plusieurs critères.
     * Les paramètres de recherche sont passés via des paramètres de requête.
     *
     * @param numeroRue  Le numéro de la rue.
     * @param nomRue     Le nom de la rue.
     * @param ville      La ville.
     * @param codePostal Le code postal.
     * @param idPays     L'identifiant unique du pays.
     * @return {@link ResponseEntity} contenant l'objet {@link Adresse} trouvé et le statut HTTP:
     * - {@link HttpStatus#OK} si une adresse correspondante est trouvée.
     * - {@link HttpStatus#NOT_FOUND} si aucune adresse correspondante n'est trouvée.
     */
    @GetMapping("/recherche")
    public ResponseEntity<Adresse> rechercherAdresseComplete(
            @RequestParam int numeroRue,
            @RequestParam String nomRue,
            @RequestParam String ville,
            @RequestParam String codePostal,
            @RequestParam Long idPays
    ) {
        Pays pays = new Pays();
        pays.setIdPays(idPays);
        Adresse adresse = adresseService.rechercherAdresseComplete(numeroRue, nomRue, ville, codePostal, pays);
        if (adresse != null) {
            return new ResponseEntity<>(adresse, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Endpoint pour rechercher les adresses par ville pour les disciplines.
     *
     * @param ville Le nom de la ville pour laquelle rechercher les adresses.
     * @return {@link ResponseEntity} contenant une liste des objets {@link Adresse} situés dans la ville spécifiée et le statut HTTP {@link HttpStatus#OK}.
     * Si aucune adresse n'est trouvée dans cette ville, une liste vide sera retournée avec le statut {@link HttpStatus#OK}.
     */
    @GetMapping("/ville/{ville}")
    public ResponseEntity<List<Adresse>> rechercherAdressesParVillePourDisciplines(@PathVariable String ville) {
        List<Adresse> adresses = adresseService.rechercherAdressesParVillePourDisciplines(ville);
        return new ResponseEntity<>(adresses, HttpStatus.OK);
    }

    /**
     * Endpoint pour rechercher les adresses pour une discipline spécifique et filtrées par l'ID du pays.
     *
     * @param idDiscipline L'identifiant unique de la discipline.
     * @param idPays      L'identifiant unique du pays des adresses à rechercher.
     * @return {@link ResponseEntity} contenant une liste des objets {@link Adresse} liés à la discipline et appartenant au pays spécifié,
     * et le statut HTTP {@link HttpStatus#OK}. Si aucune adresse correspondante n'est trouvée, une liste vide sera retournée
     * avec le statut {@link HttpStatus#OK}.
     * Note : La création d'un objet {@link Discipline}
     */
    @GetMapping("/discipline/{idDiscipline}/pays/{idPays}")
    public ResponseEntity<List<Adresse>> rechercherAdressesParDisciplineEtPays(
            @PathVariable Long idDiscipline,
            @PathVariable Long idPays
    ) {
        Discipline discipline = new Discipline();
        discipline.setIdDiscipline(idDiscipline); // Création d'un objet Discipline avec l'ID passé en paramètre
        List<Adresse> adresses = adresseService.rechercherAdressesParDisciplineEtPays(discipline, idPays);
        return new ResponseEntity<>(adresses, HttpStatus.OK);
    }

    /**
     * Endpoint pour mettre à jour une adresse existante.
     *
     * @param id              L'identifiant unique de l'adresse à mettre à jour, passé dans le chemin de l'URI.
     * @param nouvelleAdresse L'objet {@link Adresse} contenant les nouvelles informations, passé dans le corps de la requête au format JSON.
     * @return {@link ResponseEntity} contenant l'objet {@link Adresse} mis à jour et le statut HTTP:
     * - {@link HttpStatus#OK} si la mise à jour a réussi.
     * - {@link HttpStatus#NOT_FOUND} si aucune adresse n'est trouvée avec cet ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Adresse> updateAdresse(@PathVariable Long id, @RequestBody Adresse nouvelleAdresse) {
        try {
            Adresse adresseMiseAJour = adresseService.updateAdresse(id, nouvelleAdresse);
            return new ResponseEntity<>(adresseMiseAJour, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Endpoint pour supprimer une adresse par son ID.
     *
     * @param id L'identifiant unique de l'adresse à supprimer, passé dans le chemin de l'URI.
     * @return {@link ResponseEntity} avec le statut HTTP:
     * - {@link HttpStatus#NO_CONTENT} si la suppression a réussi (pas de contenu à retourner).
     * - {@link HttpStatus#NOT_FOUND} si aucune adresse n'est trouvée avec cet ID.
     * - {@link HttpStatus#CONFLICT} si l'adresse est liée à un événement et ne peut pas être supprimée.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdresse(@PathVariable Long id) {
        try {
            adresseService.deleteAdresse(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Suppression réussie, pas de contenu à retourner.
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (AdresseLieeAUneDisciplineException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT); // L'adresse est liée à un événement.
        }
    }
}