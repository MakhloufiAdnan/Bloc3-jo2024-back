package fr.studi.bloc3jo2024.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.studi.bloc3jo2024.dto.epreuves.MettreAJourEpreuveVedetteDto;
import fr.studi.bloc3jo2024.entity.Epreuve;
import fr.studi.bloc3jo2024.service.EpreuveService;
import jakarta.persistence.EntityNotFoundException;
import fr.studi.bloc3jo2024.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(GlobalExceptionHandler.class) // Assurez-vous que votre gestionnaire global d'exceptions est importé
class EpreuveControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean // Ou @MockBean si vous passez à @WebMvcTest
    private EpreuveService epreuveService;

    @Autowired
    private ObjectMapper objectMapper;

    private Epreuve epreuve1;
    private Epreuve epreuve2;
    private MettreAJourEpreuveVedetteDto dtoUpdateVedette;

    @BeforeEach
    void setUp() {
        epreuve1 = Epreuve.builder()
                .idEpreuve(1L)
                .nomEpreuve("100m Sprint")
                .isFeatured(false)
                .build();

        epreuve2 = Epreuve.builder()
                .idEpreuve(2L)
                .nomEpreuve("Saut en longueur")
                .isFeatured(true)
                .build();

        dtoUpdateVedette = new MettreAJourEpreuveVedetteDto();
        dtoUpdateVedette.setIdEpreuve(1L);
        dtoUpdateVedette.setIsFeatured(true);
    }

    @Test
    void getAllEpreuves_shouldReturnPagedEpreuves() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Epreuve> epreuvesList = Arrays.asList(epreuve1, epreuve2);
        Page<Epreuve> epreuvesPage = new PageImpl<>(epreuvesList, pageable, epreuvesList.size());

        when(epreuveService.getAllEpreuves(any(Pageable.class))).thenReturn(epreuvesPage);

        // Act & Assert
        mockMvc.perform(get("/admin/epreuves") // Vérifiez que ce chemin correspond à votre EpreuveController
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].nomEpreuve", is(epreuve1.getNomEpreuve())))
                .andExpect(jsonPath("$.content[1].nomEpreuve", is(epreuve2.getNomEpreuve())))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.totalElements", is(2)));

        verify(epreuveService, times(1)).getAllEpreuves(any(Pageable.class));
    }

    @Test
    void getEpreuvesEnVedette_shouldReturnListOfFeaturedEpreuves() throws Exception {
        // Arrange
        // La méthode du service getEpreuvesEnVedette() retourne List<Epreuve>
        List<Epreuve> featuredEpreuvesList = Collections.singletonList(epreuve2);
        when(epreuveService.getEpreuvesEnVedette()).thenReturn(featuredEpreuvesList);

        // Act & Assert
        // Si votre contrôleur pagine cette liste, la réponse sera paginée.
        // Sinon, ce sera une liste JSON simple.
        // Cet exemple suppose que le contrôleur retourne la liste directement (non paginée par le contrôleur lui-même).
        mockMvc.perform(get("/admin/epreuves/vedette") // Vérifiez que ce chemin correspond
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))) // S'attend à une liste JSON
                .andExpect(jsonPath("$[0].nomEpreuve", is(epreuve2.getNomEpreuve())))
                .andExpect(jsonPath("$[0].featured", is(true))); // Vérifie la propriété 'featured'

        verify(epreuveService, times(1)).getEpreuvesEnVedette();
    }

    @Test
    void mettreAJourStatutVedette_validInput_shouldReturnUpdatedEpreuve() throws Exception {
        // Arrange
        Epreuve epreuveMiseAJour = Epreuve.builder()
                .idEpreuve(dtoUpdateVedette.getIdEpreuve())
                .nomEpreuve(epreuve1.getNomEpreuve()) // Supposons que le nom ne change pas pour ce test
                .isFeatured(dtoUpdateVedette.getIsFeatured())
                .build();

        when(epreuveService.mettreAJourStatutVedette(any(MettreAJourEpreuveVedetteDto.class))).thenReturn(epreuveMiseAJour);

        // Act & Assert
        mockMvc.perform(patch("/admin/epreuves/vedette") // Vérifiez que ce chemin correspond
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoUpdateVedette)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEpreuve", is(dtoUpdateVedette.getIdEpreuve().intValue())))
                .andExpect(jsonPath("$.nomEpreuve", is(epreuveMiseAJour.getNomEpreuve())))
                .andExpect(jsonPath("$.featured", is(dtoUpdateVedette.getIsFeatured())));

        verify(epreuveService, times(1)).mettreAJourStatutVedette(any(MettreAJourEpreuveVedetteDto.class));
    }

    @Test
    void mettreAJourStatutVedette_epreuveNotFound_shouldReturnNotFound() throws Exception {
        // Arrange
        String expectedErrorMessage = "Épreuve non trouvée avec l'id : " + dtoUpdateVedette.getIdEpreuve();
        when(epreuveService.mettreAJourStatutVedette(any(MettreAJourEpreuveVedetteDto.class)))
                .thenThrow(new EntityNotFoundException(expectedErrorMessage)); // Utilisez le message attendu

        // Act & Assert
        mockMvc.perform(patch("/admin/epreuves/vedette") // Vérifiez que ce chemin correspond
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoUpdateVedette)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedErrorMessage))); // Si GlobalExceptionHandler configure le message

        verify(epreuveService, times(1)).mettreAJourStatutVedette(any(MettreAJourEpreuveVedetteDto.class));
    }
}
