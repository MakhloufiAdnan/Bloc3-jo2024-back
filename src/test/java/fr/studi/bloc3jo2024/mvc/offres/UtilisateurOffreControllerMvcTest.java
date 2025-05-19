package fr.studi.bloc3jo2024.mvc.offres;

import fr.studi.bloc3jo2024.dto.offres.OffreDto;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.service.offres.UtilisateurOffreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(UtilisateurOffreControllerMvcTest.class)
@AutoConfigureMockMvc
class UtilisateurOffreControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;
    private UtilisateurOffreService utilisateurOffreService;

    private OffreDto sampleOffreDto;

    @BeforeEach
    void setUp() {
        sampleOffreDto = new OffreDto(
                1L,
                10L,
                TypeOffre.SOLO,
                BigDecimal.valueOf(25.00),
                4, // capacite
                StatutOffre.DISPONIBLE,
                LocalDateTime.now().plusDays(30),
                50, // quantiteDisponible
                false // featured
        );
    }

    @Test
    void obtenirToutesLesOffresDisponibles_shouldReturnPagedOffres() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        List<OffreDto> offresList = Collections.singletonList(sampleOffreDto);
        Page<OffreDto> offresPage = new PageImpl<>(offresList, pageable, offresList.size());

        when(utilisateurOffreService.obtenirToutesLesOffresDisponibles(any(Pageable.class))).thenReturn(offresPage);

        mockMvc.perform(get("/offres")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].typeOffre", is(TypeOffre.SOLO.toString())))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.totalElements", is(1L)));
    }

    @Test
    void obtenirToutesLesOffresDisponibles_shouldReturnEmptyPageWhenNoOffres() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OffreDto> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(utilisateurOffreService.obtenirToutesLesOffresDisponibles(any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/offres")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0L)));
    }


    @Test
    void obtenirOffreDisponibleParId_shouldReturnOffreWhenFoundAndDisponible() throws Exception {
        when(utilisateurOffreService.obtenirOffreDisponibleParId(1L)).thenReturn(sampleOffreDto);

        mockMvc.perform(get("/offres/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.typeOffre", is(TypeOffre.SOLO.toString())));
    }

    @Test
    void obtenirOffreDisponibleParId_shouldReturnNotFoundWhenOffreNonExistent() throws Exception {
        String errorMessage = "Offre non trouvée avec l'ID : 99";
        when(utilisateurOffreService.obtenirOffreDisponibleParId(99L))
                .thenThrow(new ResourceNotFoundException(errorMessage));

        mockMvc.perform(get("/offres/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(errorMessage));
    }

    @Test
    void obtenirOffreDisponibleParId_shouldReturnNotFoundWhenOffreNotDisponible() throws Exception {
        String errorMessage = "L'offre avec l'ID 2 n'est pas disponible.";
        when(utilisateurOffreService.obtenirOffreDisponibleParId(2L))
                .thenThrow(new ResourceNotFoundException(errorMessage));

        mockMvc.perform(get("/offres/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(errorMessage));
    }
}