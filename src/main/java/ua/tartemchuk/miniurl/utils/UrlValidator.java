package ua.tartemchuk.miniurl.utils;

import org.springframework.http.HttpStatus;
import ua.tartemchuk.miniurl.exeptions.ServerException;

import java.util.regex.Pattern;

public class UrlValidator {

    public static final Pattern urlValidator = Pattern.compile(
            "(https?://(?:www\\.|(?!www))" +
                    "[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.\\S{2,}" +
                    "|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.\\S{2,}" +
                    "|https?://(?:www\\.|(?!www))" +
                    "[a-zA-Z0-9]+\\.\\S{2,}|www\\.[a-zA-Z0-9]+\\.\\S{2,})");

    public static void validateUrl(String longUrl) {
        if (!urlValidator.matcher(longUrl).matches()) {
            throw new ServerException("This type of url is not supported by the service", HttpStatus.BAD_REQUEST);
        }
    }

}
