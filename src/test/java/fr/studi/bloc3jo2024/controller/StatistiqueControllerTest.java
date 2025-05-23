package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.offres.VenteParOffreDto;
import fr.studi.bloc3jo2024.repository.PaiementRepository;
import fr.studi.bloc3jo2024.repository.UtilisateurRepository;
import fr.studi.bloc3jo2024.service.StatistiqueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
@ExtendWith(MockitoExtension.class)
class StatistiqueControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StatistiqueService statistiqueService;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PaiementRepository paiementRepository;

    @InjectMocks
    private StatistiqueController statistiqueController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(statistiqueController).build();
    }

    /**
     * Tests the {@code getVentesJournalieresParTypeOffre} endpoint pour une récupération de données réussie.
     * Vérifie que le service est appelé et que la réponse est correctement formatée.
     * @throws Exception si les performances de MockMvc échouent.
     */
    @Test
    void testGetVentesJournalieresParTypeOffre_Success() throws Exception {
        LocalDate date1 = LocalDate.of(2024, 5, 20);
        LocalDate date2 = LocalDate.of(2024, 5, 21);
        List<VenteParOffreDto> mockStats = Arrays.asList(
                new VenteParOffreDto(date1, "SOLO", 10L),
                new VenteParOffreDto(date1, "DUO", 5L),
                new VenteParOffreDto(date2, "SOLO", 12L)
        );

        when(statistiqueService.calculerVentesJournalieresParTypeOffre()).thenReturn(mockStats);

        mockMvc.perform(get("/api/admin/stats/ventes-journalieres-par-type")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].date", is(date1.toString())))
                .andExpect(jsonPath("$[0].nomOffre", is("SOLO")))
                .andExpect(jsonPath("$[0].nombreVentes", is(10)))
                .andExpect(jsonPath("$[2].date", is(date2.toString())))
                .andExpect(jsonPath("$[2].nomOffre", is("SOLO")))
                .andExpect(jsonPath("$[2].nombreVentes", is(12)));
    }

    /**
     * Tests the {@code getVentesJournalieresParTypeOffre} endpoint lorsque le service renvoie une liste vide.
     * @throws Exception si MockMvc les performances échouent.
     */
    @Test
    void testGetVentesJournalieresParTypeOffre_EmptyList() throws Exception {
        when(statistiqueService.calculerVentesJournalieresParTypeOffre()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/stats/ventes-journalieres-par-type")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /**
     * Tests the {@code getVentesParOffrePourJour} endpoint pour une récupération réussie des données pour une date spécifique.
     * Vérifie que le service est appelé avec le paramètre de date correct et que la réponse est correctement formatée.
     * @throws Exception si MockMvc les performances échouent.
     */
    @Test
    void testGetVentesParOffrePourJour_Success() throws Exception {
        LocalDate specificDate = LocalDate.of(2024, 5, 22);
        List<VenteParOffreDto> mockStats = Arrays.asList(
                new VenteParOffreDto(specificDate, "SOLO", 7L),
                new VenteParOffreDto(specificDate, "FAMILLE", 3L)
        );

        when(statistiqueService.calculerVentesParOffrePourJourDonne(specificDate)).thenReturn(mockStats);

        mockMvc.perform(get("/api/admin/stats/ventes-par-offre-pour-jour")
                        .param("date", specificDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].date", is(specificDate.toString())))
                .andExpect(jsonPath("$[0].nomOffre", is("SOLO")))
                .andExpect(jsonPath("$[0].nombreVentes", is(7)))
                .andExpect(jsonPath("$[1].date", is(specificDate.toString())))
                .andExpect(jsonPath("$[1].nomOffre", is("FAMILLE")))
                .andExpect(jsonPath("$[1].nombreVentes", is(3)));
    }

    /**
     * Tests the {@code getVentesParOffrePourJour} endpoint avec un format de date non valide dans le paramètre de demande.
     * Expects a 400 Bad Demande de réponse en raison de l'échec de conversion de type de Spring.
     * @throws Exception si MockMvc les performances échouent.
     */
    @Test
    void testGetVentesParOffrePourJour_InvalidDateFormat() throws Exception {
        mockMvc.perform(get("/api/admin/stats/ventes-par-offre-pour-jour")
                        .param("date", "invalid-date-format")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests the {@code getStatsGlobales} endpoint pour une récupération de données réussie.
     * Vérifie que les méthodes du référentiel sont appelées et que la réponse est correctement formatée.
     * @throws Exception si MockMvc les performances échouent.
     */
    @Test
    void testGetStatsGlobales_Success() throws Exception {
        long mockUserCount = 150L;
        long mockSuccessfulPayments = 75L;

        when(utilisateurRepository.count()).thenReturn(mockUserCount);
        when(paiementRepository.countByStatutPaiementAndTransaction_StatutTransaction(any(), any())).thenReturn(mockSuccessfulPayments);

        mockMvc.perform(get("/api/admin/stats/global")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.utilisateurs", is((int) mockUserCount)))
                .andExpect(jsonPath("$.paiementsReussis", is((int) mockSuccessfulPayments)));
    }
}
