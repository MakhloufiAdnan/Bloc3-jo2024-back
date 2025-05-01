package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Evenement;
import fr.studi.bloc3jo2024.entity.Pays;
import fr.studi.bloc3jo2024.exception.AdresseLieeAUnEvenementException;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.AdresseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdresseService {

    private static final String ADRESSE_NON_TROUVEE = "Adresse non trouvée avec l'ID : ";

    private final AdresseRepository adresseRepository;

    /**
     * Crée une nouvelle adresse. Vérifie au préalable si une adresse identique existe déjà.
     * Si une adresse identique est trouvée, retourne l'ID de l'adresse existante.
     * @param adresse L'objet Adresse à créer.
     * @return L'objet Adresse créé et persisté ou l'objet Adresse existant.
     */
    public Adresse creerAdresseSiNonExistante(Adresse adresse) {
        Optional<Adresse> adresseExistante = adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        );
        return adresseExistante.orElseGet(() -> adresseRepository.save(adresse));
    }

    /**
     * Récupère une adresse par son ID.
     * @param id L'ID de l'adresse à récupérer.
     * @return L'objet Adresse trouvé.
     * @throws ResourceNotFoundException Si aucune adresse n'est trouvée avec cet ID.
     */
    @Transactional(readOnly = true)
    public Adresse getAdresseById(Long id) {
        return adresseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ADRESSE_NON_TROUVEE + id));
    }

    /**
     * Récupère toutes les adresses.
     * @return Une liste de toutes les adresses.
     */
    @Transactional(readOnly = true)
    public List<Adresse> getAllAdresses() {
        return adresseRepository.findAll();
    }

    /**
     * Récupère les adresses associées à un utilisateur spécifique.
     * @param idUtilisateur L'UUID de l'utilisateur.
     * @return Une liste des adresses de l'utilisateur.
     */
    @Transactional(readOnly = true)
    public List<Adresse> getAdressesByUtilisateurId(UUID idUtilisateur) {
        return adresseRepository.findByUtilisateurs_IdUtilisateur(idUtilisateur);
    }

    /**
     * Récupère l'adresse associée à un événement spécifique.
     * @param evenement L'objet Evenement.
     * @return L'adresse de l'événement.
     * @throws ResourceNotFoundException Si aucune adresse n'est trouvée pour cet événement.
     */
    @Transactional(readOnly = true)
    public Adresse getAdresseByEvenement(Evenement evenement) {
        return adresseRepository.findByEvenements(evenement)
                .orElseThrow(() -> new ResourceNotFoundException("Adresse non trouvée pour l'événement avec l'ID : " + evenement.getIdEvenement()));
    }

    /**
     * Récupère toutes les adresses liées à un événement spécifique.
     * @param evenement L'objet Evenement pour lequel récupérer les adresses.
     * @return Une liste des objets Adresse liés à cet événement.
     */
    @Transactional(readOnly = true)
    public List<Adresse> getAdressesByEvenement(Evenement evenement) {
        return adresseRepository.findByEvenementsContaining(evenement);
    }

    /**
     * Vérifie si une adresse existe déjà en fonction de ses attributs.
     * @param adresse L'objet Adresse à vérifier.
     * @return true si une adresse identique existe, false sinon.
     */
    @Transactional(readOnly = true)
    public boolean adresseExisteDeja(Adresse adresse) {
        return adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        ).isPresent();
    }

    /**
     * Récupère l'ID d'une adresse existante si elle correspond aux informations fournies.
     * @param adresse L'objet Adresse à rechercher.
     * @return L'ID de l'adresse existante, ou null si aucune correspondance n'est trouvée.
     */
    @Transactional(readOnly = true)
    public Long getIdAdresseSiExistante(Adresse adresse) {
        return adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        ).map(Adresse::getIdAdresse).orElse(null);
    }

    /**
     * Vérifie si une adresse est liée à un événement.
     * @param idAdresse L'ID de l'adresse à vérifier.
     * @return true si l'adresse est liée à un événement, false sinon.
     */
    @Transactional(readOnly = true)
    public boolean isAdresseLieeAUnEvenement(Long idAdresse) {
        return adresseRepository.isAdresseLieeAUnEvenement(idAdresse);
    }

    /**
     * Recherche une adresse complète pour une offre.
     * @param numeroRue Le numéro de la rue.
     * @param nomRue Le nom de la rue.
     * @param ville La ville.
     * @param codePostal Le code postal.
     * @param pays L'objet Pays associé à l'adresse.
     * @return L'objet Adresse trouvé, ou null si non trouvé.
     */
    @Transactional(readOnly = true)
    public Adresse rechercherAdresseComplete(int numeroRue, String nomRue, String ville, String codePostal, Pays pays) {
        return adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(numeroRue, nomRue, ville, codePostal, pays)
                .orElse(null);
    }

    /**
     * Recherche les adresses par ville pour les événements.
     * @param ville Le nom de la ville.
     * @return Une liste d'adresses situées dans la ville spécifiée.
     */
    @Transactional(readOnly = true)
    public List<Adresse> rechercherAdressesParVillePourEvenements(String ville) {
        return adresseRepository.findByVille(ville);
    }

    /**
     * Recherche les adresses pour un événement spécifique et filtrées par l'ID du pays.
     * @param evenement L'objet Evenement.
     * @param idPays L'ID du pays des adresses à rechercher.
     * @return Une liste d'adresses liées à l'événement et appartenant au pays spécifié.
     */
    @Transactional(readOnly = true)
    public List<Adresse> rechercherAdressesParEvenementEtPays(Evenement evenement, Long idPays) {
        return adresseRepository.findByEvenementsAndPays_IdPays(evenement, idPays);
    }

    /**
     * Met à jour une adresse existante.
     * @param id L'ID de l'adresse à mettre à jour.
     * @param nouvelleAdresse L'objet Adresse contenant les nouvelles informations.
     * @return L'objet Adresse mis à jour.
     * @throws ResourceNotFoundException Si aucune adresse n'est trouvée avec cet ID.
     */
    public Adresse updateAdresse(Long id, Adresse nouvelleAdresse) {
        return adresseRepository.findById(id)
                .map(adresseExistante -> {
                    adresseExistante.setNumeroRue(nouvelleAdresse.getNumeroRue());
                    adresseExistante.setNomRue(nouvelleAdresse.getNomRue());
                    adresseExistante.setVille(nouvelleAdresse.getVille());
                    adresseExistante.setCodePostal(nouvelleAdresse.getCodePostal());
                    adresseExistante.setPays(nouvelleAdresse.getPays());
                    return adresseRepository.save(adresseExistante);
                })
                .orElseThrow(() -> new ResourceNotFoundException(ADRESSE_NON_TROUVEE + id));
    }

    /**
     * Supprime une adresse par son ID après avoir vérifié qu'elle n'est pas liée à un événement actif.
     * @param id L'ID de l'adresse à supprimer.
     * @throws ResourceNotFoundException Si aucune adresse n'est trouvée avec cet ID.
     * @throws AdresseLieeAUnEvenementException Si l'adresse est liée à un événement.
     */
    public void deleteAdresse(Long id) {
        Adresse adresse = adresseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ADRESSE_NON_TROUVEE + id));

        if (adresseRepository.isAdresseLieeAUnEvenement(id)) {
            throw new AdresseLieeAUnEvenementException("L'adresse avec l'ID " + id + " est liée à un ou plusieurs événements et ne peut pas être supprimée.");
        }
        adresseRepository.delete(adresse);
    }
}