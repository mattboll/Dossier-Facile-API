package fr.minint.sgin.attestationvalidatorapi.exception.functional;

import fr.minint.sgin.attestationvalidatorapi.exception.GlobalAbstractException;
import org.springframework.http.HttpStatus;

public class FunctionalException extends GlobalAbstractException {

    public FunctionalException(String msg) {
        super(msg, HttpStatus.BAD_REQUEST);
    }

    public FunctionalException(String msg, HttpStatus status) {
        super(msg, status);
    }
}
