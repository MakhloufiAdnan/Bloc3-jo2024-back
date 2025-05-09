package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.dto.paiement.PaiementDto;
import fr.studi.bloc3jo2024.dto.paiement.PaiementSimulationResultDto;
import fr.studi.bloc3jo2024.entity.enums.MethodePaiementEnum;

import java.util.Optional;
import java.util.UUID;

public interface PaiementService {
    PaiementDto effectuerPaiement(UUID utilisateurId, Long idPanier, MethodePaiementEnum methodePaiement);
    PaiementSimulationResultDto simulerResultatPaiement(Long idPaiement, boolean paiementReussi, String detailsSimules);
    Optional<PaiementDto> getPaiementParPanier(UUID utilisateurId, Long idPanier);
    Optional<PaiementDto> getPaiementParId(Long idPaiement);
}