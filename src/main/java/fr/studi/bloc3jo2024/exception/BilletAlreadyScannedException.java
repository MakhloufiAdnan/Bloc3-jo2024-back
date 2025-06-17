package fr.studi.bloc3jo2024.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class BilletAlreadyScannedException extends RuntimeException {
    public BilletAlreadyScannedException(String message) {
        super(message);
    }
}
