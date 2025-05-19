package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.entity.Adresse;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdresseControllerTest {

    @Mock
    private AdresseService adresseService;

    @InjectMocks
    private AdresseController adresseController;

    private Adresse adresseExemple;
    private Pays paysExemple;
    private UUID utilisateurIdExemple;
    private final Long disciplineIdExemple = 200L;

    @BeforeEach
    void setUp() {
        paysExemple = Pays.builder().idPays(1L).nomPays("France").build();
        adresseExemple = Adresse.builder()
                .idAdresse(1L)
                .numeroRue(10) // Integer
                .nomRue("Rue de la Paix")
                .ville("Paris")
                .codePostal("75001")
                .pays(paysExemple)
                .build();
        utilisateurIdExemple = UUID.randomUUID();
    }

    @Test
    void creerAdresseSiNonExistante_AdresseExistante() {
        Long existingId = 5L;
        when(adresseService.getIdAdresseSiExistante(any(Adresse.class))).thenReturn(existingId);

        ResponseEntity<Object> response = adresseController.creerAdresseSiNonExistante(adresseExemple);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(Map.class, response.getBody());
        Map<?,?> responseBodyMap = (Map<?,?>) response.getBody();
        assertEquals(existingId, responseBodyMap.get("idAdresseExistante"));
        verify(adresseService).getIdAdresseSiExistante(any(Adresse.class));
        verify(adresseService, never()).creerAdresseSiNonExistante(any(Adresse.class));
    }

    @Test
    void creerAdresseSiNonExistante_NouvelleAdresse() {
        Adresse nouvelleAdresseCreee = Adresse.builder().idAdresse(2L).numeroRue(20).ville("Lyon").pays(paysExemple).build();
        when(adresseService.getIdAdresseSiExistante(any(Adresse.class))).thenReturn(null);
        when(adresseService.creerAdresseSiNonExistante(any(Adresse.class))).thenReturn(nouvelleAdresseCreee);

        ResponseEntity<Object> response = adresseController.creerAdresseSiNonExistante(adresseExemple);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(Adresse.class, response.getBody());
        Adresse returnedAdresse = (Adresse) response.getBody();
        assertEquals(nouvelleAdresseCreee.getIdAdresse(), returnedAdresse.getIdAdresse());
        assertEquals("Lyon", returnedAdresse.getVille());
        verify(adresseService).getIdAdresseSiExistante(any(Adresse.class));
        verify(adresseService).creerAdresseSiNonExistante(any(Adresse.class));
    }

    @Test
    void creerAdresseSiNonExistante_VilleNull_ShouldThrowBadRequest() {
        Adresse adresse = Adresse.builder()
                .numeroRue(10)
                .nomRue("Rue des Invalides")
                .ville(null)
                .codePostal("75007")
                .pays(paysExemple)
                .build();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> adresseController.creerAdresseSiNonExistante(adresse)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        String reason = exception.getReason();
        assertNotNull(reason);
        assertTrue(reason.toLowerCase().contains("champ(s) obligatoire(s) manquant(s)"));
    }

    @Test
    void creerAdresseSiNonExistante_IllegalArgumentExceptionFromService() {
        String errorMessage = "Pays invalide pour la création";
        when(adresseService.getIdAdresseSiExistante(any(Adresse.class))).thenReturn(null);
        when(adresseService.creerAdresseSiNonExistante(any(Adresse.class))).thenThrow(new IllegalArgumentException(errorMessage));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> adresseController.creerAdresseSiNonExistante(adresseExemple)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason() != null && exception.getReason().contains(errorMessage));
    }

    @Test
    void creerAdresseSiNonExistante_ResourceNotFoundExceptionFromService() {
        String errorMessage = "Pays non trouvé";
        when(adresseService.getIdAdresseSiExistante(any(Adresse.class))).thenReturn(null);
        when(adresseService.creerAdresseSiNonExistante(any(Adresse.class))).thenThrow(new ResourceNotFoundException(errorMessage));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> adresseController.creerAdresseSiNonExistante(adresseExemple)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason() != null && exception.getReason().contains(errorMessage));
    }


    @Test
    void getAdresseById_Success() {
        when(adresseService.getAdresseById(1L)).thenReturn(adresseExemple);
        ResponseEntity<Adresse> response = adresseController.getAdresseById(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(adresseExemple, response.getBody());
        verify(adresseService).getAdresseById(1L);
    }

    @Test
    void getAdresseById_NotFound() {
        String errorMessage = "Adresse non trouvée";
        when(adresseService.getAdresseById(99L)).thenThrow(new ResourceNotFoundException(errorMessage));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> adresseController.getAdresseById(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason() != null && exception.getReason().contains(errorMessage));
        verify(adresseService).getAdresseById(99L);
    }

    @Test
    void getAllAdresses_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Adresse> adressesList = Collections.singletonList(adresseExemple);
        Page<Adresse> adressesPage = new PageImpl<>(adressesList, pageable, adressesList.size());
        when(adresseService.getAllAdresses(pageable)).thenReturn(adressesPage);

        ResponseEntity<Page<Adresse>> response = adresseController.getAllAdresses(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals(adresseExemple.getVille(), response.getBody().getContent().getFirst().getVille());
        verify(adresseService).getAllAdresses(pageable);
    }

    @Test
    void getAdressesByUserId_Success() {
        List<Adresse> adresses = Collections.singletonList(adresseExemple);
        when(adresseService.getAdressesByUtilisateurId(utilisateurIdExemple)).thenReturn(adresses);
        ResponseEntity<List<Adresse>> response = adresseController.getAdressesByUserId(utilisateurIdExemple);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(adresses, response.getBody());
        verify(adresseService).getAdressesByUtilisateurId(utilisateurIdExemple);
    }

    @Test
    void getAdresseByDiscipline_Success() {
        when(adresseService.getAdresseByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple)))).thenReturn(adresseExemple);
        ResponseEntity<Adresse> response = adresseController.getAdresseByDiscipline(disciplineIdExemple);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(adresseExemple, response.getBody());
        verify(adresseService).getAdresseByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple)));
    }

    @Test
    void getAdresseByDiscipline_IllegalArgumentFromService() {
        String errorMessage = "Discipline ID null";
        when(adresseService.getAdresseByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple))))
                .thenThrow(new IllegalArgumentException(errorMessage));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> adresseController.getAdresseByDiscipline(disciplineIdExemple)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason() != null && exception.getReason().contains(errorMessage));
    }


    @Test
    void getAdressesByDiscipline_All_Success() {
        List<Adresse> adresses = Collections.singletonList(adresseExemple);
        when(adresseService.getAdressesByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple))))
                .thenReturn(adresses);

        ResponseEntity<List<Adresse>> response = adresseController.getAdressesByDiscipline(disciplineIdExemple);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(adresses, response.getBody());
        verify(adresseService).getAdressesByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple)));
    }

    @Test
    void getAdressesByDiscipline_All_IllegalArgumentFromService() {
        String errorMessage = "Discipline ID null pour /all";
        when(adresseService.getAdressesByDiscipline(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple))))
                .thenThrow(new IllegalArgumentException(errorMessage));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> adresseController.getAdressesByDiscipline(disciplineIdExemple)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason() != null && exception.getReason().contains(errorMessage));
    }


    @Test
    void rechercherAdresseComplete_Found() {
        Integer numeroRue = 10; String nomRue = "Rue de la Paix"; String ville = "Paris"; String codePostal = "75001"; Long idPays = 1L;
        when(adresseService.rechercherAdresseComplete(eq(numeroRue), eq(nomRue), eq(ville), eq(codePostal), argThat(p -> p.getIdPays().equals(idPays))))
                .thenReturn(adresseExemple);
        ResponseEntity<Adresse> response = adresseController.rechercherAdresseComplete(numeroRue, nomRue, ville, codePostal, idPays);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(adresseExemple, response.getBody());
    }

    @Test
    void rechercherAdresseComplete_NotFound() {
        Integer numeroRue = 10; String nomRue = "Rue de la Paix"; String ville = "Paris"; String codePostal = "75001"; Long idPays = 1L;
        when(adresseService.rechercherAdresseComplete(eq(numeroRue), eq(nomRue), eq(ville), eq(codePostal), argThat(p -> p.getIdPays().equals(idPays))))
                .thenReturn(null);
        ResponseEntity<Adresse> response = adresseController.rechercherAdresseComplete(numeroRue, nomRue, ville, codePostal, idPays);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void rechercherAdresseComplete_IllegalArgumentFromService() {
        Integer numeroRue = 10; String nomRue = "Rue de la Paix"; String ville = "Paris"; String codePostal = "75001"; Long idPays = 1L;
        String errorMessage = "Pays invalide";
        when(adresseService.rechercherAdresseComplete(eq(numeroRue), eq(nomRue), eq(ville), eq(codePostal), argThat(p -> p.getIdPays().equals(idPays))))
                .thenThrow(new IllegalArgumentException(errorMessage));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> adresseController.rechercherAdresseComplete(numeroRue, nomRue, ville, codePostal, idPays)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason() != null && exception.getReason().contains(errorMessage));
    }


    @Test
    void rechercherAdressesParVillePourDisciplines_Success() {
        String ville = "Paris";
        List<Adresse> adresses = Collections.singletonList(adresseExemple);
        when(adresseService.rechercherAdressesParVillePourDisciplines(ville)).thenReturn(adresses);
        ResponseEntity<List<Adresse>> response = adresseController.rechercherAdressesParVillePourDisciplines(ville);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(adresses, response.getBody());
    }

    @Test
    void rechercherAdressesParDisciplineEtPays_Success() {
        Long paysId = 1L;
        List<Adresse> adresses = Collections.singletonList(adresseExemple);
        when(adresseService.rechercherAdressesParDisciplineEtPays(argThat(d -> d.getIdDiscipline().equals(disciplineIdExemple)), eq(paysId)))
                .thenReturn(adresses);
        ResponseEntity<List<Adresse>> response = adresseController.rechercherAdressesParDisciplineEtPays(disciplineIdExemple, paysId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(adresses, response.getBody());
    }

    @Test
    void updateAdresse_Success() {
        Long adresseId = 1L;
        Adresse adresseMaj = Adresse.builder().idAdresse(adresseId).ville("Lyon").pays(paysExemple).build();
        when(adresseService.updateAdresse(eq(adresseId), any(Adresse.class))).thenReturn(adresseMaj);
        ResponseEntity<Adresse> response = adresseController.updateAdresse(adresseId, adresseMaj);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(adresseMaj, response.getBody());
    }

    @Test
    void updateAdresse_IllegalArgumentFromService() {
        Long adresseId = 1L;
        Adresse adresseMaj = Adresse.builder().idAdresse(adresseId).ville("Lyon").build();
        String errorMessage = "Pays invalide pour la mise à jour";
        when(adresseService.updateAdresse(eq(adresseId), any(Adresse.class))).thenThrow(new IllegalArgumentException(errorMessage));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> adresseController.updateAdresse(adresseId, adresseMaj)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason() != null && exception.getReason().contains(errorMessage));
    }


    @Test
    void deleteAdresse_Success() {
        Long adresseId = 1L;
        doNothing().when(adresseService).deleteAdresse(adresseId);
        ResponseEntity<Void> response = adresseController.deleteAdresse(adresseId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(adresseService).deleteAdresse(adresseId);
    }

    @Test
    void deleteAdresse_NotFound() {
        Long adresseId = 99L;
        String errorMessage = "Not found";
        doThrow(new ResourceNotFoundException(errorMessage)).when(adresseService).deleteAdresse(adresseId);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> adresseController.deleteAdresse(adresseId)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason() != null && exception.getReason().contains(errorMessage));
    }

    @Test
    void deleteAdresse_AdresseLieeAUneDisciplineException() {
        Long adresseId = 1L;
        String errorMessage = "Adresse liée";
        doThrow(new AdresseLieeAUneDisciplineException(errorMessage)).when(adresseService).deleteAdresse(adresseId);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> adresseController.deleteAdresse(adresseId)
        );
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason() != null && exception.getReason().contains(errorMessage));
    }
}
