package fr.studi.bloc3jo2024.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Pays;
import fr.studi.bloc3jo2024.exception.AdresseLieeAUneDisciplineException;
import fr.studi.bloc3jo2024.exception.GlobalExceptionHandler;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.service.AdresseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

/**
 * Tests d'intégration MVC pour {@link fr.studi.bloc3jo2024.controller.AdresseController}.
 * Ces tests utilisent {@link MockMvc} pour simuler les requêtes HTTP et vérifier les réponses
 * sans démarrer un serveur complet. Le service {@link AdresseService} est moqué.
 * Le {@link GlobalExceptionHandler} est importé pour tester la gestion des exceptions.
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser // Simule un utilisateur authentifié pour les tests de sécurité
@Import(GlobalExceptionHandler.class)
class AdresseControllerMvcTest {

    @Autowired
    private MockMvc mockMvc; // Permet de simuler des appels HTTP

    @MockitoBean // Crée un mock du service AdresseService et l'injecte dans le contexte
    private AdresseService adresseService;

    @Autowired
    private ObjectMapper objectMapper; // Utilisé pour convertir les objets Java en JSON et vice-versa

    // Données de test communes
    private Adresse adresseExemple;
    private Pays paysExemple;
    private UUID utilisateurIdExemple;
    private final Long disciplineIdExemple = 200L;
    private final Long adresseIdExemple = 1L;


    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();

        // Initialisation des données de test avant chaque test
        paysExemple = Pays.builder().idPays(1L).nomPays("France").build();
        adresseExemple = Adresse.builder()
                .idAdresse(adresseIdExemple)
                .numeroRue(10)
                .nomRue("Rue de la Paix")
                .ville("Paris")
                .codePostal("75001")
                .pays(paysExemple)
                .build();
        utilisateurIdExemple = UUID.randomUUID();
    }

    @Nested
    @DisplayName("POST /api/adresses - Création d'adresses")
    class CreerAdresseTests {

        @Test
        @DisplayName("Doit retourner 200 OK avec l'ID existant si l'adresse existe déjà")
        void creerAdresseSiNonExistante_AdresseExistante_shouldReturnOkWithExistingId() throws Exception {
            // Arrange
            Long existingId = 5L;
            when(adresseService.getIdAdresseSiExistante(any(Adresse.class))).thenReturn(existingId);

            // Act & Assert
            mockMvc.perform(post("/api/adresses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(adresseExemple)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idAdresseExistante", is(existingId.intValue())));

            verify(adresseService).getIdAdresseSiExistante(any(Adresse.class));
            verify(adresseService, never()).creerAdresseSiNonExistante(any(Adresse.class));
        }

        @Test
        @DisplayName("Doit retourner 201 Created avec la nouvelle adresse si elle n'existe pas")
        void creerAdresseSiNonExistante_NouvelleAdresse_shouldReturnCreatedWithAdresse() throws Exception {
            // Arrange
            Adresse nouvelleAdresseCreee = Adresse.builder().idAdresse(2L).numeroRue(20).ville("Lyon").pays(paysExemple).build();
            when(adresseService.getIdAdresseSiExistante(any(Adresse.class))).thenReturn(null);
            when(adresseService.creerAdresseSiNonExistante(any(Adresse.class))).thenReturn(nouvelleAdresseCreee);

            // Act & Assert
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
        @DisplayName("Doit retourner 400 Bad Request si le service lève IllegalArgumentException")
        void creerAdresseSiNonExistante_IllegalArgumentExceptionFromService_shouldReturnBadRequest() throws Exception {
            // Arrange
            String errorMessage = "Données d'adresse non valides";
            when(adresseService.getIdAdresseSiExistante(any(Adresse.class))).thenReturn(null);
            when(adresseService.creerAdresseSiNonExistante(any(Adresse.class))).thenThrow(new IllegalArgumentException(errorMessage));

            // Act & Assert
            mockMvc.perform(post("/api/adresses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(adresseExemple)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", is(errorMessage)));
        }

        @Test
        @DisplayName("Doit retourner 404 Not Found si le service lève ResourceNotFoundException (ex: Pays non trouvé lors de la création)")
        void creerAdresseSiNonExistante_ResourceNotFoundExceptionFromService_shouldReturnNotFound() throws Exception {
            // Arrange
            String errorMessage = "Ressource parente non trouvée (ex: Pays)";
            when(adresseService.getIdAdresseSiExistante(any(Adresse.class))).thenReturn(null);
            when(adresseService.creerAdresseSiNonExistante(any(Adresse.class))).thenThrow(new ResourceNotFoundException(errorMessage));

            // Act & Assert
            mockMvc.perform(post("/api/adresses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(adresseExemple)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(is(errorMessage)));
        }
    }

    @Nested
    @DisplayName("GET /api/adresses - Récupération d'adresses")
    class GetAdresseTests {

        @Test
        @DisplayName("Doit retourner 200 OK avec l'adresse si trouvée par ID")
        void getAdresseById_Found_shouldReturnAdresse() throws Exception {
            // Arrange
            when(adresseService.getAdresseById(adresseIdExemple)).thenReturn(adresseExemple);

            // Act & Assert
            mockMvc.perform(get("/api/adresses/{id}", adresseIdExemple))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idAdresse", is(adresseExemple.getIdAdresse().intValue())))
                    .andExpect(jsonPath("$.ville", is(adresseExemple.getVille())));
            verify(adresseService).getAdresseById(adresseIdExemple);
        }

        @Test
        @DisplayName("Doit retourner 404 Not Found si l'adresse n'est pas trouvée par ID")
        void getAdresseById_NotFound_shouldReturnNotFoundStatus() throws Exception {
            // Arrange
            Long idInexistant = 99L;
            String errorMessage = "Adresse non trouvée avec l'ID : " + idInexistant;
            when(adresseService.getAdresseById(idInexistant)).thenThrow(new ResourceNotFoundException(errorMessage));

            // Act & Assert
            mockMvc.perform(get("/api/adresses/{id}", idInexistant))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(is(errorMessage)));
            verify(adresseService).getAdresseById(idInexistant);
        }

        @Test
        @DisplayName("Doit retourner 200 OK avec une page d'adresses pour getAllAdresses")
        void getAllAdresses_Success_shouldReturnPagedAdresses() throws Exception {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Adresse> adressesList = Collections.singletonList(adresseExemple);
            Page<Adresse> adressesPage = new PageImpl<>(adressesList, pageable, adressesList.size());
            when(adresseService.getAllAdresses(any(Pageable.class))).thenReturn(adressesPage);

            // Act & Assert
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
        @DisplayName("Doit retourner 200 OK avec la liste des adresses pour un utilisateur")
        void getAdressesByUserId_Success_shouldReturnListOfAdresses() throws Exception {
            // Arrange
            List<Adresse> adresses = Collections.singletonList(adresseExemple);
            when(adresseService.getAdressesByUtilisateurId(utilisateurIdExemple)).thenReturn(adresses);

            // Act & Assert
            mockMvc.perform(get("/api/adresses/utilisateur/{idUtilisateur}", utilisateurIdExemple))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].ville", is(adresseExemple.getVille())));
            verify(adresseService).getAdressesByUtilisateurId(utilisateurIdExemple);
        }

        @Test
        @DisplayName("Doit retourner 200 OK avec l'adresse pour une discipline")
        void getAdresseByDiscipline_Success_shouldReturnAdresse() throws Exception {
            // Arrange
            when(adresseService.getAdresseByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple))))
                    .thenReturn(adresseExemple);

            // Act & Assert
            mockMvc.perform(get("/api/adresses/discipline/{idDiscipline}", disciplineIdExemple))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ville", is(adresseExemple.getVille())));
            verify(adresseService).getAdresseByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple)));
        }

        @Test
        @DisplayName("Doit retourner 404 Not Found si aucune adresse pour la discipline")
        void getAdresseByDiscipline_NotFound_shouldReturnNotFound() throws Exception {
            // Arrange
            String errorMessage = "Adresse non trouvée pour la discipline";
            when(adresseService.getAdresseByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple))))
                    .thenThrow(new ResourceNotFoundException(errorMessage));

            // Act & Assert
            mockMvc.perform(get("/api/adresses/discipline/{idDiscipline}", disciplineIdExemple))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(is(errorMessage)));
        }

        @Test
        @DisplayName("Doit retourner 400 Bad Request pour getAdresseByDiscipline si IllegalArgumentException")
        void getAdresseByDiscipline_IllegalArgumentFromService_shouldReturnBadRequest() throws Exception {
            // Arrange
            String errorMessage = "ID de discipline invalide";
            when(adresseService.getAdresseByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple))))
                    .thenThrow(new IllegalArgumentException(errorMessage));

            // Act & Assert
            mockMvc.perform(get("/api/adresses/discipline/{idDiscipline}", disciplineIdExemple))
                    .andExpect(status().isBadRequest())
                    // Vérifie le message d'erreur dans le corps JSON (via AuthReponseDto)
                    .andExpect(jsonPath("$.message", is(errorMessage)));
        }

        @Test
        @DisplayName("Doit retourner 200 OK avec la liste des adresses pour une discipline (endpoint /all)")
        void getAdressesByDiscipline_All_Success_shouldReturnListOfAdresses() throws Exception {
            // Arrange
            List<Adresse> adresses = Collections.singletonList(adresseExemple);
            when(adresseService.getAdressesByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple))))
                    .thenReturn(adresses);

            // Act & Assert
            mockMvc.perform(get("/api/adresses/discipline/{idDiscipline}/all", disciplineIdExemple))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].ville", is(adresseExemple.getVille())));
            verify(adresseService).getAdressesByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple)));
        }

        @Test
        @DisplayName("Doit retourner 400 Bad Request pour getAdressesByDiscipline (/all) si IllegalArgumentException")
        void getAdressesByDiscipline_All_IllegalArgumentFromService_shouldReturnBadRequest() throws Exception {
            // Arrange
            String errorMessage = "ID de discipline invalide pour /all";
            when(adresseService.getAdressesByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple))))
                    .thenThrow(new IllegalArgumentException(errorMessage));

            // Act & Assert
            mockMvc.perform(get("/api/adresses/discipline/{idDiscipline}/all", disciplineIdExemple))
                    .andExpect(status().isBadRequest())
                    // Vérifie le message d'erreur dans le corps JSON (via AuthReponseDto)
                    .andExpect(jsonPath("$.message", is(errorMessage)));
        }

        @Test
        @DisplayName("Doit retourner 200 OK avec l'adresse trouvée par recherche complète")
        void rechercherAdresseComplete_Found_shouldReturnAdresse() throws Exception {
            // Arrange
            Integer numeroRue = 10; String nomRue = "Rue de la Paix"; String ville = "Paris"; String codePostal = "75001"; Long idPays = 1L;
            when(adresseService.rechercherAdresseComplete(eq(numeroRue), eq(nomRue), eq(ville), eq(codePostal), argThat(p -> p.getIdPays().equals(idPays))))
                    .thenReturn(adresseExemple);

            // Act & Assert
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
        @DisplayName("Doit retourner 404 Not Found si la recherche complète ne trouve rien")
        void rechercherAdresseComplete_NotFound_shouldReturnNotFoundStatus() throws Exception {
            // Arrange
            Integer numeroRue = 10; String nomRue = "Rue Introuvable"; String ville = "Paris"; String codePostal = "75001"; Long idPays = 1L;
            when(adresseService.rechercherAdresseComplete(eq(numeroRue), eq(nomRue), eq(ville), eq(codePostal), argThat(p -> p.getIdPays().equals(idPays))))
                    .thenReturn(null); // Le service retourne null si non trouvé, le contrôleur devrait convertir cela en 404.

            // Act & Assert
            mockMvc.perform(get("/api/adresses/recherche")
                            .param("numeroRue", numeroRue.toString())
                            .param("nomRue", nomRue)
                            .param("ville", ville)
                            .param("codePostal", codePostal)
                            .param("idPays", idPays.toString()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Doit retourner 400 Bad Request pour recherche complète si IllegalArgumentException")
        void rechercherAdresseComplete_IllegalArgumentFromService_shouldReturnBadRequest() throws Exception {
            // Arrange
            Integer numeroRue = 10; String nomRue = "Rue de la Paix"; String ville = "Paris"; String codePostal = "75001"; Long idPays = 1L;
            String errorMessage = "ID Pays manquant";
            when(adresseService.rechercherAdresseComplete(eq(numeroRue), eq(nomRue), eq(ville), eq(codePostal), argThat(p -> p.getIdPays().equals(idPays))))
                    .thenThrow(new IllegalArgumentException(errorMessage));

            // Act & Assert
            mockMvc.perform(get("/api/adresses/recherche")
                            .param("numeroRue", numeroRue.toString())
                            .param("nomRue", nomRue)
                            .param("ville", ville)
                            .param("codePostal", codePostal)
                            .param("idPays", idPays.toString()))
                    .andExpect(status().isBadRequest())
                    // Vérifie le message d'erreur dans le corps JSON (via AuthReponseDto)
                    .andExpect(jsonPath("$.message", is(errorMessage)));
        }

        @Test
        @DisplayName("Doit retourner 200 OK avec la liste des adresses par ville")
        void rechercherAdressesParVillePourDisciplines_Success_shouldReturnListOfAdresses() throws Exception {
            // Arrange
            String ville = "Paris";
            List<Adresse> adresses = Collections.singletonList(adresseExemple);
            when(adresseService.rechercherAdressesParVillePourDisciplines(ville)).thenReturn(adresses);

            // Act & Assert
            mockMvc.perform(get("/api/adresses/ville/{ville}", ville))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].ville", is(ville)));
        }

        @Test
        @DisplayName("Doit retourner 200 OK avec la liste des adresses par discipline et pays")
        void rechercherAdressesParDisciplineEtPays_Success_shouldReturnListOfAdresses() throws Exception {
            // Arrange
            Long paysId = 1L;
            List<Adresse> adresses = Collections.singletonList(adresseExemple);
            when(adresseService.rechercherAdressesParDisciplineEtPays(
                    argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple)), eq(paysId)))
                    .thenReturn(adresses);

            // Act & Assert
            mockMvc.perform(get("/api/adresses/discipline/{idDiscipline}/pays/{idPays}", disciplineIdExemple, paysId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].idAdresse", is(adresseExemple.getIdAdresse().intValue())));
        }
    }

    @Nested
    @DisplayName("PUT /api/adresses/{id} - Mise à jour d'adresses")
    class UpdateAdresseTests {

        @Test
        @DisplayName("Doit retourner 200 OK avec l'adresse mise à jour")
        void updateAdresse_Success_shouldReturnUpdatedAdresse() throws Exception {
            // Arrange
            Adresse adresseMiseAJour = Adresse.builder().idAdresse(adresseIdExemple).ville("Lyon").pays(paysExemple).build();
            when(adresseService.updateAdresse(eq(adresseIdExemple), any(Adresse.class))).thenReturn(adresseMiseAJour);

            // Act & Assert
            mockMvc.perform(put("/api/adresses/{id}", adresseIdExemple)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(adresseMiseAJour)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ville", is("Lyon")));
        }

        @Test
        @DisplayName("Doit retourner 404 Not Found si l'adresse à mettre à jour n'existe pas")
        void updateAdresse_NotFound_shouldReturnNotFound() throws Exception {
            // Arrange
            String errorMessage = "Adresse non trouvée pour la mise à jour";
            when(adresseService.updateAdresse(eq(adresseIdExemple), any(Adresse.class)))
                    .thenThrow(new ResourceNotFoundException(errorMessage));

            // Act & Assert
            mockMvc.perform(put("/api/adresses/{id}", adresseIdExemple)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(adresseExemple)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(is(errorMessage)));
        }

        @Test
        @DisplayName("Doit retourner 400 Bad Request pour updateAdresse si IllegalArgumentException")
        void updateAdresse_IllegalArgumentFromService_shouldReturnBadRequest() throws Exception {
            // Arrange
            String errorMessage = "Données de mise à jour invalides (ex: Pays manquant)";
            when(adresseService.updateAdresse(eq(adresseIdExemple), any(Adresse.class))).thenThrow(new IllegalArgumentException(errorMessage));

            // Act & Assert
            mockMvc.perform(put("/api/adresses/{id}", adresseIdExemple)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(adresseExemple)))
                    .andExpect(status().isBadRequest())
                    // Vérifie le message d'erreur dans le corps JSON (via AuthReponseDto)
                    .andExpect(jsonPath("$.message", is(errorMessage)));
        }
    }

    @Nested
    @DisplayName("DELETE /api/adresses/{id} - Suppression d'adresses")
    class DeleteAdresseTests {

        @Test
        @DisplayName("Doit retourner 204 No Content si la suppression réussit")
        void deleteAdresse_Success_shouldReturnNoContent() throws Exception {
            // Arrange
            doNothing().when(adresseService).deleteAdresse(adresseIdExemple);

            // Act & Assert
            mockMvc.perform(delete("/api/adresses/{id}", adresseIdExemple))
                    .andExpect(status().isNoContent());
            verify(adresseService).deleteAdresse(adresseIdExemple);
        }

        @Test
        @DisplayName("Doit retourner 404 Not Found si l'adresse à supprimer n'existe pas")
        void deleteAdresse_NotFound_shouldReturnNotFoundStatus() throws Exception {
            // Arrange
            Long idInexistant = 99L;
            String errorMessage = "Adresse non trouvée pour suppression";
            doThrow(new ResourceNotFoundException(errorMessage)).when(adresseService).deleteAdresse(idInexistant);

            // Act & Assert
            mockMvc.perform(delete("/api/adresses/{id}", idInexistant))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(is(errorMessage)));
        }

        @Test
        @DisplayName("Doit retourner 409 Conflict si l'adresse est liée à une discipline")
        void deleteAdresse_AdresseLieeAUneDisciplineException_shouldReturnConflictStatus() throws Exception {
            // Arrange
            Long adresseLieeId = 1L;
            String errorMessage = "L'adresse est liée à une ou plusieurs disciplines et ne peut pas être supprimée.";
            doThrow(new AdresseLieeAUneDisciplineException(errorMessage)).when(adresseService).deleteAdresse(adresseLieeId);

            // Act & Assert
            // Ce test s'attend à ce que GlobalExceptionHandler retourne 409 et un corps JSON spécifique.
            mockMvc.perform(delete("/api/adresses/{id}", adresseLieeId))
                    .andExpect(status().isConflict()) // Attend 409 Conflict
                    .andExpect(jsonPath("$.erreur", is("Conflit de ressource")))
                    .andExpect(jsonPath("$.message", is(errorMessage)));
        }
    }
}