package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.dto.CreerOffreDto;
import fr.studi.bloc3jo2024.dto.MettreAJourOffreDto;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.DisciplineRepository;
import fr.studi.bloc3jo2024.repository.OffreRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminOffreService {

    private final OffreRepository offreRepository;
    private final DisciplineRepository disciplineRepository;
    private final PanierService panierService; // Ajout du PanierService

    public AdminOffreService(OffreRepository offreRepository, DisciplineRepository disciplineRepository, PanierService panierService) {
        this.offreRepository = offreRepository;
        this.disciplineRepository = disciplineRepository;
        this.panierService = panierService; // Injection du PanierService
    }

    // Mettre à jour les propriétés de l'offre (à partir de CreerOffreDTO)
    private void updateOffreFromDTO(Offre offre, CreerOffreDto dto) {
        offre.setTypeOffre(dto.getTypeOffre());
        offre.setQuantite(dto.getQuantite());
        offre.setPrix(dto.getPrix());
        offre.setDateExpiration(dto.getDateExpiration());
        offre.setStatutOffre(dto.getStatutOffre());
    }

    // Mettre à jour les propriétés de l'offre (à partir de MettreAJourOffreDTO)
    private void updateOffreFromDTO(Offre offre, MettreAJourOffreDto dto) {
        offre.setTypeOffre(dto.getTypeOffre());
        offre.setQuantite(dto.getQuantite());
        offre.setPrix(dto.getPrix());
        offre.setDateExpiration(dto.getDateExpiration());
        offre.setStatutOffre(dto.getStatutOffre());
    }

    private Discipline getEvenementById(Long evenementId) {
        return disciplineRepository.findById(evenementId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement non trouvé avec l'ID : " + evenementId));
    }

    // Ajouter une nouvelle offre à partir du DTO
    public Offre ajouterOffre(CreerOffreDto creerOffreDTO) {
        Offre nouvelleOffre = new Offre();
        updateOffreFromDTO(nouvelleOffre, creerOffreDTO);

        Discipline discipline = getEvenementById(creerOffreDTO.getEvenementId());
        nouvelleOffre.setDiscipline(discipline);

        Offre offreSave = offreRepository.save(nouvelleOffre);

        // Ajout de l'offre au panier via le PanierService
        panierService.ajouterOffreAuPanier(creerOffreDTO.getPanierId(), offreSave, creerOffreDTO.getQuantite()); // Utilisation de la quantité du DTO

        return offreSave;
    }

    // Obtenir une offre par ID
    public Offre obtenirOffreParId(Long idOffre) {
        return offreRepository.findById(idOffre)
                .orElse(null); // Retourne null si non trouvé, le contrôleur gère le 404
    }

    // Mettre à jour une offre à partir du DTO
    public Offre mettreAJourOffre(Long idOffre, MettreAJourOffreDto mettreAJourOffreDTO) {
        Offre offreExistante = offreRepository.findById(idOffre)
                .orElseThrow(() -> new ResourceNotFoundException("Offre non trouvée avec l'ID : " + idOffre));

        updateOffreFromDTO(offreExistante, mettreAJourOffreDTO);

        Discipline discipline = getEvenementById(mettreAJourOffreDTO.getEvenementId());
        offreExistante.setDiscipline(discipline);

        Offre offreMiseAJour = offreRepository.save(offreExistante);

        // Mise à jour de l'offre dans le panier via le PanierService
        panierService.mettreAJourQuantiteOffrePanier(mettreAJourOffreDTO.getPanierId(), offreMiseAJour, mettreAJourOffreDTO.getQuantite()); // Utilisation de la quantité du DTO

        return offreMiseAJour;
    }

    // Modifier le prix d'une offre
    public Offre modifierPrixOffre(Long idOffre, double nouveauPrix) {
        Offre offreExistante = offreRepository.findById(idOffre)
                .orElseThrow(() -> new ResourceNotFoundException("Offre non trouvée avec l'ID : " + idOffre));
        offreExistante.setPrix(nouveauPrix);
        return offreRepository.save(offreExistante);
    }

    // Supprimer une offre
    public boolean supprimerOffre(Long idOffre) {
        if (offreRepository.existsById(idOffre)) {
            offreRepository.deleteById(idOffre);
            return true;
        }
        return false; // Le contrôleur gère le 404 si false
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