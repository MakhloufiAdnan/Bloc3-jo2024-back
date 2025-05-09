package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.offres.VenteParOffreDto;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.enums.StatutPaiement;
import fr.studi.bloc3jo2024.entity.enums.StatutTransaction;
import fr.studi.bloc3jo2024.repository.OffreRepository;
import fr.studi.bloc3jo2024.repository.PaiementRepository;
import fr.studi.bloc3jo2024.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
public class StatistiqueController {

    private final OffreRepository offreRepository;
    private final PaiementRepository paiementRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Autowired
    public StatistiqueController(OffreRepository offreRepository, PaiementRepository paiementRepository, UtilisateurRepository utilisateurRepository) {
        this.offreRepository = offreRepository;
        this.paiementRepository = paiementRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping("/ventes-par-offre")
    public List<VenteParOffreDto> getVenteParOffre() {
        List<Offre> offres = offreRepository.findAll();
        List<VenteParOffreDto> stats = new ArrayList<>();

        for (Offre offre : offres) {
            long ventes = paiementRepository.countByOffreIdAndStatutPaiementAndTransaction_StatutTransaction(
                    offre.getIdOffre(), StatutPaiement.ACCEPTE, StatutTransaction.REUSSI
            );
            stats.add(new VenteParOffreDto(offre.getTypeOffre().name(), ventes));
        }
        return stats;
    }

    @GetMapping("/global")
    public Map<String, Long> getStatsGlobales() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("utilisateurs", utilisateurRepository.count());
        stats.put("paiements", paiementRepository.countByStatutPaiementAndTransaction_StatutTransaction(StatutPaiement.ACCEPTE, StatutTransaction.REUSSI));
        return stats;
    }
}
