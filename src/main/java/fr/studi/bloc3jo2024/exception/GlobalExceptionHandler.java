package fr.studi.bloc3jo2024.exception;

import fr.studi.bloc3jo2024.dto.authentification.AuthReponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// Gestion des exceptions dans les contrôleurs REST
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  // Gestion des erreurs de validation (@Valid) des DTO (@RequestBody)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
        errors.put(fieldName, errorMessage);
    });
      return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  // Gestion de l'exception personnalisée pour les ressources non trouvées
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND); // Retourne le message avec le statut 404
  }

  // Gestion de l'exception personnalisée pour les adresses liées à un événement
  @ExceptionHandler(AdresseLieeAUneDisciplineException.class)
  public ResponseEntity<String> handleAdresseLieeAUnEvenement(AdresseLieeAUneDisciplineException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST); // Retourne le message avec le statut 400
  }

  // Gestion des erreurs de contraintes de base de données (violation d'unicité)
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
    String errorMessage = "Erreur de contrainte de base de données.";
      // Vous pouvez inspecter ex.getMessage() ou ex.getCause() pour des messages plus spécifiques si besoin
      log.error("DataIntegrityViolationException: {}", ex.getMessage()); // Log de l'erreur
      if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("unique constraint") || ex.getMessage().toLowerCase().contains("unicité non respectée")) {
          errorMessage = "Une valeur unique est dupliquée (par exemple, email ou nom d'utilisateur).";
    }
      return new ResponseEntity<>(errorMessage, HttpStatus.CONFLICT);
  }

  // Gestion de l'exception IllegalArgumentException pour l'email déjà utilisé
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<AuthReponseDto> handleIllegalArgument(IllegalArgumentException ex) {
      log.warn("IllegalArgumentException: {}", ex.getMessage()); // Utilisez warn pour les erreurs client attendues
      return ResponseEntity.status(HttpStatus.BAD_REQUEST) // Ou CONFLICT(409) si c'est plus approprié pour l'email dupliqué
            .body(new AuthReponseDto(null, ex.getMessage()));
  }

    @ExceptionHandler(MailException.class) // Intercepte MailSendException et autres MailException
    public ResponseEntity<AuthReponseDto> handleMailException(MailException ex) {
        log.error("Erreur lors de l'opération d'envoi d'email: ", ex); // Log l'exception complète
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthReponseDto(null, "Erreur interne du serveur lors de la tentative d'envoi d'email."));
    }

    @ExceptionHandler(IllegalStateException.class)
    // Soyez prudent si cette exception est utilisée pour divers cas non liés
    public ResponseEntity<AuthReponseDto> handleIllegalState(IllegalStateException ex) {
        log.error("État illégal rencontré: ", ex); // Log l'exception complète
        // Si le message est "Rôle USER manquant.", un 500 est approprié car c'est un problème de configuration/données serveur
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthReponseDto(null, "Erreur interne du serveur : " + ex.getMessage()));
    }
}
