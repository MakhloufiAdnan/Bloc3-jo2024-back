package fr.bloc_jo2024.exception;

public class AdresseLieeAUnEvenementException extends RuntimeException {

    private static final String MESSAGE_DEFAUT = "L'adresse est liée à un événement et ne peut pas être supprimée.";
    private static final String MESSAGE_ERREUR_VERIFICATION = "Erreur lors de la vérification si l'adresse est liée à un événement.";

    public AdresseLieeAUnEvenementException() {
        super(MESSAGE_DEFAUT);
    }

    public AdresseLieeAUnEvenementException(String message, Throwable cause) {
        super(message, cause);
    }

    public static AdresseLieeAUnEvenementException erreurVerification(Throwable cause) {
        return new AdresseLieeAUnEvenementException(MESSAGE_ERREUR_VERIFICATION, cause);
    }
}