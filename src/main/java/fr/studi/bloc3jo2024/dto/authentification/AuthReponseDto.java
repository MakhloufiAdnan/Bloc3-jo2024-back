package fr.studi.bloc3jo2024.dto.authentification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthReponseDto {

    /**
     * Le token d'authentification (par exemple, JWT).
     * Peut-être nul si l'authentification a échoué ou si aucun token n'est pertinent pour la réponse.
     */
    private String token;

    /**
     * Un message descriptif concernant le résultat de l'opération d'authentification.
     * Peut indiquer un succès ou une erreur.
     */
    private String message;
}