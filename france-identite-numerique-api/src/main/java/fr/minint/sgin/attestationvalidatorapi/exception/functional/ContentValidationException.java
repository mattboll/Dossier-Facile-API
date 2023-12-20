package fr.minint.sgin.attestationvalidatorapi.exception.functional;

import org.springframework.http.HttpStatus;

public class ContentValidationException extends FunctionalException {

    public ContentValidationException(String msg) {
        super(msg, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
