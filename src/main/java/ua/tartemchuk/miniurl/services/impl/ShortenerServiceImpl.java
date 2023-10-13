package ua.tartemchuk.miniurl.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.tartemchuk.miniurl.exeptions.ServerException;
import ua.tartemchuk.miniurl.persistance.entities.UrlMapping;
import ua.tartemchuk.miniurl.persistance.repositories.UrlRepository;
import ua.tartemchuk.miniurl.services.ShortenerService;
import ua.tartemchuk.miniurl.utils.UrlValidator;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ShortenerServiceImpl implements ShortenerService {

    public static final Logger logger = LoggerFactory.getLogger(ShortenerServiceImpl.class);

    private final StringRedisTemplate redisTemplate;

    private final UrlRepository urlRepository;

    @Value(value = "${url-shortener.redis.expiration-time}")
    private String expirationTime;

    public ShortenerServiceImpl(StringRedisTemplate redisTemplate, UrlRepository urlRepository) {
        this.redisTemplate = redisTemplate;
        this.urlRepository = urlRepository;
    }

    @Override
    public String obtainRedirectionTarget(String shortUrl) {
        AtomicReference<String> longUrl = new AtomicReference<>(redisTemplate.opsForValue().get(shortUrl));
        if (longUrl.get() == null) {
            logger.info(String.format("Short URL: %s - wasn't in cache, checking db...", shortUrl));
            Optional<UrlMapping> urlMapping = urlRepository.findByShortUrl(shortUrl);
            urlMapping.ifPresentOrElse(mapping -> {
                logger.info(String.format("Short URL: %s - found in db, storing to cache...", shortUrl));
                longUrl.set(mapping.getLongUrl());
                redisTemplate.opsForValue().set(mapping.getShortUrl(), mapping.getLongUrl());
                redisTemplate.expire(mapping.getShortUrl(), Integer.parseInt(expirationTime), TimeUnit.MINUTES);
            }, () -> {
                throw new ServerException("There is not such short url", HttpStatus.NOT_FOUND);
            });
        }
        return longUrl.get();
    }

    @Override
    public String shortenUrl(String longUrl) {
        UrlValidator.validateUrl(longUrl);

        int hashCode = longUrl.hashCode();
        String shortUrl = Base64.getUrlEncoder().encodeToString(String.valueOf(hashCode).getBytes(StandardCharsets.UTF_8));
        logger.info(String.format("Long URL: %s - converted to %s", longUrl, shortUrl));

        String cachedLongUrl = redisTemplate.opsForValue().get(shortUrl);
        if (cachedLongUrl == null) {
            Optional<UrlMapping> urlMapping = urlRepository.findByShortUrl(shortUrl);
            urlMapping.ifPresentOrElse(mapping -> {
                redisTemplate.opsForValue().set(mapping.getShortUrl(), mapping.getLongUrl());
                redisTemplate.expire(mapping.getShortUrl(), 30, TimeUnit.MINUTES);
            }, () -> {
                UrlMapping mappingToStore = new UrlMapping();
                mappingToStore.setShortUrl(shortUrl);
                mappingToStore.setLongUrl(longUrl);
                urlRepository.insert(mappingToStore);
            });
        }
        return shortUrl;
    }

}
