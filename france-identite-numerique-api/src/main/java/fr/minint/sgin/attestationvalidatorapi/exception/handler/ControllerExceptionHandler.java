package fr.minint.sgin.attestationvalidatorapi.exception.handler;

import fr.minint.sgin.attestationvalidatorapi.exception.GlobalAbstractException;
import fr.minint.sgin.attestationvalidatorapi.exception.ResourceNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.validation.ConstraintViolationException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@RestControllerAdvice
public class ControllerExceptionHandler {

    /**
     * Erreur 404
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorMessage> resourceNotFoundException(Exception ex, WebRequest request) {
        logException("Resource not found : " + ex.getMessage());
        ErrorMessage errorMessage = new ErrorMessage(HttpStatus.NOT_FOUND.value(),
                new Date(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }

    /**
     * Erreurs techniques et fonctionnelles
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler(GlobalAbstractException.class)
    public ResponseEntity<ErrorMessage> handleGlobalAbstractException(GlobalAbstractException ex, WebRequest request) {
        logException("An error " + ex.getStatus().value() + " occurs : " + ex.getMessage());
        ErrorMessage errorMessage = new ErrorMessage(ex.getStatus().value(),
                new Date(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorMessage, ex.getStatus());
    }

    /**
     * Erreurs validation, mauvais arguments, file
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(t -> t.getField() + " : " + t.getDefaultMessage()).collect(Collectors.toList());
        logException("Error during validation for the following reasons : [ " + String.join(", ", errors) + " ]");
        return new ResponseEntity<>(getErrorsMap(errors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ConstraintViolationException.class, MaxUploadSizeExceededException.class})
    public ResponseEntity<ErrorMessage> handleConstraintViolationException(Exception ex, WebRequest request) {
        logException(ex.getMessage());
        ErrorMessage errorMessage = new ErrorMessage(HttpStatus.BAD_REQUEST.value(), new Date(), ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleInternalException(Exception ex, WebRequest request) {
        logException(ex.getMessage());
        ErrorMessage errorMessage = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                new Date(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Map<String, List<String>> getErrorsMap(List<String> errors) {
        Map<String, List<String>> errorResponse = new HashMap<>();
        errorResponse.put("errors", errors);
        return errorResponse;
    }

    private void logException(String message) {
        log.error(message);
    }
}