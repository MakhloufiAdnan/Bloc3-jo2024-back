package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.offres.VenteParOffreDto;
import fr.studi.bloc3jo2024.entity.enums.StatutPaiement;
import fr.studi.bloc3jo2024.entity.enums.StatutTransaction;
import fr.studi.bloc3jo2024.repository.PaiementRepository;
import fr.studi.bloc3jo2024.repository.UtilisateurRepository;
import fr.studi.bloc3jo2024.service.StatistiqueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
public class StatistiqueController {

    private static final Logger log = LoggerFactory.getLogger(StatistiqueController.class);

    private final PaiementRepository paiementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final StatistiqueService statistiqueService;

    @Autowired
    public StatistiqueController(
                                  PaiementRepository paiementRepository,
                                  UtilisateurRepository utilisateurRepository,
                                  StatistiqueService statistiqueService) {
        this.paiementRepository = paiementRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.statistiqueService = statistiqueService;
    }

    /**
     * Récupère les statistiques de ventes quotidiennes pour chaque type d'offre.
     * Ce endpoint est destiné à alimenter en données des graphiques ou des rapports de ventes.
     *
     * @return A {@link ResponseEntity} contenant une liste de {@link VenteParOffreDto}.
     */
    @GetMapping("/ventes-journalieres-par-type")
    public ResponseEntity<List<VenteParOffreDto>> getVentesJournalieresParTypeOffre() {
        log.info("Request to get daily sales per offer type.");
        List<VenteParOffreDto> stats = statistiqueService.calculerVentesJournalieresParTypeOffre();
        return ResponseEntity.ok(stats);
    }

    /**
     * Récupère le total des ventes pour chaque type d'offre pour un jour spécifique.
     *
     * @param jour The specific day for which to calculate sales (format YYYY-MM-DD).
     * @return A {@link ResponseEntity} contenant une liste de {@link VenteParOffreDto},
     * où le champ de date de chaque DTO est renseigné avec le jour demandé.
     */
    @GetMapping("/ventes-par-offre-pour-jour")
    public ResponseEntity<List<VenteParOffreDto>> getVentesParOffrePourJour(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate jour) {
        log.info("Request to get sales per offer type for the day: {}", jour);
        List<VenteParOffreDto> stats = statistiqueService.calculerVentesParOffrePourJourDonne(jour);
        return ResponseEntity.ok(stats);
    }

    /**
     * Récupère les statistiques globales (nombre total d'utilisateurs, nombre total de paiements réussis).
     *
     * @return A {@link ResponseEntity} contenant une {@link Map} avec des statistiques globales.
     */
    @GetMapping("/global")
    public ResponseEntity<Map<String, Long>> getStatsGlobales() {
        log.info("Request to get global statistics.");
        Map<String, Long> stats = new HashMap<>();
        stats.put("utilisateurs", utilisateurRepository.count());
        stats.put("paiementsReussis", paiementRepository.countByStatutPaiementAndTransaction_StatutTransaction(StatutPaiement.ACCEPTE, StatutTransaction.REUSSI));
        return ResponseEntity.ok(stats);
    }
}
