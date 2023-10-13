package ua.tartemchuk.miniurl.exeptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ServerException extends RuntimeException {

    private final HttpStatus status;

    public ServerException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}
