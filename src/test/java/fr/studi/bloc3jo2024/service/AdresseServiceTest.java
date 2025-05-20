package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Pays;
import fr.studi.bloc3jo2024.exception.AdresseLieeAUneDisciplineException;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.AdresseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour {@link AdresseService}.
 * Ces tests se concentrent sur la logique métier du service,
 * en moquant le repository {@link AdresseRepository}.
 */
@ExtendWith(MockitoExtension.class)
class AdresseServiceTest {

    @Mock
    private AdresseRepository adresseRepository;

    @InjectMocks
    private AdresseService adresseService;

    // Données de test communes
    private Adresse adresseValide;
    private Pays paysFrance;
    private Discipline disciplineAssociee;
    private final Long adresseIdExistant = 1L;
    private final UUID utilisateurIdExistant = UUID.randomUUID();

    private static final String ADRESSE_NON_TROUVEE_MSG_PREFIX = "Adresse non trouvée avec l'ID : ";

    @BeforeEach
    void setUp() {

        final Long disciplineId = 10L;
        final Long paysIdExistant = 100L;

        paysFrance = Pays.builder().idPays(paysIdExistant).nomPays("France").build();
        adresseValide = Adresse.builder()
                .idAdresse(adresseIdExistant)
                .numeroRue(10)
                .nomRue("Rue de la Paix")
                .ville("Paris")
                .codePostal("75001")
                .pays(paysFrance)
                .build();
        disciplineAssociee = Discipline.builder().idDiscipline(disciplineId).nomDiscipline("Natation").build();
    }

    @Nested
    @DisplayName("Tests pour creerAdresseSiNonExistante")
    class CreerAdresseSiNonExistanteTests {

        @Test
        @DisplayName("Doit retourner l'adresse existante si trouvée par ses détails")
        void shouldReturnExistingAdresse_whenAdresseFoundByDetails() {
            // Arrange
            when(adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                    adresseValide.getNumeroRue(), adresseValide.getNomRue(), adresseValide.getVille(), adresseValide.getCodePostal(), adresseValide.getPays()
            )).thenReturn(Optional.of(adresseValide));

            // Act
            Adresse result = adresseService.creerAdresseSiNonExistante(adresseValide);

            // Assert
            assertNotNull(result, "Le résultat ne doit pas être null.");
            assertEquals(adresseValide.getIdAdresse(), result.getIdAdresse(), "L'ID de l'adresse retournée doit être celui de l'adresse existante.");
            verify(adresseRepository).findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                    adresseValide.getNumeroRue(), adresseValide.getNomRue(), adresseValide.getVille(), adresseValide.getCodePostal(), adresseValide.getPays()
            );
            verify(adresseRepository, never()).save(any(Adresse.class)); // Ne doit pas appeler save
        }

        @Test
        @DisplayName("Doit sauvegarder et retourner une nouvelle adresse si non trouvée")
        void shouldSaveAndReturnNewAdresse_whenAdresseNotFound() {
            // Arrange
            Adresse nouvelleAdresseInput = Adresse.builder() // Adresse en entrée, sans ID
                    .numeroRue(20).nomRue("Avenue des Champs-Élysées").ville("Paris").codePostal("75008").pays(paysFrance)
                    .build();
            Adresse adresseSauvegardeeSimulee = Adresse.builder() // Adresse simulée après sauvegarde (avec ID)
                    .idAdresse(2L).numeroRue(20).nomRue("Avenue des Champs-Élysées").ville("Paris").codePostal("75008").pays(paysFrance)
                    .build();

            when(adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                    nouvelleAdresseInput.getNumeroRue(), nouvelleAdresseInput.getNomRue(), nouvelleAdresseInput.getVille(), nouvelleAdresseInput.getCodePostal(), nouvelleAdresseInput.getPays()
            )).thenReturn(Optional.empty()); // Adresse non trouvée
            when(adresseRepository.save(any(Adresse.class))).thenReturn(adresseSauvegardeeSimulee); // Comportement de save

            // Act
            Adresse result = adresseService.creerAdresseSiNonExistante(nouvelleAdresseInput);

            // Assert
            assertNotNull(result, "Le résultat ne doit pas être null.");
            assertEquals(adresseSauvegardeeSimulee.getIdAdresse(), result.getIdAdresse(), "L'ID de l'adresse retournée doit être celui de l'adresse sauvegardée.");
            assertEquals(nouvelleAdresseInput.getVille(), result.getVille(), "La ville doit correspondre.");

            ArgumentCaptor<Adresse> adresseCaptor = ArgumentCaptor.forClass(Adresse.class);
            verify(adresseRepository).save(adresseCaptor.capture()); // Capture l'argument passé à save
            assertEquals(paysFrance, adresseCaptor.getValue().getPays(), "L'entité Pays de l'adresse sauvegardée doit être correcte.");
            assertNull(adresseCaptor.getValue().getIdAdresse(), "L'ID de l'adresse doit être null avant la sauvegarde pour une nouvelle adresse.");
        }

        @Test
        @DisplayName("Doit lever IllegalArgumentException si Pays est null dans l'adresse en entrée")
        void shouldThrowIllegalArgumentException_whenPaysInInputIsNull() {
            Adresse adresseSansPays = Adresse.builder().numeroRue(1).nomRue("Test").ville("Test").codePostal("123").pays(null).build();

            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> adresseService.creerAdresseSiNonExistante(adresseSansPays));

            assertEquals("L'objet Pays avec son ID est requis pour créer ou vérifier une adresse.", thrown.getMessage());
            verify(adresseRepository, never()).findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(any(), any(), any(), any(), any());
            verify(adresseRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit lever IllegalArgumentException si l'ID de Pays est null dans l'adresse en entrée")
        void shouldThrowIllegalArgumentException_whenPaysIdInInputIsNull() {
            Pays paysSansId = Pays.builder().nomPays("Inconnu").build(); // ID est null
            Adresse adresseAvecPaysSansId = Adresse.builder().numeroRue(1).nomRue("Test").ville("Test").codePostal("123").pays(paysSansId).build();

            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> adresseService.creerAdresseSiNonExistante(adresseAvecPaysSansId));

            assertEquals("L'objet Pays avec son ID est requis pour créer ou vérifier une adresse.", thrown.getMessage());
        }
    }

    @Nested
    @DisplayName("Tests pour getAdresseById")
    class GetAdresseByIdTests {
        @Test
        @DisplayName("Doit retourner l'adresse si trouvée par ID")
        void shouldReturnAdresse_whenFoundById() {
            when(adresseRepository.findById(adresseIdExistant)).thenReturn(Optional.of(adresseValide));
            Adresse result = adresseService.getAdresseById(adresseIdExistant);
            assertNotNull(result);
            assertEquals(adresseIdExistant, result.getIdAdresse());
            verify(adresseRepository).findById(adresseIdExistant);
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si non trouvée par ID")
        void shouldThrowResourceNotFoundException_whenNotFoundById() {
            Long idInexistant = 99L;
            when(adresseRepository.findById(idInexistant)).thenReturn(Optional.empty());
            ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class,
                    () -> adresseService.getAdresseById(idInexistant));
            assertEquals(ADRESSE_NON_TROUVEE_MSG_PREFIX + idInexistant, thrown.getMessage());
        }
    }

    @Nested
    @DisplayName("Tests pour getAllAdresses (avec pagination)")
    class GetAllAdressesTests {
        @Test
        @DisplayName("Doit retourner une page d'adresses")
        void shouldReturnPageOfAdresses() {
            Pageable pageable = PageRequest.of(0, 10);
            List<Adresse> adressesList = Arrays.asList(adresseValide, Adresse.builder().idAdresse(2L).build());
            Page<Adresse> adressesPage = new PageImpl<>(adressesList, pageable, adressesList.size());
            when(adresseRepository.findAll(pageable)).thenReturn(adressesPage);

            Page<Adresse> result = adresseService.getAllAdresses(pageable);

            assertNotNull(result, "La page retournée ne doit pas être null.");
            assertEquals(2, result.getTotalElements(), "Le nombre total d'éléments doit être correct.");
            assertTrue(result.getContent().contains(adresseValide), "Le contenu doit inclure l'adresse de test.");
            verify(adresseRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("Tests pour adresseExisteDeja")
    class AdresseExisteDejaTests {
        @Test
        @DisplayName("Doit retourner true si l'adresse existe")
        void shouldReturnTrue_whenAdresseExists() {
            when(adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                    adresseValide.getNumeroRue(), adresseValide.getNomRue(), adresseValide.getVille(), adresseValide.getCodePostal(), adresseValide.getPays()
            )).thenReturn(Optional.of(adresseValide));

            assertTrue(adresseService.adresseExisteDeja(adresseValide));
        }

        @Test
        @DisplayName("Doit retourner false si l'adresse n'existe pas")
        void shouldReturnFalse_whenAdresseNotExists() {
            when(adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                    adresseValide.getNumeroRue(), adresseValide.getNomRue(), adresseValide.getVille(), adresseValide.getCodePostal(), adresseValide.getPays()
            )).thenReturn(Optional.empty());

            assertFalse(adresseService.adresseExisteDeja(adresseValide));
        }

        @Test
        @DisplayName("Doit retourner false si Pays est null dans l'adresse en entrée")
        void shouldReturnFalse_whenPaysInInputIsNull() {
            Adresse adresseSansPays = Adresse.builder().numeroRue(1).nomRue("Test").ville("Test").codePostal("123").pays(null).build();
            assertFalse(adresseService.adresseExisteDeja(adresseSansPays));
            verify(adresseRepository, never()).findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(any(), any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Tests pour rechercherAdresseComplete")
    class RechercherAdresseCompleteTests {
        @Test
        @DisplayName("Doit lever IllegalArgumentException si Pays est null")
        void shouldThrowIllegalArgumentException_whenPaysIsNull() {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                    adresseService.rechercherAdresseComplete(10, "Rue", "Ville", "CP", null)
            );
            assertEquals("L'objet Pays avec son ID est requis pour la recherche d'adresse complète.", thrown.getMessage());
        }

        @Test
        @DisplayName("Doit lever IllegalArgumentException si l'ID de Pays est null")
        void shouldThrowIllegalArgumentException_whenPaysIdIsNull() {
            Pays paysSansId = Pays.builder().nomPays("France").build(); // ID est null
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                    adresseService.rechercherAdresseComplete(10, "Rue", "Ville", "CP", paysSansId)
            );
            assertEquals("L'objet Pays avec son ID est requis pour la recherche d'adresse complète.", thrown.getMessage());
        }

        @Test
        @DisplayName("Doit retourner l'adresse si trouvée")
        void shouldReturnAdresse_whenFound() {
            when(adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                    adresseValide.getNumeroRue(), adresseValide.getNomRue(), adresseValide.getVille(), adresseValide.getCodePostal(), adresseValide.getPays()
            )).thenReturn(Optional.of(adresseValide));

            Adresse result = adresseService.rechercherAdresseComplete(
                    adresseValide.getNumeroRue(), adresseValide.getNomRue(), adresseValide.getVille(), adresseValide.getCodePostal(), adresseValide.getPays()
            );
            assertNotNull(result);
            assertEquals(adresseValide.getIdAdresse(), result.getIdAdresse());
        }
    }

    @Nested
    @DisplayName("Tests pour updateAdresse")
    class UpdateAdresseTests {
        @Test
        @DisplayName("Doit mettre à jour et retourner l'adresse si trouvée")
        void shouldUpdateAndReturnAdresse_whenFound() {
            // Arrange
            Adresse detailsMiseAJour = Adresse.builder()
                    .numeroRue(99).nomRue("Nouvelle Rue").ville("Nouvelle Ville").codePostal("99999")
                    .pays(Pays.builder().idPays(200L).nomPays("Canada").build()) // Nouveau pays pour la mise à jour
                    .build();

            when(adresseRepository.findById(adresseIdExistant)).thenReturn(Optional.of(adresseValide));
            // Simule que save retourne l'entité mise à jour (l'argument qui lui est passé)
            when(adresseRepository.save(any(Adresse.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Adresse updatedAdresse = adresseService.updateAdresse(adresseIdExistant, detailsMiseAJour);

            // Assert
            assertNotNull(updatedAdresse, "L'adresse mise à jour ne doit pas être null.");
            assertEquals(adresseIdExistant, updatedAdresse.getIdAdresse(), "L'ID doit rester le même.");
            assertEquals(detailsMiseAJour.getNumeroRue(), updatedAdresse.getNumeroRue());
            assertEquals(detailsMiseAJour.getNomRue(), updatedAdresse.getNomRue());
            assertEquals(detailsMiseAJour.getVille(), updatedAdresse.getVille());
            assertEquals(detailsMiseAJour.getCodePostal(), updatedAdresse.getCodePostal());
            assertEquals(detailsMiseAJour.getPays().getIdPays(), updatedAdresse.getPays().getIdPays(), "L'ID du pays doit être mis à jour.");

            verify(adresseRepository).findById(adresseIdExistant);
            verify(adresseRepository).save(any(Adresse.class));
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si l'adresse à mettre à jour n'est pas trouvée")
        void shouldThrowResourceNotFoundException_whenUpdateTargetNotFound() {
            Long idInexistant = 99L;
            Adresse detailsMiseAJour = Adresse.builder().pays(paysFrance).build(); // Pays est requis
            when(adresseRepository.findById(idInexistant)).thenReturn(Optional.empty());

            ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class,
                    () -> adresseService.updateAdresse(idInexistant, detailsMiseAJour));
            assertEquals(ADRESSE_NON_TROUVEE_MSG_PREFIX + idInexistant, thrown.getMessage());
            verify(adresseRepository, never()).save(any(Adresse.class));
        }

        @Test
        @DisplayName("Doit lever IllegalArgumentException pour update si Pays est null")
        void updateAdresse_shouldThrowIllegalArgumentException_whenPaysIsNull() {
            Adresse detailsMiseAJour = Adresse.builder().pays(null).build();
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> adresseService.updateAdresse(adresseIdExistant, detailsMiseAJour));
            assertEquals("L'objet Pays avec son ID est requis pour la mise à jour de l'adresse.", thrown.getMessage());
        }

    }

    @Nested
    @DisplayName("Tests pour deleteAdresse")
    class DeleteAdresseTests {
        @Test
        @DisplayName("Doit supprimer l'adresse si elle existe et n'est pas liée")
        void shouldDeleteAdresse_whenExistsAndNotLinked() {
            when(adresseRepository.findById(adresseIdExistant)).thenReturn(Optional.of(adresseValide));
            when(adresseRepository.isAdresseLieeAUnDiscipline(adresseIdExistant)).thenReturn(false); // Non liée
            doNothing().when(adresseRepository).delete(adresseValide); // Comportement pour delete

            assertDoesNotThrow(() -> adresseService.deleteAdresse(adresseIdExistant));

            verify(adresseRepository).findById(adresseIdExistant);
            verify(adresseRepository).isAdresseLieeAUnDiscipline(adresseIdExistant);
            verify(adresseRepository).delete(adresseValide);
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si l'adresse à supprimer n'est pas trouvée")
        void shouldThrowResourceNotFoundException_whenDeleteTargetNotFound() {
            Long idInexistant = 99L;
            when(adresseRepository.findById(idInexistant)).thenReturn(Optional.empty());

            ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class,
                    () -> adresseService.deleteAdresse(idInexistant));
            assertEquals(ADRESSE_NON_TROUVEE_MSG_PREFIX + idInexistant, thrown.getMessage());
            verify(adresseRepository, never()).isAdresseLieeAUnDiscipline(anyLong());
            verify(adresseRepository, never()).delete(any(Adresse.class));
        }

        @Test
        @DisplayName("Doit lever AdresseLieeAUneDisciplineException si l'adresse est liée")
        void shouldThrowAdresseLieeAUneDisciplineException_whenLinkedToDiscipline() {
            when(adresseRepository.findById(adresseIdExistant)).thenReturn(Optional.of(adresseValide));
            when(adresseRepository.isAdresseLieeAUnDiscipline(adresseIdExistant)).thenReturn(true); // Liée!

            AdresseLieeAUneDisciplineException thrown = assertThrows(AdresseLieeAUneDisciplineException.class,
                    () -> adresseService.deleteAdresse(adresseIdExistant));
            assertTrue(thrown.getMessage().contains("L'adresse avec l'ID " + adresseIdExistant + " est liée"),
                    "Le message d'erreur doit indiquer que l'adresse est liée.");
            verify(adresseRepository, never()).delete(any(Adresse.class)); // Ne doit pas appeler delete
        }
    }
}