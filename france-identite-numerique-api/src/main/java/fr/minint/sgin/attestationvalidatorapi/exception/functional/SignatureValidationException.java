package fr.minint.sgin.attestationvalidatorapi.exception.functional;

import org.springframework.http.HttpStatus;

public class SignatureValidationException extends FunctionalException {

    public SignatureValidationException(String msg) {
        super(msg, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
