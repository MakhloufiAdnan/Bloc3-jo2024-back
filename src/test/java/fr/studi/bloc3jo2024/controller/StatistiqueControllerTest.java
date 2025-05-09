package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.offres.VenteParOffreDto;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.enums.StatutPaiement;
import fr.studi.bloc3jo2024.entity.enums.StatutTransaction;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import fr.studi.bloc3jo2024.repository.OffreRepository;
import fr.studi.bloc3jo2024.repository.PaiementRepository;
import fr.studi.bloc3jo2024.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatistiqueControllerTest {

    @InjectMocks
    private StatistiqueController statistiqueController;

    @Mock
    private OffreRepository offreRepository;

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    private Offre offre1;
    private Offre offre2;

    @BeforeEach
    void setUp() {
        offre1 = new Offre();
        offre1.setIdOffre(1L);
        offre1.setTypeOffre(TypeOffre.DUO);

        offre2 = new Offre();
        offre2.setIdOffre(2L);
        offre2.setTypeOffre (TypeOffre.FAMILIALE);
    }

    @Test
    void getVenteParOffre_ReturnsCorrectStats() {
        // Arrange
        List<Offre> offres = new ArrayList<>();
        offres.add(offre1);
        offres.add(offre2);
        when(offreRepository.findAll()).thenReturn(offres);

        when(paiementRepository.countByOffreIdAndStatutPaiementAndTransaction_StatutTransaction(
                1L, StatutPaiement.ACCEPTE, StatutTransaction.REUSSI
        )).thenReturn(10L);
        when(paiementRepository.countByOffreIdAndStatutPaiementAndTransaction_StatutTransaction(
                2L, StatutPaiement.ACCEPTE, StatutTransaction.REUSSI
        )).thenReturn(5L);

        // Act
        List<VenteParOffreDto> result = statistiqueController.getVenteParOffre();

        // Assert
        assertEquals(2, result.size());
        assertEquals(offre1.getTypeOffre().name(), result.getFirst().getNomOffre()); // Utilisation de getFirst()
        assertEquals(10L, result.getFirst().getNombreVentes());
        assertEquals(offre2.getTypeOffre().name(), result.get(1).getNomOffre());
        assertEquals(5L, result.get(1).getNombreVentes());
    }

    @Test
    void getStatsGlobales_ReturnsCorrectStats() {
        // Arrange
        when(utilisateurRepository.count()).thenReturn(100L);
        when(paiementRepository.countByStatutPaiementAndTransaction_StatutTransaction(StatutPaiement.ACCEPTE, StatutTransaction.REUSSI)).thenReturn(50L);

        // Act
        Map<String, Long> result = statistiqueController.getStatsGlobales();

        // Assert
        assertEquals(2, result.size());
        assertEquals(100L, result.get("utilisateurs"));
        assertEquals(50L, result.get("paiements"));
    }
}
