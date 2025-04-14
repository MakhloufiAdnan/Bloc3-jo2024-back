package fr.bloc_jo2024.service;

import fr.bloc_jo2024.entity.Evenement;
import fr.bloc_jo2024.repository.EvenementRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class EvenementService {

    private final EvenementRepository evenementRepository;

    public EvenementService(EvenementRepository evenementRepository) {
        this.evenementRepository = evenementRepository;
    }

    /**
     * Réserve des places en les retirant de l'événement.
     * @param idEvenement ID de l'événement concerné
     * @param nb Nombre de places à retirer
     * @return L'événement mis à jour
     */
    public Evenement retirerPlaces(Long idEvenement, int nb) {
        Evenement evenement = getEvenementOrThrow(idEvenement);

        if (nb <= 0) throw new IllegalArgumentException("Le nombre à retirer doit être positif.");
        if (nb > evenement.getNbPlaceDispo()) throw new IllegalArgumentException("Pas assez de places disponibles.");

        evenement.setNbPlaceDispo(evenement.getNbPlaceDispo() - nb);
        return evenementRepository.save(evenement);
    }

    /**
     * Ajoute des places à un événement.
     * @param idEvenement ID de l'événement concerné
     * @param nb Nombre de places à ajouter
     * @return L'événement mis à jour
     */
    public Evenement ajouterPlaces(Long idEvenement, int nb) {
        Evenement evenement = getEvenementOrThrow(idEvenement);

        if (nb <= 0) throw new IllegalArgumentException("Le nombre à ajouter doit être positif.");

        evenement.setNbPlaceDispo(evenement.getNbPlaceDispo() + nb);
        return evenementRepository.save(evenement);
    }

    /**
     * Met à jour la date d'un événement s'il est à venir.
     * @param idEvenement ID de l'événement
     * @param nouvelleDate Nouvelle date à appliquer
     * @return L'événement mis à jour
     */
    public Evenement updateDate(Long idEvenement, LocalDateTime nouvelleDate) {
        if (nouvelleDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La nouvelle date ne peut pas être dans le passé.");
        }

        Evenement evenement = getEvenementOrThrow(idEvenement);
        evenement.setDateEvenement(nouvelleDate);
        return evenementRepository.save(evenement);
    }

    // Méthode utilitaire pour éviter de répéter la récupération.
    private Evenement getEvenementOrThrow(Long id) {
        return evenementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Événement non trouvé avec l'id " + id));
    }


    // Méthodes de recherche d'événements à venir
    public List<Evenement> getEvenementsAvenir() {
        return evenementRepository.findByDateEvenementAfter(LocalDateTime.now());
    }

    // Méthodes de recherche d'événements par ville
    public List<Evenement> getEvenementsByVille(String ville) {
        return evenementRepository.findEvenementsByVille(ville);
    }

    // Méthodes de recherche d'événements par épreuve
    public List<Evenement> getEvenementsByEpreuve(Long idEpreuve) {
        return evenementRepository.findEvenementsByEpreuveId(idEpreuve);
    }

    // Méthode pour obtenir les événements à une date donnée
    public List<Evenement> getEvenementsByDate(LocalDateTime dateEvenement) {
        return evenementRepository.findEvenementsByDateEvenement(dateEvenement);
    }
}