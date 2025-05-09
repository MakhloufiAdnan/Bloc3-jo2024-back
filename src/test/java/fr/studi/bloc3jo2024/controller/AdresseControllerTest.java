package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Pays;
import fr.studi.bloc3jo2024.exception.AdresseLieeAUneDisciplineException;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.service.AdresseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdresseControllerTest {

    @Mock
    private AdresseService adresseService;

    @InjectMocks
    private AdresseController adresseController;

    private Adresse adresseExemple;
    private UUID utilisateurIdExemple;

    @BeforeEach
    void setUp() {
        // Arrange
        Pays paysExemple;
        paysExemple = new Pays();
        paysExemple.setIdPays(1L);
        paysExemple.setNomPays("France");

        adresseExemple = new Adresse();
        adresseExemple.setIdAdresse(1L);
        adresseExemple.setNumeroRue(10);
        adresseExemple.setNomRue("Rue de la Paix");
        adresseExemple.setVille("Paris");
        adresseExemple.setCodePostal("75001");
        adresseExemple.setPays(paysExemple);

        Discipline disciplineExemple;
        disciplineExemple = new Discipline();
        disciplineExemple.setIdDiscipline(200L);

        utilisateurIdExemple = UUID.randomUUID();
    }

    @Test
    void creerAdresseSiNonExistante_AdresseExistante() {
        // Arrange
        Long existingId = 5L;
        when(adresseService.getIdAdresseSiExistante(any(Adresse.class))).thenReturn(existingId);

        // Act
        ResponseEntity<Object> response = adresseController.creerAdresseSiNonExistante(adresseExemple);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(existingId, response.getBody());
        verify(adresseService, times(1)).getIdAdresseSiExistante(any(Adresse.class));
        verify(adresseService, never()).creerAdresseSiNonExistante(any(Adresse.class));
    }

    @Test
    void creerAdresseSiNonExistante_NouvelleAdresse() {
        // Arrange
        Adresse nouvelleAdresseCreee = new Adresse();
        nouvelleAdresseCreee.setIdAdresse(2L);
        nouvelleAdresseCreee.setNumeroRue(20);
        when(adresseService.getIdAdresseSiExistante(any(Adresse.class))).thenReturn(null);
        when(adresseService.creerAdresseSiNonExistante(any(Adresse.class))).thenReturn(nouvelleAdresseCreee);

        // Act
        ResponseEntity<Object> response = adresseController.creerAdresseSiNonExistante(adresseExemple);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(Adresse.class, response.getBody());
        Adresse returnedAdresse = (Adresse) response.getBody();
        assertEquals(nouvelleAdresseCreee.getIdAdresse(), returnedAdresse.getIdAdresse());
        verify(adresseService, times(1)).getIdAdresseSiExistante(any(Adresse.class));
        verify(adresseService, times(1)).creerAdresseSiNonExistante(any(Adresse.class));
    }

    // --- Tests Endpoint GET /api/adresses/{id} (getAdresseById) ---

    @Test
    void getAdresseById_Success() {
        // Arrange
        Long adresseId = 1L;
        when(adresseService.getAdresseById(adresseId)).thenReturn(adresseExemple);

        // Act
        ResponseEntity<Adresse> response = adresseController.getAdresseById(adresseId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(adresseId, response.getBody().getIdAdresse());
        verify(adresseService, times(1)).getAdresseById(adresseId);
    }

    @Test
    void getAdresseById_NotFound() {
        // Arrange
        Long adresseId = 99L;
        when(adresseService.getAdresseById(adresseId)).thenThrow(ResourceNotFoundException.class);

        // Act
        ResponseEntity<Adresse> response = adresseController.getAdresseById(adresseId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(adresseService, times(1)).getAdresseById(adresseId);
    }

    // --- Tests Endpoint GET /api/adresses/utilisateur/{idUtilisateur} (getAdressesByUtilisateurId) ---

    @Test
    void getAdressesByUtilisateurId_Success() {
        // Arrange
        List<Adresse> adresses = Arrays.asList(adresseExemple, new Adresse()); // Liste avec une ou plusieurs adresses
        when(adresseService.getAdressesByUtilisateurId(utilisateurIdExemple)).thenReturn(adresses);

        // Act
        ResponseEntity<List<Adresse>> response = adresseController.getAdressesByUtilisateurId(utilisateurIdExemple);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(adresseService, times(1)).getAdressesByUtilisateurId(utilisateurIdExemple);
    }

    @Test
    void getAdressesByUtilisateurId_EmptyList() {
        // Arrange
        List<Adresse> adresses = Collections.emptyList(); // Liste vide
        when(adresseService.getAdressesByUtilisateurId(utilisateurIdExemple)).thenReturn(adresses);

        // Act
        ResponseEntity<List<Adresse>> response = adresseController.getAdressesByUtilisateurId(utilisateurIdExemple);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(adresseService, times(1)).getAdressesByUtilisateurId(utilisateurIdExemple);
    }

    // --- Tests Endpoint GET /api/adresses/discipline/{idDiscipline} (getAdresseByDiscipline) ---

    @Test
    void getAdresseByDiscipline_Success() {
        // Arrange
        Long disciplineId = 200L;
        // Créer une instance de Discipline avec le bon ID pour le match de Mockito
        Discipline disciplineArgument = new Discipline();
        disciplineArgument.setIdDiscipline(disciplineId);
        when(adresseService.getAdresseByDiscipline(any(Discipline.class))).thenReturn(adresseExemple); // Matcher sur n'importe quelle instance de Discipline

        // Act
        ResponseEntity<Adresse> response = adresseController.getAdresseByDiscipline(disciplineId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(adresseExemple.getIdAdresse(), response.getBody().getIdAdresse());
        // Vérifier que le service a été appelé avec une instance de Discipline ayant le bon ID
        verify(adresseService, times(1)).getAdresseByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineId)));
    }

    @Test
    void getAdresseByDiscipline_NotFound() {
        // Arrange
        Long disciplineId = 999L;
        Discipline disciplineArgument = new Discipline();
        disciplineArgument.setIdDiscipline(disciplineId);
        when(adresseService.getAdresseByDiscipline(any(Discipline.class))).thenThrow(ResourceNotFoundException.class); // Matcher sur n'importe quelle instance de Discipline

        // Act
        ResponseEntity<Adresse> response = adresseController.getAdresseByDiscipline(disciplineId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(adresseService, times(1)).getAdresseByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineId)));
    }

    // --- Tests Endpoint GET /api/adresses/discipline/{idDiscipline}/all (getAdressesByDiscipline) ---

    @Test
    void getAdressesByDiscipline_All_Success() {
        // Arrange
        Long disciplineId = 200L;
        // Créer une instance de Discipline avec le bon ID pour le match de Mockito
        Discipline disciplineArgument = new Discipline();
        disciplineArgument.setIdDiscipline(disciplineId);
        List<Adresse> adresses = Arrays.asList(adresseExemple, new Adresse()); // Liste avec une ou plusieurs adresses
        when(adresseService.getAdressesByDiscipline(any(Discipline.class))).thenReturn(adresses); // Matcher sur n'importe quelle instance de Discipline

        // Act
        ResponseEntity<List<Adresse>> response = adresseController.getAdressesByDiscipline(disciplineId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        // Vérifier que le service a été appelé avec une instance de Discipline ayant le bon ID
        verify(adresseService, times(1)).getAdressesByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineId)));
    }

    @Test
    void getAdressesByDiscipline_All_EmptyList() {
        // Arrange
        Long disciplineId = 999L;
        Discipline disciplineArgument = new Discipline();
        disciplineArgument.setIdDiscipline(disciplineId);
        List<Adresse> adresses = Collections.emptyList(); // Liste vide
        when(adresseService.getAdressesByDiscipline(any(Discipline.class))).thenReturn(adresses); // Matcher sur n'importe quelle instance de Discipline

        // Act
        ResponseEntity<List<Adresse>> response = adresseController.getAdressesByDiscipline(disciplineId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(adresseService, times(1)).getAdressesByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineId)));
    }

    // --- Tests Endpoint GET /api/adresses/recherche (rechercherAdresseComplete) ---

    @Test
    void rechercherAdresseComplete_Found() {
        // Arrange
        int numeroRue = 10;
        String nomRue = "Rue de la Paix";
        String ville = "Paris";
        String codePostal = "75001";
        Long idPays = 1L;

        // Créer une instance de Pays avec le bon ID pour le match de Mockito
        Pays paysArgument = new Pays();
        paysArgument.setIdPays(idPays);

        when(adresseService.rechercherAdresseComplete(eq(numeroRue), eq(nomRue), eq(ville), eq(codePostal), any(Pays.class)))
                .thenReturn(adresseExemple); // Matcher avec eq() pour les primitives/String et any() pour l'objet

        // Act
        ResponseEntity<Adresse> response = adresseController.rechercherAdresseComplete(numeroRue, nomRue, ville, codePostal, idPays);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(adresseExemple.getIdAdresse(), response.getBody().getIdAdresse());
        verify(adresseService, times(1)).rechercherAdresseComplete(eq(numeroRue), eq(nomRue), eq(ville), eq(codePostal), argThat(p -> p.getIdPays().equals(idPays)));
    }

    @Test
    void rechercherAdresseComplete_NotFound() {
        // Arrange
        int numeroRue = 99;
        String nomRue = "Rue Inconnue";
        String ville = "Lyon";
        String codePostal = "69000";
        Long idPays = 2L;

        // Créer une instance de Pays avec le bon ID pour le match de Mockito
        Pays paysArgument = new Pays();
        paysArgument.setIdPays(idPays);

        when(adresseService.rechercherAdresseComplete(eq(numeroRue), eq(nomRue), eq(ville), eq(codePostal), any(Pays.class)))
                .thenReturn(null); // Service retourne null si non trouvé

        // Act
        ResponseEntity<Adresse> response = adresseController.rechercherAdresseComplete(numeroRue, nomRue, ville, codePostal, idPays);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(adresseService, times(1)).rechercherAdresseComplete(eq(numeroRue), eq(nomRue), eq(ville), eq(codePostal), argThat(p -> p.getIdPays().equals(idPays)));
    }

    // --- Tests Endpoint GET /api/adresses/ville/{ville} (rechercherAdressesParVillePourDisciplines) ---

    @Test
    void rechercherAdressesParVillePourDisciplines_Success() {
        // Arrange
        String ville = "Paris";
        List<Adresse> adresses = Arrays.asList(adresseExemple, new Adresse()); // Liste avec une ou plusieurs adresses
        when(adresseService.rechercherAdressesParVillePourDisciplines(ville)).thenReturn(adresses);

        // Act
        ResponseEntity<List<Adresse>> response = adresseController.rechercherAdressesParVillePourDisciplines(ville);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(adresseService, times(1)).rechercherAdressesParVillePourDisciplines(ville);
    }

    @Test
    void rechercherAdressesParVillePourDisciplines_EmptyList() {
        // Arrange
        String ville = "Marseille";
        List<Adresse> adresses = Collections.emptyList(); // Liste vide
        when(adresseService.rechercherAdressesParVillePourDisciplines(ville)).thenReturn(adresses);

        // Act
        ResponseEntity<List<Adresse>> response = adresseController.rechercherAdressesParVillePourDisciplines(ville);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(adresseService, times(1)).rechercherAdressesParVillePourDisciplines(ville);
    }

    // --- Tests Endpoint GET /api/adresses/discipline/{idDiscipline}/pays/{idPays} (rechercherAdressesParDisciplineEtPays) ---

    @Test
    void rechercherAdressesParDisciplineEtPays_Success() {
        // Arrange
        Long disciplineId = 200L;
        Long paysId = 1L;
        // Créer une instance de Discipline avec le bon ID pour le match de Mockito
        Discipline disciplineArgument = new Discipline();
        disciplineArgument.setIdDiscipline(disciplineId);
        List<Adresse> adresses = Arrays.asList(adresseExemple, new Adresse()); // Liste avec une ou plusieurs adresses
        when(adresseService.rechercherAdressesParDisciplineEtPays(any(Discipline.class), eq(paysId))).thenReturn(adresses); // Matcher avec eq() pour l'ID du pays

        // Act
        ResponseEntity<List<Adresse>> response = adresseController.rechercherAdressesParDisciplineEtPays(disciplineId, paysId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(adresseService, times(1)).rechercherAdressesParDisciplineEtPays(argThat(d -> d.getIdDiscipline().equals(disciplineId)), eq(paysId));
    }

    @Test
    void rechercherAdressesParDisciplineEtPays_EmptyList() {
        // Arrange
        Long disciplineId = 999L;
        Long paysId = 2L;
        // Créer une instance de Discipline avec le bon ID pour le match de Mockito
        Discipline disciplineArgument = new Discipline();
        disciplineArgument.setIdDiscipline(disciplineId);
        List<Adresse> adresses = Collections.emptyList(); // Liste vide
        when(adresseService.rechercherAdressesParDisciplineEtPays(any(Discipline.class), eq(paysId))).thenReturn(adresses); // Matcher avec eq() pour l'ID du pays

        // Act
        ResponseEntity<List<Adresse>> response = adresseController.rechercherAdressesParDisciplineEtPays(disciplineId, paysId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(adresseService, times(1)).rechercherAdressesParDisciplineEtPays(argThat(d -> d.getIdDiscipline().equals(disciplineId)), eq(paysId));
    }

    // --- Tests Endpoint PUT /api/adresses/{id} (updateAdresse) ---

    @Test
    void updateAdresse_Success() {
        // Arrange
        Long adresseId = 1L;
        Adresse adresseMaj = new Adresse();
        adresseMaj.setVille("Lyon"); // Mettre à jour un champ
        adresseMaj.setIdAdresse(adresseId); // L'ID dans le body pourrait être ignoré par le service mais le test le configure

        when(adresseService.updateAdresse(eq(adresseId), any(Adresse.class))).thenReturn(adresseMaj);

        // Act
        ResponseEntity<Adresse> response = adresseController.updateAdresse(adresseId, adresseMaj);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(adresseMaj.getIdAdresse(), response.getBody().getIdAdresse());
        assertEquals(adresseMaj.getVille(), response.getBody().getVille());
        verify(adresseService, times(1)).updateAdresse(eq(adresseId), any(Adresse.class));
    }

    @Test
    void updateAdresse_NotFound() {
        // Arrange
        Long adresseId = 99L;
        Adresse adresseMaj = new Adresse();
        adresseMaj.setVille("Inconnue");

        when(adresseService.updateAdresse(eq(adresseId), any(Adresse.class))).thenThrow(ResourceNotFoundException.class);

        // Act
        ResponseEntity<Adresse> response = adresseController.updateAdresse(adresseId, adresseMaj);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(adresseService, times(1)).updateAdresse(eq(adresseId), any(Adresse.class));
    }

    // --- Tests Endpoint DELETE /api/adresses/{id} (deleteAdresse) ---

    @Test
    void deleteAdresse_Success() {
        // Arrange
        Long adresseId = 1L;
        doNothing().when(adresseService).deleteAdresse(adresseId);

        // Act
        ResponseEntity<Void> response = adresseController.deleteAdresse(adresseId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(adresseService, times(1)).deleteAdresse(adresseId);
    }

    @Test
    void deleteAdresse_NotFound() {
        // Arrange
        Long adresseId = 99L;
        doThrow(ResourceNotFoundException.class).when(adresseService).deleteAdresse(adresseId);

        // Act
        ResponseEntity<Void> response = adresseController.deleteAdresse(adresseId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(adresseService, times(1)).deleteAdresse(adresseId);
    }

    @Test
    void deleteAdresse_AdresseLieeAUneDisciplineException() {
        // Arrange
        Long adresseId = 1L;
        doThrow(AdresseLieeAUneDisciplineException.class).when(adresseService).deleteAdresse(adresseId);

        // Act
        ResponseEntity<Void> response = adresseController.deleteAdresse(adresseId);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(adresseService, times(1)).deleteAdresse(adresseId);
    }
}