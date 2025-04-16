package fr.bloc_jo2024.service;

import fr.bloc_jo2024.entity.Offre;
import fr.bloc_jo2024.entity.Panier;
import fr.bloc_jo2024.entity.ContenuPanier;
import fr.bloc_jo2024.entity.ContenuPanierId;
import fr.bloc_jo2024.exception.ResourceNotFoundException;
import fr.bloc_jo2024.repository.ContenuPanierRepository;
import fr.bloc_jo2024.repository.PanierRepository;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

@Service
public class PanierService {

    private static final String PANIER_NON_TROUVE = "Panier non trouvé avec l'ID : ";

    private final PanierRepository panierRepository;
    private final ContenuPanierRepository contenuPanierRepository;

    public PanierService(PanierRepository panierRepository, ContenuPanierRepository contenuPanierRepository) {
        this.panierRepository = panierRepository;
        this.contenuPanierRepository = contenuPanierRepository;
    }

    @Transactional
    public void recalculerMontantTotal(Panier panier) {
        double total = panier.getContenuPaniers().stream()
                .mapToDouble(contenuPanier -> contenuPanier.getOffre().getPrix() * contenuPanier.getQuantiteCommandee())
                .sum();

        if (total < 0) {
            throw new IllegalArgumentException("Le montant ne peut pas être négatif.");
        }

        panier.setMontantTotal(total);
        panierRepository.save(panier);
    }

    @Transactional
    public void ajouterOffreAuPanier(Long panierId, Offre offre, int quantite) {
        Panier panier = panierRepository.findById(panierId)
                .orElseThrow(() -> new ResourceNotFoundException(PANIER_NON_TROUVE + panierId));

        ContenuPanierId key = new ContenuPanierId(panierId, offre.getIdOffre());
        ContenuPanier contenuPanier = contenuPanierRepository.findById(key).orElse(null);

        if (contenuPanier == null) {
            contenuPanier = new ContenuPanier();
            contenuPanier.setPanier(panier);
            contenuPanier.setOffre(offre);
            contenuPanier.setQuantiteCommandee(quantite);
            contenuPanierRepository.save(contenuPanier);
        } else {
            contenuPanier.setQuantiteCommandee(contenuPanier.getQuantiteCommandee() + quantite);
            contenuPanierRepository.save(contenuPanier);
        }

        recalculerMontantTotal(panier);
    }

    @Transactional
    public void mettreAJourQuantiteOffrePanier(Long panierId, Offre offre, int nouvelleQuantite) {
        Panier panier = panierRepository.findById(panierId)
                .orElseThrow(() -> new ResourceNotFoundException(PANIER_NON_TROUVE + panierId));

        ContenuPanierId key = new ContenuPanierId(panierId, offre.getIdOffre());
        ContenuPanier contenuPanier = contenuPanierRepository.findById(key)
                .orElseThrow(() -> new ResourceNotFoundException("L'offre avec l'ID " + offre.getIdOffre() + " n'est pas dans le panier avec l'ID " + panierId));

        contenuPanier.setQuantiteCommandee(nouvelleQuantite);
        contenuPanierRepository.save(contenuPanier);

        recalculerMontantTotal(panier);
    }

    @Transactional
    public void retirerOffreDuPanier(Long panierId, Offre offre) {
        Panier panier = panierRepository.findById(panierId)
                .orElseThrow(() -> new ResourceNotFoundException(PANIER_NON_TROUVE + panierId));

        ContenuPanierId key = new ContenuPanierId(panierId, offre.getIdOffre());
        if (contenuPanierRepository.existsById(key)) {
            contenuPanierRepository.deleteById(key);
            recalculerMontantTotal(panier);
        }
    }

    @Transactional
    public void supprimerOffreDeTousLesPaniers(Offre offre) {
        contenuPanierRepository.deleteByOffre(offre);
    }
}