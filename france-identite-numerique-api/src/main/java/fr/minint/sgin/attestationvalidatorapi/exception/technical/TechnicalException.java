package fr.minint.sgin.attestationvalidatorapi.exception.technical;

import fr.minint.sgin.attestationvalidatorapi.exception.GlobalAbstractException;
import org.springframework.http.HttpStatus;

public class TechnicalException extends GlobalAbstractException {

    public TechnicalException(String msg) {
        super(msg, HttpStatus.NOT_EXTENDED);
    }

    public TechnicalException(String msg, HttpStatus status) {
        super(msg, status);
    }
}
