package ua.tartemchuk.miniurl.endtoend;

import org.junit.ClassRule;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class ContainersInitializer {

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2")
            .withEnv("MONGO_INIT_ROOT_USERNAME","admin").withEnv("MONGO_INIT_ROOT_PASSWORD","admin");

    @Container
    public static GenericContainer redisContainer = new GenericContainer("redis")
            .withExposedPorts(6379);

    static {
        mongoDBContainer.start();
        redisContainer.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> "mongodb://" + mongoDBContainer.getHost() + ":" + mongoDBContainer.getFirstMappedPort());

        registry.add("spring.data.redis.host", () -> redisContainer.getHost());
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379).toString());
    }

}
