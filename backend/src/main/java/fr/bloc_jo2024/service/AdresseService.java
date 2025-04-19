package fr.bloc_jo2024.service;

import fr.bloc_jo2024.entity.Adresse;
import fr.bloc_jo2024.exception.AdresseLieeAUnEvenementException;
import fr.bloc_jo2024.exception.ResourceNotFoundException;
import fr.bloc_jo2024.repository.AdresseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdresseService {

    private static final String ADRESSE_NON_TROUVEE = "Adresse non trouvée avec l'ID : ";

    private final AdresseRepository adresseRepository;

    /**
     * Crée une nouvelle adresse.
     * @param adresse L'objet Adresse à créer.
     * @return L'objet Adresse créé et persisté.
     */
    public Adresse creerAdresse(Adresse adresse) {
        return adresseRepository.save(adresse);
    }

    /**
     * Récupère une adresse par son ID.
     * @param id L'ID de l'adresse à récupérer.
     * @return L'objet Adresse trouvé.
     * @throws ResourceNotFoundException Si aucune adresse n'est trouvée avec cet ID.
     */
    public Adresse getAdresseById(Long id) {
        return adresseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ADRESSE_NON_TROUVEE + id));
    }

    /**
     * Récupère toutes les adresses.
     * @return Une liste de tous les objets Adresse.
     */
    public List<Adresse> getAllAdresses() {
        return adresseRepository.findAll();
    }

    /**
     * Met à jour une adresse existante.
     * @param id L'ID de l'adresse à mettre à jour.
     * @param nouvelleAdresse L'objet Adresse contenant les nouvelles informations.
     * @return L'objet Adresse mis à jour.
     * @throws ResourceNotFoundException Si aucune adresse n'est trouvée avec cet ID.
     */
    public Adresse updateAdresse(Long id, Adresse nouvelleAdresse) {
        Adresse adresseExistante = adresseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ADRESSE_NON_TROUVEE + id));

        adresseExistante.setNumeroRue(nouvelleAdresse.getNumeroRue());
        adresseExistante.setNomRue(nouvelleAdresse.getNomRue());
        adresseExistante.setVille(nouvelleAdresse.getVille());
        adresseExistante.setCodePostale(nouvelleAdresse.getCodePostale());
        adresseExistante.setPays(nouvelleAdresse.getPays());

        return adresseRepository.save(adresseExistante);
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

        try {
            if (adresseRepository.adresseEstLieeAUnEvenement(id)) {
                throw new AdresseLieeAUnEvenementException();
            }
        } catch (Exception e) {
            throw AdresseLieeAUnEvenementException.erreurVerification(e);
        }
        adresseRepository.delete(adresse);
    }
}