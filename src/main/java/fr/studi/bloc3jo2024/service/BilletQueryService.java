package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.entity.Billet;
import fr.studi.bloc3jo2024.exception.BilletNotFoundException;
import fr.studi.bloc3jo2024.repository.BilletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service dédié aux opérations de lecture (requêtes) sur les entités Billet.
 * Permet de séparer les responsabilités et d'éviter les dépendances circulaires
 * lors de l'invocation de méthodes transactionnelles par Spring AOP.
 */
@Service
@RequiredArgsConstructor
public class BilletQueryService {

    private final BilletRepository billetRepository;

    /**
     * Récupère un billet par son identifiant.
     * La transaction est en lecture seule pour optimiser les performances.
     * @param id L'identifiant du billet.
     * @return Un Optional contenant le billet si trouvé, vide sinon.
     */
    @Transactional(readOnly = true)
    public Optional<Billet> recupererBilletParId(Long id) {
        return billetRepository.findById(id);
    }

    /**
     * Récupère un billet par sa clé finale unique.
     * Lève une BilletNotFoundException si aucun billet n'est trouvé avec la clé spécifiée.
     * La transaction est en lecture seule.
     * @param cleFinaleBillet La clé finale unique du billet.
     * @return Le billet correspondant à la clé.
     * @throws BilletNotFoundException si le billet n'existe pas.
     */
    @Transactional(readOnly = true)
    public Billet recupererBilletParCleFinale(String cleFinaleBillet) {
        return billetRepository.findByCleFinaleBillet(cleFinaleBillet)
                .orElseThrow(() -> new BilletNotFoundException("Billet non trouvé avec la clé : " + cleFinaleBillet));
    }

    /**
     * Récupère une liste de toutes les clés de billets valides (non scannées)
     * pour la synchronisation en mode hors-ligne.
     * La transaction est en lecture seule.
     * @return Une liste de chaînes de caractères représentant les clés valides.
     */
    @Transactional(readOnly = true)
    public List<String> getClesBilletsValides() {
        return billetRepository.findAllValidTicketKeys();
    }
}