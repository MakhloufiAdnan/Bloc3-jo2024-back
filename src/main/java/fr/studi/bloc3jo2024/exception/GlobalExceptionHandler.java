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
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /**
   * Gère les erreurs de validation des DTO annotés avec @Valid.
   * @param ex L'exception levée lors de la validation.
   * @return Une ResponseEntity avec un statut 400 BAD_REQUEST et une map des erreurs par champ.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });
    log.warn("Erreurs de validation des arguments : {}", errors);
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  /**
   * Gère l'exception personnalisée pour les ressources non trouvées.
   * @param ex L'exception ResourceNotFoundException.
   * @return Une ResponseEntity avec un statut 404 NOT_FOUND et le message de l'exception en tant que String brute.
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
    log.warn("Ressource non trouvée : {}", ex.getMessage());
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  /**
   * Gère l'exception personnalisée pour les adresses liées à une discipline (ou un événement).
   * @param ex L'exception AdresseLieeAUneDisciplineException.
   * @param request L'objet WebRequest pour potentiellement logger plus d'informations.
   * @return Une ResponseEntity avec un statut 409 CONFLICT et un corps JSON structuré.
   */
  @ExceptionHandler(AdresseLieeAUneDisciplineException.class)
  public ResponseEntity<Map<String, Object>> handleAdresseLieeAUneDisciplineException(
          AdresseLieeAUneDisciplineException ex, WebRequest request) {

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now().toString()); // Ajout d'un timestamp pour plus de détails
    body.put("status", HttpStatus.CONFLICT.value());
    body.put("erreur", "Conflit de ressource"); // Clé "erreur" attendue par le test
    body.put("message", ex.getMessage());      // Message de l'exception

    String path = "N/A";

    if (request instanceof ServletWebRequest servletWebRequest) {
      path = servletWebRequest.getRequest().getRequestURI();
      body.put("path", path); // Ajout du chemin de la requête pour le contexte
    }

    log.warn("Conflit de ressource (AdresseLieeAUneDisciplineException) sur le chemin '{}': {}",
            path, // Utilisation de la variable path initialisée
            ex.getMessage());

    return new ResponseEntity<>(body, HttpStatus.CONFLICT);
  }

  /**
   * Gère les erreurs de contraintes d'intégrité de la base de données (ex: violation d'unicité).
   * @param ex L'exception DataIntegrityViolationException.
   * @return Une ResponseEntity avec un statut 409 CONFLICT et un message d'erreur.
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
    String specificUserMessage = "Une opération a violé une contrainte d'intégrité de la base de données.";
    // Log plus détaillé pour les développeurs
    log.error("DataIntegrityViolationException: Cause gốc: [{}], Message: [{}]", ex.getMostSpecificCause().getMessage(), ex.getMessage());

    // Tentative de fournir un message plus convivial pour les violations d'unicité courantes
    String rootCauseMessage = ex.getMostSpecificCause().getMessage().toLowerCase();
    if (rootCauseMessage.contains("unique constraint") || rootCauseMessage.contains("constraint violation") || rootCauseMessage.contains("duplicate key value violates unique constraint")) {
      // Essayer d'extraire le nom de la contrainte ou le champ si possible (dépend du SGBD et du driver)
      // Ceci est une simplification ; une analyse plus poussée pourrait être nécessaire pour des messages précis.
      if (rootCauseMessage.contains("email")) {
        specificUserMessage = "L'adresse e-mail fournie existe déjà.";
      } else if (rootCauseMessage.contains("username") || rootCauseMessage.contains("nom_utilisateur")) {
        specificUserMessage = "Le nom d'utilisateur fourni existe déjà.";
      } else {
        specificUserMessage = "Une valeur fournie doit être unique et existe déjà dans le système.";
      }
    }
    return new ResponseEntity<>(specificUserMessage, HttpStatus.CONFLICT);
  }

  /**
   * Gère les exceptions de type IllegalArgumentException.
   * @param ex L'exception IllegalArgumentException.
   * @return Une ResponseEntity avec un statut 400 BAD_REQUEST et un AuthReponseDto contenant le message.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<AuthReponseDto> handleIllegalArgument(IllegalArgumentException ex) {
    log.warn("IllegalArgumentException: {}", ex.getMessage());
    // Assurez-vous que AuthReponseDto est correctement sérialisé en JSON avec un champ "message"
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new AuthReponseDto(null, ex.getMessage()));
  }

  /**
   * Gère les exceptions liées à l'envoi d'e-mails.
   * @param ex L'exception MailException.
   * @return Une ResponseEntity avec un statut 500 INTERNAL_SERVER_ERROR et un message générique.
   */
  @ExceptionHandler(MailException.class)
  public ResponseEntity<AuthReponseDto> handleMailException(MailException ex) {
    log.error("Erreur lors de l'opération d'envoi d'email: {}", ex.getMessage(), ex); // Log avec la stack trace
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new AuthReponseDto(null, "Erreur interne du serveur lors de la tentative d'envoi d'email. Veuillez réessayer plus tard."));
  }

  /**
   * Gère les exceptions de type IllegalStateException.
   * @param ex L'exception IllegalStateException.
   * @return Une ResponseEntity avec un statut 500 INTERNAL_SERVER_ERROR et un message d'erreur.
   */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<AuthReponseDto> handleIllegalState(IllegalStateException ex) {
    log.error("État illégal rencontré: {}", ex.getMessage(), ex); // Log avec la stack trace
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new AuthReponseDto(null, "Erreur interne du serveur : " + ex.getMessage()));
  }
}