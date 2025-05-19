package fr.studi.bloc3jo2024.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Pays;
import fr.studi.bloc3jo2024.exception.AdresseLieeAUneDisciplineException;
import fr.studi.bloc3jo2024.exception.GlobalExceptionHandler;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.service.AdresseService;
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
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
@Import(GlobalExceptionHandler.class)
class AdresseControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdresseService adresseService;

    @Autowired
    private ObjectMapper objectMapper;

    private Adresse adresseExemple;
    private Pays paysExemple;
    private UUID utilisateurIdExemple;
    private final Long disciplineIdExemple = 200L;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.findAndRegisterModules();

        paysExemple = Pays.builder().idPays(1L).nomPays("France").build();
        adresseExemple = Adresse.builder()
                .idAdresse(1L)
                .numeroRue(10)
                .nomRue("Rue de la Paix")
                .ville("Paris")
                .codePostal("75001")
                .pays(paysExemple)
                .build();
        utilisateurIdExemple = UUID.randomUUID();
    }

    @Test
    void creerAdresseSiNonExistante_AdresseExistante_shouldReturnOkWithExistingId() throws Exception {
        Long existingId = 5L;
        when(adresseService.getIdAdresseSiExistante(any(Adresse.class))).thenReturn(existingId);

        mockMvc.perform(post("/api/adresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adresseExemple)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAdresseExistante", is(existingId.intValue())));

        verify(adresseService).getIdAdresseSiExistante(any(Adresse.class));
        verify(adresseService, never()).creerAdresseSiNonExistante(any(Adresse.class));
    }

    @Test
    void creerAdresseSiNonExistante_NouvelleAdresse_shouldReturnCreatedWithAdresse() throws Exception {
        Adresse nouvelleAdresseCreee = Adresse.builder().idAdresse(2L).numeroRue(20).ville("Lyon").pays(paysExemple).build();
        when(adresseService.getIdAdresseSiExistante(any(Adresse.class))).thenReturn(null);
        when(adresseService.creerAdresseSiNonExistante(any(Adresse.class))).thenReturn(nouvelleAdresseCreee);

        mockMvc.perform(post("/api/adresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adresseExemple)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAdresse", is(nouvelleAdresseCreee.getIdAdresse().intValue())))
                .andExpect(jsonPath("$.ville", is("Lyon")));

        verify(adresseService).getIdAdresseSiExistante(any(Adresse.class));
        verify(adresseService).creerAdresseSiNonExistante(any(Adresse.class));
    }

    @Test
    void creerAdresseSiNonExistante_IllegalArgumentExceptionFromService_shouldReturnBadRequest() throws Exception {
        String errorMessage = "Pays invalide pour la création";
        when(adresseService.getIdAdresseSiExistante(any(Adresse.class))).thenReturn(null);
        when(adresseService.creerAdresseSiNonExistante(any(Adresse.class))).thenThrow(new IllegalArgumentException(errorMessage));

        mockMvc.perform(post("/api/adresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adresseExemple)))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(containsString(errorMessage)));
    }

    @Test
    void creerAdresseSiNonExistante_ResourceNotFoundExceptionFromService_shouldReturnBadRequest() throws Exception {
        String errorMessage = "Pays non trouvé";
        when(adresseService.getIdAdresseSiExistante(any(Adresse.class))).thenReturn(null);
        when(adresseService.creerAdresseSiNonExistante(any(Adresse.class))).thenThrow(new ResourceNotFoundException(errorMessage));

        mockMvc.perform(post("/api/adresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adresseExemple)))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(containsString(errorMessage)));
    }

    @Test
    void getAdresseById_Success_shouldReturnAdresse() throws Exception {
        when(adresseService.getAdresseById(1L)).thenReturn(adresseExemple);

        mockMvc.perform(get("/api/adresses/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAdresse", is(adresseExemple.getIdAdresse().intValue())))
                .andExpect(jsonPath("$.ville", is(adresseExemple.getVille())));
        verify(adresseService).getAdresseById(1L);
    }

    @Test
    void getAdresseById_NotFound_shouldReturnNotFoundStatus() throws Exception {
        String errorMessage = "Adresse non trouvée avec l'ID : 99";
        when(adresseService.getAdresseById(99L)).thenThrow(new ResourceNotFoundException(errorMessage));

        mockMvc.perform(get("/api/adresses/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(status().reason(containsString(errorMessage)));
        verify(adresseService).getAdresseById(99L);
    }

    @Test
    void getAllAdresses_Success_shouldReturnPagedAdresses() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        List<Adresse> adressesList = Collections.singletonList(adresseExemple);
        Page<Adresse> adressesPage = new PageImpl<>(adressesList, pageable, adressesList.size());
        when(adresseService.getAllAdresses(pageable)).thenReturn(adressesPage);

        mockMvc.perform(get("/api/adresses")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].ville", is(adresseExemple.getVille())))
                .andExpect(jsonPath("$.totalElements", is(1)));
        verify(adresseService).getAllAdresses(pageable);
    }

    @Test
    void getAdressesByUserId_Success_shouldReturnListOfAdresses() throws Exception {
        List<Adresse> adresses = Collections.singletonList(adresseExemple);
        when(adresseService.getAdressesByUtilisateurId(utilisateurIdExemple)).thenReturn(adresses);

        mockMvc.perform(get("/api/adresses/utilisateur/{idUtilisateur}", utilisateurIdExemple))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ville", is(adresseExemple.getVille())));
        verify(adresseService).getAdressesByUtilisateurId(utilisateurIdExemple);
    }

    @Test
    void getAdresseByDiscipline_Success_shouldReturnAdresse() throws Exception {
        when(adresseService.getAdresseByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple)))).thenReturn(adresseExemple);

        mockMvc.perform(get("/api/adresses/discipline/{idDiscipline}", disciplineIdExemple))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ville", is(adresseExemple.getVille())));
        verify(adresseService).getAdresseByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple)));
    }

    @Test
    void getAdresseByDiscipline_IllegalArgumentFromService_shouldReturnBadRequest() throws Exception {
        String errorMessage = "Discipline ID null";
        when(adresseService.getAdresseByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple))))
                .thenThrow(new IllegalArgumentException(errorMessage));

        mockMvc.perform(get("/api/adresses/discipline/{idDiscipline}", disciplineIdExemple))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(containsString(errorMessage)));
    }

    @Test
    void getAdressesByDiscipline_All_Success_shouldReturnListOfAdresses() throws Exception {
        List<Adresse> adresses = Collections.singletonList(adresseExemple);
        when(adresseService.getAdressesByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple))))
                .thenReturn(adresses);

        mockMvc.perform(get("/api/adresses/discipline/{idDiscipline}/all", disciplineIdExemple))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ville", is(adresseExemple.getVille())));
        verify(adresseService).getAdressesByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple)));
    }

    @Test
    void getAdressesByDiscipline_All_IllegalArgumentFromService_shouldReturnBadRequest() throws Exception {
        String errorMessage = "Discipline ID null pour /all";
        when(adresseService.getAdressesByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple))))
                .thenThrow(new IllegalArgumentException(errorMessage));

        mockMvc.perform(get("/api/adresses/discipline/{idDiscipline}/all", disciplineIdExemple))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(containsString(errorMessage)));
    }

    @Test
    void rechercherAdresseComplete_Found_shouldReturnAdresse() throws Exception {
        Integer numeroRue = 10; String nomRue = "Rue de la Paix"; String ville = "Paris"; String codePostal = "75001"; Long idPays = 1L;
        when(adresseService.rechercherAdresseComplete(eq(numeroRue), eq(nomRue), eq(ville), eq(codePostal), argThat(p -> p.getIdPays().equals(idPays))))
                .thenReturn(adresseExemple);

        mockMvc.perform(get("/api/adresses/recherche")
                        .param("numeroRue", numeroRue.toString())
                        .param("nomRue", nomRue)
                        .param("ville", ville)
                        .param("codePostal", codePostal)
                        .param("idPays", idPays.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ville", is(adresseExemple.getVille())));
    }

    @Test
    void rechercherAdresseComplete_NotFound_shouldReturnNotFoundStatus() throws Exception {
        Integer numeroRue = 10; String nomRue = "Rue de la Paix"; String ville = "Paris"; String codePostal = "75001"; Long idPays = 1L;
        when(adresseService.rechercherAdresseComplete(eq(numeroRue), eq(nomRue), eq(ville), eq(codePostal), argThat(p -> p.getIdPays().equals(idPays))))
                .thenReturn(null);

        mockMvc.perform(get("/api/adresses/recherche")
                        .param("numeroRue", numeroRue.toString())
                        .param("nomRue", nomRue)
                        .param("ville", ville)
                        .param("codePostal", codePostal)
                        .param("idPays", idPays.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void rechercherAdresseComplete_IllegalArgumentFromService_shouldReturnBadRequest() throws Exception {
        Integer numeroRue = 10; String nomRue = "Rue de la Paix"; String ville = "Paris"; String codePostal = "75001"; Long idPays = 1L;
        String errorMessage = "Pays invalide";
        when(adresseService.rechercherAdresseComplete(eq(numeroRue), eq(nomRue), eq(ville), eq(codePostal), argThat(p -> p.getIdPays().equals(idPays))))
                .thenThrow(new IllegalArgumentException(errorMessage));

        mockMvc.perform(get("/api/adresses/recherche")
                        .param("numeroRue", numeroRue.toString())
                        .param("nomRue", nomRue)
                        .param("ville", ville)
                        .param("codePostal", codePostal)
                        .param("idPays", idPays.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(containsString(errorMessage)));
    }

    @Test
    void rechercherAdressesParVillePourDisciplines_Success_shouldReturnListOfAdresses() throws Exception {
        String ville = "Paris";
        List<Adresse> adresses = Collections.singletonList(adresseExemple);
        when(adresseService.rechercherAdressesParVillePourDisciplines(ville)).thenReturn(adresses);

        mockMvc.perform(get("/api/adresses/ville/{ville}", ville))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ville", is(ville)));
    }

    @Test
    void rechercherAdressesParDisciplineEtPays_Success_shouldReturnListOfAdresses() throws Exception {
        Long paysId = 1L;
        List<Adresse> adresses = Collections.singletonList(adresseExemple);
        when(adresseService.rechercherAdressesParDisciplineEtPays(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple)), eq(paysId)))
                .thenReturn(adresses);

        mockMvc.perform(get("/api/adresses/discipline/{idDiscipline}/pays/{idPays}", disciplineIdExemple, paysId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idAdresse", is(adresseExemple.getIdAdresse().intValue())));
    }

    @Test
    void updateAdresse_Success_shouldReturnUpdatedAdresse() throws Exception {
        Long adresseId = 1L;
        Adresse adresseMaj = Adresse.builder().idAdresse(adresseId).ville("Lyon").pays(paysExemple).build();
        when(adresseService.updateAdresse(eq(adresseId), any(Adresse.class))).thenReturn(adresseMaj);

        mockMvc.perform(put("/api/adresses/{id}", adresseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adresseMaj)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ville", is("Lyon")));
    }

    @Test
    void updateAdresse_IllegalArgumentFromService_shouldReturnBadRequest() throws Exception {
        Long adresseId = 1L;
        Adresse adresseMaj = Adresse.builder().idAdresse(adresseId).ville("Lyon").build();
        String errorMessage = "Pays invalide pour la mise à jour";
        when(adresseService.updateAdresse(eq(adresseId), any(Adresse.class))).thenThrow(new IllegalArgumentException(errorMessage));

        mockMvc.perform(put("/api/adresses/{id}", adresseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adresseMaj)))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(containsString(errorMessage)));
    }

    @Test
    void deleteAdresse_Success_shouldReturnNoContent() throws Exception {
        Long adresseId = 1L;
        doNothing().when(adresseService).deleteAdresse(adresseId);

        mockMvc.perform(delete("/api/adresses/{id}", adresseId))
                .andExpect(status().isNoContent());
        verify(adresseService).deleteAdresse(adresseId);
    }

    @Test
    void deleteAdresse_NotFound_shouldReturnNotFoundStatus() throws Exception {
        Long adresseId = 99L;
        String errorMessage = "Adresse non trouvée";
        doThrow(new ResourceNotFoundException(errorMessage)).when(adresseService).deleteAdresse(adresseId);

        mockMvc.perform(delete("/api/adresses/{id}", adresseId))
                .andExpect(status().isNotFound())
                .andExpect(status().reason(containsString(errorMessage)));
    }

    @Test
    void deleteAdresse_AdresseLieeAUneDisciplineException_shouldReturnConflictStatus() throws Exception {
        Long adresseId = 1L;
        String errorMessage = "Adresse liée à une discipline";
        doThrow(new AdresseLieeAUneDisciplineException(errorMessage)).when(adresseService).deleteAdresse(adresseId);

        mockMvc.perform(delete("/api/adresses/{id}", adresseId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erreur", is("Conflit de ressource")))
                .andExpect(jsonPath("$.message", is(errorMessage)));
    }
}