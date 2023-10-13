package ua.tartemchuk.miniurl.endtoend;

import lombok.SneakyThrows;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import ua.tartemchuk.miniurl.persistance.entities.UrlMapping;
import ua.tartemchuk.miniurl.persistance.repositories.UrlRepository;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ShortenerControllerIT extends ContainersInitializer {

    public static final String LONG_URL = "https://example.com";

    private final String BASE_URI = "/urls/shortUrl";

    @Value(value = "${url-shortener.redis.expiration-time}")
    private String expirationTime;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @AfterEach
    void clearStorage() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        urlRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void longUrl_shorten_shortUrlInDBAndCacheIsClear() {
        this.mockMvc.perform(MockMvcRequestBuilders.post(BASE_URI)
                .contentType(MediaType.TEXT_PLAIN)
                .content(LONG_URL)
                .accept(MediaType.TEXT_PLAIN));

        assertEquals(1, urlRepository.count());

        String key = urlRepository.findAll().get(0).getShortUrl();
        String respFromCache = redisTemplate.opsForValue().get(key);
        assertNull(respFromCache);
    }

    @Test
    @SneakyThrows
    void longUrl_shorten_shortUrlInCache() {
        this.mockMvc.perform(MockMvcRequestBuilders.post(BASE_URI)
                .contentType(MediaType.TEXT_PLAIN)
                .content(LONG_URL)
                .accept(MediaType.TEXT_PLAIN));
        this.mockMvc.perform(MockMvcRequestBuilders.post(BASE_URI)
                .contentType(MediaType.TEXT_PLAIN)
                .content(LONG_URL)
                .accept(MediaType.TEXT_PLAIN));

        String key = urlRepository.findAll().get(0).getShortUrl();
        String respFromCache = redisTemplate.opsForValue().get(key);
        assertEquals(LONG_URL, respFromCache);
    }

    @Test
    @SneakyThrows
    void shortUrl_redirectLongUrl_urlFromDbStoredInCache() {
        UrlMapping urlMapping = UrlMapping.urlMapping("shortUrl", "longUrl");
        urlRepository.insert(urlMapping);

        this.mockMvc.perform(MockMvcRequestBuilders.get(BASE_URI + "/" + urlMapping.getShortUrl()));

        String cachedLongUrl = redisTemplate.opsForValue().get(urlMapping.getShortUrl());
        assertEquals(urlMapping.getLongUrl(), cachedLongUrl);
        Long expirationTimeLeft = redisTemplate.getExpire(urlMapping.getShortUrl(), TimeUnit.MINUTES);
        // it means that value was just placed in the cache
        assertTrue(Integer.parseInt(expirationTime) - expirationTimeLeft <= 1);
    }

    @Test
    @SneakyThrows
    void invalidLongUrl_shorten_badRequest() {
        this.mockMvc.perform(MockMvcRequestBuilders.post(BASE_URI)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("invalidUrl")
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void invalidShortUrl_redirectLongUrl_notFound() {
        UrlMapping urlMapping = UrlMapping.urlMapping("shortUrl", "longUrl");
        urlRepository.insert(urlMapping);

        this.mockMvc.perform(MockMvcRequestBuilders.get(BASE_URI + "/" + "nonExisting"))
                .andExpect(status().isNotFound());
    }

}
