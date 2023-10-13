package ua.tartemchuk.miniurl.exeptions.handlers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ua.tartemchuk.miniurl.exeptions.ServerException;

@ControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<Object> handleInsuranceException(ServerException exc) {
        return ResponseEntity.status(exc.getStatus()).body(exc.getMessage());
    }

}
