package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Pays;
import fr.studi.bloc3jo2024.exception.AdresseLieeAUneDisciplineException;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.service.AdresseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour {@link AdresseController}.
 * Ces tests vérifient le comportement du contrôleur en isolation,
 * en moquant le service {@link AdresseService}.
 */
@ExtendWith(MockitoExtension.class)
class AdresseControllerTest {

    @Mock
    private AdresseService adresseService; // Mock du service

    @InjectMocks
    private AdresseController adresseController; // Instance du contrôleur avec les mocks injectés

    // Données de test communes
    private Adresse adresseExemple;
    private Pays paysExemple;
    private UUID utilisateurIdExemple;
    private final Long disciplineIdExemple = 200L;
    private final Long adresseIdExemple = 1L;

    @BeforeEach
    void setUp() {
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
    @DisplayName("Tests pour la création d'adresses (POST /api/adresses)")
    class CreerAdresseTests {

        @Test
        @DisplayName("Doit retourner 200 OK avec l'ID existant si l'adresse existe déjà")
        void creerAdresseSiNonExistante_AdresseExistante_ShouldReturnOkWithExistingId() {
            // Arrange
            Long existingId = 5L;
            when(adresseService.getIdAdresseSiExistante(any(Adresse.class))).thenReturn(existingId);

            // Act
            ResponseEntity<Object> response = adresseController.creerAdresseSiNonExistante(adresseExemple);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode(), "Le statut HTTP doit être 200 OK");
            assertNotNull(response.getBody(), "Le corps de la réponse ne doit pas être null");
            assertInstanceOf(Map.class, response.getBody(), "Le corps doit être une Map");
            Map<?, ?> responseBodyMap = (Map<?, ?>) response.getBody();
            assertEquals(existingId, responseBodyMap.get("idAdresseExistante"), "L'ID de l'adresse existante doit correspondre");

            verify(adresseService).getIdAdresseSiExistante(any(Adresse.class)); // Vérifie l'appel au service
            verify(adresseService, never()).creerAdresseSiNonExistante(any(Adresse.class)); // Vérifie que la création n'est pas appelée
        }

        @Test
        @DisplayName("Doit retourner 201 Created avec la nouvelle adresse si elle n'existe pas")
        void creerAdresseSiNonExistante_NouvelleAdresse_ShouldReturnCreatedWithNewAdresse() {
            // Arrange
            Adresse nouvelleAdresseCreee = Adresse.builder().idAdresse(2L).numeroRue(20).ville("Lyon").pays(paysExemple).build();
            when(adresseService.getIdAdresseSiExistante(any(Adresse.class))).thenReturn(null); // Adresse non existante
            when(adresseService.creerAdresseSiNonExistante(any(Adresse.class))).thenReturn(nouvelleAdresseCreee);

            // Act
            ResponseEntity<Object> response = adresseController.creerAdresseSiNonExistante(adresseExemple);

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Le statut HTTP doit être 201 Created");
            assertNotNull(response.getBody(), "Le corps de la réponse ne doit pas être null");
            assertInstanceOf(Adresse.class, response.getBody(), "Le corps doit être une instance d'Adresse");
            Adresse returnedAdresse = (Adresse) response.getBody();
            assertEquals(nouvelleAdresseCreee.getIdAdresse(), returnedAdresse.getIdAdresse(), "L'ID de l'adresse retournée doit correspondre");
            assertEquals("Lyon", returnedAdresse.getVille(), "La ville de l'adresse retournée doit correspondre");

            verify(adresseService).getIdAdresseSiExistante(any(Adresse.class));
            verify(adresseService).creerAdresseSiNonExistante(any(Adresse.class));
        }

        @Test
        @DisplayName("Doit retourner 400 Bad Request si le service lève IllegalArgumentException")
        void creerAdresseSiNonExistante_IllegalArgumentExceptionFromService_ShouldReturnBadRequest() {
            // Arrange
            String errorMessage = "Données d'adresse invalides";
            when(adresseService.getIdAdresseSiExistante(any(Adresse.class))).thenReturn(null);
            when(adresseService.creerAdresseSiNonExistante(any(Adresse.class))).thenThrow(new IllegalArgumentException(errorMessage));

            // Act & Assert
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> adresseController.creerAdresseSiNonExistante(adresseExemple),
                    "ResponseStatusException attendue");
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode(), "Le statut HTTP doit être 400 Bad Request");
            assertTrue(exception.getReason() != null && exception.getReason().contains(errorMessage), "Le message d'erreur doit correspondre");
        }
    }

    @Nested
    @DisplayName("Tests pour la récupération d'adresses (GET)")
    class GetAdresseTests {

        @Test
        @DisplayName("Doit retourner 200 OK avec l'adresse si trouvée par ID")
        void getAdresseById_Found_ShouldReturnOkWithAdresse() {
            // Arrange
            when(adresseService.getAdresseById(adresseIdExemple)).thenReturn(adresseExemple);

            // Act
            ResponseEntity<Adresse> response = adresseController.getAdresseById(adresseIdExemple);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(adresseExemple, response.getBody());
            verify(adresseService).getAdresseById(adresseIdExemple);
        }

        @Test
        @DisplayName("Doit retourner 404 Not Found si l'adresse n'est pas trouvée par ID")
        void getAdresseById_NotFound_ShouldReturnNotFound() {
            // Arrange
            Long idInexistant = 99L;
            String errorMessage = "Adresse non trouvée";
            when(adresseService.getAdresseById(idInexistant)).thenThrow(new ResourceNotFoundException(errorMessage));

            // Act & Assert
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> adresseController.getAdresseById(idInexistant));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertTrue(exception.getReason() != null && exception.getReason().contains(errorMessage));
            verify(adresseService).getAdresseById(idInexistant);
        }

        @Test
        @DisplayName("Doit retourner 200 OK avec une page d'adresses")
        void getAllAdresses_ShouldReturnOkWithPageOfAdresses() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Adresse> adressesList = Collections.singletonList(adresseExemple);
            Page<Adresse> adressesPage = new PageImpl<>(adressesList, pageable, adressesList.size());
            // Assumes AdresseService.getAllAdresses returns Page<Adresse>
            when(adresseService.getAllAdresses(pageable)).thenReturn(adressesPage);

            // Act
            ResponseEntity<Page<Adresse>> response = adresseController.getAllAdresses(pageable);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getTotalElements());
            assertEquals(adresseExemple.getVille(), response.getBody().getContent().getFirst().getVille());
            verify(adresseService).getAllAdresses(pageable);
        }

        @Test
        @DisplayName("Doit retourner 200 OK avec la liste des adresses pour un utilisateur")
        void getAdressesByUserId_ShouldReturnOkWithListOfAdresses() {
            // Arrange
            List<Adresse> adressesUtilisateur = Collections.singletonList(adresseExemple);
            when(adresseService.getAdressesByUtilisateurId(utilisateurIdExemple)).thenReturn(adressesUtilisateur);

            // Act
            ResponseEntity<List<Adresse>> response = adresseController.getAdressesByUserId(utilisateurIdExemple);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(adressesUtilisateur, response.getBody());
            verify(adresseService).getAdressesByUtilisateurId(utilisateurIdExemple);
        }

        @Test
        @DisplayName("Doit retourner 200 OK avec l'adresse pour une discipline")
        void getAdresseByDiscipline_Found_ShouldReturnOkWithAdresse() {
            // Arrange
            when(adresseService.getAdresseByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple)))).thenReturn(adresseExemple);

            // Act
            ResponseEntity<Adresse> response = adresseController.getAdresseByDiscipline(disciplineIdExemple);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(adresseExemple, response.getBody());
            verify(adresseService).getAdresseByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple)));
        }

        @Test
        @DisplayName("Doit retourner 404 Not Found si aucune adresse n'est trouvée pour une discipline")
        void getAdresseByDiscipline_NotFound_ShouldReturnNotFound() {
            // Arrange
            String errorMessage = "Adresse non trouvée pour cette discipline";
            when(adresseService.getAdresseByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple))))
                    .thenThrow(new ResourceNotFoundException(errorMessage));

            // Act & Assert
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> adresseController.getAdresseByDiscipline(disciplineIdExemple));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertTrue(exception.getReason() != null && exception.getReason().contains(errorMessage));
        }

        @Test
        @DisplayName("Doit retourner 400 Bad Request pour getAdresseByDiscipline si IllegalArgumentException")
        void getAdresseByDiscipline_IllegalArgument_ShouldReturnBadRequest() {
            // Arrange
            String errorMessage = "ID Discipline invalide";
            when(adresseService.getAdresseByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple))))
                    .thenThrow(new IllegalArgumentException(errorMessage));

            // Act & Assert
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> adresseController.getAdresseByDiscipline(disciplineIdExemple));
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertTrue(exception.getReason() != null && exception.getReason().contains(errorMessage));
        }


        @Test
        @DisplayName("Doit retourner 200 OK avec la liste des adresses pour une discipline (/all)")
        void getAdressesByDiscipline_All_ShouldReturnOkWithList() {
            // Arrange
            List<Adresse> adressesPourDiscipline = Collections.singletonList(adresseExemple);
            when(adresseService.getAdressesByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple))))
                    .thenReturn(adressesPourDiscipline);

            // Act
            ResponseEntity<List<Adresse>> response = adresseController.getAdressesByDiscipline(disciplineIdExemple); // This calls the /all variant implicitly due to distinct method signature in controller based on path

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(adressesPourDiscipline, response.getBody());
            verify(adresseService).getAdressesByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple)));
        }

        @Test
        @DisplayName("Doit retourner 400 Bad Request pour getAdressesByDiscipline (/all) si IllegalArgumentException")
        void getAdressesByDiscipline_All_IllegalArgument_ShouldReturnBadRequest() {
            // Arrange
            String errorMessage = "ID Discipline invalide pour /all";
            when(adresseService.getAdressesByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple))))
                    .thenThrow(new IllegalArgumentException(errorMessage));

            // Act & Assert
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> adresseController.getAdressesByDiscipline(disciplineIdExemple)); // Calls the /all variant
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertTrue(exception.getReason() != null && exception.getReason().contains(errorMessage));
        }


        @Test
        @DisplayName("Doit retourner 200 OK avec l'adresse trouvée par recherche complète")
        void rechercherAdresseComplete_Found_ShouldReturnOk() {
            // Arrange
            Integer numeroRue = 10; String nomRue = "Rue Test"; String ville = "Testville"; String cp = "12345"; Long idPays = 1L;
            when(adresseService.rechercherAdresseComplete(eq(numeroRue), eq(nomRue), eq(ville), eq(cp), argThat(p -> p.getIdPays().equals(idPays))))
                    .thenReturn(adresseExemple);

            // Act
            ResponseEntity<Adresse> response = adresseController.rechercherAdresseComplete(numeroRue, nomRue, ville, cp, idPays);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(adresseExemple, response.getBody());
        }

        @Test
        @DisplayName("Doit retourner 404 Not Found si la recherche complète ne trouve rien")
        void rechercherAdresseComplete_NotFound_ShouldReturnNotFound() {
            // Arrange
            Integer numeroRue = 10; String nomRue = "Rue Test"; String ville = "Testville"; String cp = "12345"; Long idPays = 1L;
            when(adresseService.rechercherAdresseComplete(eq(numeroRue), eq(nomRue), eq(ville), eq(cp), argThat(p -> p.getIdPays().equals(idPays))))
                    .thenReturn(null); // Service retourne null si non trouvé

            // Act
            ResponseEntity<Adresse> response = adresseController.rechercherAdresseComplete(numeroRue, nomRue, ville, cp, idPays);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Doit retourner 400 Bad Request pour recherche complète si IllegalArgumentException")
        void rechercherAdresseComplete_IllegalArgument_ShouldReturnBadRequest() {
            // Arrange
            Integer numeroRue = 10; String nomRue = "Rue Test"; String ville = "Testville"; String cp = "12345"; Long idPays = 1L;
            String errorMessage = "Paramètres de recherche invalides";
            when(adresseService.rechercherAdresseComplete(eq(numeroRue), eq(nomRue), eq(ville), eq(cp), argThat(p -> p.getIdPays().equals(idPays))))
                    .thenThrow(new IllegalArgumentException(errorMessage));

            // Act & Assert
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> adresseController.rechercherAdresseComplete(numeroRue, nomRue, ville, cp, idPays));
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertTrue(exception.getReason() != null && exception.getReason().contains(errorMessage));
        }


        @Test
        @DisplayName("Doit retourner 200 OK avec la liste des adresses par ville")
        void rechercherAdressesParVillePourDisciplines_ShouldReturnOkWithList() {
            // Arrange
            String ville = "Paris";
            List<Adresse> adressesTrouvees = Collections.singletonList(adresseExemple);
            when(adresseService.rechercherAdressesParVillePourDisciplines(ville)).thenReturn(adressesTrouvees);

            // Act
            ResponseEntity<List<Adresse>> response = adresseController.rechercherAdressesParVillePourDisciplines(ville);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(adressesTrouvees, response.getBody());
        }

        @Test
        @DisplayName("Doit retourner 200 OK avec la liste des adresses par discipline et pays")
        void rechercherAdressesParDisciplineEtPays_ShouldReturnOkWithList() {
            // Arrange
            Long paysId = 1L;
            List<Adresse> adressesTrouvees = Collections.singletonList(adresseExemple);
            when(adresseService.rechercherAdressesParDisciplineEtPays(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple)), eq(paysId)))
                    .thenReturn(adressesTrouvees);

            // Act
            ResponseEntity<List<Adresse>> response = adresseController.rechercherAdressesParDisciplineEtPays(disciplineIdExemple, paysId);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(adressesTrouvees, response.getBody());
        }
    }

    @Nested
    @DisplayName("Tests pour la mise à jour d'adresses (PUT /api/adresses/{id})")
    class UpdateAdresseTests {

        @Test
        @DisplayName("Doit retourner 200 OK avec l'adresse mise à jour")
        void updateAdresse_Success_ShouldReturnOkWithUpdatedAdresse() {
            // Arrange
            Adresse adresseMiseAJour = Adresse.builder().idAdresse(adresseIdExemple).ville("Lyon").pays(paysExemple).build();
            when(adresseService.updateAdresse(eq(adresseIdExemple), any(Adresse.class))).thenReturn(adresseMiseAJour);

            // Act
            ResponseEntity<Adresse> response = adresseController.updateAdresse(adresseIdExemple, adresseMiseAJour);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(adresseMiseAJour, response.getBody());
        }

        @Test
        @DisplayName("Doit retourner 404 Not Found si l'adresse à mettre à jour n'existe pas")
        void updateAdresse_NotFound_ShouldReturnNotFound() {
            // Arrange
            Long idInexistant = 99L;
            String errorMessage = "Adresse non trouvée pour la mise à jour";
            when(adresseService.updateAdresse(eq(idInexistant), any(Adresse.class))).thenThrow(new ResourceNotFoundException(errorMessage));

            // Act & Assert
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> adresseController.updateAdresse(idInexistant, adresseExemple));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertTrue(exception.getReason() != null && exception.getReason().contains(errorMessage));
        }

        @Test
        @DisplayName("Doit retourner 400 Bad Request si les données de mise à jour sont invalides")
        void updateAdresse_IllegalArgument_ShouldReturnBadRequest() {
            // Arrange
            String errorMessage = "Données de mise à jour invalides";
            when(adresseService.updateAdresse(eq(adresseIdExemple), any(Adresse.class))).thenThrow(new IllegalArgumentException(errorMessage));

            // Act & Assert
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> adresseController.updateAdresse(adresseIdExemple, adresseExemple));
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertTrue(exception.getReason() != null && exception.getReason().contains(errorMessage));
        }
    }

    @Nested
    @DisplayName("Tests pour la suppression d'adresses (DELETE /api/adresses/{id})")
    class DeleteAdresseTests {

        @Test
        @DisplayName("Doit retourner 204 No Content si la suppression réussit")
        void deleteAdresse_Success_ShouldReturnNoContent() {
            // Arrange
            doNothing().when(adresseService).deleteAdresse(adresseIdExemple);

            // Act
            ResponseEntity<Void> response = adresseController.deleteAdresse(adresseIdExemple);

            // Assert
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(adresseService).deleteAdresse(adresseIdExemple);
        }

        @Test
        @DisplayName("Doit retourner 404 Not Found si l'adresse à supprimer n'existe pas")
        void deleteAdresse_NotFound_ShouldReturnNotFound() {
            // Arrange
            Long idInexistant = 99L;
            String errorMessage = "Adresse non trouvée pour suppression";
            doThrow(new ResourceNotFoundException(errorMessage)).when(adresseService).deleteAdresse(idInexistant);

            // Act & Assert
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> adresseController.deleteAdresse(idInexistant));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertTrue(exception.getReason() != null && exception.getReason().contains(errorMessage));
        }

        @Test
        @DisplayName("Doit retourner 409 Conflict si l'adresse est liée à une discipline")
        void deleteAdresse_LinkedToDiscipline_ShouldReturnConflict() {
            // Arrange
            String errorMessage = "Adresse liée, ne peut être supprimée";
            doThrow(new AdresseLieeAUneDisciplineException(errorMessage)).when(adresseService).deleteAdresse(adresseIdExemple);

            // Act & Assert
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> adresseController.deleteAdresse(adresseIdExemple));
            assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
            assertTrue(exception.getReason() != null && exception.getReason().contains(errorMessage));
        }
    }
}