package fr.studi.bloc3jo2024.mvc.offres;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.studi.bloc3jo2024.controller.offres.AdminOffreController;
import fr.studi.bloc3jo2024.dto.offres.CreerOffreDto;
import fr.studi.bloc3jo2024.dto.offres.MettreAJourOffreDto;
import fr.studi.bloc3jo2024.dto.offres.OffreAdminDto;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import fr.studi.bloc3jo2024.exception.GlobalExceptionHandler;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.service.offres.AdminOffreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;


@WebMvcTest(AdminOffreController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser(roles = {"ADMIN"})
class AdminOffreControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminOffreService adminOffreService;

    private CreerOffreDto creerOffreDto;
    private MettreAJourOffreDto mettreAJourOffreDto;
    private OffreAdminDto sampleOffreAdminDto;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();

        creerOffreDto = new CreerOffreDto(
                TypeOffre.SOLO, 10, BigDecimal.valueOf(50.00),
                LocalDateTime.now().plusDays(60), StatutOffre.DISPONIBLE,
                1L, 200, false
        );

        mettreAJourOffreDto = new MettreAJourOffreDto(
                TypeOffre.DUO, 15, BigDecimal.valueOf(90.00),
                LocalDateTime.now().plusDays(90), StatutOffre.DISPONIBLE,
                2L, 300, true
        );

        sampleOffreAdminDto = new OffreAdminDto(
                1L, TypeOffre.SOLO, 10, BigDecimal.valueOf(50.00),
                creerOffreDto.getDateExpiration(),
                StatutOffre.DISPONIBLE, 200, 1L, 0, false
        );
    }

    @Test
    void ajouterOffre_shouldReturnCreatedOffre() throws Exception {
        when(adminOffreService.ajouterOffre(any(CreerOffreDto.class))).thenReturn(sampleOffreAdminDto);

        mockMvc.perform(post("/admin/offres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creerOffreDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.typeOffre", is(TypeOffre.SOLO.toString())));
    }

    @Test
    void ajouterOffre_whenDisciplineNotFoundByService_shouldReturnBadRequestFromGlobalHandler() throws Exception {
        String errorMessage = "Discipline non trouvée avec l'ID : " + creerOffreDto.getIdDiscipline();
        when(adminOffreService.ajouterOffre(any(CreerOffreDto.class)))
                .thenThrow(new ResourceNotFoundException(errorMessage));

        mockMvc.perform(post("/admin/offres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creerOffreDto)))
                .andExpect(status().isNotFound()) // GlobalExceptionHandler le transforme en 404
                .andExpect(jsonPath("$.message", containsString(errorMessage)));
    }


    @Test
    void ajouterOffre_withInvalidDto_shouldReturnBadRequestFromValidation() throws Exception {
        CreerOffreDto invalidDto = new CreerOffreDto(
                null, 0, BigDecimal.valueOf(-10), LocalDateTime.now().plusDays(60),
                null, null, 0, false
        );
        mockMvc.perform(post("/admin/offres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getOffreParId_shouldReturnOffre() throws Exception {
        when(adminOffreService.obtenirOffreParId(1L)).thenReturn(sampleOffreAdminDto);

        mockMvc.perform(get("/admin/offres/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void getOffreParId_shouldReturnNotFoundWhenOffreNonExistentFromService() throws Exception {
        String errorMessage = "Offre non trouvée avec l'ID : 99";
        when(adminOffreService.obtenirOffreParId(99L))
                .thenThrow(new ResourceNotFoundException(errorMessage));

        mockMvc.perform(get("/admin/offres/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Géré par GlobalExceptionHandler
                .andExpect(jsonPath("$.message", containsString(errorMessage)));
    }

    @Test
    void mettreAJourOffre_shouldReturnUpdatedOffre() throws Exception {
        OffreAdminDto updatedDto = new OffreAdminDto(1L, TypeOffre.DUO, 15, BigDecimal.valueOf(90.00), mettreAJourOffreDto.getDateExpiration(), StatutOffre.DISPONIBLE, 300, 2L, 0, true);
        when(adminOffreService.mettreAJourOffre(eq(1L), any(MettreAJourOffreDto.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/admin/offres/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mettreAJourOffreDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.typeOffre", is(TypeOffre.DUO.toString())))
                .andExpect(jsonPath("$.featured", is(true)));
    }

    @Test
    void mettreAJourOffre_whenOffreNotFoundByService_shouldReturnNotFoundFromGlobalHandler() throws Exception {
        String errorMessage = "Offre non trouvée avec l'ID : 99";
        when(adminOffreService.mettreAJourOffre(eq(99L), any(MettreAJourOffreDto.class)))
                .thenThrow(new ResourceNotFoundException(errorMessage));

        mockMvc.perform(put("/admin/offres/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mettreAJourOffreDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString(errorMessage)));
    }

    @Test
    void mettreAJourOffre_whenIllegalArgumentByService_shouldReturnBadRequestFromGlobalHandler() throws Exception {
        String errorMessage = "Argument invalide pour la mise à jour";
        when(adminOffreService.mettreAJourOffre(eq(1L), any(MettreAJourOffreDto.class)))
                .thenThrow(new IllegalArgumentException(errorMessage));

        mockMvc.perform(put("/admin/offres/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mettreAJourOffreDto)))
                .andExpect(status().isBadRequest()) // Géré par GlobalExceptionHandler
                .andExpect(jsonPath("$.message", containsString(errorMessage)));
    }


    @Test
    void supprimerOffre_shouldReturnNoContent() throws Exception {
        doNothing().when(adminOffreService).supprimerOffre(1L);

        mockMvc.perform(delete("/admin/offres/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void supprimerOffre_whenOffreNotFoundByService_shouldReturnNotFoundFromGlobalHandler() throws Exception {
        String errorMessage = "Offre non trouvée avec l'ID : 99";
        doThrow(new ResourceNotFoundException(errorMessage)).when(adminOffreService).supprimerOffre(99L);

        mockMvc.perform(delete("/admin/offres/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString(errorMessage)));
    }

    @Test
    void obtenirToutesLesOffres_shouldReturnPagedOffres() throws Exception {
        Pageable pageable = PageRequest.of(0, 5);
        List<OffreAdminDto> offresList = Collections.singletonList(sampleOffreAdminDto);
        Page<OffreAdminDto> offresPage = new PageImpl<>(offresList, pageable, offresList.size());

        when(adminOffreService.obtenirToutesLesOffres(any(Pageable.class))).thenReturn(offresPage);

        mockMvc.perform(get("/admin/offres")
                        .param("page", "0")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)));
    }

    @Test
    void getVentesParOffre_shouldReturnMap() throws Exception {
        Map<Long, Long> ventesMap = new HashMap<>();
        ventesMap.put(1L, 100L);
        ventesMap.put(2L, 50L);

        when(adminOffreService.getNombreDeVentesParOffre()).thenReturn(ventesMap);

        mockMvc.perform(get("/admin/offres/ventes/par-offre")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['1']", is(100)))
                .andExpect(jsonPath("$['2']", is(50)));
    }

    @Test
    void getVentesParType_shouldReturnMap() throws Exception {
        Map<String, Long> ventesMap = new HashMap<>();
        ventesMap.put(TypeOffre.SOLO.toString(), 200L);
        ventesMap.put(TypeOffre.DUO.toString(), 150L);

        when(adminOffreService.getVentesParTypeOffre()).thenReturn(ventesMap);

        mockMvc.perform(get("/admin/offres/ventes/par-type")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SOLO", is(200)))
                .andExpect(jsonPath("$.DUO", is(150)));
    }
}