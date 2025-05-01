package fr.studi.bloc3jo2024.exception;

import fr.studi.bloc3jo2024.dto.AuthReponseDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

  // Gestion des erreurs de validation (@Valid) des DTO (@RequestBody)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField(); // Récupère le nom du champ avec l'erreur
      String errorMessage = error.getDefaultMessage(); // Récupère le message d'erreur
      errors.put(fieldName, errorMessage); // Ajoute l'erreur au map
    });
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // Retourne la map d'erreurs avec le statut 400
  }

  // Gestion de l'exception personnalisée pour les ressources non trouvées
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND); // Retourne le message avec le statut 404
  }

  // Gestion de l'exception personnalisée pour les adresses liées à un événement
  @ExceptionHandler(AdresseLieeAUnEvenementException.class)
  public ResponseEntity<String> handleAdresseLieeAUnEvenement(AdresseLieeAUnEvenementException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST); // Retourne le message avec le statut 400
  }

  // Gestion des erreurs de contraintes de base de données (violation d'unicité)
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
    String errorMessage = "Erreur de contrainte de base de données.";
    if (ex.getMessage() != null && ex.getMessage().contains("Unicité non respectée")) {
      errorMessage = "Cette donnée existe déjà.";
    }
    return new ResponseEntity<>(errorMessage, HttpStatus.CONFLICT); // Retourne un message avec le statut 409 Conflict
  }

  // Gestion de l'exception IllegalArgumentException pour l'email déjà utilisé
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<AuthReponseDto> handleIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new AuthReponseDto(null, ex.getMessage()));
  }
}
