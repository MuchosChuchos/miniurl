package ua.tartemchuk.miniurl.persistance.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import ua.tartemchuk.miniurl.persistance.entities.UrlMapping;

import java.util.Optional;

public interface UrlRepository extends MongoRepository<UrlMapping, ObjectId> {

    Optional<UrlMapping> findByShortUrl(String shortUrl);

}
