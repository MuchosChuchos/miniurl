package ua.tartemchuk.miniurl.services;

public interface ShortenerService {

    String shortenUrl(String longUrl);

    String obtainRedirectionTarget(String shortUrl);

}
