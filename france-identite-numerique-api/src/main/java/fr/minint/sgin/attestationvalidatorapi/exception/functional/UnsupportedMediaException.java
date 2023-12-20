package fr.minint.sgin.attestationvalidatorapi.exception.functional;

import org.springframework.http.HttpStatus;

public class UnsupportedMediaException extends FunctionalException {

    public UnsupportedMediaException(String msg) {
        super(msg, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }
}
