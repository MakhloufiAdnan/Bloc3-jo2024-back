package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Evenement;
import fr.studi.bloc3jo2024.entity.Pays;
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
     * Utilise le service pour créer l'adresse si elle n'existe pas déjà.
     *
     * @param adresse L'objet Adresse à créer, passé dans le corps de la requête au format JSON.
     * @return ResponseEntity contenant l'objet Adresse créé (ou existant) et le statut HTTP approprié.
     */
    @PostMapping
    public ResponseEntity<Adresse> createAdresse(@RequestBody Adresse adresse) {
        Adresse nouvelleAdresse = adresseService.creerAdresseSiNonExistante(adresse);
        if (adresseService.adresseExisteDeja(adresse)) {
            return new ResponseEntity<>(nouvelleAdresse, HttpStatus.OK); // L'adresse existait déjà.
        } else {
            return new ResponseEntity<>(nouvelleAdresse, HttpStatus.CREATED); // Nouvelle adresse créée avec succès.
        }
    }

    /**
     * Endpoint pour récupérer une adresse spécifique par son ID.
     *
     * @param id L'identifiant unique de l'adresse à récupérer, passé dans le chemin de l'URI.
     * @return ResponseEntity contenant l'objet Adresse trouvé et le statut HTTP 200 (OK),
     * ou le statut HTTP 404 (NOT FOUND) si aucune adresse n'est trouvée avec cet ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Adresse> getAdresseById(@PathVariable Long id) {
        try {
            Adresse adresse = adresseService.getAdresseById(id);
            return new ResponseEntity<>(adresse, HttpStatus.OK);
        } catch (fr.studi.bloc3jo2024.exception.ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Endpoint pour récupérer toutes les adresses.
     *
     * @return ResponseEntity contenant une liste de tous les objets Adresse et le statut HTTP 200 (OK).
     * Si aucune adresse n'est trouvée, une liste vide sera retournée avec le statut 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<Adresse>> getAllAdresses() {
        List<Adresse> adresses = adresseService.getAllAdresses();
        return new ResponseEntity<>(adresses, HttpStatus.OK);
    }

    /**
     * Endpoint pour récupérer toutes les adresses associées à un utilisateur spécifique.
     *
     * @param idUtilisateur L'identifiant unique de l'utilisateur dont on souhaite récupérer les adresses.
     * @return ResponseEntity contenant une liste des objets Adresse associés à l'utilisateur et le statut HTTP 200 (OK).
     * Si aucun adresse n'est associée à cet utilisateur, une liste vide sera retournée avec le statut 200 (OK).
     */
    @GetMapping("/utilisateur/{idUtilisateur}")
    public ResponseEntity<List<Adresse>> getAdressesByUtilisateurId(@PathVariable UUID idUtilisateur) {
        List<Adresse> adresses = adresseService.getAdressesByUtilisateurId(idUtilisateur);
        return new ResponseEntity<>(adresses, HttpStatus.OK);
    }

    /**
     * Endpoint pour récupérer l'adresse associée à un événement spécifique.
     *
     * @param idEvenement L'identifiant unique de l'événement dont on souhaite récupérer l'adresse.
     * @return ResponseEntity contenant l'objet Adresse associé à l'événement et le statut HTTP 200 (OK),
     * ou le statut HTTP 404 (NOT FOUND) si aucune adresse n'est trouvée pour cet événement.
     */
    @GetMapping("/evenement/{idEvenement}")
    public ResponseEntity<Adresse> getAdresseByEvenement(@PathVariable Long idEvenement) {
        Evenement evenement = new Evenement();
        evenement.setIdEvenement(idEvenement);
        try {
            Adresse adresse = adresseService.getAdresseByEvenement(evenement);
            return new ResponseEntity<>(adresse, HttpStatus.OK);
        } catch (fr.studi.bloc3jo2024.exception.ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Endpoint pour récupérer toutes les adresses liées à un événement spécifique.
     *
     * @param idEvenement L'identifiant unique de l'événement dont on souhaite récupérer les adresses liées.
     * @return ResponseEntity contenant une liste des objets Adresse liés à l'événement et le statut HTTP 200 (OK).
     * Si aucune adresse n'est liée à cet événement, une liste vide sera retournée avec le statut 200 (OK).
     */
    @GetMapping("/evenement/{idEvenement}/all")
    public ResponseEntity<List<Adresse>> getAdressesByEvenement(@PathVariable Long idEvenement) {
        Evenement evenement = new Evenement();
        evenement.setIdEvenement(idEvenement);
        List<Adresse> adresses = adresseService.getAdressesByEvenement(evenement);
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
     * @return ResponseEntity contenant l'objet Adresse trouvé et le statut HTTP 200 (OK),
     * ou le statut HTTP 404 (NOT FOUND) si aucune adresse correspondante n'est trouvée.
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
     * Endpoint pour rechercher les adresses par ville pour les événements.
     *
     * @param ville Le nom de la ville pour laquelle rechercher les adresses.
     * @return ResponseEntity contenant une liste des objets Adresse situés dans la ville spécifiée et le statut HTTP 200 (OK).
     * Si aucune adresse n'est trouvée dans cette ville, une liste vide sera retournée avec le statut 200 (OK).
     */
    @GetMapping("/ville/{ville}")
    public ResponseEntity<List<Adresse>> rechercherAdressesParVillePourEvenements(@PathVariable String ville) {
        List<Adresse> adresses = adresseService.rechercherAdressesParVillePourEvenements(ville);
        return new ResponseEntity<>(adresses, HttpStatus.OK);
    }

    /**
     * Endpoint pour rechercher les adresses pour un événement spécifique et filtrées par l'ID du pays.
     *
     * @param idEvenement L'identifiant unique de l'événement.
     * @param idPays      L'identifiant unique du pays des adresses à rechercher.
     * @return ResponseEntity contenant une liste des objets Adresse liés à l'événement et appartenant au pays spécifié,
     * et le statut HTTP 200 (OK). Si aucune adresse correspondante n'est trouvée, une liste vide sera retournée
     * avec le statut 200 (OK).
     * Note : La création d'un objet Evenement temporaire ici n'est pas idéale pour une API robuste.
     */
    @GetMapping("/evenement/{idEvenement}/pays/{idPays}")
    public ResponseEntity<List<Adresse>> rechercherAdressesParEvenementEtPays(
            @PathVariable Long idEvenement,
            @PathVariable Long idPays
    ) {
        Evenement evenement = new Evenement();
        evenement.setIdEvenement(idEvenement); // Potentiellement à refactorer pour une meilleure gestion des entités liées.
        List<Adresse> adresses = adresseService.rechercherAdressesParEvenementEtPays(evenement, idPays);
        return new ResponseEntity<>(adresses, HttpStatus.OK);
    }

    /**
     * Endpoint pour mettre à jour une adresse existante.
     *
     * @param id              L'identifiant unique de l'adresse à mettre à jour, passé dans le chemin de l'URI.
     * @param nouvelleAdresse L'objet Adresse contenant les nouvelles informations, passé dans le corps de la requête au format JSON.
     * @return ResponseEntity contenant l'objet Adresse mis à jour et le statut HTTP 200 (OK),
     * ou le statut HTTP 404 (NOT FOUND) si aucune adresse n'est trouvée avec cet ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Adresse> updateAdresse(@PathVariable Long id, @RequestBody Adresse nouvelleAdresse) {
        try {
            Adresse adresseMiseAJour = adresseService.updateAdresse(id, nouvelleAdresse);
            return new ResponseEntity<>(adresseMiseAJour, HttpStatus.OK);
        } catch (fr.studi.bloc3jo2024.exception.ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Endpoint pour supprimer une adresse par son ID.
     *
     * @param id L'identifiant unique de l'adresse à supprimer, passé dans le chemin de l'URI.
     * @return ResponseEntity avec le statut HTTP 204 (NO CONTENT) si la suppression a réussi,
     * ou le statut HTTP 404 (NOT FOUND) si aucune adresse n'est trouvée avec cet ID,
     * ou le statut HTTP 409 (CONFLICT) si l'adresse est liée à un événement et ne peut pas être supprimée.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdresse(@PathVariable Long id) {
        try {
            adresseService.deleteAdresse(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Suppression réussie, pas de contenu à retourner.
        } catch (fr.studi.bloc3jo2024.exception.ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (fr.studi.bloc3jo2024.exception.AdresseLieeAUnEvenementException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT); // L'adresse est liée à un événement.
        }
    }
}