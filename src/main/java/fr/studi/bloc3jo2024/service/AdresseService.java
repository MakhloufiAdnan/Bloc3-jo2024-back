package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Pays;
import fr.studi.bloc3jo2024.exception.AdresseLieeAUneDisciplineException;
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
     * Si une adresse identique est trouvée, retourne l'objet Adresse existant.
     * Si aucune adresse identique n'est trouvée, crée et persiste la nouvelle adresse.
     *
     * @param adresse L'objet {@link Adresse} à créer.
     * @return L'objet {@link Adresse} créé et persisté ou l'objet {@link Adresse} existant.
     */
    public Adresse creerAdresseSiNonExistante(Adresse adresse) {
        Optional<Adresse> adresseExistante = adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        );
        return adresseExistante.orElseGet(() -> adresseRepository.save(adresse));
    }

    /**
     * Récupère une adresse par son identifiant unique.
     *
     * @param id L'identifiant unique de l'adresse à récupérer.
     * @return L'objet {@link Adresse} trouvé.
     * @throws ResourceNotFoundException Si aucune adresse n'est trouvée avec l'ID spécifié.
     */
    @Transactional(readOnly = true)
    public Adresse getAdresseById(Long id) {
        return adresseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ADRESSE_NON_TROUVEE + id));
    }

    /**
     * Récupère toutes les adresses enregistrées dans la base de données.
     *
     * @return Une liste contenant tous les objets {@link Adresse}.
     */
    @Transactional(readOnly = true)
    public List<Adresse> getAllAdresses() {
        return adresseRepository.findAll();
    }

    /**
     * Récupère les adresses associées à un utilisateur spécifique en utilisant son identifiant unique.
     *
     * @param idUtilisateur L'UUID de l'utilisateur dont on souhaite récupérer les adresses.
     * @return Une liste des objets {@link Adresse} associés à l'utilisateur.
     */
    @Transactional(readOnly = true)
    public List<Adresse> getAdressesByUtilisateurId(UUID idUtilisateur) {
        return adresseRepository.findByUtilisateurs_IdUtilisateur(idUtilisateur);
    }

    /**
     * Récupère l'adresse associée à une discipline spécifique.
     *
     * @param discipline L'objet {@link Discipline} pour lequel on souhaite récupérer l'adresse.
     * @return L'objet {@link Adresse} associé à la discipline.
     * @throws ResourceNotFoundException Si aucune adresse n'est trouvée pour la discipline spécifiée.
     */
    @Transactional(readOnly = true)
    public Adresse getAdresseByDiscipline(Discipline discipline) {
        return adresseRepository.findByDisciplines(discipline)
                .orElseThrow(() -> new ResourceNotFoundException("Adresse non trouvée pour la discipline avec l'ID : " + discipline.getIdDiscipline()));
    }

    /**
     * Récupère toutes les adresses liées à une discipline spécifique.
     *
     * @param discipline L'objet {@link Discipline} pour lequel récupérer les adresses liées.
     * @return Une liste des objets {@link Adresse} liés à cette discipline.
     */
    @Transactional(readOnly = true)
    public List<Adresse> getAdressesByDiscipline(Discipline discipline) {
        return adresseRepository.findByDisciplinesContaining(discipline);
    }

    /**
     * Vérifie si une adresse existe déjà en se basant sur ses attributs uniques.
     *
     * @param adresse L'objet {@link Adresse} à vérifier.
     * @return {@code true} si une adresse identique existe, {@code false} sinon.
     */
    @Transactional(readOnly = true)
    public boolean adresseExisteDeja(Adresse adresse) {
        return adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        ).isPresent();
    }

    /**
     * Récupère l'identifiant unique d'une adresse existante si elle correspond aux informations fournies.
     *
     * @param adresse L'objet {@link Adresse} à rechercher.
     * @return L'identifiant unique de l'adresse existante, ou {@code null} si aucune correspondance n'est trouvée.
     */
    @Transactional(readOnly = true)
    public Long getIdAdresseSiExistante(Adresse adresse) {
        return adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        ).map(Adresse::getIdAdresse).orElse(null);
    }

    /**
     * Vérifie si une adresse est actuellement liée à au moins une discipline.
     *
     * @param idAdresse L'identifiant unique de l'adresse à vérifier.
     * @return {@code true} si l'adresse est liée à une discipline, {@code false} sinon.
     */
    @Transactional(readOnly = true)
    public boolean isAdresseLieeAUnDiscipline(Long idAdresse) {
        return adresseRepository.isAdresseLieeAUnDiscipline(idAdresse);
    }

    /**
     * Recherche une adresse complète en fonction de tous ses attributs.
     *
     * @param numeroRue  Le numéro de la rue.
     * @param nomRue     Le nom de la rue.
     * @param ville      La ville.
     * @param codePostal Le code postal.
     * @param pays       L'objet {@link Pays} associé à l'adresse.
     * @return L'objet {@link Adresse} trouvé, ou {@code null} si aucune correspondance n'est trouvée.
     */
    @Transactional(readOnly = true)
    public Adresse rechercherAdresseComplete(int numeroRue, String nomRue, String ville, String codePostal, Pays pays) {
        return adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(numeroRue, nomRue, ville, codePostal, pays)
                .orElse(null);
    }

    /**
     * Recherche les adresses par ville. Cette méthode est utilisée pour trouver des adresses potentielles
     * pour des disciplines basées sur leur localisation.
     *
     * @param ville Le nom de la ville pour laquelle rechercher les adresses.
     * @return Une liste des objets {@link Adresse} situés dans la ville spécifiée.
     */
    @Transactional(readOnly = true)
    public List<Adresse> rechercherAdressesParVillePourDisciplines(String ville) {
        return adresseRepository.findByVille(ville);
    }

    /**
     * Recherche les adresses liées à une discipline spécifique et filtrées par l'identifiant unique du pays.
     *
     * @param discipline L'objet {@link Discipline} pour lequel rechercher les adresses.
     * @param idPays     L'identifiant unique du pays des adresses à rechercher.
     * @return Une liste des objets {@link Adresse} liés à la discipline et appartenant au pays spécifié.
     */
    @Transactional(readOnly = true)
    public List<Adresse> rechercherAdressesParDisciplineEtPays(Discipline discipline, Long idPays) {
        return adresseRepository.findByDisciplinesAndPays_IdPays(discipline, idPays);
    }

    /**
     * Met à jour les informations d'une adresse existante.
     *
     * @param id              L'identifiant unique de l'adresse à mettre à jour.
     * @param nouvelleAdresse L'objet {@link Adresse} contenant les nouvelles informations.
     * @return L'objet {@link Adresse} mis à jour et persisté.
     * @throws ResourceNotFoundException Si aucune adresse n'est trouvée avec l'ID spécifié.
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
     * Supprime une adresse de la base de données en vérifiant au préalable qu'elle n'est pas liée à une discipline.
     *
     * @param id L'identifiant unique de l'adresse à supprimer.
     * @throws ResourceNotFoundException        Si aucune adresse n'est trouvée avec l'ID spécifié.
     * @throws AdresseLieeAUneDisciplineException Si l'adresse est liée à une ou plusieurs disciplines.
     */
    public void deleteAdresse(Long id) {
        Adresse adresse = adresseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ADRESSE_NON_TROUVEE + id));

        if (adresseRepository.isAdresseLieeAUnDiscipline(id)) {
            throw new AdresseLieeAUneDisciplineException("L'adresse avec l'ID " + id + " est liée à un ou plusieurs événements et ne peut pas être supprimée.");
        }
        adresseRepository.delete(adresse);
    }
}