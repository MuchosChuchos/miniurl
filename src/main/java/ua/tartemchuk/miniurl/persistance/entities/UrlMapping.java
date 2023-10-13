package ua.tartemchuk.miniurl.persistance.entities;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(UrlMapping.DOCUMENT_NAME)
public class UrlMapping {

    public static final String DOCUMENT_NAME = "url_mapping";

    private ObjectId id;
    private String longUrl;
    private String shortUrl;

    public static UrlMapping urlMapping(String longUrl, String shortUrl) {
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setLongUrl(longUrl);
        urlMapping.setShortUrl(shortUrl);
        return urlMapping;
    }

}

