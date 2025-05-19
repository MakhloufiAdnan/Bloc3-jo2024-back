package fr.studi.bloc3jo2024.entities;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Pays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class AdresseEntityTest {

    private Pays pays1;
    private Pays pays2;

    @BeforeEach
    void setUp() {
        pays1 = Pays.builder().idPays(1L).nomPays("France").build();
        pays2 = Pays.builder().idPays(2L).nomPays("Allemagne").build();
    }

    @Test
    @DisplayName("Test de la réflexivité de equals")
    void equals_shouldBeReflexive() {
        Adresse adresse1 = Adresse.builder()
                .idAdresse(1L)
                .numeroRue(10)
                .nomRue("Rue de la Paix")
                .ville("Paris")
                .codePostal("75001")
                .pays(pays1)
                .disciplines(new HashSet<>())
                .utilisateurs(new HashSet<>())
                .build();
        assertEquals(adresse1, adresse1, "Une adresse doit être égale à elle-même.");
    }

    @Test
    @DisplayName("Test de la symétrie de equals")
    void equals_shouldBeSymmetric() {
        Adresse adresse1 = Adresse.builder()
                .idAdresse(1L).numeroRue(10).nomRue("Rue de la Paix").ville("Paris").codePostal("75001").pays(pays1)
                .disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                .build();
        Adresse adresse2 = Adresse.builder()
                .idAdresse(1L).numeroRue(10).nomRue("Rue de la Paix").ville("Paris").codePostal("75001").pays(pays1)
                .disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                .build();

        assertEquals(adresse1, adresse2, "adresse1 devrait être égale à adresse2.");
        assertEquals(adresse2, adresse1, "adresse2 devrait être égale à adresse1.");
    }

    @Test
    @DisplayName("Test de la transitivité de equals")
    void equals_shouldBeTransitive() {
        Adresse adresse1 = Adresse.builder()
                .idAdresse(1L).numeroRue(10).nomRue("Rue de la Paix").ville("Paris").codePostal("75001").pays(pays1)
                .disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                .build();
        Adresse adresse2 = Adresse.builder()
                .idAdresse(1L).numeroRue(10).nomRue("Rue de la Paix").ville("Paris").codePostal("75001").pays(pays1)
                .disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                .build();
        Adresse adresse3 = Adresse.builder()
                .idAdresse(1L).numeroRue(10).nomRue("Rue de la Paix").ville("Paris").codePostal("75001").pays(pays1)
                .disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                .build();

        assertEquals(adresse1, adresse2, "adresse1 devrait être égale à adresse2.");
        assertEquals(adresse2, adresse3, "adresse2 devrait être égale à adresse3.");
        assertEquals(adresse1, adresse3, "adresse1 devrait être égale à adresse3.");
    }

    @Test
    @DisplayName("Test de la consistance de equals")
    void equals_shouldBeConsistent() {
        Adresse adresse1 = Adresse.builder()
                .idAdresse(1L).numeroRue(10).nomRue("Rue de la Paix").ville("Paris").codePostal("75001").pays(pays1)
                .disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                .build();
        Adresse adresse2 = Adresse.builder()
                .idAdresse(1L).numeroRue(10).nomRue("Rue de la Paix").ville("Paris").codePostal("75001").pays(pays1)
                .disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                .build();

        assertEquals(adresse1, adresse2, "Appels multiples à equals devraient retourner le même résultat.");
        assertEquals(adresse1, adresse2, "Appels multiples à equals devraient retourner le même résultat.");
    }

    @Test
    @DisplayName("Test de equals avec null")
    void equals_shouldReturnFalseForNull() {
        Adresse adresse1 = Adresse.builder()
                .idAdresse(1L).numeroRue(10).nomRue("Rue de la Paix").ville("Paris").codePostal("75001").pays(pays1)
                .disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                .build();
        // L'IDE peut signaler que adresse1.equals(null) est toujours faux,
        // mais c'est le comportement attendu et testé.
        // assertNotEquals(expected, actual) implies expected.equals(actual) is false.
        assertNotEquals(null, adresse1, "Une adresse ne devrait pas être égale à null.");
    }

    @Test
    @DisplayName("Test de equals avec un objet de type différent")
    void equals_shouldReturnFalseForDifferentType() {
        Adresse adresse1 = Adresse.builder()
                .idAdresse(1L).numeroRue(10).nomRue("Rue de la Paix").ville("Paris").codePostal("75001").pays(pays1)
                .disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                .build();
        String autreObjet = "Ceci n'est pas une Adresse";
        assertNotEquals(adresse1, autreObjet, "equals avec un type différent devrait retourner false.");
    }

    @Test
    @DisplayName("Test de equals avec des ID différents")
    void equals_shouldReturnFalseForDifferentIds() {
        Adresse adresse1 = Adresse.builder()
                .idAdresse(1L).numeroRue(10).nomRue("Rue de la Paix").ville("Paris").codePostal("75001").pays(pays1)
                .disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                .build();
        Adresse adresse2 = Adresse.builder()
                .idAdresse(2L).numeroRue(10).nomRue("Rue de la Paix").ville("Paris").codePostal("75001").pays(pays1)
                .disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                .build();
        assertNotEquals(adresse1, adresse2, "Adresses avec des ID différents ne devraient pas être égales.");
    }

    @Test
    @DisplayName("Test de equals avec un ID null et l'autre non null")
    void equals_shouldReturnFalseForOneNullId() {
        Adresse adresse1 = Adresse.builder()
                .numeroRue(10).nomRue("Rue de la Paix").ville("Paris").codePostal("75001").pays(pays1) // idAdresse est null
                .disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                .build();
        Adresse adresse2 = Adresse.builder()
                .idAdresse(2L).numeroRue(10).nomRue("Rue de la Paix").ville("Paris").codePostal("75001").pays(pays1)
                .disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                .build();
        assertNotEquals(adresse1, adresse2, "Une adresse avec ID null ne devrait pas être égale à une adresse avec un ID non null.");
        assertNotEquals(adresse2, adresse1, "Une adresse avec ID non null ne devrait pas être égale à une adresse avec un ID null.");
    }

    @Test
    @DisplayName("Test de equals avec deux ID nulls (instances différentes)")
    void equals_shouldReturnFalseForTwoDifferentInstancesWithNullIds() {
        Adresse adresse1 = Adresse.builder()
                .numeroRue(10).nomRue("Rue de la Paix").ville("Paris").codePostal("75001").pays(pays1) // idAdresse est null
                .disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                .build();
        Adresse adresse2 = Adresse.builder()
                .numeroRue(10).nomRue("Rue de la Paix").ville("Paris").codePostal("75001").pays(pays1) // idAdresse est null
                .disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                .build();
        assertNotEquals(adresse1, adresse2, "Deux instances différentes avec des ID null ne devraient pas être égales (comportement de Object.equals).");
    }


    @Test
    @DisplayName("Test du contrat hashCode : objets égaux ont le même hashCode")
    void hashCode_shouldBeSameForEqualObjects() {
        Adresse adresse1 = Adresse.builder()
                .idAdresse(1L).numeroRue(10).nomRue("Rue de la Paix").ville("Paris").codePostal("75001").pays(pays1)
                .disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                .build();
        Adresse adresse2 = Adresse.builder()
                .idAdresse(1L).numeroRue(10).nomRue("Rue de la Paix").ville("Paris").codePostal("75001").pays(pays1)
                .disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                .build();
        assertEquals(adresse1.hashCode(), adresse2.hashCode(), "Objets égaux doivent avoir le même hashCode.");
    }

    @Test
    @DisplayName("Test du contrat hashCode : objets différents peuvent avoir des hashCodes différents")
    void hashCode_shouldBeDifferentForDifferentObjects() {
        Adresse adresse1 = Adresse.builder()
                .idAdresse(1L).numeroRue(10).nomRue("Rue de la Paix").ville("Paris").codePostal("75001").pays(pays1)
                .disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                .build();
        Adresse adresse2 = Adresse.builder()
                .idAdresse(2L).numeroRue(20).nomRue("Autre Rue").ville("Lyon").codePostal("69001").pays(pays2)
                .disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                .build();
        assertNotEquals(adresse1.hashCode(), adresse2.hashCode(), "Objets avec ID différents devraient avoir des hashCodes différents.");
    }

    @Test
    @DisplayName("Test de la méthode toString")
    void toString_shouldReturnExpectedFormat() {
        Adresse adresse = Adresse.builder()
                .idAdresse(1L)
                .numeroRue(10)
                .nomRue("Rue de la Paix")
                .ville("Paris")
                .codePostal("75001")
                .pays(pays1) // pays est défini mais ne sera pas dans toString
                .disciplines(new HashSet<>())
                .utilisateurs(new HashSet<>())
                .build();

        String expectedString = "Adresse{" +
                "idAdresse=1" +
                ", numeroRue=10" +
                ", nomRue='Rue de la Paix'" +
                ", ville='Paris'" +
                ", codePostal='75001'" +
                '}';
        assertEquals(expectedString, adresse.toString(), "Le format de toString n'est pas celui attendu.");
    }

    @Test
    @DisplayName("Test de la méthode toString avec pays null")
    void toString_withNullPays_shouldHandleGracefully() {
        Adresse adresse = Adresse.builder()
                .idAdresse(1L)
                .numeroRue(10)
                .nomRue("Rue de la Paix")
                .ville("Paris")
                .codePostal("75001")
                .pays(null) // Pays est null, ne change rien au toString car pays est exclu
                .disciplines(new HashSet<>())
                .utilisateurs(new HashSet<>())
                .build();

        String expectedString = "Adresse{" +
                "idAdresse=1" +
                ", numeroRue=10" +
                ", nomRue='Rue de la Paix'" +
                ", ville='Paris'" +
                ", codePostal='75001'" +
                '}';
        assertEquals(expectedString, adresse.toString(), "toString devrait retourner le format attendu même avec pays null (car pays est exclu).");
    }

    @Test
    @DisplayName("Test des constructeurs et des getters/setters via le Builder")
    void constructorAndGettersSetters_shouldWorkCorrectly() {
        Long id = 5L;
        Integer numero = 123;
        String nomRue = "Avenue Principale";
        String ville = "Bordeaux";
        String codePostal = "33000";

        Adresse adresse = Adresse.builder()
                .idAdresse(id)
                .numeroRue(numero)
                .nomRue(nomRue)
                .ville(ville)
                .codePostal(codePostal)
                .pays(pays1)
                .disciplines(new HashSet<>())
                .utilisateurs(new HashSet<>())
                .build();

        assertEquals(id, adresse.getIdAdresse());
        assertEquals(numero, adresse.getNumeroRue());
        assertEquals(nomRue, adresse.getNomRue());
        assertEquals(ville, adresse.getVille());
        assertEquals(codePostal, adresse.getCodePostal());
        assertEquals(pays1, adresse.getPays());
        assertNotNull(adresse.getDisciplines());
        assertTrue(adresse.getDisciplines().isEmpty());
        assertNotNull(adresse.getUtilisateurs());
        assertTrue(adresse.getUtilisateurs().isEmpty());

        // Test des setters
        adresse.setVille("Marseille");
        assertEquals("Marseille", adresse.getVille());

        adresse.setNomRue("Nouveau Nom de Rue");
        assertEquals("Nouveau Nom de Rue", adresse.getNomRue());

        Pays nouveauPays = Pays.builder().idPays(3L).nomPays("Espagne").build();
        adresse.setPays(nouveauPays);
        assertEquals(nouveauPays, adresse.getPays());
    }
}