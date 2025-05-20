package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Pays;
import fr.studi.bloc3jo2024.exception.AdresseLieeAUneDisciplineException;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.AdresseRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdresseService {

    private static final Logger log = LoggerFactory.getLogger(AdresseService.class);
    private static final String ADRESSE_NON_TROUVEE_ID = "Adresse non trouvée avec l'ID : ";

    private final AdresseRepository adresseRepository;

    public Adresse creerAdresseSiNonExistante(Adresse adresse) {
        if (adresse.getPays() == null || adresse.getPays().getIdPays() == null) {
            log.warn("Tentative de création d'adresse avec Pays null ou Pays ID null. Adresse: {}", adresse);
            throw new IllegalArgumentException("L'objet Pays avec son ID est requis pour créer ou vérifier une adresse.");
        }

        Optional<Adresse> adresseExistante = adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        );
        if (adresseExistante.isPresent()) {
            log.info("Adresse existante trouvée avec ID: {}. Retour de l'instance existante.", adresseExistante.get().getIdAdresse());
            return adresseExistante.get();
        } else {
            log.info("Aucune adresse existante trouvée. Création d'une nouvelle adresse pour: {}", adresse);
            return adresseRepository.save(adresse);
        }
    }

    @Transactional(readOnly = true)
    public Adresse getAdresseById(Long id) {
        log.info("Récupération de l'adresse avec ID: {}", id);
        return adresseRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Adresse non trouvée avec l'ID : {}", id);
                    return new ResourceNotFoundException(ADRESSE_NON_TROUVEE_ID + id);
                });
    }

    @Transactional(readOnly = true)
    public Page<Adresse> getAllAdresses(Pageable pageable) {
        log.info("Récupération de toutes les adresses avec pagination: {}", pageable);
        return adresseRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Adresse> getAdressesByUtilisateurId(UUID idUtilisateur) {
        log.info("Récupération des adresses pour l'utilisateur ID: {}", idUtilisateur);
        return adresseRepository.findByUtilisateurs_IdUtilisateur(idUtilisateur);
    }

    @Transactional(readOnly = true)
    public Adresse getAdresseByDiscipline(Discipline discipline) {
        if (discipline == null || discipline.getIdDiscipline() == null) {
            throw new IllegalArgumentException("L'objet Discipline avec son ID est requis.");
        }
        log.info("Récupération de l'adresse pour la discipline ID: {}", discipline.getIdDiscipline());
        return adresseRepository.findByDisciplines(discipline)
                .orElseThrow(() -> new ResourceNotFoundException("Adresse non trouvée pour la discipline avec l'ID : " + discipline.getIdDiscipline()));
    }

    @Transactional(readOnly = true)
    public List<Adresse> getAdressesByDiscipline(Discipline discipline) {
        if (discipline == null || discipline.getIdDiscipline() == null) {
            throw new IllegalArgumentException("L'objet Discipline avec son ID est requis.");
        }
        log.info("Récupération de toutes les adresses pour la discipline ID: {}", discipline.getIdDiscipline());
        return adresseRepository.findByDisciplinesContaining(discipline);
    }

    @Transactional(readOnly = true)
    public boolean adresseExisteDeja(Adresse adresse) {
        if (adresse.getPays() == null || adresse.getPays().getIdPays() == null) {
            log.warn("Tentative de vérification d'existence d'adresse avec Pays null ou Pays ID null. Adresse: {}", adresse);
            return false;
        }
        return adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        ).isPresent();
    }

    @Transactional(readOnly = true)
    public Long getIdAdresseSiExistante(Adresse adresse) {
        if (adresse.getPays() == null || adresse.getPays().getIdPays() == null) {
            log.warn("Tentative de récupération d'ID d'adresse existante avec Pays null ou Pays ID null. Adresse: {}", adresse);
            return null;
        }
        return adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        ).map(Adresse::getIdAdresse).orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean isAdresseLieeAUnDiscipline(Long idAdresse) {
        return adresseRepository.isAdresseLieeAUnDiscipline(idAdresse);
    }

    @Transactional(readOnly = true)
    public Adresse rechercherAdresseComplete(Integer numeroRue, String nomRue, String ville, String codePostal, Pays pays) {
        if (pays == null || pays.getIdPays() == null) {
            throw new IllegalArgumentException("L'objet Pays avec son ID est requis pour la recherche d'adresse complète.");
        }
        return adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(numeroRue, nomRue, ville, codePostal, pays)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Adresse> rechercherAdressesParVillePourDisciplines(String ville) {
        return adresseRepository.findByVille(ville);
    }

    @Transactional(readOnly = true)
    public List<Adresse> rechercherAdressesParDisciplineEtPays(Discipline discipline, Long idPays) {
        if (discipline == null || discipline.getIdDiscipline() == null || idPays == null) {
            throw new IllegalArgumentException("Discipline (avec ID) et ID Pays sont requis.");
        }
        return adresseRepository.findByDisciplinesAndPays_IdPays(discipline, idPays);
    }

    public Adresse updateAdresse(Long id, Adresse nouvelleAdresse) {
        if (nouvelleAdresse.getPays() == null || nouvelleAdresse.getPays().getIdPays() == null) {
            throw new IllegalArgumentException("L'objet Pays avec son ID est requis pour la mise à jour de l'adresse.");
        }

        return adresseRepository.findById(id)
                .map(adresseExistante -> {
                    adresseExistante.setNumeroRue(nouvelleAdresse.getNumeroRue());
                    adresseExistante.setNomRue(nouvelleAdresse.getNomRue());
                    adresseExistante.setVille(nouvelleAdresse.getVille());
                    adresseExistante.setCodePostal(nouvelleAdresse.getCodePostal());
                    adresseExistante.setPays(nouvelleAdresse.getPays());
                    log.info("Mise à jour de l'adresse ID: {}", id);
                    return adresseRepository.save(adresseExistante);
                })
                .orElseThrow(() -> new ResourceNotFoundException(ADRESSE_NON_TROUVEE_ID + id));
    }

    public void deleteAdresse(Long id) {
        Adresse adresse = adresseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ADRESSE_NON_TROUVEE_ID + id));

        if (adresseRepository.isAdresseLieeAUnDiscipline(id)) {
            log.warn("Tentative de suppression de l'adresse ID {} échouée car elle est liée à une discipline.", id);
            throw new AdresseLieeAUneDisciplineException("L'adresse avec l'ID " + id + " est liée à un ou plusieurs événements et ne peut pas être supprimée.");
        }
        log.info("Suppression de l'adresse ID: {}", id);
        adresseRepository.delete(adresse);
    }
}