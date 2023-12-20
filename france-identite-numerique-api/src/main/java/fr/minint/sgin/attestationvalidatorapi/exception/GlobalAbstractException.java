package fr.minint.sgin.attestationvalidatorapi.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class GlobalAbstractException extends Exception {

    private static final long serialVersionUID = 1L;
    private final HttpStatus status;

    protected GlobalAbstractException(String msg, HttpStatus status) {
        super(msg);
        this.status = status;
    }
}
