package fr.bloc_jo2024.service;

import fr.bloc_jo2024.entity.Offre;
import fr.bloc_jo2024.entity.TypeOffre;
import fr.bloc_jo2024.repository.OffreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminOffreService {

    @Autowired
    private OffreRepository offreRepository;

    // Ajouter une nouvelle offre
    public Offre ajouterOffre(Offre nouvelleOffre) {
        return offreRepository.save(nouvelleOffre);
    }

    // Modifier le prix d'une offre
    public Offre modifierPrixOffre(Long idOffre, double nouveauPrix) {
        Optional<Offre> offreOpt = offreRepository.findById(idOffre);
        if (offreOpt.isPresent()) {
            Offre offre = offreOpt.get();
            offre.setPrix(nouveauPrix);  // Mise à jour du prix
            return offreRepository.save(offre);
        }
        return null;  // Si l'offre n'existe pas
    }

    // Supprimer une offre
    public boolean supprimerOffre(Long idOffre) {
        if (offreRepository.existsById(idOffre)) {
            offreRepository.deleteById(idOffre);
            return true;
        }
        return false;
    }

    // Liste des offres par type
    public List<Offre> obtenirOffresParType(TypeOffre typeOffre) {
        return offreRepository.findByTypeOffre(typeOffre);
    }

    // Liste de toutes les offres
    public List<Offre> obtenirToutesLesOffres() {
        return offreRepository.findAll();
    }
}
