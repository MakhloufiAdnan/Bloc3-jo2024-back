package fr.studi.bloc3jo2024.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.studi.bloc3jo2024.dto.disciplines.DisciplineDto;
import fr.studi.bloc3jo2024.dto.disciplines.MettreAJourDisciplineDto;
import fr.studi.bloc3jo2024.dto.disciplines.CreerDisciplineDto;
import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Pays;
import fr.studi.bloc3jo2024.exception.GlobalExceptionHandler;
import fr.studi.bloc3jo2024.service.DisciplineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
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
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(GlobalExceptionHandler.class)
class DisciplineControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DisciplineService disciplineService;

    @MockitoBean
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Discipline discipline1;
    private DisciplineDto disciplineDto1;
    private Adresse adresse;

    @BeforeEach
    void setUp() {
        // Enregistre le module pour gérer correctement la sérialisation/désérialisation de LocalDateTime.
        objectMapper.registerModule(new JavaTimeModule());

        Pays pays;
        pays = Pays.builder().idPays(1L).nomPays("France").build();
        adresse = new Adresse(1L, 10, "Rue Test", "TestVille", "75000", null, null, pays);
        discipline1 = new Discipline(1L, "Natation", LocalDateTime.now().plusDays(5), 100, false, 0L, adresse, Collections.emptySet(), Collections.emptySet());
        disciplineDto1 = new DisciplineDto(1L, "Natation", discipline1.getDateDiscipline(), 100, (adresse != null ? adresse.getIdAdresse() : null) );
    }

    @Test
    void getDisciplines_shouldReturnPageOfDisciplineDtos() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Discipline> disciplineList = List.of(discipline1);
        Page<Discipline> disciplinePage = new PageImpl<>(disciplineList, pageable, disciplineList.size());

        when(disciplineService.findDisciplinesFiltered(null, null, null, pageable)).thenReturn(disciplinePage);
        when(modelMapper.map(discipline1, DisciplineDto.class)).thenReturn(disciplineDto1);

        // Act
        ResultActions resultActions = mockMvc.perform(get("/disciplines")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nomDiscipline", is(disciplineDto1.getNomDiscipline())))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void getDisciplines_withVilleFilter_shouldReturnFilteredPage() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5);
        String ville = "TestVille";
        List<Discipline> disciplineList = List.of(discipline1);
        Page<Discipline> disciplinePage = new PageImpl<>(disciplineList, pageable, disciplineList.size());

        when(disciplineService.findDisciplinesFiltered(eq(ville), any(), any(), eq(pageable))).thenReturn(disciplinePage);
        when(modelMapper.map(discipline1, DisciplineDto.class)).thenReturn(disciplineDto1);

        // Act
        ResultActions resultActions = mockMvc.perform(get("/disciplines")
                .param("ville", ville)
                .param("page", "0")
                .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nomDiscipline", is(disciplineDto1.getNomDiscipline())));
    }

    @Test
    void getDisciplinesAvenir_shouldReturnPageOfFutureDisciplineDtos() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Discipline> disciplineList = List.of(discipline1);
        Page<Discipline> disciplinePage = new PageImpl<>(disciplineList, pageable, disciplineList.size());

        when(disciplineService.getDisciplinesAvenir(pageable)).thenReturn(disciplinePage);
        when(modelMapper.map(discipline1, DisciplineDto.class)).thenReturn(disciplineDto1);

        // Act
        ResultActions resultActions = mockMvc.perform(get("/disciplines/avenir")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nomDiscipline", is(disciplineDto1.getNomDiscipline())));
    }

    @Test
    void getDisciplinesEnVedette_shouldReturnSetOfDisciplineDtos() throws Exception {
        // Arrange
        Set<Discipline> disciplineSet = Set.of(discipline1);
        when(disciplineService.getDisciplinesEnVedette()).thenReturn(disciplineSet);
        when(modelMapper.map(discipline1, DisciplineDto.class)).thenReturn(disciplineDto1);

        // Act
        ResultActions resultActions = mockMvc.perform(get("/disciplines/vedette")
                .contentType(MediaType.APPLICATION_JSON));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nomDiscipline", is(disciplineDto1.getNomDiscipline())));
    }

    @Test
    void creerDiscipline_shouldReturnCreatedDisciplineDto_whenValidInput() throws Exception {
        // Arrange
        CreerDisciplineDto creerDto = new CreerDisciplineDto();
        creerDto.setNomDiscipline("Nouvelle Discipline");
        creerDto.setDateDiscipline(LocalDateTime.now().plusDays(1));
        creerDto.setNbPlaceDispo(50);
        creerDto.setIdAdresse(1L);

        Discipline disciplineCree = Discipline.builder()
                .idDiscipline(2L)
                .nomDiscipline(creerDto.getNomDiscipline())
                .dateDiscipline(creerDto.getDateDiscipline())
                .nbPlaceDispo(creerDto.getNbPlaceDispo())
                .adresse(adresse)
                .build();

        DisciplineDto disciplineCreeDto = new DisciplineDto(
                disciplineCree.getIdDiscipline(),
                disciplineCree.getNomDiscipline(),
                disciplineCree.getDateDiscipline(),
                disciplineCree.getNbPlaceDispo(),
                (disciplineCree.getAdresse() != null ? disciplineCree.getAdresse().getIdAdresse() : null)
        );

        when(disciplineService.creerDiscipline(any(CreerDisciplineDto.class))).thenReturn(disciplineCree);
        when(modelMapper.map(disciplineCree, DisciplineDto.class)).thenReturn(disciplineCreeDto);

        // Act
        ResultActions resultActions = mockMvc.perform(post("/disciplines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creerDto)));

        // Assert
        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomDiscipline", is(creerDto.getNomDiscipline())))
                .andExpect(jsonPath("$.idDiscipline", is(2)));
    }

    @Test
    void creerDiscipline_shouldReturnBadRequest_whenInvalidInput() throws Exception {
        // Arrange
        CreerDisciplineDto creerDto = new CreerDisciplineDto();
        creerDto.setNomDiscipline(null);
        creerDto.setDateDiscipline(LocalDateTime.now().minusDays(1));
        creerDto.setNbPlaceDispo(-10);

        // Act
        ResultActions resultActions = mockMvc.perform(post("/disciplines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creerDto)));

        // Assert
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    void mettreAJourDiscipline_shouldReturnUpdatedDisciplineDto_whenValidInputAndMatchingIds() throws Exception {
        // Arrange
        Long disciplineId = 1L;
        MettreAJourDisciplineDto majDto = new MettreAJourDisciplineDto();
        majDto.setIdDiscipline(disciplineId);
        majDto.setNomDiscipline("Discipline MAJ");
        majDto.setDateDiscipline(LocalDateTime.now().plusDays(2));
        majDto.setNbPlaceDispo(60);
        majDto.setIdAdresse(1L);

        Discipline disciplineMaj = new Discipline(disciplineId, majDto.getNomDiscipline(), majDto.getDateDiscipline(), majDto.getNbPlaceDispo(), false, 1L, adresse, null, null);
        DisciplineDto disciplineMajDto = new DisciplineDto(disciplineId, majDto.getNomDiscipline(), majDto.getDateDiscipline(), majDto.getNbPlaceDispo(), 1L);

        when(disciplineService.mettreAJourDiscipline(any(MettreAJourDisciplineDto.class))).thenReturn(disciplineMaj);
        when(modelMapper.map(disciplineMaj, DisciplineDto.class)).thenReturn(disciplineMajDto);

        // Act
        ResultActions resultActions = mockMvc.perform(put("/disciplines/{id}", disciplineId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(majDto)));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.nomDiscipline", is(majDto.getNomDiscipline())));
    }

    @Test
    void mettreAJourDiscipline_shouldReturnBadRequest_whenIdMismatch() throws Exception {
        // Arrange
        Long pathId = 1L;
        MettreAJourDisciplineDto majDto = new MettreAJourDisciplineDto();
        majDto.setIdDiscipline(2L);
        majDto.setNomDiscipline("Discipline MAJ");
        majDto.setDateDiscipline(LocalDateTime.now().plusDays(2));
        majDto.setNbPlaceDispo(60);
        majDto.setIdAdresse(1L);

        // Act
        ResultActions resultActions = mockMvc.perform(put("/disciplines/{id}", pathId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(majDto)));

        // Assert
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    void supprimerDiscipline_shouldReturnNoContent_whenSuccessful() throws Exception {
        // Arrange
        Long disciplineId = 1L;
        doNothing().when(disciplineService).supprimerDiscipline(disciplineId);

        // Act
        ResultActions resultActions = mockMvc.perform(delete("/disciplines/{id}", disciplineId));

        // Assert
        resultActions.andExpect(status().isNoContent());
    }

    @Test
    void retirerPlaces_shouldReturnOkWithUpdatedDiscipline() throws Exception {
        // Arrange
        Long disciplineId = 1L;
        int nb = 5;
        Discipline disciplineApresRetrait = Discipline.builder()
                .idDiscipline(discipline1.getIdDiscipline())
                .nomDiscipline(discipline1.getNomDiscipline())
                .dateDiscipline(discipline1.getDateDiscipline())
                .nbPlaceDispo(discipline1.getNbPlaceDispo() - nb)
                .adresse(discipline1.getAdresse())
                .build();
        DisciplineDto disciplineDtoApresRetrait = new DisciplineDto(
                disciplineApresRetrait.getIdDiscipline(),
                disciplineApresRetrait.getNomDiscipline(),
                disciplineApresRetrait.getDateDiscipline(),
                disciplineApresRetrait.getNbPlaceDispo(),
                (disciplineApresRetrait.getAdresse() != null ? disciplineApresRetrait.getAdresse().getIdAdresse() : null)
        );

        when(disciplineService.retirerPlaces(disciplineId, nb)).thenReturn(disciplineApresRetrait);
        when(modelMapper.map(disciplineApresRetrait, DisciplineDto.class)).thenReturn(disciplineDtoApresRetrait);

        // Act
        ResultActions resultActions = mockMvc.perform(patch("/disciplines/{id}/retirer-places", disciplineId)
                .param("nb", String.valueOf(nb)));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.nomDiscipline", is(disciplineDtoApresRetrait.getNomDiscipline())))
                .andExpect(jsonPath("$.nbPlaceDispo", is(disciplineDtoApresRetrait.getNbPlaceDispo())));
    }

    @Test
    void ajouterPlaces_shouldReturnOkWithUpdatedDiscipline() throws Exception {
        // Arrange
        Long disciplineId = 1L;
        int nb = 5;
        Discipline disciplineApresAjout = Discipline.builder()
                .idDiscipline(discipline1.getIdDiscipline())
                .nomDiscipline(discipline1.getNomDiscipline())
                .dateDiscipline(discipline1.getDateDiscipline())
                .nbPlaceDispo(discipline1.getNbPlaceDispo() + nb)
                .adresse(discipline1.getAdresse())
                .build();
        DisciplineDto disciplineDtoApresAjout = new DisciplineDto(
                disciplineApresAjout.getIdDiscipline(),
                disciplineApresAjout.getNomDiscipline(),
                disciplineApresAjout.getDateDiscipline(),
                disciplineApresAjout.getNbPlaceDispo(),
                (disciplineApresAjout.getAdresse() != null ? disciplineApresAjout.getAdresse().getIdAdresse() : null)
        );

        when(disciplineService.ajouterPlaces(disciplineId, nb)).thenReturn(disciplineApresAjout);
        when(modelMapper.map(disciplineApresAjout, DisciplineDto.class)).thenReturn(disciplineDtoApresAjout);

        // Act
        ResultActions resultActions = mockMvc.perform(patch("/disciplines/{id}/ajouter-places", disciplineId)
                .param("nb", String.valueOf(nb)));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.nomDiscipline", is(disciplineDtoApresAjout.getNomDiscipline())))
                .andExpect(jsonPath("$.nbPlaceDispo", is(disciplineDtoApresAjout.getNbPlaceDispo())));
    }

    @Test
    void updateDate_shouldReturnOkWithUpdatedDiscipline() throws Exception {
        // Arrange
        Long disciplineId = 1L;
        LocalDateTime nouvelleDate = LocalDateTime.now().plusMonths(1);

        Discipline disciplineApresUpdateDate = Discipline.builder()
                .idDiscipline(discipline1.getIdDiscipline())
                .nomDiscipline(discipline1.getNomDiscipline())
                .dateDiscipline(nouvelleDate)
                .nbPlaceDispo(discipline1.getNbPlaceDispo())
                .adresse(discipline1.getAdresse())
                .build();
        DisciplineDto disciplineDtoApresUpdateDate = new DisciplineDto(
                disciplineApresUpdateDate.getIdDiscipline(),
                disciplineApresUpdateDate.getNomDiscipline(),
                disciplineApresUpdateDate.getDateDiscipline(),
                disciplineApresUpdateDate.getNbPlaceDispo(),
                (disciplineApresUpdateDate.getAdresse() != null ? disciplineApresUpdateDate.getAdresse().getIdAdresse() : null)
        );

        when(disciplineService.updateDate(disciplineId, nouvelleDate)).thenReturn(disciplineApresUpdateDate);
        when(modelMapper.map(disciplineApresUpdateDate, DisciplineDto.class)).thenReturn(disciplineDtoApresUpdateDate);

        // Act
        ResultActions resultActions = mockMvc.perform(patch("/disciplines/{id}/modifier-date", disciplineId)
                .param("date", nouvelleDate.toString())); // Utilise le format ISO par défaut pour LocalDateTime

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.nomDiscipline", is(disciplineDtoApresUpdateDate.getNomDiscipline())))
                .andExpect(jsonPath("$.dateDiscipline").value(nouvelleDate.toString().substring(0,23)));
    }
}
