package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Pays;
import fr.studi.bloc3jo2024.exception.AdresseLieeAUneDisciplineException;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.AdresseRepository;
import fr.studi.bloc3jo2024.repository.PaysRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdresseService {

    private static final Logger log = LoggerFactory.getLogger(AdresseService.class);
    private static final String ADRESSE_NON_TROUVEE_ID = "Adresse non trouvée avec l'ID : ";
    private static final String PAYS_NON_TROUVE_ID = "Pays non trouvé avec l'ID : ";


    private final AdresseRepository adresseRepository;
    private final PaysRepository paysRepository;

    /**
     * Crée une nouvelle adresse si elle n'existe pas déjà (basé sur ses attributs uniques).
     * @param adresse L'objet Adresse à créer.
     * @return L'adresse créée ou existante.
     * @throws IllegalArgumentException si les informations du pays dans l'adresse fournie sont invalides ou manquantes.
     */
    public Adresse creerAdresseSiNonExistante(Adresse adresse) {
        if (adresse.getPays() == null || adresse.getPays().getIdPays() == null) {
            log.warn("Tentative de création/vérification d'adresse sans informations de pays valides.");
            throw new IllegalArgumentException("Les informations du pays (avec ID) sont requises pour créer ou vérifier une adresse.");
        }

        Optional<Adresse> adresseExistante = adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        );

        if (adresseExistante.isPresent()) {
            log.info("Adresse existante trouvée avec ID : {}", adresseExistante.get().getIdAdresse());
            return adresseExistante.get();
        } else {
            log.info("Aucune adresse existante trouvée. Création d'une nouvelle adresse.");
            return adresseRepository.save(adresse);
        }
    }

    /**
     * Récupère une adresse par son ID.
     * @param id L'ID de l'adresse.
     * @return L'adresse trouvée.
     * @throws ResourceNotFoundException si non trouvée.
     */
    @Transactional(readOnly = true)
    public Adresse getAdresseById(Long id) {
        return adresseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ADRESSE_NON_TROUVEE_ID + id));
    }

    /**
     * Récupère toutes les adresses, avec pagination.
     * @param pageable L'objet de pagination.
     * @return Une page de toutes les adresses.
     */
    @Transactional(readOnly = true)
    public Page<Adresse> getAllAdresses(Pageable pageable) {
        return adresseRepository.findAll(pageable);
    }

    /**
     * Récupère les adresses associées à un utilisateur.
     * @param idUtilisateur L'ID de l'utilisateur.
     * @return Liste des adresses de l'utilisateur.
     */
    @Transactional(readOnly = true)
    public List<Adresse> getAdressesByUtilisateurId(UUID idUtilisateur) {
        return adresseRepository.findByUtilisateurs_IdUtilisateur(idUtilisateur);
    }

    /**
     * Récupère l'adresse associée à une discipline.
     * @param discipline L'entité Discipline.
     * @return L'adresse trouvée.
     * @throws ResourceNotFoundException si non trouvée.
     * @throws IllegalArgumentException si l'objet discipline ou son ID est null.
     */
    @Transactional(readOnly = true)
    public Adresse getAdresseByDiscipline(Discipline discipline) {
        if (discipline == null || discipline.getIdDiscipline() == null) {
            throw new IllegalArgumentException("L'objet Discipline et son ID ne peuvent pas être null.");
        }
        return adresseRepository.findByDisciplines(discipline)
                .orElseThrow(() -> new ResourceNotFoundException("Adresse non trouvée pour la discipline avec l'ID : " + discipline.getIdDiscipline()));
    }

    /**
     * Récupère toutes les adresses liées à une discipline.
     * @param discipline L'entité Discipline.
     * @return Liste des adresses liées.
     * @throws IllegalArgumentException si l'objet discipline ou son ID est null.
     */
    @Transactional(readOnly = true)
    public List<Adresse> getAdressesByDiscipline(Discipline discipline) {
        if (discipline == null || discipline.getIdDiscipline() == null) {
            throw new IllegalArgumentException("L'objet Discipline et son ID ne peuvent pas être null.");
        }
        return adresseRepository.findByDisciplinesContaining(discipline);
    }


    /**
     * Vérifie si une adresse (par ses attributs) existe déjà.
     * @param adresse L'objet Adresse à vérifier.
     * @return true si elle existe, false sinon.
     */
    @Transactional(readOnly = true)
    public boolean adresseExisteDeja(Adresse adresse) {
        if (adresse.getPays() == null || adresse.getPays().getIdPays() == null) {
            log.debug("Vérification d'existence d'adresse avec informations de pays manquantes.");
            return false;
        }
        return adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        ).isPresent();
    }

    /**
     * Récupère l'ID d'une adresse si elle existe (basé sur ses attributs).
     * @param adresse L'objet Adresse à rechercher.
     * @return L'ID de l'adresse existante, ou null.
     */
    @Transactional(readOnly = true)
    public Long getIdAdresseSiExistante(Adresse adresse) {
        if (adresse.getPays() == null || adresse.getPays().getIdPays() == null) {
            log.debug("Tentative de récupération d'ID d'adresse avec informations de pays manquantes.");
            return null; // Ou lever une exception
        }
        return adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        ).map(Adresse::getIdAdresse).orElse(null);
    }


    /**
     * Vérifie si une adresse est liée à au moins une discipline.
     * @param idAdresse L'ID de l'adresse.
     * @return true si liée, false sinon.
     */
    @Transactional(readOnly = true)
    public boolean isAdresseLieeAUnDiscipline(Long idAdresse) {
        return adresseRepository.isAdresseLieeAUnDiscipline(idAdresse);
    }

    /**
     * Recherche une adresse par ses attributs complets.
     * @param numeroRue Numéro de rue.
     * @param nomRue Nom de rue.
     * @param ville Ville.
     * @param codePostal Code postal.
     * @param pays Pays.
     * @return L'adresse trouvée, ou null.
     */
    @Transactional(readOnly = true)
    public Adresse rechercherAdresseComplete(Integer numeroRue, String nomRue, String ville, String codePostal, Pays pays) {
        if (pays == null || pays.getIdPays() == null) {
            throw new IllegalArgumentException("L'objet Pays avec son ID est requis pour la recherche d'adresse complète.");
        }
        return adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(numeroRue, nomRue, ville, codePostal, pays)
                .orElse(null);
    }

    /**
     * Recherche les adresses par ville (utilisé pour les disciplines).
     * @param ville La ville.
     * @return Liste des adresses.
     */
    @Transactional(readOnly = true)
    public List<Adresse> rechercherAdressesParVillePourDisciplines(String ville) {
        return adresseRepository.findByVille(ville);
    }

    /**
     * Recherche les adresses par discipline et ID de pays.
     * @param discipline La discipline.
     * @param idPays L'ID du pays.
     * @return Liste des adresses.
     */
    @Transactional(readOnly = true)
    public List<Adresse> rechercherAdressesParDisciplineEtPays(Discipline discipline, Long idPays) {
        return adresseRepository.findByDisciplinesAndPays_IdPays(discipline, idPays);
    }

    /**
     * Met à jour une adresse existante.
     * @param id L'ID de l'adresse à mettre à jour.
     * @param nouvelleAdresse Les nouvelles informations de l'adresse.
     * @return L'adresse mise à jour.
     * @throws ResourceNotFoundException si l'adresse n'est pas trouvée.
     */
    public Adresse updateAdresse(Long id, Adresse nouvelleAdresse) {
        Adresse adresseExistante = adresseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ADRESSE_NON_TROUVEE_ID + id));

        adresseExistante.setNumeroRue(nouvelleAdresse.getNumeroRue());
        adresseExistante.setNomRue(nouvelleAdresse.getNomRue());
        adresseExistante.setVille(nouvelleAdresse.getVille());
        adresseExistante.setCodePostal(nouvelleAdresse.getCodePostal());

        if (nouvelleAdresse.getPays() != null && nouvelleAdresse.getPays().getIdPays() != null) {
            Pays paysManaged = paysRepository.findById(nouvelleAdresse.getPays().getIdPays())
                    .orElseThrow(() -> new ResourceNotFoundException(PAYS_NON_TROUVE_ID + nouvelleAdresse.getPays().getIdPays()));
            adresseExistante.setPays(paysManaged);
        } else {
            throw new IllegalArgumentException("L'ID du pays est requis pour mettre à jour l'adresse.");
        }

        return adresseRepository.save(adresseExistante);
    }

    /**
     * Supprime une adresse par son ID, si elle n'est pas liée à une discipline.
     * @param id L'ID de l'adresse à supprimer.
     * @throws ResourceNotFoundException si l'adresse n'est pas trouvée.
     * @throws AdresseLieeAUneDisciplineException si l'adresse est liée à une discipline.
     */
    public void deleteAdresse(Long id) {
        Adresse adresse = adresseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ADRESSE_NON_TROUVEE_ID + id));

        if (adresseRepository.isAdresseLieeAUnDiscipline(id)) {
            throw new AdresseLieeAUneDisciplineException("L'adresse avec l'ID " + id + " est liée à une ou plusieurs disciplines et ne peut pas être supprimée.");
        }
        adresseRepository.delete(adresse);
        log.info("Adresse ID {} supprimée avec succès.", id);
    }
}
