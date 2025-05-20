package fr.studi.bloc3jo2024.entities;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Pays;
import fr.studi.bloc3jo2024.entity.Utilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class AdresseEntityTest {

    private Pays paysFrance;
    private Pays paysAllemagne;

    @BeforeEach
    void setUp() {
        // Initialisation des objets Pays avant chaque test pour assurer l'isolation
        paysFrance = Pays.builder().idPays(1L).nomPays("France").build();
        paysAllemagne = Pays.builder().idPays(2L).nomPays("Allemagne").build();
    }

    @Nested
    @DisplayName("Tests du contrat de la méthode equals()")
    class EqualsContractTests {

        @Test
        @DisplayName("Réflexivité : un objet doit être égal à lui-même")
        void equals_shouldBeReflexive() {
            Adresse adresse1 = Adresse.builder()
                    .idAdresse(1L).numeroRue(10).nomRue("Paix").ville("Paris").codePostal("75001")
                    .pays(paysFrance).disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                    .build();
            // Vérifie que a.equals(a) est vrai
            assertEquals(adresse1, adresse1, "Une adresse doit être égale à elle-même.");
        }

        @Test
        @DisplayName("Symétrie : si a.equals(b) est vrai, alors b.equals(a) doit être vrai")
        void equals_shouldBeSymmetric() {
            Adresse adresse1 = Adresse.builder()
                    .idAdresse(1L).numeroRue(10).nomRue("Paix").ville("Paris").codePostal("75001")
                    .pays(paysFrance).disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                    .build();
            Adresse adresse2 = Adresse.builder()
                    .idAdresse(1L).numeroRue(10).nomRue("Paix").ville("Paris").codePostal("75001")
                    .pays(paysFrance).disciplines(new HashSet<>()).utilisateurs(new HashSet<>()) // Même ID et mêmes attributs pertinents
                    .build();

            assertEquals(adresse1, adresse2, "adresse1 devrait être égale à adresse2.");
            assertEquals(adresse2, adresse1, "adresse2 devrait être égale à adresse1.");
        }

        @Test
        @DisplayName("Transitivité : si a.equals(b) et b.equals(c) sont vrais, alors a.equals(c) doit être vrai")
        void equals_shouldBeTransitive() {
            Adresse adresse1 = Adresse.builder()
                    .idAdresse(1L).numeroRue(10).nomRue("Paix").ville("Paris").codePostal("75001")
                    .pays(paysFrance).disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                    .build();
            Adresse adresse2 = Adresse.builder()
                    .idAdresse(1L).numeroRue(10).nomRue("Paix").ville("Paris").codePostal("75001")
                    .pays(paysFrance).disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                    .build();
            Adresse adresse3 = Adresse.builder()
                    .idAdresse(1L).numeroRue(10).nomRue("Paix").ville("Paris").codePostal("75001")
                    .pays(paysFrance).disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                    .build();

            assertEquals(adresse1, adresse2, "adresse1 devrait être égale à adresse2.");
            assertEquals(adresse2, adresse3, "adresse2 devrait être égale à adresse3.");
            assertEquals(adresse1, adresse3, "Par transitivité, adresse1 devrait être égale à adresse3.");
        }

        @Test
        @DisplayName("Consistance : appels multiples à equals doivent retourner le même résultat")
        void equals_shouldBeConsistent() {
            Adresse adresse1 = Adresse.builder()
                    .idAdresse(1L).numeroRue(10).nomRue("Paix").ville("Paris").codePostal("75001")
                    .pays(paysFrance).disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                    .build();
            Adresse adresse2 = Adresse.builder()
                    .idAdresse(1L).numeroRue(10).nomRue("Paix").ville("Paris").codePostal("75001")
                    .pays(paysFrance).disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                    .build();
            Adresse adresse3 = Adresse.builder()
                    .idAdresse(2L).numeroRue(20).nomRue("Guerre").ville("Lyon").codePostal("69001")
                    .pays(paysAllemagne).disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                    .build();

            // Vérifie la consistance pour les objets égaux
            assertEquals(adresse1, adresse2, "La comparaison entre adresse1 et adresse2 doit être consistante (true).");
            assertEquals(adresse1, adresse2, "Un autre appel doit donner le même résultat (true).");

            // Vérifie la consistance pour les objets non égaux
            assertNotEquals(adresse1, adresse3, "La comparaison entre adresse1 et adresse3 doit être consistante (false).");
            assertNotEquals(adresse1, adresse3, "Un autre appel doit donner le même résultat (false).");
        }

        @Test
        @DisplayName("Comparaison avec null : a.equals(null) doit retourner false")
        void equals_shouldReturnFalseForNull() {
            Adresse adresse1 = Adresse.builder()
                    .idAdresse(1L).numeroRue(10).nomRue("Paix").ville("Paris").codePostal("75001")
                    .pays(paysFrance).disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                    .build();
            assertNotEquals(null, adresse1, "Une adresse ne devrait pas être égale à null.");
        }

        @Test
        @DisplayName("Comparaison avec un objet de type différent")
        void equals_shouldReturnFalseForDifferentType() {
            Adresse adresse1 = Adresse.builder()
                    .idAdresse(1L).numeroRue(10).nomRue("Paix").ville("Paris").codePostal("75001")
                    .pays(paysFrance).disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                    .build();
            String autreObjet = "Ceci n'est pas une Adresse";
            assertNotEquals(adresse1, autreObjet, "equals avec un type différent devrait retourner false.");
        }

        @Test
        @DisplayName("Adresses avec des ID différents ne doivent pas être égales")
        void equals_shouldReturnFalseForDifferentIds() {
            Adresse adresse1 = Adresse.builder()
                    .idAdresse(1L).numeroRue(10).nomRue("Paix").ville("Paris").codePostal("75001")
                    .pays(paysFrance).disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                    .build();
            Adresse adresse2 = Adresse.builder()
                    .idAdresse(2L).numeroRue(10).nomRue("Paix").ville("Paris").codePostal("75001") // ID différent
                    .pays(paysFrance).disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                    .build();
            assertNotEquals(adresse1, adresse2, "Adresses avec des ID différents ne devraient pas être égales.");
        }

        @Test
        @DisplayName("Adresse avec ID null vs adresse avec ID non null")
        void equals_shouldReturnFalseForOneNullId() {
            Adresse adresseAvecIdNull = Adresse.builder() // idAdresse est null par défaut avec le builder si non spécifié
                    .numeroRue(10).nomRue("Paix").ville("Paris").codePostal("75001")
                    .pays(paysFrance).disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                    .build();
            Adresse adresseAvecId = Adresse.builder()
                    .idAdresse(2L).numeroRue(10).nomRue("Paix").ville("Paris").codePostal("75001")
                    .pays(paysFrance).disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                    .build();

            assertNotEquals(adresseAvecIdNull, adresseAvecId, "Une adresse avec ID null ne devrait pas être égale à une adresse avec un ID non null.");
            assertNotEquals(adresseAvecId, adresseAvecIdNull, "Et vice-versa pour la symétrie.");
        }

        @Test
        @DisplayName("Deux instances différentes avec ID null (comportement Object.equals attendu)")
        void equals_shouldReturnFalseForTwoDifferentInstancesWithNullIds() {
            Adresse adresse1IdNull = Adresse.builder()
                    .numeroRue(10).nomRue("Paix").ville("Paris").codePostal("75001")
                    .pays(paysFrance).disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                    .build();
            Adresse adresse2IdNull = Adresse.builder()
                    .numeroRue(10).nomRue("Paix").ville("Paris").codePostal("75001") // Mêmes attributs
                    .pays(paysFrance).disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                    .build();

            assertNotEquals(adresse1IdNull, adresse2IdNull, "Deux instances différentes avec des ID null ne devraient pas être égales (délégation à Object.equals).");
        }
    }

    @Nested
    @DisplayName("Tests du contrat de la méthode hashCode()")
    class HashCodeContractTests {

        @Test
        @DisplayName("Objets égaux doivent avoir le même hashCode")
        void hashCode_shouldBeSameForEqualObjects() {
            Adresse adresse1 = Adresse.builder()
                    .idAdresse(1L).numeroRue(10).nomRue("Paix").ville("Paris").codePostal("75001")
                    .pays(paysFrance).disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                    .build();
            Adresse adresse2 = Adresse.builder()
                    .idAdresse(1L).numeroRue(10).nomRue("Paix").ville("Paris").codePostal("75001") // Mêmes ID et attributs
                    .pays(paysFrance).disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                    .build();
            assertEquals(adresse1.hashCode(), adresse2.hashCode(), "Objets égaux doivent avoir le même hashCode.");
        }

        @Test
        @DisplayName("Objets avec ID différents devraient généralement avoir des hashCodes différents")
        void hashCode_shouldLikelyBeDifferentForObjectsWithDifferentIds() {
            Adresse adresse1 = Adresse.builder()
                    .idAdresse(1L).numeroRue(10).nomRue("Paix").ville("Paris").codePostal("75001")
                    .pays(paysFrance).disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                    .build();
            Adresse adresse2 = Adresse.builder()
                    .idAdresse(2L).numeroRue(20).nomRue("Guerre").ville("Lyon").codePostal("69001") // ID différent
                    .pays(paysAllemagne).disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                    .build();

            assertNotEquals(adresse1.hashCode(), adresse2.hashCode(), "Objets avec ID différents devraient avoir des hashCodes différents.");
        }

        @Test
        @DisplayName("hashCode pour une instance avec ID null")
        void hashCode_forInstanceWithNullId() {
            Adresse adresseIdNull = Adresse.builder() // idAdresse est null
                    .numeroRue(10).nomRue("Paix").ville("Paris").codePostal("75001")
                    .pays(paysFrance).disciplines(new HashSet<>()).utilisateurs(new HashSet<>())
                    .build();

            int expectedHashCodeForNullId = Objects.hash((Long) null);
            assertEquals(expectedHashCodeForNullId, adresseIdNull.hashCode(), "Le hashCode pour un ID null doit être consistent (Objects.hash(null)).");
        }
    }

    @Nested
    @DisplayName("Tests de la méthode toString()")
    class ToStringTests {

        @Test
        @DisplayName("toString() doit retourner le format attendu (champs de base, relations exclues)")
        void toString_shouldReturnExpectedFormat() {
            Adresse adresse = Adresse.builder()
                    .idAdresse(1L).numeroRue(10).nomRue("Rue de la Paix").ville("Paris").codePostal("75001")
                    .pays(paysFrance)
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
    }

    @Nested
    @DisplayName("Tests des constructeurs et accesseurs (générés par Lombok)")
    class ConstructorAndAccessorsTests {

        @Test
        @DisplayName("Le Builder, les getters et setters doivent fonctionner correctement")
        void builderAndAccessors_shouldWorkCorrectly() {
            // Arrange
            Long id = 5L;
            Integer numero = 123;
            String nomRue = "Avenue Principale";
            String ville = "Bordeaux";
            String codePostal = "33000";
            HashSet<Discipline> disciplines = new HashSet<>();
            HashSet<Utilisateur> utilisateurs = new HashSet<>();

            // Act
            Adresse adresse = Adresse.builder()
                    .idAdresse(id).numeroRue(numero).nomRue(nomRue).ville(ville).codePostal(codePostal)
                    .pays(paysFrance).disciplines(disciplines).utilisateurs(utilisateurs)
                    .build();

            // Assert
            assertEquals(id, adresse.getIdAdresse());
            assertEquals(numero, adresse.getNumeroRue());
            assertEquals(nomRue, adresse.getNomRue());
            assertEquals(ville, adresse.getVille());
            assertEquals(codePostal, adresse.getCodePostal());
            assertEquals(paysFrance, adresse.getPays());
            assertNotNull(adresse.getDisciplines(), "La collection de disciplines ne doit pas être null.");
            assertSame(disciplines, adresse.getDisciplines(), "La collection de disciplines doit être la même instance que celle passée au builder.");
            assertNotNull(adresse.getUtilisateurs(), "La collection d'utilisateurs ne doit pas être null.");
            assertSame(utilisateurs, adresse.getUtilisateurs(), "La collection d'utilisateurs doit être la même instance que celle passée au builder.");

            // Test des Setters (Lombok @Setter)
            adresse.setVille("Marseille");
            assertEquals("Marseille", adresse.getVille(), "La ville doit être mise à jour par le setter.");

            Pays nouveauPays = Pays.builder().idPays(3L).nomPays("Espagne").build();
            adresse.setPays(nouveauPays);
            assertEquals(nouveauPays, adresse.getPays(), "Le pays doit être mis à jour par le setter.");
        }

        @Test
        @DisplayName("Le constructeur par défaut (via @NoArgsConstructor) doit exister et initialisations par défaut")
        void defaultConstructor_shouldExistAndInitializeDefaults() {
            // Act
            Adresse adresse = new Adresse();

            // Assert
            assertNull(adresse.getIdAdresse(), "L'ID doit être null après le constructeur par défaut.");
            assertNull(adresse.getNomRue(), "Le nom de rue doit être null après le constructeur par défaut.");
            assertNotNull(adresse.getDisciplines(), "La collection de disciplines doit être initialisée (non null).");
            assertTrue(adresse.getDisciplines().isEmpty(), "La collection de disciplines doit être vide par défaut.");
            assertNotNull(adresse.getUtilisateurs(), "La collection d'utilisateurs doit être initialisée (non null).");
            assertTrue(adresse.getUtilisateurs().isEmpty(), "La collection d'utilisateurs doit être vide par défaut.");
        }

        @Test
        @DisplayName("Le constructeur avec tous les arguments (via @AllArgsConstructor) doit fonctionner")
        void allArgsConstructor_shouldWorkCorrectly() {
            // Arrange
            Long id = 7L;
            Integer numero = 77;
            String nomRue = "Place Royale";
            String ville = "Nantes";
            String codePostal = "44000";
            HashSet<Discipline> disciplines = new HashSet<>();
            HashSet<Utilisateur> utilisateurs = new HashSet<>();

            // Act
            Adresse adresse = new Adresse(id, numero, nomRue, ville, codePostal, disciplines, utilisateurs, paysFrance);

            // Assert
            assertEquals(id, adresse.getIdAdresse());
            assertEquals(numero, adresse.getNumeroRue());
            assertEquals(nomRue, adresse.getNomRue());
            assertEquals(ville, adresse.getVille());
            assertEquals(codePostal, adresse.getCodePostal());
            assertEquals(paysFrance, adresse.getPays());
            assertSame(disciplines, adresse.getDisciplines(), "La collection de disciplines doit être la même instance.");
            assertSame(utilisateurs, adresse.getUtilisateurs(), "La collection d'utilisateurs doit être la même instance.");
        }
    }
}
