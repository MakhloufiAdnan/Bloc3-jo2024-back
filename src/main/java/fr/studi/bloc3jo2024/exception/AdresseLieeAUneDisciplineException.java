package fr.studi.bloc3jo2024.exception;

public class AdresseLieeAUneDisciplineException extends RuntimeException {

  private static final String MESSAGE_DEFAUT = "L'adresse est liée à un événement et ne peut pas être supprimée.";
  private static final String MESSAGE_ERREUR_VERIFICATION = "Erreur lors de la vérification si l'adresse est liée à un événement.";

  public AdresseLieeAUneDisciplineException() {
    super(MESSAGE_DEFAUT);
  }

  public AdresseLieeAUneDisciplineException(String message) {
    super(message);
  }

  public AdresseLieeAUneDisciplineException(String message, Throwable cause) {
    super(message, cause);
  }

  public static AdresseLieeAUneDisciplineException erreurVerification(Throwable cause) {
    return new AdresseLieeAUneDisciplineException(MESSAGE_ERREUR_VERIFICATION, cause);
  }
}
