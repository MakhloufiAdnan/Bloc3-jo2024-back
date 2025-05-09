package fr.studi.bloc3jo2024.entities;

import fr.studi.bloc3jo2024.entity.Discipline;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DisciplineTest {

    // --- Tests pour la méthode @PrePersist (verifierDateDiscipline) ---

    @Test
    void verifierDateDiscipline_DateFuture_NeLancePasException() {
        // Arrange
        LocalDateTime dateFuture = LocalDateTime.now().plusDays(1);
        Discipline discipline = new Discipline();
        discipline.setDateDiscipline(dateFuture);

        // Act & Assert
        assertDoesNotThrow(discipline::verifierDateDiscipline,
                "La validation de date ne devrait pas lancer d'exception pour une date future");
    }

    @Test
    void verifierDateDiscipline_DatePresente_NeLancePasException() {
        // Arrange
        // Ajout d'une seconde pour éviter les effets de timing trop serrés
        LocalDateTime datePresente = LocalDateTime.now().plusSeconds(1);
        Discipline discipline = new Discipline();
        discipline.setDateDiscipline(datePresente);

        // Act & Assert
        assertDoesNotThrow(discipline::verifierDateDiscipline,
                "La validation de date ne devrait pas lancer d'exception pour la date/heure présente");
    }

    @Test
    void verifierDateDiscipline_DatePassee_LanceIllegalArgumentException() {
        // Arrange
        LocalDateTime datePassee = LocalDateTime.now().minusDays(1);
        Discipline discipline = new Discipline();
        discipline.setDateDiscipline(datePassee);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                discipline::verifierDateDiscipline,
                "La validation de date devrait lancer une IllegalArgumentException pour une date passée");

        assertTrue(exception.getMessage().contains("La date de la discipline ne peut pas être dans le passé."),
                "Le message de l'exception devrait indiquer le problème de date");
    }
}
