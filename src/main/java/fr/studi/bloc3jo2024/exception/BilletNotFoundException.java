package fr.studi.bloc3jo2024.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class BilletNotFoundException extends RuntimeException {
    public BilletNotFoundException(String message) {
        super(message);
    }
}
